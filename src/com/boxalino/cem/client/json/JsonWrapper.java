package com.boxalino.cem.client.json;

import java.io.FileReader;
import java.io.Serializable;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


/**
 * An abstract json wrapper
 *
 * @author nitro
 */
public abstract class JsonWrapper implements Serializable {
	/**
	 * Json data type
	 *
	 * @author nitro
	 */
	public static enum Type {
		/** No type (not defined) */
		NONE,

		/** Null reference */
		NULL,

		/** Undefined reference */
		UNDEFINED,

		/** Boolean literal */
		BOOLEAN,

		/** Numeric literal */
		NUMERIC,

		/** String literal */
		STRING,

		/** Date literal */
		DATE,

		/** Object literal */
		OBJECT,

		/** Array literal */
		ARRAY;
	}


	/**
	 * Parse json source
	 *
	 * @param json json source
	 * @return json wrapper
	 */
	public static JsonWrapper eval(String json) {
		if (json != null) {
			return visitJson(new Tokenizer(json.trim()));
		}
		return JsonValue.NULL;
	}

	/**
	 * Parse json source as array
	 *
	 * @param json json source
	 * @return json array or null if none
	 */
	public static JsonArray evalArray(String json) {
		if (json != null) {
			return visitArray(new Tokenizer(json.trim()));
		}
		return null;
	}

	/**
	 * Parse json source as object
	 *
	 * @param json json source
	 * @return json object or null if none
	 */
	public static JsonObject evalObject(String json) {
		if (json != null) {
			return visitObject(new Tokenizer(json.trim()));
		}
		return null;
	}


	/** Date formatter (full) */
	protected static ThreadLocal<SimpleDateFormat> df1 = new ThreadLocal<SimpleDateFormat>() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

			df.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
			return df;
		}
	};

	/** Date formatter (date/time only) */
	protected static ThreadLocal<SimpleDateFormat> df2 = new ThreadLocal<SimpleDateFormat>() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			df.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
			return df;
		}
	};

	/** Date formatter (date/time only) */
	protected static ThreadLocal<SimpleDateFormat> df3 = new ThreadLocal<SimpleDateFormat>() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			df.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
			return df;
		}
	};


	/**
	 * Constructor
	 *
	 */
	protected JsonWrapper() {
	}


	/**
	 * Get json representation
	 *
	 * @return json representation
	 */
	@Override
	public String toString() {
		return toJson();
	}


	/**
	 * Check if this wrapper is an array literal
	 *
	 * @return true if array
	 */
	public boolean isArray() {
		return false;
	}

	/**
	 * Check if this wrapper is an object literal
	 *
	 * @return true if object
	 */
	public boolean isObject() {
		return false;
	}

	/**
	 * Check if this wrapper is a value literal
	 *
	 * @return true if value literal (boolean, date, numeric or string)
	 */
	public boolean isValue() {
		return false;
	}

	/**
	 * Convert this wrapper as an array literal
	 *
	 * @return array literal
	 */
	public JsonArray asArray() {
		return new JsonArray();
	}

	/**
	 * Convert this wrapper as an object literal
	 *
	 * @return object literal
	 */
	public JsonObject asObject() {
		return new JsonObject();
	}

	/**
	 * Convert this wrapper as a value literal
	 *
	 * @return value literal
	 */
	public JsonValue asValue() {
		return JsonValue.NULL;
	}

	/**
	 * Convert this object to strict json source
	 *
	 * @return json source
	 */
	public String toJson() {
	 	return toJson(true);
	}

	/**
	 * Convert this object to json source
	 *
	 * @param strict strict json format or only js-compatible
	 * @return json source
	 */
	public abstract String toJson(boolean strict);


	/**
	 * Convert a json representation to a date
	 *
	 * @param json json representation or null if none
	 * @return converted date or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static Date evalDate(String json) {
		return evalDate(new Tokenizer(json));
	}

	/**
	 * Convert a json representation to a date
	 *
	 * @param json json representation or null if none
	 * @return converted date or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static Date evalDate(Tokenizer json) {
		String str = evalString(json);

		if (str != null) {
			try {
				return df1.get().parse(str);
			} catch (ParseException e) {
			}
			try {
				return df2.get().parse(str);
			} catch (ParseException e) {
			}
			try {
				return df3.get().parse(str);
			} catch (ParseException e) {
			}
		}
		return null;
	}

	/**
	 * Convert a json representation to a value string
	 *
	 * @param json json representation or null if none
	 * @return converted value as string or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static String evalString(String json) {
		return evalString(new Tokenizer(json));
	}

	/**
	 * Convert a json representation to a value string
	 *
	 * @param json json representation or null if none
	 * @return converted value as string or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static String evalString(Tokenizer json) {
		if (json.hasNext()) {
			// parse string
			char c = json.peek();

			if (c == '\'' || c == '"') {
				// string literal
				StringBuilder buffer = new StringBuilder();
				char quote = json.next();

quote:			while (json.hasNext()) {
					c = json.next();
					switch (c) {
					case '\n':
					case '\r':
						throw new IllegalStateException("unclosed string in json source (" + json + ")");

					case '\\':
						c = json.next();
						switch (c) {
						case 'b':
							buffer.append("\b");
							break;

						case 't':
							buffer.append("\t");
							break;

						case 'n':
							buffer.append("\n");
							break;

						case 'r':
							buffer.append("\r");
							break;

						case 'f':
							buffer.append("\f");
							break;

						case 'u':
							buffer.append(
								Character.toString((char)Integer.parseInt(json.nextString(4), 16))
							);
							break;

						case 'x':
							buffer.append(
								Character.toString((char)Integer.parseInt(json.nextString(2), 16))
							);
							break;

						default:
							buffer.append(c);
							break;
						}
						break;

					case '\'':
					case '"':
						if (c == quote) {
							return buffer.toString();
						}

					default:
						buffer.append(c);
						break;
					}
				}
				throw new IllegalStateException("unclosed string in json source (" + json + ")");
			}
			if (c == '_' || c == '-' || c == '.' || Character.isLetterOrDigit(c)) {
				// consume all alpha-numeric characters (and '.', '-', '_')
				StringBuilder buffer = new StringBuilder();

				buffer.append(json.next());
separator:		while (json.hasNext()) {
					c = json.peek();
					switch (c) {
					case '_':
					case '-':
					case '.':
						break;

					default:
						if (!Character.isLetterOrDigit(c)) {
							break separator;
						}
						break;
					}
					buffer.append(c);
					json.skip();
				}

				// convert to string
				String text = buffer.toString();

				// convert null / undefined to null
				if (text.equals("null") || text.equals("undefined")) {
					return null;
				}

				// convert hexdecimal numbers
				if (text.length() >= 2 && text.charAt(0) == '0' && text.charAt(1) == 'x') {
					return Integer.toString(Integer.parseInt(text.substring(2), 16));
				}
				return text;
			}
		}
		return null;
	}


	/**
	 * Convert a date to json representation
	 *
	 * @param date date to convert
	 * @return json representation or null if none
	 */
	protected static String toJsonDate(Date date) {
		return toJsonString(df1.get().format(date), '"');
	}

	/**
	 * Convert a string to json representation ("quoted")
	 *
	 * @param str string to convert
	 * @param quote quote type
	 * @return json representation or null if none
	 */
	protected static String toJsonString(String str, char quote) {
		if (str != null) {
			// quote string
			StringBuilder buffer = new StringBuilder(str.length() + 2);
			char p = 0;

			buffer.append(quote);
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);

				switch (c) {
				case '\\':
					buffer.append("\\\\");
					break;

				case '"':
				case '\'':
					if (c == quote) {
						buffer.append('\\');
					}
					buffer.append(c);
					break;

				case '\b':
					buffer.append("\\b");
					break;

				case '\f':
					buffer.append("\\f");
					break;

				case '\n':
					buffer.append("\\n");
					break;

				case '\r':
					buffer.append("\\r");
					break;

				case '\t':
					buffer.append("\\t");
					break;

				case '/':
					if (p == '<') {
						buffer.append('\\');
					}
					buffer.append(c);
					break;

				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
						String code = "000" + Integer.toHexString(c);

						buffer.append("\\u" + code.substring(code.length() - 4));
						break;
					}
					buffer.append(c);
					break;
				}
				p = c;
			}
			buffer.append(quote);
			return buffer.toString();
		}
		return "null";
	}


	/**
	 * Fetch a single json entity
	 *
	 * @param json json representation or null if none
	 * @return json entity or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	private static JsonWrapper visitJson(Tokenizer json) {
		if (json.hasNext()) {
			// parse value
			char c = json.peek();

			if (c == '_' || c == '-' || c == '.' || Character.isLetterOrDigit(c)) {
				// consume all alpha-numeric characters (and '_','.', '-')
				StringBuilder buffer = new StringBuilder();

				buffer.append(json.next());
separator:		while (json.hasNext()) {
					c = json.peek();
					switch (c) {
					case '_':
					case '-':
					case '.':
						break;

					default:
						if (!Character.isLetterOrDigit(c)) {
							break separator;
						}
						break;
					}
					buffer.append(c);
					json.skip();
				}

				// convert hexdecimal numbers
				if (buffer.length() >= 2 && buffer.charAt(0) == '0' && buffer.charAt(1) == 'x') {
					return new JsonNumericValue(Integer.parseInt(buffer.substring(2), 16));
				}
				return new JsonValue(buffer.toString());
			}
			if (c == '\'' || c == '"') {
				// string literal
				StringBuilder buffer = new StringBuilder();
				char quote = json.next();

				buffer.append(quote);
				while (json.hasNext()) {
					c = json.next();
					switch (c) {
					case '\n':
					case '\r':
						throw new IllegalStateException("unclosed string in json source (" + json + ")");

					case '\\':
						buffer.append(c);
						buffer.append(json.next());
						break;

					case '\'':
					case '"':
						if (c == quote) {
							buffer.append(c);
							return new JsonValue(buffer.toString());
//							return new JsonStringValue(buffer.toString());
						}

					default:
						buffer.append(c);
						break;
					}
				}
				throw new IllegalStateException("unclosed string in json source (" + json + ")");
			}
			if (c == '[') {
				return visitArray(json);
			}
			if (c == '{') {
				return visitObject(json);
			}
		}
		return null;
	}

	/**
	 * Visit a json array
	 *
	 * @param json json representation or null if none
	 * @return parsed array or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static JsonArray visitArray(Tokenizer json) {
		if (json.hasNext() && json.allowNext('[')) {
			List<JsonWrapper> items = new ArrayList<JsonWrapper>();
			boolean added = false;

			// array literal
			while (json.hasNext()) {
				char c = json.peek();

				// skip whitespace
				json.skipWhitespace();

				// end of array definition
				if (json.allowNext(']')) {
					return new JsonArray(items);
				}

				// allow empty elements
				if (json.allowNext(',')) {
					if (!added) {
						items.add(null);
					}
					continue;
				}

				// parse value
				JsonWrapper value = visitJson(json);

				if (value == null) {
					throw new IllegalStateException("null array item value (" + json + ")");
				}
				items.add(value);
				added = true;
			}
			throw new IllegalStateException("unclosed array literal in json source (" + json + ")");
		}
		return null;
	}

	/**
	 * Visit a json object
	 *
	 * @param json json representation or null if none
	 * @return parsed object or null if none
	 * @throws IllegalStateException if source contains an error
	 */
	protected static JsonObject visitObject(Tokenizer json) {
		if (json.hasNext() && json.allowNext('{')) {
			Map<String, JsonWrapper> items = new TreeMap<String, JsonWrapper>();

			// object literal
			while (json.hasNext()) {
				char c = json.peek();

				// skip whitespace
				json.skipWhitespace();

				// end of array definition
				if (json.allowNext('}')) {
					return new JsonObject(items);
				}

				// allow elements separator
				if (json.allowNext(',')) {
					continue;
				}

				// parse key
				String key = evalString(json);

				if (key == null) {
					throw new IllegalStateException("null object property name (" + json + ")");
				}

				// skip whitespace
				json.skipWhitespace();

				// allow empty elements
				if (!json.allowNext(':')) {
					throw new IllegalStateException("object property must be followed by ':' (" + json + ")");
				}

				// skip whitespace
				json.skipWhitespace();

				// parse value
				JsonWrapper value = visitJson(json);

				if (value == null) {
					throw new IllegalStateException("null object property value (" + json + ")");
				}
				items.put(key, value);
			}
			throw new IllegalStateException("unclosed object literal in json source (" + json + ")");
		}
		return null;
	}


	/**
	 * Check if a string is a valid number
	 *
	 * @param str string to check
	 * @return true if it is a number, false otherwise
	 */
	protected static boolean isNumber(String str) {
		boolean hasDot = false;

		if (str.length() == 0) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			switch (c) {
			case '.':
				if (hasDot) {
					return false;
				}
				hasDot = true;
				break;

			case '-':
				if (i != 0) {
					return false;
				}
				break;

			default:
				if (!Character.isDigit(c)) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	/**
	 * Check if a string is a valid identifier
	 *
	 * @param str string to check
	 * @return true if it is a identifier, false otherwise
	 */
	protected static boolean isIdentifier(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(str.charAt(0))) {
			return false;
		}
		if (isKeyword(str)) {
			return false;
		}
		for (int i = 1; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if a string is a keyword
	 *
	 * @param str string to check
	 * @return true if it is a keyword, false otherwise
	 */
	protected static boolean isKeyword(String str) {
		if (str.equals("class") ||
			str.equals("if") ||
			str.equals("else") ||
			str.equals("do") ||
			str.equals("while") ||
			str.equals("for") ||
			str.equals("in") ||
			str.equals("with") ||
			str.equals("break") ||
			str.equals("continue") ||
			str.equals("return") ||
			str.equals("try") ||
			str.equals("catch") ||
			str.equals("finally") ||
			str.equals("throw") ||
			str.equals("function") ||
			str.equals("typeof") ||
			str.equals("delete") ||
			str.equals("new") ||
			str.equals("prototype") ||
			str.equals("null") ||
			str.equals("undefined") ||
			str.equals("true") ||
			str.equals("false") ||
			str.equals("switch") ||
			str.equals("case") ||
			str.equals("default")) {
			return true;
		}
		return false;
	}


	/**
	 * Simple json source tokenizer
	 *
	 * @author nitro
	 */
	protected static class Tokenizer {
		/** Json source */
		private String source;

		/** Json offset */
		private int offset = 0;


		/**
		 * Constructor
		 *
		 * @param source json source
		 */
		protected Tokenizer(String source) {
			this.source = source;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			int start = offset - 16;
			int end = offset + 16;

			if (start < 0) {
				start = 0;
			}
			if (end > source.length()) {
				end = source.length();
			}
			buffer.append("@" + offset + " ");
			if (start > 0) {
				buffer.append("[...]");
			}
			if (start < offset) {
				buffer.append(source.substring(start, offset));
			}
			buffer.append("><");
			if (end > offset) {
				buffer.append(source.substring(offset, end));
			}
			if (end < source.length()) {
				buffer.append("[...]");
			}
			return buffer.toString();
		}


		/**
		 * Get source offset
		 *
		 * @return source offset
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * Get source length
		 *
		 * @return source length
		 */
		public int length() {
			return source.length();
		}

		/**
		 * Check if a delta offset is within bound
		 *
		 * @param i delta offset
		 * @return true if available, false if out of bound
		 */
		public boolean has(int i) {
			i += offset;
			if (i >= 0 && i < source.length()) {
				return true;
			}
			return false;
		}

		/**
		 * Check if a next character is available
		 *
		 * @return true if available, false otherwise
		 */
		public boolean hasNext() {
			return (offset < source.length());
		}

		/**
		 * Get next character available
		 *
		 * @return next character
		 * @throws IllegalStateException if end of source is reached
		 */
		public char next() {
			if (offset < source.length()) {
				return source.charAt(offset++);
			}
			throw new IllegalStateException("unexpected end of source");
		}

		/**
		 * Get next character string
		 *
		 * @param length total string length to fetch
		 * @return next character string
		 * @throws IllegalStateException if end of source is reached
		 */
		public String nextString(int length) {
			StringBuilder str = new StringBuilder(length);

			for (int i = 0; i < length; i++) {
				str.append(next());
			}
			return str.toString();
		}

		/**
		 * Skip next character
		 *
		 * @throws IllegalStateException if end of source is reached
		 */
		public void skip() {
			skip(1);
		}

		/**
		 * Skip a group of next characters
		 *
		 * @param i delta offset
		 * @throws IllegalStateException if end of source is reached
		 */
		public void skip(int i) {
			i += offset;
			if (i >= 0 && i <= source.length()) {
				offset = i;
			} else {
				throw new IllegalStateException("unexpected end of source");
			}
		}

		/**
		 * Peek next character available (without changing internal state)
		 *
		 * @return next character
		 * @throws IllegalStateException if end of source is reached
		 */
		public char peek() {
			return peek(0);
		}

		/**
		 * Peek next character available (without changing internal state)
		 *
		 * @param i delta offset
		 * @return next character at given delta
		 * @throws IllegalStateException if end of source is reached
		 */
		public char peek(int i) {
			i += offset;
			if (i >= 0 && i < source.length()) {
				return source.charAt(i);
			}
			throw new IllegalStateException("unexpected end of source");
		}


		/**
		 * Consume next character available if match
		 *
		 * @param c character to match
		 * @return true if match, false otherwise
		 * @throws IllegalStateException if end of source is reached
		 */
		public boolean allowNext(char c) {
			if (peek() == c) {
				skip();
				return true;
			}
			return false;
		}

		/**
		 * Skip whitespace
		 *
		 */
		public void skipWhitespace() {
			char c = peek();

			while (Character.isWhitespace(c)) {
				skip();
				c = peek();
			}
		}
	}


	/**
	 * Test program
	 *
	 */
	public static void main(String [] args) throws Exception {
		FileReader r = new FileReader(args[0]);
		StringBuilder buffer = new StringBuilder();
		int br;

		while ((br = r.read()) >= 0) {
			buffer.append((char)br);
		}

		System.out.println(eval(buffer.toString()));
	}
}
