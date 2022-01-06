package com.valqueries.automapper;

import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.valqueries.IStatement;
import com.valqueries.ITransactionContext;
import com.valqueries.OrmResultSet;
import com.valqueries.Setter;
import com.valqueries.UpdateResult;
import io.ran.Clazz;
import io.ran.CompoundKey;
import io.ran.CrudRepository;
import io.ran.GenericFactory;
import io.ran.KeySet;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.Property;
import io.ran.RelationDescriber;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import io.ran.token.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private Collection<RelationDescriber> eagers = new ArrayList<>();
	private String tableAlias = "main";
	private GenericFactory genericFactory;
	private Integer limit = null;
	private int offset = 0;
	private SqlNameFormatter sqlNameFormatter;
	private MappingHelper mappingHelper;

	public ValqueriesQueryImpl(ITransactionContext transactionContext, Class<T> modelType, GenericFactory genericFactory, SqlNameFormatter sqlNameFormatter, MappingHelper mappingHelper) {
		super(modelType, genericFactory);
		this.transactionContext = transactionContext;
		this.modelType = modelType;
		this.genericFactory = genericFactory;
		this.sqlNameFormatter = sqlNameFormatter;
		this.mappingHelper = mappingHelper;
	}

	@Override
	public ValqueriesQuery<T> eq(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "=", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> in(Property.PropertyValueList propertyValues) {
		elements.add(new ListElement(this, propertyValues, "IN", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> like(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "like",++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	ValqueriesQuery<T> freetext(Property.PropertyValue<?> propertyValue) {
		elements.add(new FreeTextElement(this, propertyValue, ++fieldNum, sqlNameFormatter));
		return this;
	}


	@Override
	public ValqueriesQuery<T> gt(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, ">", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> gte(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, ">=", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> lt(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "<", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> lte(Property.PropertyValue<?> propertyValue) {
		elements.add(new SimpleElement(this, propertyValue, "<=", ++fieldNum, sqlNameFormatter));
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <X, Z extends CrudRepository.InlineQuery<X, Z>> ValqueriesQuery<T> subQuery(RelationDescriber relation, Consumer<Z> consumer) {
		if (relation. getToKeys().size() > 1) {
			return join(relation, consumer);
		}
		if (!relation.getVia().isEmpty()) {
			return subQuery(relation.getVia().get(0), q -> {
				((ValqueriesQuery<X>)q).subQuery(relation.getVia().get(1), (Consumer) consumer);
			});
		}
		ValqueriesQueryImpl otherQuery = new ValqueriesQueryImpl(transactionContext, relation.getToClass().clazz, genericFactory, sqlNameFormatter, mappingHelper);
		otherQuery.tableAlias = "sub"+(++subQueryNum);
		consumer.accept((Z) otherQuery);
		elements.add(new RelationSubQueryElement(tableAlias, otherQuery.tableAlias, ++subQueryNum, relation, otherQuery, sqlNameFormatter));
		return this;
	}

	private <X, Z extends CrudRepository.InlineQuery<X, Z>> ValqueriesQuery<T> join(RelationDescriber relation, Consumer<Z> consumer) {
		if (!relation.getVia().isEmpty()) {
			return join(relation.getVia().get(0), q -> {
				((ValqueriesQueryImpl<X>)q).join(relation.getVia().get(1), (Consumer) consumer);
			});
		}
		ValqueriesQueryImpl otherQuery = new ValqueriesQueryImpl(transactionContext, relation.getToClass().clazz, genericFactory, sqlNameFormatter, mappingHelper);
		otherQuery.tableAlias = "sub"+(++subQueryNum);
		consumer.accept((Z) otherQuery);
		elements.add(new RelationSubQueryElement(tableAlias, otherQuery.tableAlias, ++subQueryNum, relation, otherQuery, sqlNameFormatter));
		return this;
	}

	@Override
	public ValqueriesQuery<T> isNull(Property<?> property) {
		elements.add(new SimpleElement(this, property.value(null),"IS NULL", ++fieldNum, sqlNameFormatter));
		return this;
	}

	public ValqueriesQuery<T> isNotNull(Property<?> property) {
		elements.add(new SimpleElement(this, property.value(null),"IS NOT NULL", ++fieldNum, sqlNameFormatter));
		return this;
	}



	private String buildSelectSql(String tableAlias, String... columns) {
		T t = genericFactory.get(modelType);
		String columnsSql;
		if (columns.length == 0) {
			ValqueriesColumnBuilder columnBuilder = new ValqueriesColumnBuilder(tableAlias, sqlNameFormatter);
			mappingHelper.hydrate(t, columnBuilder);
			columnsSql = columnBuilder.getSql();
		} else {
			columnsSql = Arrays.stream(columns).collect(Collectors.joining(", "));
		}

		StringBuilder eagerSelect = new StringBuilder();
		StringBuilder eagerJoin = new StringBuilder();
		int i = 0;
		for (RelationDescriber relation : eagers) {
			String eagerTable = getTableName(relation.getToClass());
			String eagerAlias = "eager"+(++i);
			eagerJoin.append(" LEFT JOIN `"+eagerTable+"` "+eagerAlias+" ON ");
			List<KeySet.Field> from = relation.getFromKeys().stream().collect(Collectors.toList());
			List<KeySet.Field> to = relation.getToKeys().stream().collect(Collectors.toList());
			List<String> onParams = new ArrayList<>();
			for(int x=0;x<from.size();x++) {
				onParams.add(tableAlias+"."+sqlNameFormatter.column(from.get(x).getToken())+" = "+eagerAlias+"."+sqlNameFormatter.column(to.get(x).getToken()));
			}

			eagerJoin.append(String.join(" AND ", onParams));
			TypeDescriber<?> eagerRelationTypeDescriber = TypeDescriberImpl.getTypeDescriber(relation.getToClass().clazz);

			eagerSelect.append(", "+eagerRelationTypeDescriber.fields().stream().map(property -> eagerAlias+"."+sqlNameFormatter.column(property.getToken())+" "+eagerAlias+"_"+sqlNameFormatter.column(property.getToken())).collect(Collectors.joining(", ")));

		}
		String sql = "SELECT " + columnsSql + eagerSelect.toString() + " FROM `" + getTableName(Clazz.of(typeDescriber.clazz())) + "` "+tableAlias+" "+eagerJoin.toString()+" "+elements.stream().map(Element::fromString).filter(Objects::nonNull).collect(Collectors.joining(", "));
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		if (!sortElements.isEmpty()) {
			sql += " ORDER BY "+sortElements.stream().map(Element::queryString).collect(Collectors.joining(", "));
		}
		if (limit !=  null) {
			sql += " LIMIT "+offset+","+limit;
		}
//		System.out.println(sql);
		return sql;
	}

	private String getTableName(Clazz<?> toClass) {
		return sqlNameFormatter.table(Token.CamelCase(toClass.getSimpleName()));
	}

	public ValqueriesQuery<T> withEager(RelationDescriber relation) {
		eagers.add(relation);
		return this;
	}

	@Override
	public <X extends Comparable<X>> ValqueriesQuery<T> sortAscending(Property<X> property) {
		sortElements.add(new SortElement(this, property, true, sqlNameFormatter));
		return this;
	}

	@Override
	public <X extends Comparable<X>> ValqueriesQuery<T> sortDescending(Property<X> property) {
		sortElements.add(new SortElement(this, property, false, sqlNameFormatter));
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
			Map<CompoundKey, T> alreadyLoaded = new LinkedHashMap<>();
			Map<Token, Map<CompoundKey, List>> eagerModels = new HashMap<>();
			transactionContext.query(buildSelectSql("main"), this, row -> {
				T t2 = genericFactory.get(modelType);
				mappingHelper.hydrate(t2, new ValqueriesHydrator("main_", row, sqlNameFormatter));
				CompoundKey key = mappingHelper.getKey(t2);
				if (alreadyLoaded.containsKey(key)) {
					t2 = alreadyLoaded.get(key);
				} else {
					alreadyLoaded.put(key, t2);
				}
				int i=0;
				for (RelationDescriber relationDescriber : eagers) {
					Object hydrated = hydrateEager(t2, relationDescriber, row, ++i);
					if (hydrated != null) {
						eagerModels
								.computeIfAbsent(relationDescriber.getField(), (k) -> new HashMap<>())
								.computeIfAbsent(key, (k) -> new ArrayList())
								.add(hydrated);
					}
				}
				return t2;
			});
			for (RelationDescriber relationDescriber : eagers) {
				Map<CompoundKey, List> eagerModel = eagerModels.get(relationDescriber.getField());
				if (eagerModel != null) {
					eagerModel.entrySet().forEach(entry -> {
						if (relationDescriber.isCollectionRelation()) {
							((Mapping)alreadyLoaded.get(entry.getKey()))._setRelation(relationDescriber, entry.getValue());
						} else {
							mapping(alreadyLoaded.get(entry.getKey()))._setRelation(relationDescriber, entry.getValue().get(0));
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
				CapturingHydrator hydrator = new CapturingHydrator(new ValqueriesHydrator(row, sqlNameFormatter));
				T t = genericFactory.get(modelType);
				mappingHelper.hydrate(t, hydrator);
				return new GroupNumericResultImpl.Grouping(hydrator.getValues(), row.getLong("the_count"));
			}).stream().collect(Collectors.toMap(g -> g, g -> (long)g.getValue()));
			return new GroupNumericResultImpl(res);
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
	public CrudRepository.CrudUpdateResult delete() {
		try {
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
		String sql = "DELETE "+tableAlias+" FROM `" + getTableName(Clazz.of(typeDescriber.clazz())) + "` "+tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		if (limit !=  null) {
			sql += " LIMIT "+offset+","+limit;
		}
//		System.out.println(sql);
		return sql;
	}

	private String buildCountSql() {
		String sql = "SELECT COUNT(1) as the_count FROM `" + getTableName(Clazz.of(typeDescriber.clazz())) + "` "+tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
//		System.out.println(sql);
		return sql;
	}


	private String buildGroupAggregateSql(Property resultProperty, String aggregateMethod) {
		String sql = "SELECT "+aggregateMethod+"("+this.sqlNameFormatter.column(resultProperty.getToken())+") as the_count , "+groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", "))+" FROM `" + getTableName(Clazz.of(typeDescriber.clazz())) + "` "+tableAlias;
		if (!elements.isEmpty()) {
			sql += " WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += " GROUP BY "+groupByProperties.stream().map(p -> sqlNameFormatter.column(p.getToken())).collect(Collectors.joining(", "))+"";
//		System.out.println(sql);
		return sql;
	}

	private <X> X hydrateEager(T rootObject, RelationDescriber relationDescriber, OrmResultSet row, int i) {
		X otherModel = (X)genericFactory.get(relationDescriber.getToClass().clazz);
		mapping(otherModel).hydrate(new ValqueriesHydrator("eager"+(i)+"_", row, sqlNameFormatter));
		if (((Property.PropertyValueList<?>)mapping(otherModel)._getKey().getValues()).get(0).getValue() == null) {
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
		throw new RuntimeException("Tried mapping an unmapped object: "+obj.getClass().getName());
	}

	@Override
	public CrudRepository.CrudUpdateResult update(Consumer<ValqueriesUpdate<T>> updater) {
		List<Property.PropertyValue>  newPropertyValues = this.getPropertyValuesFromUpdater(updater);
		String updateStatement = this.buildUpdateSql(newPropertyValues);

		int affectedRows = transactionContext.update(updateStatement, uStmt -> {
			newPropertyValues.forEach(v -> uStmt.set(v.getProperty().getToken().snake_case(), v.getValue()));
			set(uStmt);
		}).getAffectedRows();
		return () -> affectedRows;
	}

	private List<Property.PropertyValue> getPropertyValuesFromUpdater(Consumer<ValqueriesUpdate<T>> updater) {
		ValqueriesUpdateImpl<T> updImpl = new ValqueriesUpdateImpl(instance, queryWrapper);
		updater.accept(updImpl);
		return updImpl.getPropertyValues();
	}

	private String buildUpdateSql(List<Property.PropertyValue> newPropertyValues) {
		StringBuilder updateStatement = new StringBuilder();
		updateStatement.append("UPDATE `" + getTableName(Clazz.of(typeDescriber.clazz())) + "` main SET ");

		String columnsToUpdate = newPropertyValues.stream()
				.map(pv -> "main." + sqlNameFormatter.column(pv.getProperty().getToken()) + " = :" + pv.getProperty().getToken().snake_case())
				.collect(Collectors.joining(", "));
		updateStatement.append(columnsToUpdate);

		if (!elements.isEmpty()) {
			updateStatement.append(" WHERE " + elements.stream().map(Element::queryString).collect(Collectors.joining(" AND ")));
		}

		return updateStatement.toString();
	}

	private interface Element extends Setter {
		String queryString();

		default String fromString() {
			return null;
		}
	}

	private static class RelationSubQueryElement implements Element {
		private final ValqueriesQueryImpl<?> otherQuery;
		private SqlNameFormatter sqlNameFormatter;
		private final RelationDescriber relation;
		private final String tableAlias;
		private String parentTableAlias;
		private int subQueryNum;

		public RelationSubQueryElement(String parentTableAlias, String tableAlias, int subQueryNum, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter) {
			this.parentTableAlias = parentTableAlias;
			this.tableAlias = tableAlias;
			this.subQueryNum = subQueryNum;
			this.relation = relation;
			this.otherQuery = otherQuery;
			this.sqlNameFormatter = sqlNameFormatter;
		}

		public String queryString() {
			return parentTableAlias + ".`" + sqlNameFormatter.column(relation.getFromKeys().get(0).getToken()) + "` IN (" + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).toArray(String[]::new)) + ")";
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

		public RelationJoinElement(String parentTableAlias, String tableAlias, int subQueryNum, RelationDescriber relation, ValqueriesQueryImpl<?> otherQuery, SqlNameFormatter sqlNameFormatter) {
			this.parentTableAlias = parentTableAlias;
			this.tableAlias = tableAlias;
			this.subQueryNum = subQueryNum;
			this.relation = relation;
			this.otherQuery = otherQuery;
			this.sqlNameFormatter = sqlNameFormatter;
		}

		public String queryString() {
			return "";
		}

		@Override
		public String fromString() {
			return " JOIN "+otherQuery.getTableName(relation.getToClass())+"."+tableAlias + " ON " + otherQuery.buildSelectSql(tableAlias, relation.getToKeys().stream().map(p -> tableAlias + "." + sqlNameFormatter.column(p.getToken())).toArray(String[]::new)) + "";
		}

		@Override
		public void set(IStatement statement) {
			otherQuery.set(statement);
		}
	}

	private void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	private static class SimpleElement implements Element {
		private final ValqueriesQueryImpl<?> query;
		private final Property.PropertyValue<?> propertyValue;
		private final String operator;
		private SqlNameFormatter sqlNameFormatter;
		private final String field;

		public SimpleElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, String operator, int fieldNum, SqlNameFormatter sqlNameFormatter) {
			this.query = query;
			this.propertyValue = propertyValue;
			this.operator = operator;
			this.sqlNameFormatter = sqlNameFormatter;
			this.field = propertyValue.getProperty().getToken().snake_case()+fieldNum;
		}

		public String queryString() {
			return query.tableAlias+".`"+sqlNameFormatter.column(propertyValue.getProperty().getToken())+"` "+operator+" (:"+field+")";
		}

		@Override
		public void set(IStatement statement) {
			statement.set(field, propertyValue.getValue());
		}
	}

	private static class FreeTextElement implements Element {

		private final ValqueriesQueryImpl<?> query;
		private final Property.PropertyValue<?> propertyValue;
		private SqlNameFormatter sqlNameFormatter;
		private final String field;

		public FreeTextElement(ValqueriesQueryImpl<?> query, Property.PropertyValue<?> propertyValue, int fieldNum, SqlNameFormatter sqlNameFormatter) {
			this.query = query;
			this.propertyValue = propertyValue;
			this.sqlNameFormatter = sqlNameFormatter;
			this.field = propertyValue.getProperty().getToken().snake_case()+fieldNum;
		}

		@Override
		public String queryString() {
			return "MATCH("+query.tableAlias+".`"+sqlNameFormatter.column(propertyValue.getProperty().getToken())+"`) AGAINST(:"+field+")";
		}

		@Override
		public void set(IStatement statement) {
			statement.set(field, propertyValue.getValue());
		}

	}

	private class ListElement implements Element {
		private final ValqueriesQueryImpl<T> query;
		private final List<Property.PropertyValue> values;
		private final String operator;
		private final int fieldNum;
		private SqlNameFormatter sqlNameFormatter;
		private final String field;

		public ListElement(ValqueriesQueryImpl<T> query, List<Property.PropertyValue> values, String operator, int fieldNum, SqlNameFormatter sqlNameFormatter) {
			this.query = query;
			this.values = values;
			this.operator = operator;
			this.fieldNum = fieldNum;
			this.sqlNameFormatter = sqlNameFormatter;
			this.field = values.get(0).getProperty().getToken().snake_case()+fieldNum;

		}
		public String queryString() {
			return query.tableAlias+".`"+sqlNameFormatter.column(values.get(0).getProperty().getToken())+"` "+operator+" (:"+field+")";
		}

		@Override
		public void set(IStatement statement) {
			statement.set(field, values.stream().map(Property.PropertyValue::getValue).collect(Collectors.toList()));
		}
	}

	private class SortElement implements Element {
		private final ValqueriesQueryImpl<T> query;
		private final Property<?> property;
		private boolean ascending;
		private SqlNameFormatter sqlNameFormatter;

		public SortElement(ValqueriesQueryImpl<T> query, Property<?> property, boolean ascending, SqlNameFormatter sqlNameFormatter) {
			this.query = query;
			this.property = property;
			this.ascending = ascending;
			this.sqlNameFormatter = sqlNameFormatter;
		}

		@Override
		public String queryString() {
			return query.tableAlias+".`"+sqlNameFormatter.column(property.getToken())+"`"+(ascending ? " ASC" : " DESC");
		}

		@Override
		public void set(IStatement statement) {

		}
	}
}