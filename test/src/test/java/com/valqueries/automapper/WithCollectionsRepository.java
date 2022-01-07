package com.valqueries.automapper;

import javax.inject.Inject;
import java.util.List;

public class WithCollectionsRepository extends ValqueriesCrudRepositoryImpl<WithCollections, List> {
	@Inject
	public WithCollectionsRepository(ValqueriesRepositoryFactory factory) {
		super(factory, WithCollections.class, List.class);
	}

}
