package com.valqueries.automapper;


import com.valqueries.Database;

import static org.mockito.Mockito.mock;

public class TestDoubleGuiceModule extends GuiceModule {
	public TestDoubleGuiceModule() {
		super(null, ValqueriesTestDoubleResolver.class);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(Database.class).toProvider(()->mock(Database.class));
		bind(ValqueriesRepositoryFactory.class).to(ValqueriesTestDoubleRepositoryFactory.class);
	}
}
