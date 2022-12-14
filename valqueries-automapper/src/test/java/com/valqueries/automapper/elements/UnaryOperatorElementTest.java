package com.valqueries.automapper.elements;

import com.valqueries.automapper.SqlDialect;
import com.valqueries.automapper.ValqueriesQueryImpl;
import io.ran.Property;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UnaryOperatorElementTest {

	@Mock
	private ValqueriesQueryImpl<?> query;
	@Mock
	private Property<?> property;
	@Mock
	private SqlDialect dialect;

	@Test
	public void validWithUnaryOperator() {
		new UnaryOperatorElement(query, property, Operator.IS_NULL, dialect);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidWithBinaryOperator() {
		new UnaryOperatorElement(query, property, Operator.EQUALS, dialect);
	}
}