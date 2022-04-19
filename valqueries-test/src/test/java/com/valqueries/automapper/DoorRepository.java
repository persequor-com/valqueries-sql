package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.GenericFactory;

import javax.inject.Inject;
import javax.xml.crypto.Data;
import java.util.UUID;

public class DoorRepository extends ValqueriesCrudRepositoryImpl<Door, UUID> {

	private final Database database;
	@Inject
	public DoorRepository(ValqueriesRepositoryFactory factory, Database database) {
		super(factory, Door.class, UUID.class);
		this.database = database;
	}


	public UUID getDoorByMaterial(String material){
		return database.obtainInTransaction(tx->{
			return tx.query("select * from door where my_material=:material",statement -> {statement.set("material",material);},row -> row.getUUID("id")).get(0);
		});
	}
}
