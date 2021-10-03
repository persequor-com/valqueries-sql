/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 
 */
package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.RelationDescriber;
import io.ran.TypeDescriberImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesCrudRepositoryImpl<T, K> implements ValqueriesCrudRepository<T, K> {

	private final ValqueriesBaseCrudRepository<T, K> baseRepo;
	private Class<T> modelType;

	public ValqueriesCrudRepositoryImpl(ValqueriesRepositoryFactory factory, Class<T> modelType, Class<K> keyType) {
		this.modelType = modelType;
		baseRepo = factory.get(modelType, keyType);
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
	public CrudUpdateResult save(T t) {
		return baseRepo.save(t);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, T t) {
		return baseRepo.save(tx, t);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> t) {
		return baseRepo.save(tx, t);
	}

	@Override
	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass) {
		return baseRepo.saveOther(tx, t, oClass);
	}

	@Override
	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass) {
		return baseRepo.saveOthers(tx, t, oClass);
	}


	@Override
	public CrudUpdateResult saveIncludingRelations(T t) {
		final ChangeMonitor changed = new ChangeMonitor();
		doRetryableInTransaction(tx -> {
			saveIncludingRelationInternal(changed, tx, t, modelType);
		});
		return changed::getNumberOfChangedRows;
	}

	private <X> void saveIncludingRelationsInternal(ChangeMonitor changed, ITransactionContext tx, Collection<X> ts, Class<X> xClass) {
		Collection<X> notAlreadySaved = ts.stream().filter(t -> !changed.isAlreadySaved(t)).collect(Collectors.toList());
		changed.increment(notAlreadySaved, saveOthers(tx, notAlreadySaved, xClass).affectedRows());
		TypeDescriberImpl.getTypeDescriber(xClass).relations().forEach(relationDescriber -> {
			for(X t : notAlreadySaved) {
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
		if (relation != null) {
			if (relationDescriber.isCollectionRelation()) {
				Collection<Object> relations = (Collection<Object>) relation;
				saveIncludingRelationsInternal(changed, tx, relations, (Class<Object>) relationDescriber.getToClass().clazz);
			} else {
				saveIncludingRelationInternal(changed, tx, relation, (Class<Object>) relationDescriber.getToClass().clazz);
			}
		}
	}

	protected ValqueriesQuery<T> query() {
		return baseRepo.query();
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
}
