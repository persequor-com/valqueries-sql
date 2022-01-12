/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>,
 */
package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.*;
import io.ran.token.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesCrudRepositoryImpl<T, K> implements ValqueriesCrudRepository<T, K> {

	private final ValqueriesAccessDataLayer<T, K> baseRepo;
	private final ValqueriesRepositoryFactory factory;
	private final Class<T> modelType;

	public ValqueriesCrudRepositoryImpl(ValqueriesRepositoryFactory factory, Class<T> modelType, Class<K> keyType) {
		this.modelType = modelType;
		this.baseRepo = factory.get(modelType, keyType);
		this.factory = factory;
	}
	@Override
	public Optional<T> get(K id) {
		return baseRepo.get(id);
	}

	@Override
	public Stream<T> getAll() {
		return baseRepo.getAll();
	}

	@Override
	public CrudUpdateResult deleteById(K id) {
		return baseRepo.deleteById(id);
	}

	@Override
	public CrudUpdateResult deleteByIds(Collection<K> collection) {
		return baseRepo.deleteByIds(collection);
	}

	@Override
	public CrudUpdateResult save(T entity) {
		final ChangeMonitor changed = new ChangeMonitor();
		doRetryableInTransaction(tx -> saveIncludingRelationInternal(changed, tx, entity, modelType));
		return changed::getNumberOfChangedRows;
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, T entity) {
		final ChangeMonitor changed = new ChangeMonitor();
		saveIncludingRelationInternal(changed, tx, entity, modelType);
		return changed::getNumberOfChangedRows;
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> entities) {
		final ChangeMonitor changed = new ChangeMonitor();
		saveIncludingRelationsInternal(changed, tx, entities, modelType);
		return changed::getNumberOfChangedRows;
	}

	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass) {
		return baseRepo.saveOther(tx, t, oClass);
	}

	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass) {
		return baseRepo.saveOthers(tx, t, oClass);
	}

	private <O> void saveIncludingRelationsInternal(ChangeMonitor changed, ITransactionContext tx, Collection<O> ts, Class<O> xClass) {
		Collection<O> notAlreadySaved = ts.stream().filter(t -> !changed.isAlreadySaved(t)).collect(Collectors.toList());
		changed.increment(notAlreadySaved, saveOthers(tx, notAlreadySaved, xClass).affectedRows());
		TypeDescriberImpl.getTypeDescriber(xClass).relations().forEach(relationDescriber -> {
			for(O t : notAlreadySaved) {
				internalSaveRelation(changed, tx, t, relationDescriber);
			}
		});
	}

	private <X> void saveIncludingRelationInternal(ChangeMonitor changed, ITransactionContext tx, X t, Class<X> xClass) {
		if (changed.isAlreadySaved(t)) {
			return;
		}
		changed.increment(t, saveOther(tx, t, xClass).affectedRows());
		TypeDescriberImpl.getTypeDescriber(xClass).relations().forEach(relationDescriber -> {

			internalSaveRelation(changed,tx, t, relationDescriber);
		});
	}

	private void internalSaveRelation(ChangeMonitor changed, ITransactionContext tx, Object t, RelationDescriber relationDescriber) {
		if (!relationDescriber.getRelationAnnotation().autoSave()) {
			return;
		}
		if (!(t instanceof Mapping)) {
			throw new RuntimeException("Valqueries models should have a @Mapper annotation");
		}
		Mapping mapping = (Mapping)t;
		Object relation = mapping._getRelation(relationDescriber);
		Class<?> via = relationDescriber.getRelationAnnotation().via();
		Collection<Object> manyToManyRelations = new ArrayList<>();
        if (relation != null) {
            if (!via.equals(None.class)) {
                Collection<?> relations = (Collection<?>) relation;
                relations.forEach(rel -> {
                    Mapping mappingRelation = (Mapping) rel;
                    Mapping manyToManyRelation = (Mapping) factory.genericFactory.get(via);
                    UncheckedObjectMap map = new UncheckedObjectMap();
                    relationDescriber.getVia().forEach(viaRelationDescriber -> {
                        Token intermediateTableToken;
                        Token endTableToken;
                        if (viaRelationDescriber.getToClass().clazz.isAssignableFrom(mappingRelation.getClass())) {
                            intermediateTableToken = viaRelationDescriber.getFromKeys().toProperties().get(0).getToken();
                            endTableToken = viaRelationDescriber.getToKeys().get(0).getToken();
							map.set(intermediateTableToken, mappingRelation._getKey().getValues().get(endTableToken).getValue());
                        } else {
                            endTableToken = viaRelationDescriber.getFromKeys().toProperties().get(0).getToken();
                            intermediateTableToken = viaRelationDescriber.getToKeys().get(0).getToken();
							map.set(intermediateTableToken, mapping._getKey().getValues().get(endTableToken).getValue());
                        }

                    });
                    manyToManyRelation.hydrate(map);
                    manyToManyRelations.add(manyToManyRelation);
                });
            }
			if (relationDescriber.isCollectionRelation()) {
				Collection<Object> relations = (Collection<Object>) relation;
				saveIncludingRelationsInternal(changed, tx, relations, (Class<Object>) relationDescriber.getToClass().clazz);
                if (!manyToManyRelations.isEmpty()) {
                    saveIncludingRelationsInternal(changed, tx, manyToManyRelations, (Class<Object>) via);
                }
			} else {
				saveIncludingRelationInternal(changed, tx, relation, (Class<Object>) relationDescriber.getToClass().clazz);
			}
		}
	}

	protected ValqueriesQuery<T> query() {
		return baseRepo.query();
	}

	protected <O> ValqueriesQuery<O> query(Class<O> oClass) {
		return baseRepo.query(oClass);
	}

	protected ValqueriesQuery<T> query(ITransactionContext tx) {
		return baseRepo.query(tx);
	}

	@Override
	public <X> X obtainInTransaction(ITransactionWithResult<X> tx) {
		return baseRepo.obtainInTransaction(tx);
	}

	@Override
	public void doRetryableInTransaction(ITransaction tx) {
		baseRepo.doRetryableInTransaction(tx);
	}

	private static class UncheckedObjectMap extends ObjectMap {

		public void set(Token key, Object value) {
			put(key, value);
		}
	}
}
