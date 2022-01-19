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
			TypeDescriber<?> o = TypeDescriberImpl.getTypeDescriber(relationDescriber.getToClass().clazz);
			fromObjRelationDescriber = o.relations().get(relationDescriber.getFromClass().clazz);
			RelationDescriber intermediateRelation = fromObjRelationDescriber.get().getVia().stream().filter(via -> !via.getToClass().clazz.equals(relationDescriber.getRelationAnnotation().via())).findFirst().get().inverse();
			fromKeys = intermediateRelation.getFromKeys();
			toKeys = intermediateRelation.getToKeys();
		} else {
			fromKeys = relationDescriber.getFromKeys();
			toKeys = relationDescriber.getToKeys();
		}
		PropertiesColumnizer columnizer = new PropertiesColumnizer(fromKeys.toProperties());
		mappingHelper.columnize(from, columnizer);
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
