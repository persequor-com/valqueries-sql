package com.valqueries.automapper;

import com.valqueries.IStatement;
import com.valqueries.Setter;
import io.ran.CompoundKey;
import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.ObjectMapColumnizer;
import io.ran.Property;
import io.ran.token.Token;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CompoundColumnizer<T> extends ValqueriesColumnizer<T> implements Setter, ObjectMapColumnizer {
	protected final List<List<Consumer<IStatement>>> statements = new ArrayList<>();
	protected List<Consumer<IStatement>> currentStatements = new ArrayList<>();

	private final List<String> columns = new ArrayList<>();
	private final List<String> columnsWithoutKey = new ArrayList<>();
	private final List<List<String>> valueTokens = new ArrayList<>();
	private List<String> valueTokensCurrent = new ArrayList<>();
	private Token.TokenList tokens;
	private int index = 0;

	public CompoundColumnizer(GenericFactory genericFactory, MappingHelper mappingHelper, Collection<T> ts, SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;

		for (T t : ts) {
			if (tokens == null) {
				tokens = new Token.TokenList();
				for(Object propertyValue : mappingHelper.getKey(t).getValues()) {
					tokens.add(((Property.PropertyValue)propertyValue).getProperty().getToken());
				}
			}
			mappingHelper.columnize(t, this);
			index++;

			valueTokens.add(valueTokensCurrent);
			valueTokensCurrent = new ArrayList<>();
			statements.add(currentStatements);
			currentStatements = new ArrayList<>();

		}
		index = 0;
	}
	@Override
	public void set(IStatement statement) {
		for (List<Consumer<IStatement>> ss : statements) {
			for (Consumer<IStatement> s : ss) {
				s.accept(statement);
			}
			index++;
		}
	}

	protected String transformFieldPlaceholder(Token key) {
		return key.snake_case()+"_"+index;
	}

	protected void add(Token key, Consumer<IStatement> consumer) {
		if (index == 0) {
			columns.add(transformKey(key));
		}
		valueTokensCurrent.add(transformFieldPlaceholder(key));
		if (!tokens.contains(key)) {
			columnsWithoutKey.add(transformKey(key));
		}
		currentStatements.add(consumer);
	}

	public List<String> getColumns() {
		return columns;
	}

	public List<List<String>> getValueTokens() {
		return valueTokens;
	}

	public List<String> getColumnsWithoutKey() {
		return columnsWithoutKey;
	}
}
