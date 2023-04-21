package com.valqueries.automapper.elements;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnaryUnaryOperatorElementTest {

	@Mock
	private ValqueriesQueryImpl<?> query;
	@Mock
	private Property<?> property;
	@Mock
	private SqlDialect dialect;

	@Test
	public void validWithUnaryOperator() {
		new UnaryOperatorElement(query, property, UnaryOperator.IS_NULL, dialect);
	}
}