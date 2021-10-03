package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.ITransactionContext;
import io.ran.DbResolver;
import io.ran.GenericFactory;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.PropertiesColumnizer;
import io.ran.Property;
import io.ran.RelationDescriber;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ValqueriesResolver implements DbResolver<Valqueries> {
	private Database database;
	private GenericFactory genericFactory;
	private MappingHelper mappingHelper;
	private SqlNameFormatter sqlNameFormatter;

	@Inject
	ValqueriesResolver(Database database, GenericFactory genericFactory, MappingHelper mappingHelper, SqlNameFormatter sqlNameFormatter) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
		this.sqlNameFormatter = sqlNameFormatter;
	}

	private <FROM, TO> ValqueriesQuery<TO> getQuery(ITransactionContext t, RelationDescriber relationDescriber, FROM from) {
		if (relationDescriber.getVia() != null) {
			ValqueriesQueryImpl query = new ValqueriesQueryImpl(t, relationDescriber.getToClass().clazz, genericFactory, sqlNameFormatter);

			PropertiesColumnizer columnizer = new PropertiesColumnizer(relationDescriber.getFromKeys().toProperties());
			((Mapping) from).columnize(columnizer);
			Iterator<Property.PropertyValue> values = columnizer.getValues().iterator();
			for (Property property : relationDescriber.getToKeys().toProperties()) {
				query.eq(property.value(values.next().getValue()));
			}
			return query;
		} else {
			ValqueriesQueryImpl query = new ValqueriesQueryImpl(t, relationDescriber.getToClass().clazz, genericFactory, sqlNameFormatter);
			PropertiesColumnizer columnizer = new PropertiesColumnizer(relationDescriber.getFromKeys().toProperties());
			((Mapping) from).columnize(columnizer);
			Iterator<Property.PropertyValue> values = columnizer.getValues().iterator();
			for (Property property : relationDescriber.getToKeys().toProperties()) {
				query.eq(property.value(values.next().getValue()));
			}
			return query;
		}
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
