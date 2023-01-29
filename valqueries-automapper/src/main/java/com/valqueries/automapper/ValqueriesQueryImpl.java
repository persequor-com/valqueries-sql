package com.valqueries.automapper;

import com.valqueries.*;
import com.valqueries.automapper.elements.*;
import io.ran.*;
import io.ran.token.Token;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesQueryImpl<T> extends BaseValqueriesQuery<T> implements ValqueriesQuery<T>, Setter, AutoCloseable {
	private ITransactionContext transactionContext;
	private Class<T> modelType;
	private List<Element> elements = new ArrayList<>();
	private List<SortElement> sortElements = new ArrayList<>();
	private int fieldNum = 0;
	private int subQueryNum = 0;
	private int intermediateNum = 0;
	private Collection<RelationDescriber> eagers = new ArrayList<>();
	private String tableAlias = "main";
	private GenericFactory genericFactory;
	private Integer limit = null;
	private int offset = 0;
	private SqlNameFormatter sqlNameFormatter;
	private MappingHelper mappingHelper;
	private SqlDialect dialect;
	private boolean forcedEmpty = false;

	public ValqueriesQueryImpl(ITransactionContext transactionContext, Class<T> modelType, GenericFactory genericFactory, SqlNameFormatter sqlNameFormatter, MappingHelper mappingHelper, SqlDialect dialect) {
		super(modelType, genericFactory);
		this.transactionContext = transactionContext;
		this.modelType = modelType;
		this.genericFactory = genericFactory;
		this.sqlNameFormatter = sqlNameFormatter;
		this.mappingHelper = mappingHelper;
		this.dialect = dialect;
	}

	@Override
	public ValqueriesQuery<T> eq(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "=", ++fieldNum, dialect));
		return this;
	}

	@Override
	public ValqueriesQuery<T> in(Property.PropertyValueList propertyValues) {
		elements.add(new ListElement(this, propertyValues, "IN", ++fieldNum, sqlNameFormatter, dialect));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "like", ++fieldNum, dialect));
		return this;
	}

	@Override
	ValqueriesQuery<T> freetext(Property.PropertyValue<?> propertyValue) {
		elements.add(new FreeTextElement(this, propertyValue, ++fieldNum, sqlNameFormatter, dialect));
		return this;
	}


	@Override
	public ValqueriesQuery<T> gt(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, ">", ++fieldNum, dialect));
		return this;
	}

	@Override
	public ValqueriesQuery<T> gte(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, ">=", ++fieldNum, dialect));
		return this;
	}

	@Override
	public ValqueriesQuery<T> lt(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "<", ++fieldNum, dialect));
		return this;
	}

	@Override
	public ValqueriesQuery<T> lte(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "<=", ++fieldNum, dialect));
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <X, Z extends CrudRepository.InlineQuery<X, Z>> ValqueriesQuery<T> subQuery(RelationDescriber relation, Consumer<Z> consumer) {
		return join(relation, consumer);
	}

	private <X, Z extends CrudRepository.InlineQuery<X, Z>> ValqueriesQuery<T> join(RelationDescriber relation, Consumer<Z> consumer) {
		if (!relation.getVia().isEmpty()) {
			RelationDescriber intermediateRelation = relation.getVia().get(0);
			ValqueriesQueryImpl intermediateQuery = new ValqueriesQueryImpl(transactionContext, intermediateRelation.getToClass().clazz, genericFactory, sqlNameFormatter, mappingHelper, dialect);
			intermediateQuery.tableAlias = "intermediate" + (++intermediateNum);
			elements.add(new RelationSubQueryElement(tableAlias, intermediateQuery.tableAlias, intermediateRelation, intermediateQuery, sqlNameFormatter, dialect));
			try {
				consumer.accept((Z) intermediateQuery);
			} catch (Exception e) {
				RelationDescriber endRelation = relation.getVia().get(1);
				ValqueriesQueryImpl endRelationQuery = new ValqueriesQueryImpl(transactionContext, endRelation.getToClass().clazz, genericFactory, sqlNameFormatter, mappingHelper, dialect);
				endRelationQuery.tableAlias = "sub" + (++subQueryNum);
				consumer.accept((Z) endRelationQuery);
				intermediateQuery.elements.add(new RelationSubQueryElement(intermediateQuery.tableAlias, endRelationQuery.tableAlias, endRelation, endRelationQuery, sqlNameFormatter, dialect));
			}
		} else {
			ValqueriesQueryImpl otherQuery = new ValqueriesQueryImpl(transactionContext, relation.getToClass().clazz, genericFactory, sqlNameFormatter, mappingHelper, dialect);
			otherQuery.tableAlias = "sub" + (++subQueryNum);
			consumer.accept((Z) otherQuery);
			elements.add(new RelationSubQueryElement(tableAlias, otherQuery.tableAlias, relation, otherQuery, sqlNameFormatter, dialect));
		}
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNull(Property<?> property) {
		elements.add(new UnaryOperatorElement(this, property, "IS NULL", dialect));
		return this;
	}

	public ValqueriesQuery<T> isNotNull(Property<?> property) {
		elements.add(new UnaryOperatorElement(this, property, "IS NOT NULL", dialect));
		return this;
	}

	public String buildSimpleSelectSql(String tableAlias, String where, Collection<String> columns) {
		T t = genericFactory.get(modelType);
		String columnsSql;
		if (columns.size() == 0) {
			ValqueriesColumnBuilder columnBuilder = new ValqueriesColumnBuilder(tableAlias, sqlNameFormatter, dialect,typeDescriber);
			mappingHelper.hydrate(t, columnBuilder);
			columnsSql = columnBuilder.getSql();
		} else {
			columnsSql = String.join(", ", columns);
		}

		String sql ="SELECT "+columnsSql+" FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " " + tableAlias + " " + elements.stream().map(Element::fromString).filter(Objects::nonNull).collect(Collectors.joining(", "));

		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
			sql += " AND "+where;
		} else {
			sql += " WHERE "+where;
		}

		if (!sortElements.isEmpty()) {
			sql += " ORDER BY " + sortElements.stream().map(Element::queryString).collect(Collectors.joining(", "));
		}

		if (limit != null) {
			sql += dialect.getLimitDefinition(offset, limit);
		} else if (!sortElements.isEmpty()) {
			sql += dialect.getLimitDefinition(0, Integer.MAX_VALUE);
		}


		return sql;
	}

	public String buildSelectSql(String tableAlias, String... columns) {
		T t = genericFactory.get(modelType);
		String columnsSql;
		if (columns.length == 0) {
			ValqueriesColumnBuilder columnBuilder = new ValqueriesColumnBuilder(tableAlias, sqlNameFormatter, dialect,typeDescriber);
			mappingHelper.hydrate(t, columnBuilder);
			columnsSql = columnBuilder.getSql();
		} else {
			columnsSql = Arrays.stream(columns).map(c -> c).collect(Collectors.joining(", "));
		}

		StringBuilder eagerSelect = new StringBuilder();
		StringBuilder eagerJoin = new StringBuilder();
		int eagerCount = 0;
		int intermediateCount = 0;
		for (RelationDescriber relation : eagers) {
			eagerCount++;
			if (relation.getRelationAnnotation().via() != None.class) {
				intermediateCount++;
				RelationDescriber intermediateTableRelation = relation.getVia().stream().filter(rel -> rel.getToClass().clazz.isAssignableFrom(relation.getRelationAnnotation().via())).findFirst().get();
				String intermediateTable = getTableName(intermediateTableRelation.getToClass());
				String intermediateTableAlias = "intermediate" + intermediateCount;
				appendJoin(intermediateTableRelation, eagerJoin, eagerSelect, tableAlias, intermediateTable, intermediateTableAlias);
				RelationDescriber endTableRelation = relation.getVia().stream().filter(rel -> !rel.getToClass().clazz.isAssignableFrom(relation.getRelationAnnotation().via())).findFirst().get();
				appendJoin(endTableRelation, eagerJoin, eagerSelect, intermediateTableAlias, eagerCount);
			} else {
				appendJoin(relation, eagerJoin, eagerSelect, tableAlias, eagerCount);
			}
		}
		String sql ="SELECT " + columnsSql + eagerSelect + " FROM (SELECT * FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " " + tableAlias + " " + elements.stream().map(Element::fromString).filter(Objects::nonNull).collect(Collectors.joining(", "));

		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}

		if (!sortElements.isEmpty()) {
			sql += " ORDER BY " + sortElements.stream().map(Element::queryString).collect(Collectors.joining(", "));
		}

		if (limit != null) {
			sql += dialect.getLimitDefinition(offset, limit);
		} else if (!sortElements.isEmpty()) {
			sql += dialect.getLimitDefinition(0, Integer.MAX_VALUE);
		}

		sql += ") " + tableAlias + " " + eagerJoin;

		return sql;
	}

	public String getTableName(Clazz<?> toClass) {
		return dialect.getTableName(toClass).toSql();
	}

	public ValqueriesQuery<T> withEager(RelationDescriber relation) {
		eagers.add(relation);
		return this;
	}

	@Override
	public <X extends Comparable<X>> ValqueriesQuery<T> sortAscending(Property<X> property) {
		sortElements.add(new SortElement<T>(this, property, true, dialect));
		return this;
	}

	@Override
	public <X extends Comparable<X>> ValqueriesQuery<T> sortDescending(Property<X> property) {
		sortElements.add(new SortElement<T>(this, property, false, dialect));
		return null;
	}


	@Override
	public ValqueriesQuery<T> limit(int offset, int limit) {
		this.limit = limit;
		this.offset = offset;
		return this;
	}

	@Override
	public ValqueriesQuery<T> limit(int limit) {
		this.limit = limit;
		return this;
	}


	@Override
	public Stream<T> execute() {
		try {
			if (forcedEmpty) {
				return Stream.empty();
			}
			Map<CompoundKey, T> alreadyLoaded = new LinkedHashMap<>();
			Map<CompoundKey, Set<CompoundKey>> relationsAlreadyLoaded = new LinkedHashMap<>();
			Map<Token, Map<CompoundKey, List>> eagerModels = new HashMap<>();
			transactionContext.query(buildSelectSql("main"), this, row -> {
				T t2 = genericFactory.get(modelType);
				mappingHelper.hydrate(t2, new ValqueriesHydrator("main_", row, sqlNameFormatter,typeDescriber));
				CompoundKey key = mappingHelper.getKey(t2);
				if (alreadyLoaded.containsKey(key)) {
					t2 = alreadyLoaded.get(key);
				} else {
					alreadyLoaded.put(key, t2);
				}
				int i = 0;
				for (RelationDescriber relationDescriber : eagers) {
					List list = eagerModels
							.computeIfAbsent(relationDescriber.getField(), (k) -> new HashMap<>())
							.computeIfAbsent(key, (k) -> new ArrayList());

					Object hydrated = hydrateEager(t2, relationDescriber, row, ++i);
					if (hydrated != null) {
						CompoundKey relationKey = mappingHelper.getKey(hydrated);
						if (!relationsAlreadyLoaded.containsKey(key) || !relationsAlreadyLoaded.get(key).contains(relationKey)) {
							relationsAlreadyLoaded.computeIfAbsent(key, set -> new HashSet<>()).add(relationKey);
							list.add(hydrated);
						}
					}
				}
				return t2;
			});
			for (RelationDescriber relationDescriber : eagers) {
				Map<CompoundKey, List> eagerModel = eagerModels.get(relationDescriber.getField());
				if (eagerModel != null) {
					eagerModel.entrySet().forEach(entry -> {
						if (relationDescriber.isCollectionRelation()) {
							((Mapping) alreadyLoaded.get(entry.getKey()))._setRelation(relationDescriber, entry.getValue());
						} else {
							mapping(alreadyLoaded.get(entry.getKey()))._setRelation(relationDescriber, entry.getValue().size() > 0 ? entry.getValue().get(0): null);
						}
					});
				}
			}
			return alreadyLoaded.values().stream();
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public long count() {
		try {
			String sql = buildCountSql();
			return transactionContext.query(sql, this, row -> {
				return row.getLong("the_count");
			}).stream().findFirst().get();
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected GroupNumericResult aggregateMethod(Property resultProperty, String aggregateMethod) {
		try {
			String sql = buildGroupAggregateSql(resultProperty, aggregateMethod);
			Map<GroupNumericResultImpl.Grouping, Long> res = transactionContext.query(sql, this, row -> {
				CapturingHydrator hydrator = new CapturingHydrator(new ValqueriesHydrator(row, sqlNameFormatter, typeDescriber));
				T t = genericFactory.get(modelType);
				mappingHelper.hydrate(t, hydrator);
				return new GroupNumericResultImpl.Grouping(hydrator.getValues(), row.getLong("the_count"));
			}).stream().collect(Collectors.toMap(g -> g, g -> (long) g.getValue()));
			return new GroupNumericResultImpl(res);
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected GroupStringResult aggregateString(Property resultProperty, String separator) {
		try {
			String sql = buildGroupConcatAggregateSql(resultProperty, separator);
			Map<GroupStringResultImpl.Grouping, String> res = transactionContext.query(sql, this, row -> {
				CapturingHydrator hydrator = new CapturingHydrator(new ValqueriesHydrator(row, sqlNameFormatter, typeDescriber));
				T t = genericFactory.get(modelType);
				mappingHelper.hydrate(t, hydrator);
				return new GroupStringResultImpl.Grouping(hydrator.getValues(), row.getString("the_group_concat"));
			}).stream().collect(Collectors.toMap(g -> g, g -> g.getValue().toString()));
			return new GroupStringResultImpl(res);
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected GroupNumericResult count(Property resultProperty) {
		return aggregateMethod(resultProperty, "COUNT");
	}

	@Override
	protected GroupNumericResult sum(Property resultProperty) {
		return aggregateMethod(resultProperty, "SUM");
	}

	@Override
	protected GroupNumericResult max(Property resultProperty) {
		return aggregateMethod(resultProperty, "MAX");
	}

	@Override
	protected GroupNumericResult min(Property resultProperty) {
		return aggregateMethod(resultProperty, "MIN");
	}

	@Override
	protected GroupStringResult concat(Property resultProperty, String separator) {
		return aggregateString(resultProperty, separator);
	}

	@Override
	public CrudRepository.CrudUpdateResult delete() {
		try {
			if (forcedEmpty) {
				return () -> 0;
			}
			String sql = buildDeleteSql();
			UpdateResult update = transactionContext.update(sql, this);
			return () -> update.getAffectedRows();
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String buildDeleteSql() {
		return dialect.generateDeleteStatement(tableAlias, typeDescriber, elements, offset, limit);
	}

	private String buildCountSql() {
		String sql = "SELECT COUNT(1) as the_count FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " " + tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
//		System.out.println(sql);
		return sql;
	}


	private String buildGroupAggregateSql(Property resultProperty, String aggregateMethod) {
		String sql = "SELECT " + aggregateMethod + "(" + dialect.column(resultProperty) + ") as the_count , " + groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", ")) + " FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " " + tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += " GROUP BY " + groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", ")) + "";
//		System.out.println(sql);
		return sql;
	}

	private String buildGroupConcatAggregateSql(Property resultProperty, String separator) {

		String sql = "SELECT "+dialect.groupConcat(resultProperty, separator)+" as the_group_concat , " + groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", ")) + " FROM " + getTableName(Clazz.of(typeDescriber.clazz())) + " " + tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += " GROUP BY " + groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", ")) + "";
//		System.out.println(sql);
		return sql;
	}

	private <X> X hydrateEager(T rootObject, RelationDescriber relationDescriber, OrmResultSet row, int i) {
		X otherModel = (X) genericFactory.get(relationDescriber.getToClass().clazz);
		TypeDescriber<?> eagerRelationTypeDescriber = TypeDescriberImpl.getTypeDescriber(relationDescriber.getToClass().clazz);

		mapping(otherModel).hydrate(new ValqueriesHydrator("eager" + (i) + "_", row, sqlNameFormatter,eagerRelationTypeDescriber));
		if (((Property.PropertyValueList<?>) mapping(otherModel)._getKey().getValues()).get(0).getValue() == null) {
			return null;
		}
		return otherModel;
	}

	@Override
	public void set(IStatement statement) {
		elements.forEach(e -> e.set(statement));
	}

	@Override
	public void close() throws Exception {
		transactionContext.close();
	}

	private Mapping mapping(Object obj) {
		if (obj instanceof Mapping) {
			return (Mapping) obj;
		}
		throw new RuntimeException("Tried mapping an unmapped object: " + obj.getClass().getName());
	}

	@Override
	public CrudRepository.CrudUpdateResult update(Consumer<ValqueriesUpdate<T>> updater) {
		if (forcedEmpty) {
			return () -> 0;
		}
		List<Property.PropertyValue> newPropertyValues = this.getPropertyValuesFromUpdater(updater);
		String updateStatement = this.buildUpdateSql(newPropertyValues);

		int affectedRows = transactionContext.update(updateStatement, uStmt -> {
			newPropertyValues.forEach(v -> uStmt.set(v.getProperty().getToken().snake_case(), v.getValue()));
			set(uStmt);
		}).getAffectedRows();
		transactionContext.close();
		return () -> affectedRows;
	}

	private List<Property.PropertyValue> getPropertyValuesFromUpdater(Consumer<ValqueriesUpdate<T>> updater) {
		ValqueriesUpdateImpl<T> updImpl = new ValqueriesUpdateImpl(instance, queryWrapper);
		updater.accept(updImpl);
		return updImpl.getPropertyValues();
	}

	private String buildUpdateSql(List<Property.PropertyValue> newPropertyValues) {
		return dialect.generateUpdateStatement(typeDescriber, elements, newPropertyValues);

	}

	/*
		private static class RelationSubQueryElement implements Element {
			private final ValqueriesQueryImpl<?> otherQuery;
			private SqlNameFormatter sqlNameFormatter;
			private final RelationDescriber relation;
			private final String tableAlias;
			private String parentTableAlias;
			private int subQueryNum;
			private SqlDialect dialect;

			public RelationSubQueryElement(String parentTableAlias, String tableAlias, int subQueryNum, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
				this.parentTableAlias = parentTableAlias;
				this.tableAlias = tableAlias;
				this.subQueryNum = subQueryNum;
				this.relation = relation;
				this.otherQuery = otherQuery;
				this.sqlNameFormatter = sqlNameFormatter;
				this.dialect = dialect;
			}

			public String queryString() {
				return parentTableAlias + "." + dialect.column(relation.getFromKeys().get(0).getToken()) + " IN (" + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + dialect.column(p.getToken())).toArray(String[]::new)) + ")";
			}

			@Override
			public void set(IStatement statement) {
				otherQuery.set(statement);
			}
		}

		private static class RelationJoinElement implements Element {
			private final ValqueriesQueryImpl<?> otherQuery;
			private SqlNameFormatter sqlNameFormatter;
			private final RelationDescriber relation;
			private final String tableAlias;
			private String parentTableAlias;
			private int subQueryNum;
			private SqlDialect dialect;

			public RelationJoinElement(String parentTableAlias, String tableAlias, int subQueryNum, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter, SqlDialect dialect) {
				this.parentTableAlias = parentTableAlias;
				this.tableAlias = tableAlias;
				this.subQueryNum = subQueryNum;
				this.relation = relation;
				this.otherQuery = otherQuery;
				this.sqlNameFormatter = sqlNameFormatter;
				this.dialect = dialect;
			}

			public String queryString() {
				return "";
			}

			@Override
			public String fromString() {
				return " JOIN "+dialect.escapeColumnOrTable(otherQuery.getTableName(relation.getToClass()))+"."+tableAlias + " ON " + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).toArray(String[]::new)) + "";
			}

			@Override
			public void set(IStatement statement) {
				otherQuery.set(statement);
			}
		}
	*/
	private void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public void setEmpty() {
		forcedEmpty = true;
	}

	private void appendJoin(RelationDescriber relation, StringBuilder joinSql, StringBuilder selectSql,
							String fromTableAlias, int counter) {
		String eagerTable = getTableName(relation.getToClass());
		String eagerAlias = "eager" + counter;
		appendJoin(relation, joinSql, selectSql, fromTableAlias, eagerTable, eagerAlias);
	}

	private void appendJoin(RelationDescriber relation, StringBuilder joinSql, StringBuilder selectSql,
							String fromTableAlias, String toTable, String toTableAlias) {
		joinSql.append(" LEFT JOIN " + toTable + " " + toTableAlias + " ON ");
		List<KeySet.Field> from = relation.getFromKeys().stream().collect(Collectors.toList());
		List<KeySet.Field> to = relation.getToKeys().stream().collect(Collectors.toList());
		List<String> onParams = new ArrayList<>();
		for (int x = 0; x < from.size(); x++) {
			onParams.add(fromTableAlias + "." + dialect.column(from.get(x).getProperty()) + " = " + toTableAlias + "." + dialect.column(to.get(x).getProperty()));
		}
		joinSql.append(String.join(" AND ", onParams));
		TypeDescriber<?> eagerRelationTypeDescriber = TypeDescriberImpl.getTypeDescriber(relation.getToClass().clazz);
		selectSql.append(", ").append(eagerRelationTypeDescriber.fields().stream().map(property -> toTableAlias + "." + dialect.column(property).toSql() + " " + toTableAlias + "_" + dialect.column(property).unescaped()).collect(Collectors.joining(", ")));
	}
}