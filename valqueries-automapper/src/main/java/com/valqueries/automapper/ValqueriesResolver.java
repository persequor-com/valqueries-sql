package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.ITransactionContext;
import io.ran.*;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValqueriesResolver implements DbResolver<Valqueries> {
	private final Database database;
	private final GenericFactory genericFactory;
	private final MappingHelper mappingHelper;
	private final SqlNameFormatter sqlNameFormatter;
	private final SqlDialect dialect;

	@Inject
	public ValqueriesResolver(Database database, GenericFactory genericFactory, MappingHelper mappingHelper, SqlNameFormatter sqlNameFormatter, DialectFactory dialectFactory) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialectFactory.get(database);
	}

	private <FROM, TO> ValqueriesQuery<TO> getQuery(ITransactionContext t, RelationDescriber relationDescriber, FROM from) {
		ValqueriesQueryImpl<TO> query = new ValqueriesQueryImpl<>(t, (Class<TO>) relationDescriber.getToClass().clazz, genericFactory, sqlNameFormatter,  mappingHelper, dialect);
		KeySet fromKeys;
		KeySet toKeys;
		Optional<RelationDescriber> fromObjRelationDescriber = Optional.empty();
		if (relationDescriber.getVia() != null && !relationDescriber.getVia().isEmpty()) {
			RelationDescriber fromThis = relationDescriber.getVia().get(0);
			fromObjRelationDescriber = Optional.of(relationDescriber.inverse());
			fromKeys = fromThis.getFromKeys();
			toKeys = fromThis.getToKeys();
		} else {
			fromKeys = relationDescriber.getFromKeys();
			toKeys = relationDescriber.getToKeys();
		}
		PropertiesColumnizer columnizer = new PropertiesColumnizer(fromKeys.toProperties());
		mappingHelper.columnize(from, columnizer);
		if (columnizer.getValues().size() != toKeys.size()) {
			throw new RuntimeException("Mismatch in key and property count. Keys has "+toKeys.size()+" elements and matching properties has "+columnizer.getValues().size()+" keys");
		}
		Iterator<Property.PropertyValue> values = columnizer.getValues().iterator();
		for (Property property : toKeys.toProperties()) {
			if (fromObjRelationDescriber.isPresent()) {
				query.subQueryList(fromObjRelationDescriber.get(), q -> q.eq(property.value(values.next().getValue())));
			} else {
				query.eq(property.value(values.next().getValue()));
			}
		}
		return query;
	}

	@Override
	public <FROM, TO> TO get(RelationDescriber relationDescriber, FROM from) {
		return database.obtainInTransaction(t -> {
			ValqueriesQuery res = getQuery(t, relationDescriber, from);
			return (TO)res.execute().findFirst().orElse(null);
		});

	}

	@Override
	public <FROM, TO> Collection<TO> getCollection(RelationDescriber relationDescriber, FROM from) {
		return database.obtainInTransaction(t -> {
			ValqueriesQuery res = getQuery(t, relationDescriber, from);
			return (Collection<TO>)res.execute().collect(Collectors.toList());
		});
	}
}
