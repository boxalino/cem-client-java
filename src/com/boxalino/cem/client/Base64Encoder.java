package com.boxalino.cem.client;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;


/**
 * Base64 encoding/decoding
 *
 * @author nitro
 */
class Base64Encoder {
	/** Base64 encoding table */
	private static char [] b64EncodeTable = new char[] {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};

	/** Base64 decoding table */
	private static int [] b64DecodeTable = new int[127];

	/** Base64 table initialization */
	static {
		for (int i = 0; i < 127; i++) {
			b64DecodeTable[i] = -1;
		}
		for (int i = 0; i < 64; i++) {
			b64DecodeTable[(b64EncodeTable[i] & 0x7f)] = i;
		}
	}


	/**
	 * Constructor
	 *
	 */
	private Base64Encoder() {
	}


	/**
	 * Encode input as base64 (utf-8 string)
	 *
	 * @param input string data
	 * @return encoded text
	 */
	public static String encodeUtf8(String input) {
		try {
			return encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decode input in base64 (utf-8 string)
	 *
	 * @param input base64 input
	 * @return string data
	 */
	public static String decodeUtf8(String input) {
		try {
			return decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Encode input as base64 (string)
	 *
	 * @param input string data
	 * @param charsetName charset name
	 * @return encoded text
	 * @throws UnsupportedEncodingException if charset is not supported
	 */
	public static String encode(String input, String charsetName) throws UnsupportedEncodingException {
		return encode(input.getBytes(charsetName));
	}

	/**
	 * Decode input in base64 (string)
	 *
	 * @param input base64 input
	 * @param charsetName charset name
	 * @return string data
	 * @throws UnsupportedEncodingException if charset is not supported
	 */
	public static String decode(String input, String charsetName) throws UnsupportedEncodingException {
		return new String(decode(input), charsetName);
	}


	/**
	 * Encode input as base64
	 *
	 * @param input raw data
	 * @return encoded text
	 */
	public static String encode(byte [] input) {
		StringBuilder buffer = new StringBuilder();
		int current = 0;
		int bits = 0;

		for (int i = 0; i < input.length; i++) {
			int value = input[i] & 0xff;

			if (value >= 0 && value <= 0xff) {
				current |= value << (16 - bits);
				bits += 8;

				if (bits == 24) {
					buffer.append(b64EncodeTable[(current >> 18) & 0x3f]);
					buffer.append(b64EncodeTable[(current >> 12) & 0x3f]);
					buffer.append(b64EncodeTable[(current >> 6) & 0x3f]);
					buffer.append(b64EncodeTable[(current >> 0) & 0x3f]);

					bits = 0;
					current = 0;
				}
			}
		}
		switch (bits) {
		case 8:
			buffer.append(b64EncodeTable[(current >> 18) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 12) & 0x3f]);
			buffer.append('=');
			buffer.append('=');
			break;

		case 16:
			buffer.append(b64EncodeTable[(current >> 18) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 12) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 6) & 0x3f]);
			buffer.append('=');
			break;

		case 24:
			buffer.append(b64EncodeTable[(current >> 18) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 12) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 6) & 0x3f]);
			buffer.append(b64EncodeTable[(current >> 0) & 0x3f]);
			break;
		}
		return buffer.toString();
	}

	/**
	 * Decode input in base64
	 *
	 * @param input base64 input
	 * @return raw data
	 */
	public static byte [] decode(String input) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int current = 0;
		int bits = 0;

		for (int i = 0; i < input.length(); i++) {
			int value = input.charAt(i);

			if (value >= 0 && value <= 0x7f && b64DecodeTable[value] >= 0) {
				current |= (b64DecodeTable[value] & 0x3f) << (18 - bits);
				bits += 6;

				if (bits == 24) {
					buffer.write((current >> 16) & 0xff);
					buffer.write((current >> 8) & 0xff);
					buffer.write((current >> 0) & 0xff);

					bits = 0;
					current = 0;
				}
			}
		}
//		System.out.println("bits:" + bits + ", current=" + Integer.toString(current, 16));
		switch (bits) {
		case 0:
//		case 6:
			break;

		case 12:
			buffer.write((current >> 16) & 0xff);
			break;

		case 18:
			buffer.write((current >> 16) & 0xff);
			buffer.write((current >> 8) & 0xff);
			break;

		case 24:
			buffer.write((current >> 16) & 0xff);
			buffer.write((current >> 8) & 0xff);
			buffer.write((current >> 0) & 0xff);
			break;

		default:
			throw new IllegalStateException("invalid base64-encoded data (" + bits + ")");
		}
		return buffer.toByteArray();
	}
}
