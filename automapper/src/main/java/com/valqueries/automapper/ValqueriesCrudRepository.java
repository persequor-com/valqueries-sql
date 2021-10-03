package com.valqueries.automapper;

import com.valqueries.ITransaction;
import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepository;
import io.ran.Resolver;

import java.util.Collection;
import java.util.function.Consumer;

public interface ValqueriesCrudRepository<T, K> extends CrudRepository<T, K> {
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	<O> CrudUpdateResult saveOther(ITransactionContext tx, O t, Class<O> oClass);
	<O> CrudUpdateResult saveOthers(ITransactionContext tx, Collection<O> t, Class<O> oClass);

	CrudUpdateResult saveIncludingRelations(T t);

	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
	void doRetryableInTransaction(ITransaction tx);
}
