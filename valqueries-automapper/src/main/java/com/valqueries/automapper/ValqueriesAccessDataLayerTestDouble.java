package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ValqueriesAccessDataLayerTestDouble<T, K> implements ValqueriesAccessDataLayer<T, K> {
	private final TestDoubleDb store;
	protected GenericFactory genericFactory;
	protected Class<T> modelType;
	protected Class<K> keyType;
	protected TypeDescriber<T> typeDescriber;
	protected MappingHelper mappingHelper;

	public ValqueriesAccessDataLayerTestDouble(GenericFactory genericFactory, Class<T> modelType, Class<K> keyType, MappingHelper mappingHelper, TestDoubleDb store) {
		this.store = store;
		this.genericFactory = genericFactory;
		this.modelType = modelType;

		this.keyType = keyType;
		this.typeDescriber = TypeDescriberImpl.getTypeDescriber(modelType);
		this.mappingHelper = mappingHelper;
	}

	Map<Object, T> getStore(Class<T> modelType) {
		return store.getStore(modelType);
	}

	private T mappingCopy(T t) {
		T tc = genericFactory.get(modelType);
		mappingHelper.copyValues(modelType, t, tc);
		return tc;
	}

	private <O> O mappingCopy(Class<O> c,O t) {
		O tc = genericFactory.get(c);
		mappingHelper.copyValues(c, t, tc);
		return tc;
	}

	@Override
	public Optional<T> get(K k) {
		return Optional.ofNullable(getStore(modelType).get(k)).map(this::mappingCopy);
	}



	@Override
	public Stream<T> getAll() {
		return getStore(modelType).values().stream().map(this::mappingCopy);
	}

	@Override
	public CrudUpdateResult deleteById(K k) {
		T existing = getStore(modelType).remove(k);
		return () -> existing != null ? 1 : 0;
	}

	@Override
	public CrudUpdateResult deleteByIds(Collection<K> collection) {
		int removed = collection.stream()
				.mapToInt(key -> getStore(modelType).remove(key) != null ? 1 : 0)
				.sum();
		return () -> removed;
	}

	private <Z> CrudUpdateResult save(Z o, Class<Z> zClass) {
		Object key = getGenericKey(o);

		Z existing = store.getStore(zClass).put((Object) key, mappingCopy(zClass, o));
		return new CrudUpdateResult() {
			@Override
			public int affectedRows() {
				return existing != null && !existing.equals(o) ? 1 : 0;
			}
		};
	}

	@Override
	public CrudUpdateResult save(T t) {
		return save(t, modelType);
	}

	private <T2, K2> K2 getGenericKey(T2 t) {
		K2 key;
		CompoundKey k = getCompoundKeyFor(t);
		if (CompoundKey.class.isAssignableFrom(keyType)) {
			key = (K2)k;
		} else {
			key = (K2)((Property.PropertyValueList<?>)k.getValues()).get(0).getValue();
		}
		return key;
	}

	private K getKey(T t) {
		return getGenericKey(t);
	}

	private CompoundKey getCompoundKeyFor(Object t) {
		return mappingHelper.getKey(t);
	}

	public ValqueriesQuery<T> query() {
		return new TestDoubleQuery<T>(modelType, genericFactory, mappingHelper, store);
	}

	@Override
	public <O> ValqueriesQuery<O> query(Class<O> oClass) {
		return new TestDoubleQuery<O>(oClass, genericFactory, mappingHelper, store);
	}


	@Override
	public CrudUpdateResult save(ITransactionContext tx, T t) {
		return save(t);
	}

	@Override
	public CrudUpdateResult insert(ITransactionContext tx, T t) throws ValqueriesInsertFailedException {
		return insert(t, modelType);
	}

	private <Z> CrudUpdateResult insert(Z o, Class<Z> zClass) throws ValqueriesInsertFailedException {
		Mapping mapping = (Mapping)o;
		// for Insert, we ignore all relations on purpose
		for (RelationDescriber relation : TypeDescriberImpl.getTypeDescriber(zClass).relations()) {
				mapping._setRelation(relation, null);
				mapping._setRelationNotLoaded(relation);
		}

		Object key = getGenericKey(o);
		if(store.getStore(zClass).get((Object) key) != null){
			throw new ValqueriesInsertFailedException("Duplicate entry");
		}
		store.getStore(zClass).put((Object) key, mappingCopy(zClass, o));
		return () -> 1;
	}
	
	private <Z> void delete(Z object, Class<Z> zClass){
		Object key = getGenericKey(object);
		store.getStore(zClass).remove(object);
	}
	
	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> t) {
		t.forEach(this::save);
		return t::size;
	}

	@Override
	public CrudUpdateResult insert(ITransactionContext tx, Collection<T> ts) throws ValqueriesInsertFailedException {
		AtomicInteger affectedRows = new AtomicInteger();
		final Set<T> insertedRows = new HashSet<>();
		try{
			for (T t : ts ){
				affectedRows.addAndGet(insert(t, modelType).affectedRows());
				insertedRows.add(t);
			}
		} catch (ValqueriesInsertFailedException e){
			// mimic rollback mechanism
			insertedRows.forEach(row -> delete(row, modelType));
			throw e;
		}
		return affectedRows::get;
	}

	@Override
	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass) {
		return this.save(t, oClass);
	}

	@Override
	public <O> CrudUpdateResult insertOther(ITransactionContext tx, O t, Class<O> oClass) throws ValqueriesInsertFailedException {
		return this.insert(t, oClass);
	}

	@Override
	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> ts, Class<O> oClass) {
		ts.forEach(t -> this.saveOther(tx, t, oClass));
		return ts::size;
	}

	@Override
	public <O> CrudUpdateResult insertOthers(ITransactionContext tx, Collection<O> ts, Class<O> oClass) throws ValqueriesInsertFailedException {
		AtomicInteger affectedRows = new AtomicInteger();
		for (O t : ts ){
			affectedRows.addAndGet(insert(t, oClass).affectedRows());
		}
		return affectedRows::get;
	}


	@Override
	public ValqueriesQuery<T> query(ITransactionContext tx) {
		return query();
	}

	@Override
	public <O> ValqueriesQuery<O> query(ITransactionContext tx, Class<O> oClass) {
		return query(oClass);
	}


	@Override
	public <X> X obtainInTransaction(ITransactionWithResult<X> tx) {
		try {
			return tx.execute(new TestDoubleTransactionContext());
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void doRetryableInTransaction(ITransaction tx) {
		try {
			tx.execute(new TestDoubleTransactionContext());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<T> get(ITransactionContext tx, K k) {
		return get(k);
	}

	@Override
	public Stream<T> getAll(ITransactionContext tx) {
		return getAll();
	}

	@Override
	public CrudUpdateResult deleteById(ITransactionContext tx, K k) {
		return deleteById(k);
	}

	@Override
	public CrudUpdateResult deleteByIds(ITransactionContext tx, Collection<K> collection) {
		return deleteByIds(collection);
	}
}
