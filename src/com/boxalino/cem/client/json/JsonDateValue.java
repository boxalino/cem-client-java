package com.boxalino.cem.client.json;

import java.text.ParseException;

import java.util.Date;


/**
 * A json date value wrapper
 *
 * @author nitro
 */
public class JsonDateValue extends JsonValue {
	/**
	 * Constructor
	 *
	 */
	public JsonDateValue() {
		this(new Date());
	}

	/**
	 * Copy constructor
	 *
	 * @param value initial value
	 */
	public JsonDateValue(Date value) {
		super(toJsonDate(value));
	}

	/**
	 * Parser constructor
	 *
	 * @param value initial value
	 */
	public JsonDateValue(String value) {
		super(isDate(value) ? toJsonString(value, '"') : toJsonDate(new Date()));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type getType() {
		if (item.equals("null")) {
			return Type.NULL;
		}
		return Type.DATE;
	}


	/**
	 * Check if a string is a valid date
	 *
	 * @param str string to check
	 * @return true if it is a date, false otherwise
	 */
	protected static boolean isDate(String str) {
		try {
			df1.get().parse(str);
			return true;
		} catch (ParseException e) {
		}
		try {
			df2.get().parse(str);
			return true;
		} catch (ParseException e) {
		}
		try {
			df3.get().parse(str);
			return true;
		} catch (ParseException e) {
		}
		return false;
	}
}
