package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.*;
import io.ran.token.Token;

import java.util.*;
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
	public CrudUpdateResult insert(ITransactionContext tx, T entity) throws ValqueriesInsertFailedException {
		final ChangeMonitor changed = new ChangeMonitor();
		changed.increment(entity, insertOther(tx, entity, modelType).affectedRows());
		return changed::getNumberOfChangedRows;
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> entities) {
		final ChangeMonitor changed = new ChangeMonitor();
		saveIncludingRelationsInternal(changed, tx, entities, modelType);
		return changed::getNumberOfChangedRows;
	}

	@Override
	public CrudUpdateResult insert(ITransactionContext tx, Collection<T> entities) throws ValqueriesInsertFailedException {
		final ChangeMonitor changed = new ChangeMonitor();
		changed.increment(entities, insertOthers(tx, entities, modelType).affectedRows());
		return changed::getNumberOfChangedRows;
	}

	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass) {
		return baseRepo.saveOther(tx, t, oClass);
	}

	@Override
	public <O> CrudUpdateResult insertOther(ITransactionContext tx, O t, Class<O> oClass) throws ValqueriesInsertFailedException {
		return baseRepo.insertOther(tx, t, oClass);
	}

	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass) {
		return baseRepo.saveOthers(tx, t, oClass);
	}

	@Override
	public <O> CrudUpdateResult insertOthers(ITransactionContext tx, Collection<O> ts, Class<O> oClass) throws ValqueriesInsertFailedException {
		return baseRepo.insertOthers(tx, ts, oClass);
	}
	
	/*
		Save methods
	 */
	
	private <O> void saveIncludingRelationsInternal(ChangeMonitor changed, ITransactionContext tx, Collection<O> ts, Class<O> xClass) {
		Collection<O> notAlreadySaved = ts.stream().filter(t -> !changed.isAlreadySaved(t)).collect(Collectors.toList());
		changed.increment(notAlreadySaved, saveOthers(tx, notAlreadySaved, xClass).affectedRows());
		TypeDescriberImpl.getTypeDescriber(xClass).relations().forEach(relationDescriber -> {
			internalSaveRelation(changed, tx, notAlreadySaved, relationDescriber);
		});
	}
	
	private <X> void saveIncludingRelationInternal(ChangeMonitor changed, ITransactionContext tx, X t, Class<X> xClass) {
		if (changed.isAlreadySaved(t)) {
			return;
		}
		changed.increment(t, saveOther(tx, t, xClass).affectedRows());
		TypeDescriberImpl.getTypeDescriber(xClass).relations().forEach(relationDescriber -> {
			internalSaveRelation(changed, tx, Arrays.asList(t), relationDescriber);
		});
	}

	private <O> void internalSaveRelation(ChangeMonitor changed, ITransactionContext tx, Collection<O> ts, RelationDescriber relationDescriber) {
		Class<?> via = relationDescriber.getRelationAnnotation().via();
		Set<Object> objectsToSave = new HashSet<>();
		Set<Object> manyToManyRelations = new HashSet<>();

		for(O t : ts) {
			if (!relationDescriber.getRelationAnnotation().autoSave()) {
				return;
			}
			if (!(t instanceof Mapping)) {
				throw new RuntimeException("Valqueries models should have a @Mapper annotation");
			}
			Mapping mapping = (Mapping) t;
			Object relation = mapping._getRelation(relationDescriber);

			if (relation != null) {
				manyToManyRelations.addAll(getManyToManyRelations(relationDescriber, mapping, relation, via));
				if(relationDescriber.isCollectionRelation()) {
					objectsToSave.addAll((Collection<Object>) relation);
				} else {
					objectsToSave.add(relation);
				}
			}
		}

		if(!objectsToSave.isEmpty()) {
			saveIncludingRelationsInternal(changed, tx, objectsToSave, (Class<Object>) relationDescriber.getToClass().clazz);
		}
		if (!manyToManyRelations.isEmpty()) {
			saveIncludingRelationsInternal(changed, tx, manyToManyRelations, (Class<Object>) via);
		}
	}

	private Collection<Object> getManyToManyRelations(RelationDescriber relationDescriber, Mapping mapping, Object relation, Class<?> via) {
		Collection<Object> manyToManyRelations = new ArrayList<>();

		if (!via.equals(None.class)) {
			((Collection <?>) relation).forEach(rel -> {
				Mapping mappingRelation = (Mapping) rel;
				Mapping manyToManyRelation = (Mapping) factory.genericFactory.get(via);
				UncheckedObjectMap map = new UncheckedObjectMap();

				relationDescriber.getVia().forEach(viaRelationDescriber -> {
					Property.PropertyList properties = viaRelationDescriber.getFromKeys().toProperties();

					for (int i = 0; i < properties.size(); i++) {
						Token propertiesToken = properties.get(i).getToken();
						Token viaToken = viaRelationDescriber.getToKeys().get(i).getToken();

						if (viaRelationDescriber.getToClass().clazz.isAssignableFrom(mappingRelation.getClass())) {
							map.set(propertiesToken, mappingRelation._getKey().getValues().get(viaToken).getValue());
						} else {
							map.set(viaToken, mapping._getKey().getValues().get(propertiesToken).getValue());
						}
					}
				});
				manyToManyRelation.hydrate(map);
				manyToManyRelations.add(manyToManyRelation);
			});
		}
		return manyToManyRelations;
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
