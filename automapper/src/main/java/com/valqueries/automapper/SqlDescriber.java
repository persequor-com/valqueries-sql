package com.valqueries.automapper;

import com.valqueries.Database;
import com.valqueries.OrmResultSet;
import io.ran.Property;
import io.ran.TypeDescriber;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlDescriber {
	public DbTable describe(TypeDescriber<?> typeDescriber, String tablename, Database database) {
		return database.obtainInTransaction(tx -> {
			try {
				DbTable table = new DbTable();
				table.columns.putAll(tx.query("DESCRIBE `" + tablename + "`", DbRow::new).stream().collect(Collectors.toMap(DbRow::getField, Function.identity())));
				table.index.putAll(tx.query("show index from `" + tablename+"`", DbIndex::new).stream().collect(Collectors.toMap(DbIndex::getKeyName, Function.identity(), (idx1, idx2) -> {
					if (idx1.getKeyName().equals(idx2.getKeyName())) {
						idx2.getColumns().putAll(idx1.getColumns());
						return null;
					}
					return idx1;
				})));
				return table;
			} catch (Exception e) {
				return null;
			}
		});
	}

	public static class DbTable {
		private Map<String, DbRow> columns = new HashMap<>();
		private Map<String, DbIndex> index = new HashMap<>();

		public Map<String, DbRow> getColumns() {
			return columns;
		}

		public void setColumns(Map<String, DbRow> columns) {
			this.columns = columns;
		}

		public Map<String, DbIndex> getIndex() {
			return index;
		}

		public void setIndex(Map<String, DbIndex> index) {
			this.index = index;
		}
	}


	public static class DbIndex {
		private boolean unique;
		private String keyName;
		private Map<Integer, String> columns = new HashMap<>();

		public DbIndex() {

		}

		public DbIndex(boolean unique, String keyName, String... columns) {
			this.unique = unique;
			this.keyName = keyName;
			int i = 0;
			for(String column : columns) {
				this.columns.put(++i, column);
			}
		}

		protected DbIndex(OrmResultSet r) throws SQLException {
			unique = r.getInt("Non_unique") == 0;
			keyName = r.getString("Key_name");
			columns.put(r.getInt("Seq_in_index"), r.getString("Column_name"));
		}

		public boolean isUnique() {
			return unique;
		}

		public void setUnique(boolean unique) {
			this.unique = unique;
		}

		public String getKeyName() {
			return keyName;
		}

		public void setKeyName(String keyName) {
			this.keyName = keyName;
		}

		public Map<Integer, String> getColumns() {
			return columns;
		}

		public void setColumns(Map<Integer, String> columns) {
			this.columns = columns;
		}

		public boolean matches(DbIndex o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			DbIndex dbIndex = (DbIndex) o;

			if (unique != dbIndex.unique) return false;
			return Objects.equals(columns.keySet(), dbIndex.columns.keySet()) && Objects.equals(new ArrayList<>(columns.values()), new ArrayList<>(dbIndex.columns.values()));
		}
	}

	public static class DbRow {
		private String field;
		private String type;
		private boolean allowsNull;

		public DbRow(String field, String type, boolean allowsNull) {
			this.field = field;
			this.type = type;
			this.allowsNull = allowsNull;
		}

		protected DbRow(OrmResultSet r) throws SQLException {
			field = r.getString("Field");
			type = r.getString("Type");
			allowsNull = r.getString("Null").equals("YES") ? true : false;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public boolean isAllowsNull() {
			return allowsNull;
		}

		public void setAllowsNull(boolean allowsNull) {
			this.allowsNull = allowsNull;
		}

		public boolean matches(Property property, String sqlType) {
			return sqlType.equalsIgnoreCase(type);
		}
	}
}
