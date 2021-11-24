
package com.valqueries;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public interface IStatement {
	void set(String column, boolean value);
	void set(String column, Integer value);
	void  set(String column, Long value);
	void set(String column, Float value);
	void set(String column, Double value);
	void set(String column, ZonedDateTime value);
	void set(String column, LocalDateTime value);
	void set(String column, LocalDate value);
	void set(String column, String value);
	void set(String column, UUID value);
	void set(String column, Enum<?> value);
	void set(String column, Collection<?> value);
	void set(String column, byte[] value);
	void set(String column, Object value);
}
