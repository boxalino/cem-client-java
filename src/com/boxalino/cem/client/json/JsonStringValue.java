package com.boxalino.cem.client.json;


/**
 * A json string value wrapper
 *
 * @author nitro
 */
public class JsonStringValue extends JsonValue {
	/**
	 * Constructor
	 *
	 */
	public JsonStringValue() {
		this("");
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonStringValue(char value) {
		super(toJsonString(Character.toString(value), '\''));
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonStringValue(String value) {
		super(toJsonString(value, '"'));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		if (item.equals("null")) {
			return Type.NULL;
		}
		return Type.STRING;
	}
}
