package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CompoundKey;
import io.ran.GenericFactory;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.Property;
import io.ran.RelationDescriber;
import io.ran.TestDoubleDb;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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

	@Override
	public Optional<T> get(K k) {
		return Optional.ofNullable(getStore(modelType).get(k));
	}



	@Override
	public Stream<T> getAll() {
		return getStore(modelType).values().stream();
	}

	@Override
	public CrudUpdateResult deleteById(K k) {
		T existing = getStore(modelType).remove(k);
		return new CrudUpdateResult() {
			@Override
			public int affectedRows() {
				return existing != null ? 1 : 0;
			}
		};
	}

	private <Z> CrudUpdateResult save(Z o, Class<Z> zClass) {
		Mapping mapping = (Mapping)o;
		for (RelationDescriber relation : TypeDescriberImpl.getTypeDescriber(zClass).relations()) {
			if (!relation.getRelationAnnotation().autoSave()) {
				mapping._setRelation(relation, null);
			}
		}

		Object key = getGenericKey(o);
		Z existing = store.getStore(zClass).put((Object) key, o);
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
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> t) {
		t.forEach(this::save);
		return t::size;
	}

	@Override
	public <O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass) {
		return this.save(t, oClass);
	}

	@Override
	public <O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> ts, Class<O> oClass) {
		ts.forEach(t -> this.saveOther(tx, t, oClass));
		return ts::size;
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
}
