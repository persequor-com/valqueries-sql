package com.valqueries.automapper;

import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CompoundKey;
import io.ran.DbResolver;
import io.ran.GenericFactory;
import io.ran.KeySet;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.Property;
import io.ran.RelationDescriber;
import io.ran.Resolver;
import io.ran.TestDoubleDb;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesCrudRepositoryTestDoubleBase<T, K> implements ValqueriesBaseCrudRepository<T, K> {
	private final TestDoubleDb store;
	protected GenericFactory genericFactory;
	protected Class<T> modelType;
	protected Class<K> keyType;
	protected TypeDescriber<T> typeDescriber;
	protected MappingHelper mappingHelper;

	public ValqueriesCrudRepositoryTestDoubleBase(GenericFactory genericFactory, Class<T> modelType, Class<K> keyType, MappingHelper mappingHelper, TestDoubleDb store) {
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

	@Override
	public CrudUpdateResult save(T t) {
		K key = getKey(t);
		T existing = getStore(modelType).put((Object) key, t);
		return new CrudUpdateResult() {
			@Override
			public int affectedRows() {
				return existing != null && !existing.equals(t) ? 1 : 0;
			}
		};
	}

	private K getKey(T t) {
		K key;
		CompoundKey k = getCompoundKeyFor(t);
		if (CompoundKey.class.isAssignableFrom(keyType)) {
			key = (K)k;
		} else {
			key = (K)((Property.PropertyValueList<?>)k.getValues()).get(0).getValue();
		}
		return key;
	}

	private CompoundKey getCompoundKeyFor(Object t) {
		return mappingHelper.getKey(t);
	}

	public ValqueriesQuery<T> query() {
		return new TestDoubleQuery<T>(modelType, genericFactory, mappingHelper, store);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, T t) {
		return save(t);
	}

	@Override
	public CrudUpdateResult save(ITransactionContext tx, Collection<T> t) {
		t.forEach(this::save);
		return () -> t.size();
	}

	@Override
	public ValqueriesQuery<T> query(ITransactionContext tx) {
		return query();
	}

	@Override
	public <X> X obtainInTransaction(ITransactionWithResult<X> tx) {
		try {
			return tx.execute(new TestDoubleTransactionContext());
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
