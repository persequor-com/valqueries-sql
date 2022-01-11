package com.valqueries.automapper;

import com.valqueries.Database;
import io.ran.Clazz;
import io.ran.KeySet;
import io.ran.TypeDescriber;
import io.ran.TypeDescriberImpl;
import io.ran.token.ColumnToken;
import io.ran.token.TableToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class SqlGenerator {
	private static Logger logger = LoggerFactory.getLogger(SqlGenerator.class);
	private SqlNameFormatter sqlNameFormatter;
	private SqlDialect dialect;
	private SqlDescriber sqlDescriber;

	@Inject
	public SqlGenerator(SqlNameFormatter sqlNameFormatter, DialectFactory dialectFactory, Database database, SqlDescriber sqlDescriber) {
		this.sqlNameFormatter = sqlNameFormatter;
		this.dialect = dialectFactory.get(database);
		this.sqlDescriber = sqlDescriber;
	}

	public TableToken getTableName(TypeDescriber<?> typeDescriber) {
		return dialect.getTableName(Clazz.of(typeDescriber.clazz()));
	}

	public String generateOrModifyTable(Database database, TypeDescriber<?> typeDescriber) {
		logger.warn("generateOrModifyTable is a work in progress and is not considered stable");
		TableToken tablename = getTableName(typeDescriber);
		SqlDescriber.DbTable table = sqlDescriber.describe(typeDescriber, tablename, database);
		if (table == null) {
			return generateCreateTable(typeDescriber);
		} else {

			StringBuilder sb = new StringBuilder();
			typeDescriber.fields().forEach(property -> {
				ColumnToken columnName = dialect.column(property.getToken());
				String sqlType = dialect.getSqlType(property);
				if (!table.getColumns().containsKey(columnName.unescaped())) {
					sb.append(dialect.addColumn(tablename, columnName, sqlType));
				} else if (!table.getColumns().get(columnName.unescaped()).matches(property, sqlType)) {
					sb.append(dialect.changeColumn(tablename, columnName, sqlType));
				}
			});

			SqlDescriber.DbIndex index = table.getIndex().get("PRIMARY");
			if (!index.matches(toDbIndex(typeDescriber.primaryKeys()))) {
				sb.append("ALTER TABLE " + tablename + " DROP "+index.getRealName()+";");
				sb.append("ALTER TABLE " + tablename + " ADD PRIMARY KEY(" + getPrimaryKey(typeDescriber) + ");");
			}

			typeDescriber.indexes().forEach(key -> {
				SqlDescriber.DbIndex keyIndex = toDbIndex(key);
				Optional<SqlDescriber.DbIndex> idx = table.getIndex().values().stream().filter(keyIndex::matches).findFirst();
				if (!idx.isPresent()) {
					sb.append(dialect.addIndex(tablename, key, false));
				}
			});
			return sb.toString();

		}

	}

	private SqlDescriber.DbIndex toDbIndex(KeySet index) {
		SqlDescriber.DbIndex dbIndex = new SqlDescriber.DbIndex();
		dbIndex.setUnique(index.isPrimary());
		dbIndex.setColumns(new HashMap<>());
		index.forEach(f -> {
			dbIndex.getColumns().put(f.getOrder()+1, sqlNameFormatter.column(f.getToken()));
		});
		return dbIndex;
	}

	public String generateCreateTable(TypeDescriber<?> typeDescriber) {
		return dialect.generateCreateTable(typeDescriber);
	}

	private String getPrimaryKey(TypeDescriber<?> typeDescriber) {
		return typeDescriber.primaryKeys().stream().map(property -> {
			return dialect.escapeColumnOrTable(sqlNameFormatter.column(property.getToken()));
		}).collect(Collectors.joining(", "));
	}

	public String generateCreateTable(Class<?> clazz) {
		return generateCreateTable(TypeDescriberImpl.getTypeDescriber(clazz));
	}

}
