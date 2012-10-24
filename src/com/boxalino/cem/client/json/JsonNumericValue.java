package com.boxalino.cem.client.json;


/**
 * A json numeric value wrapper
 *
 * @author nitro
 */
public class JsonNumericValue extends JsonValue {
	/**
	 * Constructor
	 *
	 */
	public JsonNumericValue() {
		this(0);
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonNumericValue(int value) {
		super(Integer.toString(value));
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonNumericValue(long value) {
		super(Long.toString(value));
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonNumericValue(float value) {
		super(value == (int)value ? Integer.toString((int)value) : Float.toString(value));
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonNumericValue(double value) {
		super(value == (long)value ? Long.toString((long)value) : Double.toString(value));
	}

	/**
	 * Parser constructor
	 *
	 * @param value initial value
	 */
	public JsonNumericValue(String value) {
		super(isNumber(value) ? value : "0");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		if (item.equals("null")) {
			return Type.NULL;
		}
		return Type.NUMERIC;
	}
}
