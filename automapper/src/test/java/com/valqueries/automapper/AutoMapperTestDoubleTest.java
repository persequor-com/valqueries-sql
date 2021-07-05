package com.valqueries.automapper;

import com.google.inject.Guice;
import io.ran.GenericFactory;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AutoMapperTestDoubleTest extends AutoMapperBaseTests {


	@Override
	protected void setInjector() {
		GuiceModule module = new TestDoubleGuiceModule();
		injector = Guice.createInjector(module);
		factory = injector.getInstance(GenericFactory.class);
	}
}
