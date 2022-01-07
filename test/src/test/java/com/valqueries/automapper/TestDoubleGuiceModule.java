package com.valqueries.automapper;


public class TestDoubleGuiceModule extends GuiceModule {
	public TestDoubleGuiceModule() {
		super(null, ValqueriesTestDoubleResolver.class);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(ValqueriesRepositoryFactory.class).to(ValqueriesTestDoubleRepositoryFactory.class);
	}
}
