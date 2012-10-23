package com.boxalino.cem.client;

import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.concurrent.atomic.AtomicReference;

import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.jsp.PageContext;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;


/**
 * Boxalino CEM API Client
 *
 * @author nitro
 */
public class ApiClient extends HttpClient {
	/** Document builder factory */
	protected static final DocumentBuilderFactory dbf;

	/** Document builder factory */
	protected static final DocumentBuilder db;

	/** Proxy hidden headers */
	protected static final Set<String> hiddenProxyHeaders;

	/** Static initializer */
	static {
		// create the xml document builder
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setCoalescing(true);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);

		try {
			db =  dbf.newDocumentBuilder();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Set<String> headers = new TreeSet<String>();

		headers.add("authenticate");
		headers.add("connection");
		headers.add("content-encoding");
		headers.add("cookie");
		headers.add("keep-alive");
		headers.add("host");
		headers.add("proxy-authenticate");
		headers.add("proxy-authorization");
		headers.add("proxy-connection");
		headers.add("server");
		headers.add("set-cookie");
		headers.add("set-cookie2");
		headers.add("te");
		headers.add("trailer");
		headers.add("transfer-encoding");
		headers.add("upgrade");
		headers.add("www-authenticate");
		headers.add("x-powered-by");

		hiddenProxyHeaders = Collections.unmodifiableSet(headers);
	}


	/**
	 * Page content
	 *
	 * @author nitro
	 */
	public class Page {
		/** API version (x.y.z) */
		public final String apiVersion;

		/** API status (true = success) */
		public final boolean apiStatus;

		/** API processing time (ms) */
		public final double apiTime;

		/** Encoded search context */
		public final String context;

		/** Search query */
		public final String query;

		/** Result page offset */
		public final int resultOffset;

		/** Total results */
		public final int resultTotal;

		/** Result page index */
		public final int resultPageIndex;

		/** Result page count */
		public final int resultPageCount;

		/** Result page size */
		public final int resultPageSize;

		/** Result identifiers */
		public final List<String> results;

		/** Recommendation identifiers */
		public final List<String> recommendations;

		/** Page blocks */
		public final Map<String, String> blocks;


		/**
		 * Constructor
		 *
		 */
		private Page() {
			this.apiVersion = "0.0.0";
			this.apiStatus = false;
			this.apiTime = 0.0;
			this.context = "";
			this.query = "";
			this.resultOffset = 0;
			this.resultTotal = 0;
			this.resultPageIndex = 0;
			this.resultPageCount = 0;
			this.resultPageSize = 0;
			this.results = Collections.emptyList();
			this.recommendations = Collections.emptyList();
			this.blocks = Collections.emptyMap();
		}

		/**
		 * Constructor
		 *
		 * @param element xml element
		 */
		private Page(Element element) {
			this.apiVersion = element.getAttribute("version");
			this.apiStatus = Boolean.parseBoolean(element.getAttribute("status"));
			this.apiTime = Double.parseDouble(element.getAttribute("totalTime"));
			// cryptoKey / cryptoIV

			NodeList children = element.getChildNodes();
			String context = "";
			String query = "";
			int resultOffset = 0;
			int resultTotal = 0;
			int resultPageIndex = 0;
			int resultPageCount = 0;
			int resultPageSize = 0;
			List<String> results = Collections.emptyList();
			List<String> recommendations = Collections.emptyList();
			Map<String, String> blocks = Collections.emptyMap();

			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);

				switch (child.getNodeType()) {
				case Node.ELEMENT_NODE:
					if ("context".equals(child.getNodeName())) {
						context = visitTextNodes((Element)child);
					} else if ("query".equals(child.getNodeName())) {
						query = visitTextNodes((Element)child);
					} else if ("results".equals(child.getNodeName())) {
						Element childElement = (Element)child;

						resultOffset = Integer.parseInt(childElement.getAttribute("offset"));
						resultTotal = Integer.parseInt(childElement.getAttribute("total"));
						resultPageIndex = Integer.parseInt(childElement.getAttribute("pageIndex"));
						resultPageCount = Integer.parseInt(childElement.getAttribute("pageCount"));
						resultPageSize = Integer.parseInt(childElement.getAttribute("pageSize"));
						results = visitList(childElement, "result", "id");
					} else if ("recommendations".equals(child.getNodeName())) {
						Element childElement = (Element)child;

						recommendations = visitList(childElement, "recommendation", "id");
					} else if ("blocks".equals(child.getNodeName())) {
						Element childElement = (Element)child;

						blocks = visitMap(childElement, "block", "id");
					}
					break;
				}
			}
			this.context = context;
			this.query = query;
			this.resultOffset = resultOffset;
			this.resultTotal = resultTotal;
			this.resultPageIndex = resultPageIndex;
			this.resultPageCount = resultPageCount;
			this.resultPageSize = resultPageSize;
			this.results = Collections.unmodifiableList(results);
			this.recommendations = Collections.unmodifiableList(recommendations);
			this.blocks = Collections.unmodifiableMap(blocks);
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return (
				"{apiVersion=" + apiVersion +
				",apiStatus=" + apiStatus +
				",apiTime=" + apiTime +
				",context=" + context +
				",query=" + query +
				",resultOffset=" + resultOffset +
				",resultTotal=" + resultTotal +
				",resultPageIndex=" + resultPageIndex +
				",resultPageCount=" + resultPageCount +
				",resultPageSize=" + resultPageSize +
				",results=" + results +
				",recommendations=" + recommendations +
				",blocks=" + blocks + "}"
			);
		}


		/**
		 * Check if block exists
		 *
		 * @param name block name
		 * @return true if block exists
		 */
		public boolean hasBlock(String name) {
			return blocks.containsKey(name);
		}

		/**
		 * Get block content
		 *
		 * @param name block name
		 * @return block content
		 */
		public String getBlock(String name) {
			return (blocks.containsKey(name) ? blocks.get(name) : "");
		}
	}


	/** API url */
	public String url;


	/**
	 * Constructor
	 *
	 */
	public ApiClient() {
		this(null);
	}

	/**
	 * Constructor
	 *
	 * @param url api url
	 */
	public ApiClient(String url) {
		this(url, 1000, 15000, 3);
	}

	/**
	 * Constructor
	 *
	 * @param url api url
	 * @param connectTimeout connection timeout
	 * @param readTimeout read timeout
	 * @param connectMaxTries max. connection tries
	 */
	public ApiClient(String url, int connectTimeout, int readTimeout, int connectMaxTries) {
		super();
		this.url = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.connectMaxTries = connectMaxTries;
	}


	/**
	 * Proxy request
	 *
	 * @param uri remote uri
	 * @param context page context
	 */
	public void proxy(String uri, PageContext context) {
		proxy(uri, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Proxy request
	 *
	 * @param uri remote uri
	 * @param request http request
	 * @param response http response
	 */
	@SuppressWarnings("unchecked")
	public void proxy(String uri, HttpServletRequest request, HttpServletResponse response) {
		try {
			// fetch headers from request
			List<Header> headers = new ArrayList<Header>();
			Enumeration<String> headerNames = (Enumeration<String>)request.getHeaderNames();

			while (headerNames != null && headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();

				if (!hiddenProxyHeaders.contains(name.toLowerCase())) {
					Enumeration<String> headerValues = (Enumeration<String>)request.getHeaders(name);

					while (headerValues != null && headerValues.hasMoreElements()) {
						String value = headerValues.nextElement();

						headers.add(new Header(name, value));
					}
				}
			}

			// append host/via
			URL _url = new URL(url);

			headers.add(new Header("Host", _url.getHost()));
			headers.add(new Header("Via", "1.1 (Proxy)"));

			// set cookies
			javax.servlet.http.Cookie [] cookies = request.getCookies();

			if (cookies != null) {
				for (javax.servlet.http.Cookie cookie : cookies) {
					setCookie(cookie);
				}
			}

			process(
				request.getMethod(),
				new URL(url + uri + (request.getQueryString() != null ? ("?" + request.getQueryString()) : "")),
				headers.toArray(new Header[headers.size()]),
				request.getInputStream(),
				new ProxyCallback(response)
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Proxy request
	 *
	 * @param uri page uri
	 * @param context page context
	 * @return page content
	 */
	public Page loadPage(String uri, PageContext context) {
		return loadPage(uri, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Load page
	 *
	 * @param uri page uri
	 * @param request optional http request
	 * @param response optional http response
	 * @return page content
	 */
	public Page loadPage(String uri, HttpServletRequest request, HttpServletResponse response) {
		return loadPage(uri, new LinkedHashMap<String, String[]>(), request, response);
	}


	/**
	 * Proxy request
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param context page context
	 * @return page content
	 */
	public Page loadPage(String uri, String [][] parameters, PageContext context) {
		return loadPage(uri, parameters, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Load page
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param request optional http request
	 * @param response optional http response
	 * @return page content
	 */
	public Page loadPage(String uri, String [][] parameters, HttpServletRequest request, HttpServletResponse response) {
		Map<String, String[]> args = new LinkedHashMap<String, String[]>();

		if (parameters != null) {
			for (String [] parameter : parameters) {
				for (int i = 1; i < parameter.length; i++) {
					add(String.class, args, parameter[0], parameter[i]);
				}
			}
		}
		return loadPage(uri, args, request, response);
	}


	/**
	 * Proxy request
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param context page context
	 * @return page content
	 */
	public Page loadPage(String uri, Map<String, String[]> parameters, PageContext context) {
		return loadPage(uri, parameters, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Load page
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param request optional http request
	 * @param response optional http response
	 * @return page content
	 */
	@SuppressWarnings("unchecked")
	public Page loadPage(String uri, Map<String, String[]> parameters, final HttpServletRequest request, final HttpServletResponse response) {
		// apply request
		if (request != null) {
			// set cookies
			javax.servlet.http.Cookie [] cookies = request.getCookies();

			if (cookies != null) {
				for (javax.servlet.http.Cookie cookie : cookies) {
					setCookie(cookie);
				}
			}

			// append parameters
			for (Map.Entry<String, String[]> entry : ((Map<String, String[]>)request.getParameterMap()).entrySet()) {
				if (!parameters.containsKey(entry.getKey())) {
					parameters.put(entry.getKey(), entry.getValue());
				}
			}

			// append environment
			parameters.put("connection", new String [] { request.isSecure() ? "https" : "http" });
			if (!parameters.containsKey("clientAddress")) {
				if (request.getHeader("X-Forwarded-For") != null) {
					parameters.put("clientAddress", new String [] { request.getHeader("X-Forwarded-For") });
				} else {
					parameters.put("clientAddress", new String [] { request.getRemoteAddr() });
				}
			}
			if (!parameters.containsKey("clientAgent") && request.getHeader("User-Agent") != null) {
				parameters.put("clientAgent", new String [] { request.getHeader("User-Agent") });
			}
			if (!parameters.containsKey("clientReferer") && request.getHeader("Referer") != null) {
				parameters.put("clientReferer", new String [] { request.getHeader("Referer") });
			}
			if (!parameters.containsKey("serverAddress")) {
				parameters.put("serverAddress", new String [] { request.getLocalAddr() });
			}
			if (!parameters.containsKey("serverHost")) {
				parameters.put("serverHost", new String [] { request.getServerName() });
			}
			if (!parameters.containsKey("serverUri")) {
				parameters.put("serverUri", new String [] { request.getRequestURI() });
			}
		} else {
			// append environment
			parameters.put("connection", new String [] { "http" });
			parameters.put("clientAddress", new String [] { "" });
			parameters.put("clientAgent", new String [] { "" });
			parameters.put("clientReferer", new String [] { "" });
			parameters.put("serverAddress", new String [] { "" });
			parameters.put("serverHost", new String [] { "" });
			parameters.put("serverUri", new String [] { "" });
		}

		parameters.put("uri", new String [] { uri });
		try {
			final AtomicReference<Page> page = new AtomicReference<Page>();

			postFields(
				url + "/api/xml/page",
				parameters,
				"UTF-8",
				null,
				new Callback() {
					@Override
					public void beginResponse() throws Exception {
						if (response != null) {
							for (Cookie cookie : getCookies()) {
								response.addCookie(cookie.toCookie());
							}
						}
					}

					@Override
					public void parseResponse(InputStream is) throws Exception {
						Element element = db.parse(new InputSource(is)).getDocumentElement();

						if (!element.getNodeName().equals("cem")) {
							throw new IllegalStateException("invalid xml element: " + element.getNodeName());
						}
						page.set(new Page(element));
					}

					@Override
					public void error(Exception e) {
					}
				}
			);
			return page.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Page();
	}


	/**
	 * Visit an xml "array" element
	 *
	 * @param element xml element
	 * @return array (id)
	 */
	private static List<String> visitList(Element element, String tagName, String idAttribute) {
		List<String> list = new ArrayList<String>();
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (tagName.equals(child.getNodeName())) {
					Element childElement = (Element)child;

					list.add(childElement.getAttribute(idAttribute));
				}
				break;
			}
		}
		return list;
	}

	/**
	 * Visit an xml "map" element
	 *
	 * @param element xml element
	 * @return map (id => text)
	 */
	private static Map<String, String> visitMap(Element element, String tagName, String idAttribute) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (tagName.equals(child.getNodeName())) {
					Element childElement = (Element)child;

					map.put(childElement.getAttribute(idAttribute), visitTextNodes(childElement));
				}
				break;
			}
		}
		return map;
	}

	/**
	 * Visit an xml element and return all direct textual content
	 *
	 * @param element xml element
	 * @return all textual content
	 */
	private static String visitTextNodes(Element element) {
		StringBuffer buffer = new StringBuffer();
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				buffer.append(child.getNodeValue());
				break;
			}
		}
		return buffer.toString().trim();
	}


	/**
	 * Proxy callback writing to response
	 *
	 * @author nitro
	 */
	private class ProxyCallback implements Callback {
		/** Underlying response */
		private final HttpServletResponse response;


		/**
		 * Constructor
		 *
		 * @param response underlying response
		 */
		public ProxyCallback(HttpServletResponse response) {
			this.response = response;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public void beginResponse() throws Exception {
			response.setStatus(getCode());
			for (Header header : getHeaders()) {
				if (!hiddenProxyHeaders.contains(header.name.toLowerCase())) {
					response.addHeader(header.name, header.value);
				} else {
					System.err.println(header.name + "=" + header.value);
				}
			}
			for (Cookie cookie : getCookies()) {
				response.addCookie(cookie.toCookie());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void parseResponse(InputStream is) throws Exception {
			byte [] buffer = new byte[4096];
			int br;

			try {
				OutputStream os = response.getOutputStream();

				if ("gzip".equalsIgnoreCase(getHeader("content-encoding"))) {
					is = new GZIPInputStream(is);
				}
				try {
					while ((br = is.read(buffer)) >= 0) {
						os.write(buffer, 0, br);
					}
				} finally {
					os.close();
				}
			} finally {
				is.close();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void error(Exception e) {
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception e2) {
				throw new RuntimeException(e2);
			}
		}
	}
}
