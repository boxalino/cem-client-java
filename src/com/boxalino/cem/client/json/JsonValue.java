package com.boxalino.cem.client.json;

import java.util.Date;


/**
 * A json value wrapper
 *
 * @author nitro
 */
public class JsonValue extends JsonWrapper {
	/** Null singleton */
	public static final JsonValue NULL = new JsonValue("null");

	/** Undefined singleton */
	public static final JsonValue UNDEFINED = new JsonValue("undefined");


	/** Items storage */
	protected final String item;


	/**
	 * Constructor
	 *
	 */
	public JsonValue() {
		this("null");
	}

	/**
	 * Constructor
	 *
	 * @param source json source
	 */
	public JsonValue(String source) {
		super();
		this.item = source != null ? source : "null";
	}

	/**
	 * Copy constructor
	 *
	 * @param wrapper value to copy
	 */
	public JsonValue(JsonWrapper wrapper) {
		this(wrapper.toJson());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (item != null) {
			return item.hashCode();
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof JsonValue) {
			JsonValue jv = (JsonValue)o;

			if (item != null) {
				return item.equals(jv.item);
			}
			return (jv.item == null);
		}
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValue() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonValue asValue() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toJson(boolean strict) {
		return item;
	}


	/**
	 * Get value type
	 *
	 * @return value type
	 */
	public Type getType() {
		// null string
		if (item == null) {
			return Type.NONE;
		}

		// null&undefined keywords, array & object literals
		if (item.equals("null")) {
			return Type.NULL;
		}
		if (item.equals("undefined")) {
			return Type.UNDEFINED;
		}
		if (item.equals("true") || item.equals("false")) {
			return Type.BOOLEAN;
		}
		if (isNumber(item)) {
			return Type.NUMERIC;
		}

		// date literals
		if (evalDate(item) != null) {
			return Type.DATE;
		}

		// else it is a string literal
		return Type.STRING;
	}

	/**
	 * Check if value is null
	 *
	 * @return true if value is null, false otherwise
	 */
	public boolean isNull() {
		return (getType() == Type.NULL);
	}

	/**
	 * Check if value is undefined
	 *
	 * @return true if value is undefined, false otherwise
	 */
	public boolean isUndefined() {
		return (getType() == Type.UNDEFINED);
	}

	/**
	 * Check if value is boolean
	 *
	 * @return true if value is boolean, false otherwise
	 */
	public boolean isBoolean() {
		return (getType() == Type.BOOLEAN);
	}

	/**
	 * Check if value is numeric
	 *
	 * @return true if value is numeric, false otherwise
	 */
	public boolean isNumeric() {
		return (getType() == Type.NUMERIC);
	}

	/**
	 * Check if value is a date
	 *
	 * @return true if value is a date, false otherwise
	 */
	public boolean isDate() {
		return (getType() == Type.DATE);
	}

	/**
	 * Check if value is a string, a boolean, a number or a date
	 *
	 * @return true if value is a string, false otherwise
	 */
	public boolean isString() {
		switch (getType()) {
		case BOOLEAN:
		case DATE:
		case NUMERIC:
		case STRING:
			return true;
		}
		return false;
	}


	/**
	 * Get a boolean value
	 *
	 * @return boolean value or null if none
	 */
	public Boolean getBoolean() {
		String str = getString();

		if (str != null && (str.equals("true") || str.equals("false"))) {
			return Boolean.parseBoolean(str);
		}
		return null;
	}

	/**
	 * Get a char value
	 *
	 * @return char value or null if none
	 */
	public Character getChar() {
		String str = getString();

		if (str != null && str.length() == 1) {
			return str.charAt(0);
		}
		return null;
	}

	/**
	 * Get an integer value
	 *
	 * @return integer value or null if none
	 */
	public Integer getInteger() {
		Double d = getDouble();

		if (d != null) {
			return d.intValue();
		}
		return null;
	}

	/**
	 * Get a long value
	 *
	 * @return long value or null if none
	 */
	public Long getLong() {
		Double d = getDouble();

		if (d != null) {
			return d.longValue();
		}
		return null;
	}

	/**
	 * Get a float value
	 *
	 * @return float value or null if none
	 */
	public Float getFloat() {
		Double d = getDouble();

		if (d != null) {
			return d.floatValue();
		}
		return null;
	}

	/**
	 * Get a double value
	 *
	 * @return double value or null if none
	 */
	public Double getDouble() {
		String str = getString();

		if (str != null) {
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	/**
	 * Get a date value
	 *
	 * @return date value or null if none
	 */
	public Date getDate() {
		if (item != null) {
			return evalDate(item);
		}
		return null;
	}

	/**
	 * Get a string value
	 *
	 * @return string value or null if none
	 */
	public String getString() {
		if (item != null) {
			return evalString(item);
		}
		return null;
	}
}
