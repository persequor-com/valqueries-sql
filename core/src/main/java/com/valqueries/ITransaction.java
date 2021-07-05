
package com.valqueries;

public interface ITransaction {
	void execute(ITransactionContext transactionContext) throws Exception;
}
