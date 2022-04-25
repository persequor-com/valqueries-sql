package com.valqueries.automapper;

import com.valqueries.IStatement;
import com.valqueries.Setter;
import io.ran.*;
import io.ran.token.Token;

import java.util.*;
import java.util.function.Consumer;

public class CompoundColumnizer<T> extends ValqueriesColumnizer<T> implements Setter, ObjectMapColumnizer {
	protected final List<List<Consumer<IStatement>>> statements = new ArrayList<>();
	protected List<Consumer<IStatement>> currentStatements = new ArrayList<>();

	private final Set<String> columns = new LinkedHashSet<>();
	private final Set<String> columnsWithoutKey = new LinkedHashSet<>();
	private final List<List<String>> valueTokens = new ArrayList<>();
	private List<String> valueTokensCurrent = new ArrayList<>();
	private Property.PropertyList keyFields;
	private int index = 0;


	public CompoundColumnizer(GenericFactory genericFactory, MappingHelper mappingHelper, Collection<T> ts, SqlNameFormatter sqlNameFormatter, SqlDialect dialect, TypeDescriber<T> typeDescriber) {
		this.typeDescriber = typeDescriber;
		this.dialect = dialect;
		this.sqlNameFormatter = sqlNameFormatter;
		this.key = mappingHelper.getKey(ts.stream().findFirst().get());
		for (T t : ts) {
			if (keyFields == null) {
				keyFields = new Property.PropertyList();
				for(Object propertyValue : mappingHelper.getKey(t).getValues()) {
					keyFields.add(((Property.PropertyValue)propertyValue).getProperty());
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

	protected String transformFieldPlaceholder(Property key) {
		return key.getSnakeCase()+"_"+index;
	}

	protected void add(Property property, Consumer<IStatement> consumer) {
		fields.put(property.getSnakeCase(),transformKey(property));
		if (index == 0) {
			columns.add(transformKey(property));
		}
		valueTokensCurrent.add(transformFieldPlaceholder(property));
		if (keyFields.contains(property.getSnakeCase())) {
			keys.add(transformKey(property));
		} else {
			fieldsWithoutKeys.put(property.getSnakeCase(),transformKey(property));
			columnsWithoutKey.add(transformKey(property));
		}
		currentStatements.add(consumer);
	}

	public Set<String> getColumns() {
		return columns;
	}

	public List<List<String>> getValueTokens() {
		return valueTokens;
	}

	public Set<String> getColumnsWithoutKey() {
		return columnsWithoutKey;
	}
}
