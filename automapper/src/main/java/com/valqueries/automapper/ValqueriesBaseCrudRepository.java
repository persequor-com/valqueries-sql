package com.valqueries.automapper;

import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepository;
import io.ran.CrudRepositoryBaseRepo;

import java.util.Collection;

public interface ValqueriesBaseCrudRepository<T, K> extends CrudRepositoryBaseRepo<T,K, ValqueriesQuery<T>> {
	ValqueriesQuery<T> query(ITransactionContext tx);
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
}
