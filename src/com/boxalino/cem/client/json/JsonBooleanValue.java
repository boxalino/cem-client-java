package com.boxalino.cem.client.json;


/**
 * A json boolean value wrapper
 *
 * @author nitro
 */
public class JsonBooleanValue extends JsonValue {
	/** False singleton */
	public static final JsonBooleanValue FALSE = new JsonBooleanValue(false);

	/** True singleton */
	public static final JsonBooleanValue TRUE = new JsonBooleanValue(true);


	/**
	 * Convert native value to json
	 *
	 * @param value native value
	 * @return json value
	 */
	public static JsonBooleanValue fromValue(boolean value) {
		return (value ? TRUE : FALSE);
	}

	/**
	 * Convert string value to json
	 *
	 * @param value string value
	 * @return json value
	 */
	public static JsonBooleanValue fromValue(String value) {
		return fromValue(Boolean.parseBoolean(value));
	}


	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	private JsonBooleanValue(boolean value) {
		super(value ? "true" : "false");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		if (item.equals("null")) {
			return Type.NULL;
		}
		return Type.BOOLEAN;
	}
}
