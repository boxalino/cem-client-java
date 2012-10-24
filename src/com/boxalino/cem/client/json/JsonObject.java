package com.boxalino.cem.client.json;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * A json object wrapper
 *
 * @author nitro
 */
public class JsonObject extends JsonWrapper implements Iterable<String> {
	/** Items storage */
	protected final Map<String, JsonWrapper> items;


	/**
	 * Constructor
	 *
	 */
	public JsonObject() {
		super();
		this.items = new TreeMap<String, JsonWrapper>();
	}

	/**
	 * Constructor
	 *
	 * @param items initial items
	 */
	public JsonObject(Map<String, ? extends JsonWrapper> items) {
		super();
		this.items = new TreeMap<String, JsonWrapper>(items);
	}

	/**
	 * Copy constructor
	 *
	 * @param object object to copy
	 */
	public JsonObject(JsonObject object) {
		this(object.items);
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
		if (o instanceof JsonObject) {
			JsonObject ob = (JsonObject)o;

			return items.equals(ob.items);
		}
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<String> iterator() {
		return items.keySet().iterator();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isObject() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JsonObject asObject() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toJson(boolean strict) {
		StringBuilder buffer = new StringBuilder();
		int i = 0;

		buffer.append('{');
		for (Map.Entry<String, JsonWrapper> item : items.entrySet()) {
			if (i > 0) {
				buffer.append(',');
			}

			// if key is an identifier okay, else quote it
			if (strict) {
				buffer.append(toJsonString(item.getKey(), '"'));
			} else if (isIdentifier(item.getKey())) {
				buffer.append(item.getKey());
			} else {
				buffer.append(toJsonString(item.getKey(), '\''));
			}
			buffer.append(':');
			buffer.append(item.getValue().toJson(strict));

			i++;
		}
		buffer.append('}');
		return buffer.toString();
	}


	/**
	 * Get key count
	 *
	 * @return key count
	 */
	public int size() {
		return items.size();
	}


	/**
	 * Check if a value exists
	 *
	 * @param key given key
	 * @return return true if value exits, false otherwise
	 */
	public boolean has(String key) {
		return items.containsKey(key);
	}

	/**
	 * Get object keys
	 *
	 * @return object keys
	 */
	public Set<String> keys() {
		return Collections.unmodifiableSet(items.keySet());
	}


	/**
	 * Get value type at given index
	 *
	 * @param key given key
	 * @return value type at index
	 */
	public Type getType(String key) {
		JsonWrapper item = items.get(key);

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
	 * Check if value at given key is null
	 *
	 * @return true if value is null, false otherwise
	 */
	public boolean isNull(String key) {
		return (getType(key) == Type.NULL);
	}

	/**
	 * Check if value at given key is null
	 *
	 * @param key given key
	 * @return true if value is undefined, false otherwise
	 */
	public boolean isUndefined(String key) {
		return (getType(key) == Type.UNDEFINED);
	}

	/**
	 * Check if value at given key is boolean
	 *
	 * @param key given key
	 * @return true if value is boolean, false otherwise
	 */
	public boolean isBoolean(String key) {
		return (getType(key) == Type.BOOLEAN);
	}

	/**
	 * Check if value at given key is numeric
	 *
	 * @param key given key
	 * @return true if value is numeric, false otherwise
	 */
	public boolean isNumeric(String key) {
		return (getType(key) == Type.NUMERIC);
	}

	/**
	 * Check if value at given key is a date
	 *
	 * @param key given key
	 * @return true if value is a date, false otherwise
	 */
	public boolean isDate(String key) {
		return (getType(key) == Type.DATE);
	}

	/**
	 * Check if value at given key is a string, a boolean, a number or a date
	 *
	 * @param key given key
	 * @return true if value is a string, false otherwise
	 */
	public boolean isString(String key) {
		switch (getType(key)) {
		case BOOLEAN:
		case DATE:
		case NUMERIC:
		case STRING:
			return true;
		}
		return false;
	}

	/**
	 * Check if value at given key is a json array
	 *
	 * @param key given key
	 * @return true if value is a json array, false otherwise
	 */
	public boolean isJsonArray(String key) {
		return (getType(key) == Type.ARRAY);
	}

	/**
	 * Check if value at given key is a json object
	 *
	 * @param key given key
	 * @return true if value is a json object, false otherwise
	 */
	public boolean isJsonObject(String key) {
		return (getType(key) == Type.OBJECT);
	}


	/**
	 * Get a boolean value at given key
	 *
	 * @param key given key
	 * @return boolean value or null if none
	 */
	public Boolean getBoolean(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getBoolean();
		}
		return null;
	}

	/**
	 * Get a char value at given key
	 *
	 * @param key given key
	 * @return char value or null if none
	 */
	public Character getChar(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getChar();
		}
		return null;
	}

	/**
	 * Get an integer value at given key
	 *
	 * @param key given key
	 * @return integer value or null if none
	 */
	public Integer getInteger(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getInteger();
		}
		return null;
	}

	/**
	 * Get a long value at given key
	 *
	 * @param key given key
	 * @return long value or null if none
	 */
	public Long getLong(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getLong();
		}
		return null;
	}

	/**
	 * Get a float value at given key
	 *
	 * @param key given key
	 * @return float value or null if none
	 */
	public Float getFloat(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getFloat();
		}
		return null;
	}

	/**
	 * Get a double value at given key
	 *
	 * @param key given key
	 * @return double value or null if none
	 */
	public Double getDouble(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getDouble();
		}
		return null;
	}

	/**
	 * Get a date value at given key
	 *
	 * @param key given key
	 * @return date value or null if none
	 */
	public Date getDate(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getDate();
		}
		return null;
	}

	/**
	 * Get a string value at given key
	 *
	 * @param key given key
	 * @return string value or null if none
	 */
	public String getString(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item).getString();
		}
		return null;
	}

	/**
	 * Get a json array value at given key
	 *
	 * @param key given key
	 * @return json array value or null if none
	 */
	public JsonArray getJsonArray(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonArray) {
			return ((JsonArray)item);
		}
		return null;
	}

	/**
	 * Get a json object value at given key
	 *
	 * @param key given key
	 * @return json object value or null if none
	 */
	public JsonObject getJsonObject(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonObject) {
			return ((JsonObject)item);
		}
		return null;
	}

	/**
	 * Get a json value at given key
	 *
	 * @param key given key
	 * @return json value or null if none
	 */
	public JsonValue getJsonValue(String key) {
		JsonWrapper item = items.get(key);

		if (item instanceof JsonValue) {
			return ((JsonValue)item);
		}
		return null;
	}

	/**
	 * Get a json wrapper at given key
	 *
	 * @param key given key
	 * @return json wrapper or null if none
	 */
	public JsonWrapper getJson(String key) {
		return items.get(key);
	}


	/**
	 * Set a null value
	 *
	 * @param key property name
	 * @return this object
	 */
	public JsonObject setNull(String key) {
		items.put(key, JsonValue.NULL);
		return this;
	}

	/**
	 * Set an undefined value
	 *
	 * @param key property name
	 * @return this object
	 */
	public JsonObject setUndefined(String key) {
		items.put(key, JsonValue.UNDEFINED);
		return this;
	}

	/**
	 * Set a boolean value
	 *
	 * @param key property name
	 * @param value boolean value
	 * @return this object
	 */
	public JsonObject setBoolean(String key, boolean value) {
		items.put(key, value ? JsonBooleanValue.TRUE : JsonBooleanValue.FALSE);
		return this;
	}

	/**
	 * Set a character value
	 *
	 * @param key property name
	 * @param value char value
	 * @return this object
	 */
	public JsonObject setChar(String key, char value) {
		items.put(key, new JsonStringValue(value));
		return this;
	}

	/**
	 * Set an integer value
	 *
	 * @param key property name
	 * @param value integer value
	 * @return this object
	 */
	public JsonObject setInteger(String key, int value) {
		items.put(key, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a long value
	 *
	 * @param key property name
	 * @param value long value
	 * @return this object
	 */
	public JsonObject setLong(String key, long value) {
		items.put(key, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a float value
	 *
	 * @param key property name
	 * @param value float value
	 * @return this object
	 */
	public JsonObject setFloat(String key, float value) {
		items.put(key, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a double value
	 *
	 * @param key property name
	 * @param value double value
	 * @return this object
	 */
	public JsonObject setDouble(String key, double value) {
		items.put(key, new JsonNumericValue(value));
		return this;
	}

	/**
	 * Set a number value
	 *
	 * @param key property name
	 * @param value number value
	 * @return this object
	 */
	public JsonObject setNumber(String key, Number value) {
		items.put(key, new JsonNumericValue(value.doubleValue()));
		return this;
	}

	/**
	 * Set a date value
	 *
	 * @param key property name
	 * @param value date value
	 * @return this object
	 */
	public JsonObject setDate(String key, Date value) {
		items.put(key, new JsonDateValue(value));
		return this;
	}

	/**
	 * Set a string value
	 *
	 * @param key property name
	 * @param value string value
	 * @return this object
	 */
	public JsonObject setString(String key, String value) {
		items.put(key, new JsonStringValue(value));
		return this;
	}

	/**
	 * Set a json value
	 *
	 * @param key property name
	 * @param value json value
	 * @return this object
	 */
	public JsonObject setJson(String key, JsonWrapper value) {
		items.put(key, value);
		return this;
	}

	/**
	 * Set a json value
	 *
	 * @param key property name
	 * @param value json value (source)
	 * @return this object
	 */
	public JsonObject setJson(String key, String value) {
		items.put(key, eval(value));
		return this;
	}


	/**
	 * Remove element at key
	 *
	 * @param key element to remove
	 */
	public void remove(String key) {
		items.remove(key);
	}


	/**
	 * Clear object
	 *
	 */
	public void clear() {
		items.clear();
	}
}
