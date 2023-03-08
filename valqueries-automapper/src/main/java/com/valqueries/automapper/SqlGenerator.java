package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.automapper.schema.ValqueriesSchemaBuilder;
import com.valqueries.automapper.schema.ValqueriesSchemaExecutor;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.TypeDescriber;
import io.ran.token.ColumnToken;
import io.ran.token.TableToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Optional;

public class SqlGenerator {
	private static Logger logger = LoggerFactory.getLogger(SqlGenerator.class);
	private final SqlNameFormatter sqlNameFormatter;
	private final SqlDialect dialect;
	private final SqlDescriber sqlDescriber;
	private final Provider<ValqueriesSchemaBuilder> schemaBuilderProvider;

	@Inject
	public SqlGenerator(SqlNameFormatter sqlNameFormatter, DialectFactory dialectFactory, Database database, SqlDescriber sqlDescriber, Provider<ValqueriesSchemaBuilder> schemaBuilderProvider) {
		this.sqlNameFormatter = sqlNameFormatter;
		this.schemaBuilderProvider = schemaBuilderProvider;
		this.dialect = dialectFactory.get(database);
		this.sqlDescriber = sqlDescriber;
	}

	public TableToken getTableName(TypeDescriber<?> typeDescriber) {
		return dialect.getTableName(Clazz.of(typeDescriber.clazz()));
	}

	public String generateOrModifyTable(Database database, TypeDescriber<?> typeDescriber) {
//		logger.warn("generateOrModifyTable is a work in progress and is not considered stable");
		TableToken tablename = getTableName(typeDescriber);
		SqlDescriber.DbTable table = sqlDescriber.describe(tablename, database);

		if (table == null) {
			return generateCreateTable(typeDescriber);
		} else {
			ValqueriesSchemaBuilder schemaBuilder = schemaBuilderProvider.get();
			schemaBuilder.modifyTable(tablename, t -> {
				typeDescriber.fields().forEach(property -> {
					ColumnToken columnName = dialect.column(property);
					String sqlType = dialect.getSqlType(property);
					if (!table.getColumns().containsKey(columnName.name())) {
						t.addColumn(property);
					} else if (!table.getColumns().get(columnName.name()).matches(property, sqlType)) {
//						checkTypeCompatibiliy(property.getType(), table.getColumns().get(columnName.name()).getType());
						t.modifyColumn(property);
					}

				});

				SqlDescriber.DbIndex index = table.getIndex().get("PRIMARY");
				if (!index.matches(toDbIndex(typeDescriber.primaryKeys()))) {
					throw new RuntimeException("Model primary key has changed for "+typeDescriber.clazz().getName()+". Please create a manual migration or this change.");
				}

				typeDescriber.indexes().forEach(key -> {
					SqlDescriber.DbIndex keyIndex = toDbIndex(key);
					Optional<SqlDescriber.DbIndex> idx = table.getIndex().values().stream().filter(keyIndex::matches).findFirst();
					if (!idx.isPresent()) {
						t.addIndex(key);
					}
				});
			});
			schemaBuilder.build();
			return schemaBuilder.getGeneratedSql();
		}

	}

	private void checkTypeCompatibiliy(Clazz sqlType, String type) throws InvalidTypeConversionException {
		if (!dialect.allowsConversion(sqlType, type)) {
			throw new InvalidTypeConversionException(sqlType.clazz.getName(), type);
		}
	}

	private SqlDescriber.DbIndex toDbIndex(KeySet index) {
		SqlDescriber.DbIndex dbIndex = new SqlDescriber.DbIndex();
		dbIndex.setUnique(index.isPrimary());
		dbIndex.setColumns(new ArrayList<>());
		index.forEach(f -> {
			dbIndex.getColumns().add(sqlNameFormatter.column(f.getToken()));
		});
		return dbIndex;
	}

	public String generateCreateTable(TypeDescriber<?> typeDescriber) {
		ValqueriesSchemaBuilder schemaBuilder = schemaBuilderProvider.get();

		schemaBuilder.addTable(dialect.getTableName(Clazz.of(typeDescriber.clazz())), table -> {
			typeDescriber.fields().forEach(table::addColumn);
			table.addPrimaryKey(typeDescriber.primaryKeys());
			typeDescriber.indexes().forEach(table::addIndex);
		});
		schemaBuilder.build();
		return schemaBuilder.getGeneratedSql();
	}

	public String generateCreateTable(TypeDescriber<?> typeDescriber, Database targetDatabase) {
		// TODO: Check if we can do this via an injector instead of instantiating an schema executor each time.
		//  Alternatively, write an SchemaBuilder.build(database) method that tells the executor to run the queries with another db. We can not do it easily because SchemaBuilder is part of Ran.
		ValqueriesSchemaExecutor valqueriesSchemaExecutor = ValqueriesSchemaExecutor.forDatabase(targetDatabase);
		ValqueriesSchemaBuilder schemaBuilder = new ValqueriesSchemaBuilder(valqueriesSchemaExecutor, new SqlNameFormatter());
		schemaBuilder.addTable(dialect.getTableName(Clazz.of(typeDescriber.clazz())), table -> {
			typeDescriber.fields().forEach(table::addColumn);
			table.addPrimaryKey(typeDescriber.primaryKeys());
			typeDescriber.indexes().forEach(table::addIndex);
		});
		schemaBuilder.build();
		return schemaBuilder.getGeneratedSql();
	}
}
