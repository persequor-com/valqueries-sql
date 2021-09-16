package com.valqueries.automapper;

import com.valqueries.ITransactionContext;
import com.valqueries.ITransactionWithResult;
import io.ran.CrudRepository;
import io.ran.Resolver;

import java.util.Collection;
import java.util.function.Consumer;

public interface ValqueriesCrudRepository<T, K> extends CrudRepository<T, K> {
	CrudUpdateResult save(ITransactionContext tx, T t);
	CrudUpdateResult save(ITransactionContext tx, Collection<T> t);
	<X> X obtainInTransaction(ITransactionWithResult<X> tx);
}
