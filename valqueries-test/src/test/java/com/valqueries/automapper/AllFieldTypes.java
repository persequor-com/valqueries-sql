package com.valqueries.automapper;

import io.ran.PrimaryKey;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public class AllFieldTypes {
	@PrimaryKey
	private UUID id;
	private UUID uuid;
	private String string;
	private Character character;
	private ZonedDateTime zonedDateTime;
	private Instant instant;
	private LocalDateTime localDateTime;
	private LocalDate localDate;
	private BigDecimal bigDecimal;
	private Brand anEnum;

	private Integer integer;
	private Short aShort;
	private Long aLong;
	private Double aDouble;
	private Float aFloat;
	private Boolean aBoolean;
	private Byte aByte;

	private int primitiveInteger;
	private short primitiveShort;
	private long primitiveLong;
	private double primitiveDouble;
	private float primitiveFloat;
	private boolean primitiveBoolean;
	private byte primitiveByte;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Character getCharacter() {
		return character;
	}

	public void setCharacter(Character character) {
		this.character = character;
	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public void setZonedDateTime(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}

	public Instant getInstant() {
		return instant;
	}

	public void setInstant(Instant instant) {
		this.instant = instant;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public void setLocalDateTime(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
	}

	public LocalDate getLocalDate() {
		return localDate;
	}

	public void setLocalDate(LocalDate localDate) {
		this.localDate = localDate;
	}

	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}

	public void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	public Brand getAnEnum() {
		return anEnum;
	}

	public void setAnEnum(Brand anEnum) {
		this.anEnum = anEnum;
	}

	public Integer getInteger() {
		return integer;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	public Short getaShort() {
		return aShort;
	}

	public void setaShort(Short aShort) {
		this.aShort = aShort;
	}

	public Long getaLong() {
		return aLong;
	}

	public void setaLong(Long aLong) {
		this.aLong = aLong;
	}

	public Double getaDouble() {
		return aDouble;
	}

	public void setaDouble(Double aDouble) {
		this.aDouble = aDouble;
	}

	public Float getaFloat() {
		return aFloat;
	}

	public void setaFloat(Float aFloat) {
		this.aFloat = aFloat;
	}

	public Boolean getaBoolean() {
		return aBoolean;
	}

	public void setaBoolean(Boolean aBoolean) {
		this.aBoolean = aBoolean;
	}

	public Byte getaByte() {
		return aByte;
	}

	public void setaByte(Byte aByte) {
		this.aByte = aByte;
	}

	public int getPrimitiveInteger() {
		return primitiveInteger;
	}

	public void setPrimitiveInteger(int primitiveInteger) {
		this.primitiveInteger = primitiveInteger;
	}

	public short getPrimitiveShort() {
		return primitiveShort;
	}

	public void setPrimitiveShort(short primitiveShort) {
		this.primitiveShort = primitiveShort;
	}

	public long getPrimitiveLong() {
		return primitiveLong;
	}

	public void setPrimitiveLong(long primitiveLong) {
		this.primitiveLong = primitiveLong;
	}

	public double getPrimitiveDouble() {
		return primitiveDouble;
	}

	public void setPrimitiveDouble(double primitiveDouble) {
		this.primitiveDouble = primitiveDouble;
	}

	public float getPrimitiveFloat() {
		return primitiveFloat;
	}

	public void setPrimitiveFloat(float primitiveFloat) {
		this.primitiveFloat = primitiveFloat;
	}

	public boolean isPrimitiveBoolean() {
		return primitiveBoolean;
	}

	public void setPrimitiveBoolean(boolean primitiveBoolean) {
		this.primitiveBoolean = primitiveBoolean;
	}

	public byte getPrimitiveByte() {
		return primitiveByte;
	}

	public void setPrimitiveByte(byte primitiveByte) {
		this.primitiveByte = primitiveByte;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
