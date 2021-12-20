package com.valqueries.automapper;

import com.valqueries.automapper.elements.Element;
import io.ran.Clazz;
import io.ran.Property;
import io.ran.TypeDescriber;
import io.ran.token.Token;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class H2SqlDialect implements SqlDialect {
	private SqlNameFormatter sqlNameFormatter;

	public H2SqlDialect(SqlNameFormatter sqlNameFormatter) {
		this.sqlNameFormatter = sqlNameFormatter;
	}

	@Override
	public String escapeColumnOrTable(String name) {
		return "\""+name.toUpperCase()+"\"";
	}

	@Override
	public <O> String getUpsert(CompoundColumnizer<O> columnizer, Class<O> oClass) {
		return "MERGE INTO "+getTableName(Clazz.of(oClass))+" as target USING " +
				"(VALUES "+columnizer.getValueTokens().stream().map((l) -> "("+l.stream().map(e -> ":"+e).collect(Collectors.joining(", "))+")").collect(Collectors.joining(", "))+
				") incoming ("+columnizer.getFields().entrySet().stream().map((e) -> escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+") on "+columnizer.getKeys().stream().map(k -> "target."+escapeColumnOrTable(k)+" = incoming."+escapeColumnOrTable(k)).collect(Collectors.joining(" AND "))+
				(columnizer.getFieldsWithoutKeys().size() > 0 ? " WHEN MATCHED THEN UPDATE SET "+columnizer.getFieldsWithoutKeys().entrySet().stream().map(e -> escapeColumnOrTable(e.getValue())+" = incoming."+escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", ")):"")+
				" WHEN NOT MATCHED THEN INSERT ("+columnizer.getFields().entrySet().stream().map(e -> escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+") " +
				"VALUES ("+columnizer.getFields().entrySet().stream().map(e -> "incoming."+escapeColumnOrTable(e.getValue())).collect(Collectors.joining(", "))+");";
	}

	public String getSqlType(Class type, Property property) {
		MappedType mappedType = property.getAnnotations().get(MappedType.class);
		if (mappedType != null) {
			return mappedType.value();
		}
		if (type == UUID.class) {
			return "UUID";
		}
		if (type == boolean.class || type == Boolean.class) {
			return "BOOLEAN";
		}
		return SqlDialect.super.getSqlType(type, property);
	}

	@Override
	public String column(Token token) {
		return sqlNameFormatter.column(token);
	}

	@Override
	public String limit(int offset, Integer limit) {
		return " OFFSET "+offset+" ROWS" +
				"    FETCH NEXT "+limit+" ROWS ONLY";
	}

	public String getTableName(Clazz<? extends Object> modeltype) {
		return escapeColumnOrTable(sqlNameFormatter.table(modeltype.clazz));
	}

	public String delete(String tableAlias, TypeDescriber<?> typeDescriber, List<Element> elements, int offset, Integer limit) {
		String sql = "DELETE FROM " + getTableName(Clazz.of(typeDescriber.clazz()))+ " AS del";
		sql += " WHERE exists (SELECT * FROM "+getTableName(Clazz.of(typeDescriber.clazz()))+" AS "+tableAlias+ " WHERE "+typeDescriber.primaryKeys().stream().map(f -> "del."+column(f.getToken())+" = main."+column(f.getToken())).collect(Collectors.joining(" AND "));
		if (!elements.isEmpty()) {
			sql += " AND "+elements.stream().map(Element::queryString).collect(Collectors.joining(" AND "));
		}
		sql += ")";
		if (limit !=  null) {
			sql += limit(offset, limit);
		}
//		System.out.println(sql);
		return sql;
	}
}
