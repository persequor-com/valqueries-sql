package com.valqueries.automapper;

import io.ran.DbResolver;
import io.ran.GenericFactory;
import io.ran.MappingHelper;
import io.ran.Property;
import io.ran.RelationDescriber;
import io.ran.TestDoubleDb;
import io.ran.TypeDescriberImpl;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValqueriesTestDoubleResolver implements DbResolver<Valqueries> {
	private GenericFactory genericFactory;
	private MappingHelper mappingHelper;
	private TestDoubleDb store;

	@Inject
	public ValqueriesTestDoubleResolver(GenericFactory genericFactory, MappingHelper mappingHelper, TestDoubleDb store) {
		this.genericFactory = genericFactory;
		this.mappingHelper = mappingHelper;
		this.store = store;
	}

	private <FROM, TO> Stream<TO> getStream(RelationDescriber relationDescriber, FROM from) {
		TestDoubleQuery<TO> q = new TestDoubleQuery((Class) relationDescriber.getToClass().clazz, genericFactory, mappingHelper, store);
		if (!relationDescriber.getVia().isEmpty()) {
			q.subQueryList(relationDescriber.inverse(), sq -> {
				for (int i = 0; i < relationDescriber.getVia().get(0).getFromKeys().size(); i++) {
					Property fk = relationDescriber.getVia().get(0).getFromKeys().get(i).getProperty();
					Property tk = relationDescriber.getVia().get(0).getToKeys().get(i).getProperty();
					sq.eq(tk.value(mappingHelper.getValue(from, fk)));
				}
			});
		} else {
			for (int i = 0; i < relationDescriber.getFromKeys().size(); i++) {
				Property fk = relationDescriber.getFromKeys().get(i).getProperty();
				Property tk = relationDescriber.getToKeys().get(i).getProperty();
				q.eq(tk.value(mappingHelper.getValue(from, fk)));
			}
		}
		return q.execute();
	}

	@Override
	public <FROM, TO> TO get(RelationDescriber relationDescriber, FROM from) {
		return (TO) getStream(relationDescriber, from).findFirst().orElse(null);
	}

	@Override
	public <FROM, TO> Collection<TO> getCollection(RelationDescriber relationDescriber, FROM from) {
		return (Collection<TO>) getStream(relationDescriber, from).collect(Collectors.toList());
	}
}
