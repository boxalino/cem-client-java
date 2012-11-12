package com.boxalino.cem.client;

import java.lang.reflect.Array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.SocketTimeoutException;

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
		 */
		private Header() {
			this(null, null);
		}

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


		/**
		 * Compute a hash code for this object.
		 *
		 * @return hash code
		 */
		@Override
		public int hashCode() {
			int hash = 11;

			hash = 31 * hash + name.hashCode();
			return hash;
		}

		/**
		 * Test if given object is equal to this.
		 *
		 * @param o other object to test
		 * @return true if other object is equal (name, value)
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof Header) {
				Header h = (Header)o;

				return (name.equals(h.name) && value.equals(h.value));
			}
			return false;
		}


		/**
		 * Return a string representation of this object for debug purpose.
		 *
		 * @return string representation
		 */
		@Override
		public String toString() {
			return (
				"{name=" + name +
				",value=" + value + "}"
			);
		}
	}

	/**
	 * Http cookie entry
	 *
	 * @author nitro
	 */
	public static class Cookie {
		/** Netscape draft */
		private static final int VERSION_0 = 0;

		/** RFC 2109/2965 */
		private static final int VERSION_1 = 1;


		/** Name */
		private final String name;

		/** Value */
		private String value;

		/** Domain */
		private String domain;

		/** Port list */
		private String portList;

		/** Path */
		private String path;

		/** Comment */
		private String comment;

		/** Comment URL */
		private String commentURL;

		/** Version (0,1) */
		private int version;

		/** Max age */
		private long maxAge;

		/** Discard flag */
		private boolean discard;

		/** Secure flag */
		private boolean secure;

		/** Meta-data */
		private String meta = null;


		/**
		 * Constructor
		 *
		 */
		private Cookie() {
			this(null, null);
		}

		/**
		 * Constructor
		 *
		 * @param name cookie name
		 * @param value cookie value
		 */
		public Cookie(String name, String value) {
			this(name, value, null, null, null, null, null, VERSION_0, -1, false, false);
		}

		/**
		 * Constructor
		 *
		 * @param cookie servlet cookie
		 */
		public Cookie(javax.servlet.http.Cookie cookie) {
			this(
				cookie.getName(),
				cookie.getValue(),
				cookie.getDomain(),
				null,
				cookie.getPath(),
				cookie.getComment(),
				null,
				cookie.getVersion(),
				cookie.getMaxAge(),
				false,
				cookie.getSecure()
			);
		}

		/**
		 * Constructor
		 *
		 * @param name cookie name
		 * @param value cookie value
		 * @param domain domain
		 * @param portList port list
		 * @param path path
		 * @param comment comment
		 * @param commentURL comment url
		 * @param version version
		 * @param maxAge maximum age
		 * @param discard discard
		 * @param secure secure
		 */
		public Cookie(String name, String value, String domain, String portList, String path, String comment, String commentURL, int version, long maxAge, boolean discard, boolean secure) {
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.portList = portList;
			this.path = path;
			this.comment = comment;
			this.commentURL = commentURL;
			this.version = version;
			this.maxAge = maxAge;
			this.discard = discard;
			this.secure = secure;
		}


		/**
		 * Compute a hash code for this object.
		 *
		 * @return hash code
		 */
		@Override
		public int hashCode() {
			int hash = 11;

			hash = 31 * hash + name.hashCode();
			return hash;
		}

		/**
		 * Test if given object is equal to this.
		 *
		 * @param o other object to test
		 * @return true if other object is equal (name, value, domain, portList, path, comment, commentURL, version, maxAge, discard, secure)
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof Cookie) {
				Cookie c = (Cookie)o;

				return (
					name.equals(c.name) &&
					value.equals(c.value) &&
					((domain != null && domain.equals(c.domain)) || (domain == null && c.domain == null)) &&
					((portList != null && portList.equals(c.portList)) || (portList == null && c.portList == null)) &&
					((path != null && path.equals(c.path)) || (path == null && c.path == null)) &&
					((comment != null && comment.equals(c.comment)) || (comment == null && c.comment == null)) &&
					((commentURL != null && commentURL.equals(c.commentURL)) || (commentURL == null && c.commentURL == null)) &&
					version == c.version &&
					maxAge == c.maxAge &&
					discard == c.discard &&
					secure == c.secure
				);
			}
			return false;
		}

		/**
		 * Return a string representation of the cookie compatible with http headers.
		 *
		 * @return string representation
		 */
		@Override
		public String toString() {
			if (version > 0) {
				StringBuilder buffer = new StringBuilder();

				buffer.append(name);
				buffer.append("=\"");
				buffer.append(value);
				buffer.append('"');
				if (domain != null) {
					buffer.append(";$Domain=\"");
					buffer.append(domain);
					buffer.append('"');
				}
				if (portList != null) {
					buffer.append(";$Port");
					if (portList.length() > 0) {
						buffer.append("=\"");
						buffer.append(portList);
						buffer.append('"');
					}
				}
				if (path != null) {
					buffer.append(";$Path=\"");
					buffer.append(path);
					buffer.append('"');
				}
				return buffer.toString();
			}
			return (name + "=" + value);
		}


		/**
		 * Get cookie name
		 *
		 * @return cookie name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get cookie value
		 *
		 * @return cookie value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Get cookie meta-data
		 *
		 * @return cookie meta-data
		 */
		public String getMeta() {
			return meta;
		}

		/**
		 * set cookie meta-data
		 *
		 * @param meta cookie meta-data
		 */
		public void setMeta(String meta) {
			this.meta = meta;
		}

		/**
		 * Build servlet cookie
		 *
		 * @return servlet cookie
		 */
		public javax.servlet.http.Cookie toCookie() {
			javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(name, value);

/*			if (domain != null) {
				cookie.setDomain(domain);
			}
			if (path != null) {
				cookie.setPath(path);
			}*/
			cookie.setPath("/");
			if (comment != null) {
				cookie.setComment(comment);
			}
			cookie.setVersion(version);
			cookie.setMaxAge((int)(maxAge / 1000));
			cookie.setSecure(secure);
			return cookie;
		}
	}

	/**
	 * Http response callback
	 *
	 * @author nitro
	 */
	public static interface Callback {
		/**
		 * Called before reading response body
		 *
		 * @throws Exception if any error occurs
		 */
		public void beginResponse() throws Exception;

		/**
		 * Called to parse response
		 *
		 * @param is body stream
		 * @throws Exception if any error occurs
		 */
		public void parseResponse(InputStream is) throws Exception;

		/**
		 * Called if an error occurs
		 *
		 * @param e exception
		 */
		public void error(Exception e);
	}


	/**
	 * Build complete URL
	 *
	 * @param url base url
	 * @return full url
	 * @throws Exception if any error occurs
	 */
	public static URL buildURL(String url) throws Exception {
		return buildURL(url, new LinkedHashMap<String, String[]>(), null);
	}

	/**
	 * Build complete URL
	 *
	 * @param url base url
	 * @param parameters optional parameters
	 * @param fragment optional fragment
	 * @return full url
	 * @throws Exception if any error occurs
	 */
	public static URL buildURL(String url, String [][] parameters, String fragment) throws Exception {
		Map<String, String[]> _parameters = new LinkedHashMap<String, String[]>();

		if (parameters != null) {
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
	 * @throws Exception if any error occurs
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


	/** Authentication username, defaults to null */
	public String username = null;

	/** Authentication password, defaults to null */
	public String password = null;

	/** Connect timeout, defaults to 1000 [ms] */
	public int connectTimeout = 1000;

	/** Maximum connect tries, defaults to 5 */
	public int connectMaxTries = 5;

	/** Read timeout, defaults to 15000 [ms] */
	public int readTimeout = 15000;

	/** Request headers */
	private final Map<String, Header[]> requestHeaders = new LinkedHashMap<String, Header[]>();

	/** Connect tries */
	private int connectTries = 0;

	/** Process time */
	private long time = 0;

	/** Response code */
	private int responseCode = 0;

	/** Response status */
	private String responseStatus = null;

	/** Response headers */
	private final Map<String, Header[]> responseHeaders = new LinkedHashMap<String, Header[]>();

	/** Response size */
	private long size = 0;

	/** Cookies */
	private final Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();


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
	 * Get first http header value if any
	 *
	 * @param name header name
	 * @return first value or null if none
	 */
	public String getHeader(String name) {
		return (responseHeaders.containsKey(name.toLowerCase()) ? responseHeaders.get(name.toLowerCase())[0].value : null);
	}

	/**
	 * Get last http headers
	 *
	 * @return http headers
	 */
	public List<Header> getHeaders() {
		List<Header> list = new ArrayList<Header>();

		for (Header [] headers : responseHeaders.values()) {
			for (Header header : headers) {
				list.add(header);
			}
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Get cookies
	 *
	 * @return cookie names
	 */
	public List<Cookie> getCookies() {
		List<Cookie> list = new ArrayList<Cookie>();

		for (Map.Entry<String, Cookie> entry : cookies.entrySet()) {
			if ("remote".equals(entry.getValue().getMeta())) {
				list.add(entry.getValue());
			}
		}
		return Collections.unmodifiableList(list);
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
	 * @param name cookie name
	 * @param value cookie value
	 */
	public void setCookie(String name, String value) {
		cookies.put(name, new Cookie(name, value));
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
	 * Set cookie (from servlet)
	 *
	 * @param cookie cookie
	 */
	public void setCookie(javax.servlet.http.Cookie cookie) {
		cookies.put(cookie.getName(), new Cookie(cookie));
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
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int get(String url, String [][] parameters, Header [] headers, Callback callback) {
		try {
			process("GET", HttpClient.buildURL(url, parameters, null), headers, null, callback);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}

	/**
	 * Do a GET request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int get(String url, Map<String, String[]> parameters, Header [] headers, Callback callback) {
		try {
			process("GET", HttpClient.buildURL(url, parameters, null), headers, null, callback);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}

	/**
	 * Do a PUT request
	 *
	 * @param url http url
	 * @param contentType request content-type
	 * @param is request body
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int put(String url, String contentType, InputStream is, Header [] headers, Callback callback) {
		try {
			headers = add(Header.class, headers, new Header("Content-Type", contentType));
			process("PUT", HttpClient.buildURL(url), headers, is, callback);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param contentType request content-type
	 * @param is request body
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int post(String url, String contentType, InputStream is, Header [] headers, Callback callback) {
		try {
			headers = add(Header.class, headers, new Header("Content-Type", contentType));
			process("POST", HttpClient.buildURL(url), headers, is, callback);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}

	/**
	 * Do a POST request (UTF-8)
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int postFields(String url, String [][] parameters, Header [] headers, Callback callback) {
		return postFields(url, parameters, "UTF-8", headers, callback);
	}

	/**
	 * Do a POST request (UTF-8)
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int postFields(String url, Map<String, String[]> parameters, Header [] headers, Callback callback) {
		return postFields(url, parameters, "UTF-8", headers, callback);
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param charset request character set
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int postFields(String url, String [][] parameters, String charset, Header [] headers, Callback callback) {
		try {
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
			headers = add(Header.class, headers, new Header("Content-Type", "application/x-www-form-urlencoded; charset=" + charset));
			process(
				"POST",
				HttpClient.buildURL(url),
				headers,
				new ByteArrayInputStream(body.toString().getBytes(charset)),
				callback
			);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}

	/**
	 * Do a POST request
	 *
	 * @param url http url
	 * @param parameters optional http-get parameters
	 * @param charset request character set
	 * @param headers optional http headers
	 * @param callback optional response callback
	 * @return last http code
	 */
	public int postFields(String url, Map<String, String[]> parameters, String charset, Header [] headers, Callback callback) {
		try {
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
			headers = add(Header.class, headers, new Header("Content-Type", "application/x-www-form-urlencoded; charset=" + charset));
			process(
				"POST",
				HttpClient.buildURL(url),
				headers,
				new ByteArrayInputStream(body.toString().getBytes(charset)),
				callback
			);
		} catch (Exception e) {
			if (callback != null) {
				callback.error(e);
			} else {
				throw new RuntimeException(e);
			}
		}
		return responseCode;
	}


	/**
	 * Process http request
	 *
	 * @param method http method
	 * @param url http url
	 * @param headers optional request headers
	 * @param is optional request body
	 * @param callback optional response callback
	 * @throws Exception if any error occurs
	 */
	public void process(String method, URL url, Header [] headers, InputStream is, Callback callback) throws Exception {
		long beginTime = System.currentTimeMillis();
		byte[] buffer = new byte[4096];
		int s;

		time = 0;
		connectTries = 0;
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
		boolean connected = false;

		do {
			connectTries++;
			try {
				connection.connect();
				connected = true;
			} catch (SocketTimeoutException e) { }
		} while (!connected && connectTries < connectMaxTries);

		// stream request body
		if (is != null) {
			OutputStream os = connection.getOutputStream();

			try {
				while ((s = is.read(buffer)) >= 0) {
					os.write(buffer, 0, s);
				}
			} finally {
				os.close();
			}
		}

		// parse response
		responseCode = connection.getResponseCode();
		responseStatus = connection.getResponseMessage();
		for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			if (entry.getKey() != null) {
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
		}

		// notify callback
		if (callback != null) {
			callback.beginResponse();
		}

		// stream response body
		is = connection.getInputStream();
		if (is != null) {
			is = new CountingInputStream(is);
			try {
				if (callback != null) {
					callback.parseResponse(is);
				} else {
					while ((s = is.read(buffer)) >= 0);
				}
			} finally {
				is.close();
			}
		}

		time = System.currentTimeMillis() - beginTime;
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
	private String buildCookieHeader() {
		if (cookies.size() > 0) {
			StringBuilder buffer = new StringBuilder();

			for (Cookie cookie : cookies.values()) {
				if (buffer.length() > 0) {
					buffer.append("; ");
				}
				buffer.append(cookie.getName());
				buffer.append('=');
				buffer.append(cookie.getValue());
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
	private List<Cookie> parseCookieHeader(String header) {
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
	 * Add a value in existing array
	 *
	 * @param clazz value class
	 * @param list value array
	 * @param value value
	 * @return new array
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T [] add(Class<T> clazz, T [] list, T value) {
		if (list == null) {
			list = (T[])Array.newInstance(clazz, 1);
		} else {
			list = Arrays.copyOf(list, list.length + 1);
		}
		list[list.length - 1] = value;
		return list;
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

	/**
	 * Base64 encoding/decoding
	 *
	 * @author nitro
	 */
	private static class Base64Encoder {
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
			switch (bits) {
			case 0:
//			case 6:
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
}