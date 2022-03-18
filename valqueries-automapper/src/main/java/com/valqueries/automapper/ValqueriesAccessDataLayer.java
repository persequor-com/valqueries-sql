package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepositoryBaseRepo;

import java.util.Collection;


public interface ValqueriesAccessDataLayer<T, K> extends CrudRepositoryBaseRepo<T,K, ValqueriesQuery<T>> {
	<O> ValqueriesQuery<O> query(Class<O> oClass);
	ValqueriesQuery<T> query(ITransactionContext tx);
	<O> ValqueriesQuery<O> query(ITransactionContext tx, Class<O> oClass);
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult insert(ITransactionContext tx, T t) throws ValqueriesDuplicateKeyException;
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	CrudUpdateResult insert(ITransactionContext tx, Collection<T> t) throws ValqueriesDuplicateKeyException;
	<O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass);
	<O> CrudUpdateResult insertOther(ITransactionContext tx, O t, Class<O> oClass) throws ValqueriesDuplicateKeyException;
	<O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass);
	<O> CrudUpdateResult insertOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass) throws ValqueriesDuplicateKeyException;
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
	void doRetryableInTransaction(ITransaction tx);
}
