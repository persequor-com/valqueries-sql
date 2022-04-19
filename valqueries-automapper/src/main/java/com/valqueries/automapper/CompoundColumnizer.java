package com.valqueries.automapper;

import com.valqueries.IStatement;
import com.valqueries.Setter;
import io.ran.*;
import io.ran.token.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CompoundColumnizer<T> extends ValqueriesColumnizer<T> implements Setter, ObjectMapColumnizer {
	protected final List<List<Consumer<IStatement>>> statements = new ArrayList<>();
	protected List<Consumer<IStatement>> currentStatements = new ArrayList<>();

	private final List<String> columns = new ArrayList<>();
	private final List<String> columnsWithoutKey = new ArrayList<>();
	private final List<List<String>> valueTokens = new ArrayList<>();
	private List<String> valueTokensCurrent = new ArrayList<>();
	private Token.TokenList keyFields;
	private int index = 0;


	public CompoundColumnizer(GenericFactory genericFactory, MappingHelper mappingHelper, Collection<T> ts, SqlNameFormatter sqlNameFormatter, SqlDialect dialect, TypeDescriber<T> typeDescriber) {
		this.typeDescriber = typeDescriber;
		this.dialect = dialect;
		this.sqlNameFormatter = sqlNameFormatter;
		this.key = mappingHelper.getKey(ts.stream().findFirst().get());
		for (T t : ts) {
			if (keyFields == null) {
				keyFields = new Token.TokenList();
				for(Object propertyValue : mappingHelper.getKey(t).getValues()) {
					keyFields.add(((Property.PropertyValue)propertyValue).getProperty().getToken());
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

	protected void add(Token token, Consumer<IStatement> consumer) {
		fields.put(token.snake_case(),transformKey(token));
		placeholders.add(token.snake_case());
		if (index == 0) {
			columns.add(transformKey(token));
		}
		valueTokensCurrent.add(transformFieldPlaceholder(token));
		if (keyFields.contains(token)) {
			keys.add(transformKey(token));
		} else {
			fieldsWithoutKeys.put(token.snake_case(),transformKey(token));
			columnsWithoutKey.add(transformKey(token));
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
