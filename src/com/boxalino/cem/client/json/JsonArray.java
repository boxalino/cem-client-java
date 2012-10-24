package com.boxalino.cem.client.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * A json array wrapper
 *
 * @author nitro
 */
public class JsonArray extends JsonWrapper implements Iterable<Integer> {
	/** Items storage */
	protected final List<JsonWrapper> items;


	/**
	 * Constructor
	 *
	 */
	public JsonArray() {
		super();
		this.items = new LinkedList<JsonWrapper>();
	}

	/**
	 * Constructor
	 *
	 * @param items initial items
	 */
	public JsonArray(Collection<? extends JsonWrapper> items) {
		super();
		this.items = new LinkedList<JsonWrapper>(items);
	}

	/**
	 * Copy constructor
	 *
	 * @param array array to copy
	 */
	public JsonArray(JsonArray array) {
		this(array.items);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return items.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof JsonArray) {
			JsonArray a = (JsonArray)o;

			return items.equals(a.items);
		}
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			/** Internal iterator state */
			int i = 0;


			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean hasNext() {
				return (i < items.size());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Integer next() {
				return i++;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isArray() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonArray asArray() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toJson(boolean strict) {
		StringBuilder buffer = new StringBuilder();
		int i = 0;

		buffer.append('[');
		for (JsonWrapper item : items) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append(item.toJson(strict));
			i++;
		}
		buffer.append(']');
		return buffer.toString();
	}


	/**
	 * Get array size
	 *
	 * @return array size
	 */
	public int size() {
		return items.size();
	}


	/**
	 * Get value type at given index
	 *
	 * @param index given index
	 * @return value type at index
	 */
	public Type getType(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getType();
		} else if (item instanceof JsonArray) {
			return Type.ARRAY;
		} else if (item instanceof JsonObject) {
			return Type.OBJECT;
		}
		return Type.NONE;
	}

	/**
	 * Check if value at given index is null
	 *
	 * @param index given index
	 * @return true if value is null, false otherwise
	 */
	public boolean isNull(int index) {
		return (getType(index) == Type.NULL);
	}

	/**
	 * Check if value at given index is undefined
	 *
	 * @param index given index
	 * @return true if value is undefined, false otherwise
	 */
	public boolean isUndefined(int index) {
		return (getType(index) == Type.UNDEFINED);
	}

	/**
	 * Check if value at given index is boolean
	 *
	 * @param index given index
	 * @return true if value is boolean, false otherwise
	 */
	public boolean isBoolean(int index) {
		return (getType(index) == Type.BOOLEAN);
	}

	/**
	 * Check if value at given index is numeric
	 *
	 * @param index given index
	 * @return true if value is numeric, false otherwise
	 */
	public boolean isNumeric(int index) {
		return (getType(index) == Type.NUMERIC);
	}

	/**
	 * Check if value at given index is a date
	 *
	 * @param index given index
	 * @return true if value is a date, false otherwise
	 */
	public boolean isDate(int index) {
		return (getType(index) == Type.DATE);
	}

	/**
	 * Check if value at given index is a string, a boolean, a number or a date
	 *
	 * @param index given index
	 * @return true if value is a string, false otherwise
	 */
	public boolean isString(int index) {
		switch (getType(index)) {
		case BOOLEAN:
		case DATE:
		case NUMERIC:
		case STRING:
			return true;
		}
		return false;
	}

	/**
	 * Check if value at given index is a json array
	 *
	 * @param index given index
	 * @return true if value is a json array, false otherwise
	 */
	public boolean isJsonArray(int index) {
		return (getType(index) == Type.ARRAY);
	}

	/**
	 * Check if value at given index is a json object
	 *
	 * @param index given index
	 * @return true if value is a json object, false otherwise
	 */
	public boolean isJsonObject(int index) {
		return (getType(index) == Type.OBJECT);
	}


	/**
	 * Get a boolean value at given index
	 *
	 * @param index given index
	 * @return boolean value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Boolean getBoolean(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getBoolean();
		}
		return null;
	}

	/**
	 * Get a char value at given index
	 *
	 * @param index given index
	 * @return char value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Character getChar(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getChar();
		}
		return null;
	}

	/**
	 * Get an integer value at given index
	 *
	 * @param index given index
	 * @return integer value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Integer getInteger(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getInteger();
		}
		return null;
	}

	/**
	 * Get a long value at given index
	 *
	 * @param index given index
	 * @return long value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Long getLong(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getLong();
		}
		return null;
	}

	/**
	 * Get a float value at given index
	 *
	 * @param index given index
	 * @return float value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Float getFloat(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getFloat();
		}
		return null;
	}

	/**
	 * Get a double value at given index
	 *
	 * @param index given index
	 * @return double value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Double getDouble(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getDouble();
		}
		return null;
	}

	/**
	 * Get a date value at given index
	 *
	 * @param index given index
	 * @return date value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public Date getDate(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getDate();
		}
		return null;
	}

	/**
	 * Get a string value at given index
	 *
	 * @param index given index
	 * @return string value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public String getString(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getString();
		}
		return null;
	}

	/**
	 * Get a json array value at given index
	 *
	 * @param index given index
	 * @return json array value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray getJsonArray(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonArray) {
			return ((JsonArray)item);
		}
		return null;
	}

	/**
	 * Get a json object value at given index
	 *
	 * @param index given index
	 * @return json object value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonObject getJsonObject(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonObject) {
			return ((JsonObject)item);
		}
		return null;
	}

	/**
	 * Get a json value at given index
	 *
	 * @param index given index
	 * @return json value or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonValue getJsonValue(int index) {
		JsonWrapper item = items.get(index);

		if (item instanceof JsonValue) {
			return ((JsonValue)item);
		}
		return null;
	}

	/**
	 * Get a json wrapper at given index
	 *
	 * @param index given index
	 * @return json wrapper or null if none
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonWrapper getJson(int index) {
		return items.get(index);
	}


	/**
	 * Add a null value
	 *
	 * @return this array
	 */
	public JsonArray addNull() {
		return insertNull(size());
	}

	/**
	 * Add an undefined value
	 *
	 * @return this array
	 */
	public JsonArray addUndefined() {
		return insertUndefined(size());
	}

	/**
	 * Add a boolean value
	 *
	 * @param value boolean value
	 * @return this array
	 */
	public JsonArray addBoolean(boolean value) {
		return insertBoolean(size(), value);
	}

	/**
	 * Add an char value
	 *
	 * @param value char value
	 * @return this array
	 */
	public JsonArray addChar(char value) {
		return insertChar(size(), value);
	}

	/**
	 * Add an integer value
	 *
	 * @param value integer value
	 * @return this array
	 */
	public JsonArray addInteger(int value) {
		return insertInteger(size(), value);
	}

	/**
	 * Add a long value
	 *
	 * @param value long value
	 * @return this array
	 */
	public JsonArray addLong(long value) {
		return insertLong(size(), value);
	}

	/**
	 * Add a float value
	 *
	 * @param value float value
	 * @return this array
	 */
	public JsonArray addFloat(float value) {
		return insertFloat(size(), value);
	}

	/**
	 * Add a double value
	 *
	 * @param value double value
	 * @return this array
	 */
	public JsonArray addDouble(double value) {
		return insertDouble(size(), value);
	}

	/**
	 * Add a number value
	 *
	 * @param value number value
	 * @return this array
	 */
	public JsonArray addNumber(Number value) {
		return insertNumber(size(), value);
	}

	/**
	 * Add a date value
	 *
	 * @param value date value
	 * @return this array
	 */
	public JsonArray addDate(Date value) {
		return insertDate(size(), value);
	}

	/**
	 * Add a string value
	 *
	 * @param value string value
	 * @return this array
	 */
	public JsonArray addString(String value) {
		return insertString(size(), value);
	}

	/**
	 * Add a json value
	 *
	 * @param value json value
	 * @return this array
	 */
	public JsonArray addJson(JsonWrapper value) {
		return insertJson(size(), value);
	}

	/**
	 * Add a json value
	 *
	 * @param value json value (source)
	 * @return this array
	 */
	public JsonArray addJson(String value) {
		return insertJson(size(), value);
	}


	/**
	 * Insert a null value
	 *
	 * @param index index to insert value at
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertNull(int index) {
		items.add(index, JsonValue.NULL);
		return this;
	}

	/**
	 * Insert an undefined value
	 *
	 * @param index index to insert value at
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertUndefined(int index) {
		items.add(index, JsonValue.UNDEFINED);
		return this;
	}

	/**
	 * Insert a boolean value
	 *
	 * @param index index to insert value at
	 * @param value boolean value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertBoolean(int index, boolean value) {
		items.add(index, value ? JsonBooleanValue.TRUE : JsonBooleanValue.FALSE);
		return this;
	}

	/**
	 * Insert a character value
	 *
	 * @param index index to insert value at
	 * @param value char value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertChar(int index, char value) {
		items.add(index, new JsonStringValue(value));
		return this;
	}

	/**
	 * Insert an integer value
	 *
	 * @param index index to insert value at
	 * @param value integer value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertInteger(int index, int value) {
		items.add(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Insert a long value
	 *
	 * @param index index to insert value at
	 * @param value long value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertLong(int index, long value) {
		items.add(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Insert a float value
	 *
	 * @param index index to insert value at
	 * @param value float value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertFloat(int index, float value) {
		items.add(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Insert a double value
	 *
	 * @param index index to insert value at
	 * @param value double value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertDouble(int index, double value) {
		items.add(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Insert a number value
	 *
	 * @param index index to insert value at
	 * @param value number value
	 * @return this array
	 */
	public JsonArray insertNumber(int index, Number value) {
		items.add(index, new JsonNumericValue(value.doubleValue()));
		return this;
	}

	/**
	 * Insert a date value
	 *
	 * @param index index to insert value at
	 * @param value date value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertDate(int index, Date value) {
		items.add(index, new JsonDateValue(value));
		return this;
	}

	/**
	 * Insert a string value
	 *
	 * @param index index to insert value at
	 * @param value string value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertString(int index, String value) {
		items.add(index, new JsonStringValue(value));
		return this;
	}

	/**
	 * Insert a json value
	 *
	 * @param index index to insert value at
	 * @param value json value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertJson(int index, JsonWrapper value) {
		items.add(index, value);
		return this;
	}

	/**
	 * Insert a json value
	 *
	 * @param index index to insert value at
	 * @param value json value (source)
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray insertJson(int index, String value) {
		items.add(index, eval(value));
		return this;
	}


	/**
	 * Set a null value
	 *
	 * @param index index to set value at
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setNull(int index) {
		items.set(index, JsonValue.NULL);
		return this;
	}

	/**
	 * Set an undefined value
	 *
	 * @param index index to set value at
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setUndefined(int index) {
		items.set(index, JsonValue.UNDEFINED);
		return this;
	}

	/**
	 * Set a boolean value
	 *
	 * @param index index to set value at
	 * @param value boolean value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setBoolean(int index, boolean value) {
		items.set(index, value ? JsonBooleanValue.TRUE : JsonBooleanValue.FALSE);
		return this;
	}

	/**
	 * Set a character value
	 *
	 * @param index index to set value at
	 * @param value char value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setChar(int index, char value) {
		items.set(index, new JsonStringValue(value));
		return this;
	}

	/**
	 * Set an integer value
	 *
	 * @param index index to set value at
	 * @param value integer value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setInteger(int index, int value) {
		items.set(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a long value
	 *
	 * @param index index to set value at
	 * @param value long value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setLong(int index, long value) {
		items.set(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a float value
	 *
	 * @param index index to set value at
	 * @param value float value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setFloat(int index, float value) {
		items.set(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a double value
	 *
	 * @param index index to set value at
	 * @param value double value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setDouble(int index, double value) {
		items.set(index, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a number value
	 *
	 * @param index index to set value at
	 * @param value number value
	 * @return this array
	 */
	public JsonArray setNumber(int index, Number value) {
		items.set(index, new JsonNumericValue(value.doubleValue()));
		return this;
	}

	/**
	 * Set a date value
	 *
	 * @param index index to set value at
	 * @param value date value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setDate(int index, Date value) {
		items.set(index, new JsonDateValue(value));
		return this;
	}

	/**
	 * Set a string value
	 *
	 * @param index index to set value at
	 * @param value string value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setString(int index, String value) {
		items.set(index, new JsonStringValue(value));
		return this;
	}

	/**
	 * Set a json value
	 *
	 * @param index index to set value at
	 * @param value json value
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setJson(int index, JsonWrapper value) {
		items.set(index, value);
		return this;
	}

	/**
	 * Set a json value
	 *
	 * @param index index to set value at
	 * @param value json value (source)
	 * @return this array
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public JsonArray setJson(int index, String value) {
		items.set(index, eval(value));
		return this;
	}


	/**
	 * Remove element at index
	 *
	 * @param index element to remove
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public void remove(int index) {
		items.remove(index);
	}


	/**
	 * Clear array
	 *
	 */
	public void clear() {
		items.clear();
	}
}
