
package com.valqueries;

public interface ITransactionWithResult<T> {
	T execute(ITransactionContext transactionContext) throws Exception;
}
