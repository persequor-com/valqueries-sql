/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-12-02
 */
package com.valqueries;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {

	private Database database;
	@Mock
	private DataSource dataSource;
	@Mock
	private Connection connection;
	@Mock
	private ITransaction tx;
	private MySQLTransactionRollbackException deadlockEx = new MySQLTransactionRollbackException("reason", "40001", 1213);

	@Before
	public void setup() throws Exception {
		doThrow(deadlockEx).when(tx).execute(any());
		when(dataSource.getConnection()).thenReturn(connection);
		database = new Database(dataSource);
	}

	@Test
	public void doInRetryableTransaction_retries3times_reachesMaximum() throws Exception {
		try {
			database.doInTransaction( 3, Duration.ofMillis(100), tx);
			fail("Should fail at this point");
		} catch (OrmException e) {
			assertTrue(e.getCause() instanceof MySQLTransactionRollbackException);
		}
		verify(tx, times(3)).execute(any());
	}

	@Test
	public void doInRetryableTransaction_retries2times_doesntReachMaximum() throws Exception {
		doThrow(deadlockEx)
				.doNothing()
				.when(tx).execute(any());
		database.doInTransaction(3, Duration.ofMillis(100), tx);
		verify(tx, times(2)).execute(any());
	}

	@Test
	public void doInRetryableTransaction_doesntThrowDeadlock_doesntRetry() throws Exception {
		doThrow(new IllegalArgumentException())
				.doNothing()
				.when(tx).execute(any());
		try {
			database.doInTransaction(3, Duration.ofMillis(100), tx);
			fail("Should fail at this point");
		} catch (OrmException e) {
			assertEquals(e.getCause().getClass(), IllegalArgumentException.class);
		}
		verify(tx).execute(any());
	}
}
