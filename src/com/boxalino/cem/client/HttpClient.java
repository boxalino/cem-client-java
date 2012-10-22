package com.boxalino.cem.client;

import java.lang.reflect.Array;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


/**
 * Http client
 *
 * @author nitro
 */
public class HttpClient {
	/**
	 * Http header entry
	 *
	 * @author nitro
	 */
	public static class Header {
		/** Header name */
		public final String name;

		/** Header value */
		public final String value;


		/**
		 * Constructor
		 *
		 * @param name header name
		 * @param value header value
		 */
		public Header(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}


	/**
	 * Build complete URL
	 *
	 * @param url base url
	 * @return full url
	 * @throws Exception if any error occured
	 */
	public static URL buildURL(String url) throws Exception {
		return buildURL(url, (Map<String, String[]>)null, null);
	}

	/**
	 * Build complete URL
	 *
	 * @param url base url
	 * @param parameters optional parameters
	 * @param fragment optional fragment
	 * @return full url
	 * @throws Exception if any error occured
	 */
	public static URL buildURL(String url, String [][] parameters, String fragment) throws Exception {
		Map<String, String[]> _parameters = null;

		if (parameters != null) {
			_parameters = new LinkedHashMap<String, String[]>();
			for (String [] parameter : parameters) {
				for (int i = 1; i < parameter.length; i++) {
					add(String.class, _parameters, parameter[0], parameter[i]);
				}
			}
		}
		return buildURL(url, _parameters, fragment);
	}

	/**
	 * Build complete URL
	 *
	 * @param url base url
	 * @param parameters optional parameters
	 * @param fragment optional fragment
	 * @return full url
	 * @throws Exception if any error occured
	 */
	public static URL buildURL(String url, Map<String, String[]> parameters, String fragment) throws Exception {
		StringBuilder buffer = new StringBuilder();
		URL _url = (url != null && url.length() > 0) ? new URL(url) : null;

		if (_url != null) {
			buffer.append(_url.getProtocol());
			buffer.append("://");
			if (_url.getUserInfo() != null) {
				buffer.append(_url.getUserInfo());
				buffer.append('@');
			}
			buffer.append(_url.getHost());
			if (_url.getPort() >= 0 && _url.getPort() != _url.getDefaultPort()) {
				buffer.append(":" + _url.getPort());
			}
			buffer.append(_url.getPath());
			if (_url.getQuery() != null) {
				for (String part : _url.getQuery().split("&")) {
					int i = part.indexOf('=');

					if (i > 0) {
						add(
							String.class,
							parameters,
							URLDecoder.decode(part.substring(0, i), "UTF-8"),
							URLDecoder.decode(part.substring(i + 1), "UTF-8")
						);
					}
				}
			}
		}
		if (parameters.size() > 0) {
			int i = 0;

			buffer.append('?');
			for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
				for (String value : entry.getValue()) {
					if (i++ > 0) {
						buffer.append('&');
					}
					buffer.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
					buffer.append('=');
					buffer.append(URLEncoder.encode(value, "UTF-8"));
				}
			}
		}
		if (_url != null && _url.getRef() != null) {
			buffer.append('#');
			buffer.append(_url.getRef());
		}
		return new URL(buffer.toString());
	}


	/** Authentication username */
	public String username = null;

	/** Authentication password */
	public String password = null;

	/** Connect timeout [ms] */
	public int connectTimeout = 0;

	/** Maximum connect tries */
	public int connectMaxTries = 0;

	/** Read timeout [ms] */
	public int readTimeout = 0;

	/** Request headers */
	protected final Map<String, Header[]> requestHeaders = new LinkedHashMap<String, Header[]>();

	/** Process time */
	protected long time = 0;

	/** Response code */
	protected int responseCode = 0;

	/** Response status */
	protected String responseStatus = null;

	/** Response headers */
	protected final Map<String, Header[]> responseHeaders = new LinkedHashMap<String, Header[]>();

	/** Response size */
	protected long size = 0;

	/** Cookies */
	protected final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();


	/**
	 * Constructor
	 *
	 */
	public HttpClient() {
	}


	/**
	 * Add request header
	 *
	 * @param name header name
	 * @param value header value
	 */
	public void addRequestHeader(String name, String value) {
		add(Header.class, requestHeaders, name.toLowerCase(), new Header(name, value));
	}

	/**
	 * Set request header
	 *
	 * @param name header name
	 * @param value header value
	 */
	public void setRequestHeader(String name, String value) {
		requestHeaders.put(name.toLowerCase(), new Header[] { new Header(name, value) });
	}

	/**
	 * Remove request header
	 *
	 * @param name header name
	 */
	public void removeRequestHeader(String name) {
		requestHeaders.remove(name.toLowerCase());
	}

	/**
	 * Set request cluster
	 *
	 * @param cluster cluster identifier
	 */
	public void setRequestCluster(String cluster) {
		if (cluster != null && cluster.length() > 0) {
			setRequestHeader("X-Cem-Cluster", cluster);
		} else {
			removeRequestHeader("X-Cem-Cluster");
		}
	}


	/**
	 * Get total processing time
	 *
	 * @return processing time [ms]
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Get last http code
	 *
	 * @return http code
	 */
	public int getCode() {
		return responseCode;
	}

	/**
	 * Get last http message
	 *
	 * @return http message
	 */
	public String getStatus() {
		return responseStatus;
	}

	/**
	 * Get last http headers
	 *
	 * @return http headers
	 */
	public Map<String, Header[]> getHeaders() {
		return Collections.unmodifiableMap(responseHeaders);
	}

	/**
	 * Get cookies
	 *
	 * @return cookie names
	 */
	public Set<String> getCookies() {
		Set<String> set = new LinkedHashSet<String>();

		for (Map.Entry<String, Cookie> entry : cookies.entrySet()) {
			if ("remote".equals(entry.getValue().getMeta())) {
				set.add(entry.getKey());
			}
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Get last http body size
	 *
	 * @return http body size
	 */
	public long getSize() {
		return size;
	}


	/**
	 * Get cookie
	 *
	 * @param name cookie name
	 * @return cookie or null if none
	 */
	public Cookie getCookie(String name) {
		return cookies.get(name);
	}

	/**
	 * Set cookie
	 *
	 * @param cookie cookie
	 */
	public void setCookie(Cookie cookie) {
		cookies.put(cookie.getName(), cookie);
	}

	/**
	 * Remove cookie
	 *
	 * @param name cookie name
	 */
	public void removeCookie(String name) {
		cookies.remove(name);
	}


	/**
	 * Do a GET request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int get(String url, String [][] parameters, Header [] headers) throws Exception {
		process("GET", HttpClient.buildURL(url, parameters, null), headers, null);
		return responseCode;
	}

	/**
	 * Do a GET request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int get(String url, Map<String, String[]> parameters, Header [] headers) throws Exception {
		process("GET", HttpClient.buildURL(url, parameters, null), headers, null);
		return responseCode;
	}

	/**
	 * Do a PUT request
	 *
	 * @param url http url
	 * @param contentType request content-type
	 * @param is request body
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int put(String url, String contentType, InputStream is, Header [] headers) throws Exception {
		headers = Arrays.copyOf(headers, headers.length + 1);
		headers[headers.length - 1] = new Header("Content-Type", contentType);
		process("PUT", HttpClient.buildURL(url), headers, is);
		return responseCode;
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param contentType request content-type
	 * @param is request body
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int post(String url, String contentType, InputStream is, Header [] headers) throws Exception {
		headers = Arrays.copyOf(headers, headers.length + 1);
		headers[headers.length - 1] = new Header("Content-Type", contentType);
		process("POST", HttpClient.buildURL(url), headers, is);
		return responseCode;
	}

	/**
	 * Do a POST request (UTF-8)
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int postFields(String url, String [][] parameters, Header [] headers) throws Exception {
		return postFields(url, parameters, "UTF-8", headers);
	}

	/**
	 * Do a POST request (UTF-8)
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int postFields(String url, Map<String, String[]> parameters, Header [] headers) throws Exception {
		return postFields(url, parameters, "UTF-8", headers);
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param charset request character set
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int postFields(String url, String [][] parameters, String charset, Header [] headers) throws Exception {
		StringBuilder body = new StringBuilder();

		if (parameters != null) {
			int j = 0;

			for (String [] parameter : parameters) {
				for (int i = 1; i < parameter.length; i++) {
					if (j++ > 0) {
						body.append('&');
					}
					body.append(URLEncoder.encode(parameter[0], charset));
					body.append('=');
					body.append(URLEncoder.encode(parameter[i], charset));
				}
			}
		}
		headers = Arrays.copyOf(headers, headers.length + 1);
		headers[headers.length - 1] = new Header("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
		process(
			"POST",
			HttpClient.buildURL(url),
			headers,
			new ByteArrayInputStream(body.toString().getBytes(charset))
		);
		return responseCode;
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param charset request character set
	 * @param headers optional http headers
	 * @return last http code
	 * @throws Exception if any error occured
	 */
	public int postFields(String url, Map<String, String[]> parameters, String charset, Header [] headers) throws Exception {
		StringBuilder body = new StringBuilder();

		if (parameters != null) {
			int i = 0;

			for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
				for (String value : parameter.getValue()) {
					if (i++ > 0) {
						body.append('&');
					}
					body.append(URLEncoder.encode(parameter.getKey(), charset));
					body.append('=');
					body.append(URLEncoder.encode(value, charset));
				}
			}
		}
		headers = Arrays.copyOf(headers, headers.length + 1);
		headers[headers.length - 1] = new Header("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
		process(
			"POST",
			HttpClient.buildURL(url),
			headers,
			new ByteArrayInputStream(body.toString().getBytes(charset))
		);
		return responseCode;
	}


	/**
	 * Process http request
	 *
	 * @param method http method
	 * @param url http url
	 * @param headers optional request headers
	 * @param is optional request body
	 * @throws Exception if any error occurs
	 */
	public void process(String method, URL url, Header [] headers, InputStream is) throws Exception {
		long time = System.currentTimeMillis();

		responseCode = 0;
		responseStatus = null;
		responseHeaders.clear();

		// set base options
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		connection.setRequestMethod(method);
		connection.setDoInput(is != null);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
//		connection.setIfModifiedSince(ts);
		connection.setAllowUserInteraction(false);
		connection.setInstanceFollowRedirects(true);
//		connection.setChunkedStreamingMode(4096);
//		connection.setFixedLengthStreamingMode(4096);

		// set timeouts
		connection.setConnectTimeout(connectTimeout);
		connection.setReadTimeout(readTimeout);

		// set http authentication
		if (url.getUserInfo() != null && url.getUserInfo().length() > 0) {
			connection.setRequestProperty("Authorization", "Basic " + Base64Encoder.encodeUtf8(url.getUserInfo()));
		} else if (username != null && username.length() > 0 && password != null) {
			connection.setRequestProperty("Authorization", "Basic " + Base64Encoder.encodeUtf8(username + ":" + password));
		}

		// set headers
		for (Header [] list : requestHeaders.values()) {
			for (Header header : list) {
				connection.setRequestProperty(header.name, header.value);
			}
		}
		for (Header header : headers) {
			connection.setRequestProperty(header.name, header.value);
		}

		// set cookie if any
		String cookieHeader = buildCookieHeader();

		if (cookieHeader != null) {
			connection.setRequestProperty("Cookie", cookieHeader);
		}

		// connect to server
		connection.connect();

		// stream request body
		if (is != null) {
			OutputStream os = connection.getOutputStream();
			byte[] buffer = new byte[4096];
			int br;

			try {
				while ((br = is.read(buffer)) >= 0) {
					os.write(buffer);
				}
			} finally {
				os.close();
			}
		}

		// parse response
		responseCode = connection.getResponseCode();
		responseStatus = connection.getResponseMessage();
		for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			Header [] list = new Header[entry.getValue().size()];
			int i = 0;

			for (String value : entry.getValue()) {
				list[i++] = new Header(entry.getKey().toLowerCase(), value);
			}
			responseHeaders.put(entry.getKey().toLowerCase(), list);

			if ("set-cookie".equalsIgnoreCase(entry.getKey()) || "set-cookie2".equalsIgnoreCase(entry.getKey())) {
				for (String value : entry.getValue()) {
					for (Cookie cookie : parseCookieHeader(value)) {
						cookie.setMeta("remote");
						setCookie(cookie);
					}
				}
			}
		}

		// stream response body
		is = connection.getInputStream();
		if (is != null) {
			try {
				parseResponse(new CountingInputStream(is));
			} finally {
				is.close();
			}
		}

		this.time = System.currentTimeMillis() - time;
	}


	/** Netscape's expires format */
	private static final String [] expiresFormats = new String [] {
		"EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
		"EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
		"EEE MMM dd yyyy HH:mm:ss 'GMT'Z"
	};


	/**
	 * Get cookie header
	 *
	 * @return cookie header or null if none
	 */
	public String buildCookieHeader() {
		if (cookies.size() > 0) {
			StringBuilder buffer = new StringBuilder();

			for (Cookie cookie : cookies.values()) {
				if (buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(cookie.toString());
			}
			return buffer.toString();
		}
		return null;
	}

	/**
	 * Parse cookie header
	 *
	 * @param header cookie header
	 * @return parsed cookies
	 */
	public List<Cookie> parseCookieHeader(String header) {
		List<Cookie> list = new ArrayList<Cookie>();

		// segment http header and find version
		int version = Cookie.VERSION_0;
		int index = header.indexOf(':');
		String headerValue = header;
		String headerValueLower;

		if (index > 0 && index < 15) {
			// check header type
			String headerName = header.substring(0, index).toLowerCase();

			if (!headerName.equals("set-cookie") && !headerName.equals("set-cookie2")) {
				return Collections.unmodifiableList(list);
			}
			headerValue = header.substring(index + 1).trim();
		}
		headerValueLower = headerValue.toLowerCase();
		if (headerValueLower.indexOf("version=") > 0 || headerValueLower.indexOf("max-age=") > 0) {
			version = Cookie.VERSION_1;
		}

		// split cookie(s)
		List<String> definitions = new ArrayList<String>();
		int offset = 0;

		switch (version) {
		case Cookie.VERSION_0:
			definitions.add(headerValue.trim());
			break;

		case Cookie.VERSION_1:
			for (int i = 0; i < headerValue.length(); i++) {
				char c = headerValue.charAt(i);
				boolean single = false;

				switch (c) {
				case '\'':
					single = true;
				case '"':
					i++;
					while (i < headerValue.length()) {
						c = headerValue.charAt(i++);
						if ((single && c == '\'') || (!single && c == '"')) {
							break;
						}
					}
					if ((single && c != '\'') || (!single && c != '"')) {
						throw new IllegalStateException("invalid cookie header: " + header);
					}
					break;

				case ',':
					if (offset < i) {
						definitions.add(headerValue.substring(offset, i).trim());
					}
					offset = i + 1;
					break;
				}
			}
			if (offset < headerValue.length()) {
				definitions.add(headerValue.substring(offset).trim());
			}
			break;
		}

		// parse cookie(s)
		for (String definition : definitions) {
			// split cookie's pair(s)
			List<String> pairs = new ArrayList<String>();

			offset = 0;
			for (int i = 0; i < definition.length(); i++) {
				char c = definition.charAt(i);
				boolean single = false;

				switch (c) {
				case '\'':
					single = true;
				case '"':
					i++;
					while (i < definition.length()) {
						c = definition.charAt(i++);
						if ((single && c == '\'') || (!single && c == '"')) {
							break;
						}
					}
					if ((single && c != '\'') || (!single && c != '"')) {
						throw new IllegalStateException("invalid cookie header: " + header);
					}
					break;

				case ';':
					if (offset < i) {
						pairs.add(definition.substring(offset, i).trim());
					}
					offset = i + 1;
					break;
				}
			}
			if (offset < definition.length()) {
				pairs.add(definition.substring(offset).trim());
			}
			if (pairs.size() == 0) {
				throw new IllegalStateException("invalid cookie header: " + header);
			}

			// parse cookie's pair(s)
			String name = null;
			String value = null;
			String domain = null;
			String portList = null;
			String path = null;
			String comment = null;
			String commentURL = null;
			long maxAge = -1;
			boolean discard = false;
			boolean secure = false;

			for (int i = 0; i < pairs.size(); i++) {
				String pair = pairs.get(i);
				String pairName = null;
				String pairValue = null;

				index = pair.indexOf('=');
				if (index > 0) {
					pairName = pair.substring(0, index).trim();
					pairValue = pair.substring(index + 1).trim();
					if (pairValue.length() >= 2 && pairValue.charAt(0) == '\'' && pairValue.charAt(pairValue.length() - 1) == '\'') {
						pairValue = pairValue.substring(1, pairValue.length() - 2);
					} else if (pairValue.length() >= 2 && pairValue.charAt(0) == '"' && pairValue.charAt(pairValue.length() - 1) == '"') {
						pairValue = pairValue.substring(1, pairValue.length() - 2);
					}
				} else {
					pairName = pair;
				}

				if (i == 0) {
					// first pair is the cookie name=value
					name = pairName;
					value = pairValue;
				} else {
					// other pair(s) are attributes
					pairName = pairName.toLowerCase();
					if (pairName.equals("domain")) {
						domain = pairValue;
					} else if (pairName.equals("port")) {
						portList = pairValue != null ? pairValue : "";
					} else if (pairName.equals("path")) {
						path = pairValue;
					} else if (pairName.equals("comment")) {
						comment = pairValue;
					} else if (pairName.equals("commenturl")) {
						commentURL = pairValue;
					} else if (pairName.equals("max-age")) {
						maxAge = Long.parseLong(pairValue);
					} else if (pairName.equals("expires")) {
						for (String format : expiresFormats) {
							SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);

							df.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
							try {
								maxAge = df.parse(pairValue).getTime() / 1000l;
							} catch (ParseException e) { }
						}
					} else if (pairName.equals("discard")) {
						discard = true;
					} else if (pairName.equals("secure")) {
						secure = true;
					} else if (pairName.equals("version")) {
//						version = Integer.parseInt(pairValue);
					}
				}
			}
			list.add(
				new Cookie(name, value, domain, portList, path, comment, commentURL, version, maxAge, discard, secure)
			);
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Called to parse response
	 *
	 * @param is body stream
	 * @throws Exception if any error occurs
	 */
	public void parseResponse(InputStream is) throws Exception {
	}


	/**
	 * Add a value in existing multi-map
	 *
	 * @param clazz value class
	 * @param map value map
	 * @param key key
	 * @param value value
	 */
	@SuppressWarnings("unchecked")
	protected static <T> void add(Class<T> clazz, Map<String, T[]> map, String key, T value) {
		T [] list = map.get(key);

		if (list == null) {
			list = (T[])Array.newInstance(clazz, 1);
		} else {
			list = Arrays.copyOf(list, list.length + 1);
		}
		list[list.length - 1] = value;
		map.put(key, list);
	}


	/**
	 * InputStream wrapper that counts how many bytes are read
	 *
	 * @author nitro
	 */
	private class CountingInputStream extends FilterInputStream {
		/**
		 * Constructor
		 *
		 * @param in underlying stream
		 */
		public CountingInputStream(InputStream in) {
			super(in);
			HttpClient.this.size = 0;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read() throws IOException {
			int s = in.read();

			if (s >= 0) {
				HttpClient.this.size++;
			}
			return s;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte [] b) throws IOException {
			int s = in.read(b);

			if (s >= 0) {
				HttpClient.this.size += s;
			}
			return s;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte [] b, int off, int len) throws IOException {
			int s = in.read(b, off, len);

			if (s >= 0) {
				HttpClient.this.size += s;
			}
			return s;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long skip(long n) throws IOException {
			long s = in.skip(n);

			if (s >= 0) {
				HttpClient.this.size += s;
			}
			return s;
		}
	}
}