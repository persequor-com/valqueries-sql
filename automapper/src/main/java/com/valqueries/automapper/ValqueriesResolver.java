package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.DbResolver;
import io.ran.GenericFactory;
import io.ran.Mapping;
import io.ran.MappingHelper;
import io.ran.PropertiesColumnizer;
import io.ran.RelationDescriber;
import io.ran.Resolver;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import io.ran.token.Token;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ValqueriesResolver implements DbResolver<Valqueries> {
	private Database database;
	private GenericFactory genericFactory;
	private MappingHelper mappingHelper;

	@Inject
	ValqueriesResolver(Database database, GenericFactory genericFactory, MappingHelper mappingHelper) {
		this.database = database;
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
	}


	@Override
	public <FROM, TO> TO get(RelationDescriber relationDescriber, FROM from) {
		return (TO)database.obtainInTransaction(t -> {
			return new ValqueriesQueryImpl(t, relationDescriber.getToClass().clazz, genericFactory)
					.subQuery(relationDescriber.inverse(), (Consumer<ValqueriesQuery>)  q -> {
						PropertiesColumnizer columnizer = new PropertiesColumnizer(relationDescriber.getToKeys().toProperties());
						((Mapping)from).columnize(columnizer);

						columnizer.getValues().forEach(q::eq);


					}).execute().findFirst().orElse(null);
		});
	}

	@Override
	public <FROM, TO> Collection<TO> getCollection(RelationDescriber relationDescriber, FROM from) {
		return database.obtainInTransaction(t -> {
			TypeDescriber<FROM> fromTypeDescriber = (TypeDescriber<FROM>)TypeDescriberImpl.getTypeDescriber(relationDescriber.getFromClass().clazz);

			String where = relationDescriber.getToKeys().stream().map(k -> k.getToken().snake_case()+" = :"+k.getToken().snake_case()).collect(Collectors.joining(" AND "));

			return (Collection<TO>) t.query("select * from "+ Token.CamelCase(relationDescriber.getToClass().getSimpleName()).snake_case()+" WHERE "+where, new PropertyGetter<FROM>(relationDescriber.getFromKeys(), relationDescriber.getToKeys(),from, fromTypeDescriber, mappingHelper), row -> {
				TO to = (TO) genericFactory.get(relationDescriber.getToClass().clazz);

				((Mapping)to).hydrate(to,new ValqueriesHydrator(row));
				return to;
			});
		});
	}
}
