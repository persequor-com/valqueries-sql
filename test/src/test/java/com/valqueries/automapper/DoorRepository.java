package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.GenericFactory;

import javax.inject.Inject;
import java.util.UUID;

public class DoorRepository extends ValqueriesCrudRepositoryImpl<Door, UUID> {
	@Inject
	public DoorRepository(ValqueriesRepositoryFactory factory) {
		super(factory, Door.class, UUID.class);
	}
}
