package com.boxalino.cem.client;

import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import java.util.concurrent.atomic.AtomicBoolean;
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

import com.boxalino.cem.client.json.JsonArray;
import com.boxalino.cem.client.json.JsonObject;


/**
 * Boxalino CEM API Client.
 *
 * @author nitro
 */
public class ApiClient extends HttpClient {
	/** Document builder factory */
	private static final DocumentBuilderFactory dbf;

	/** Document builder factory */
	private static final DocumentBuilder db;

	/** Proxy hidden headers */
	private static final Set<String> hiddenProxyHeaders;

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
	public static class Page {
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
		 * Constructor.
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
		 * Constructor.
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
		 * Return a string representation of this object for debug purpose.
		 *
		 * @return string representation
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
		 * Check if block exists.
		 *
		 * @param name block name
		 * @return true if block exists
		 */
		public boolean hasBlock(String name) {
			return blocks.containsKey(name);
		}

		/**
		 * Get block content.
		 *
		 * @param name block name
		 * @return block content
		 */
		public String getBlock(String name) {
			return (blocks.containsKey(name) ? blocks.get(name) : "");
		}
	}

	/**
	 * A transaction item (basket/checkout)
	 *
	 * @author nitro
	 */
	public static class TransactionItem {
		/** Item identifier */
		public final String id;

		/** Item price (per unit) */
		public final double price;

		/** Item quantity */
		public final int quantity;

		/** Item name (optional) */
		public final String name;

		/** Item widget (optional) */
		public final String widget;


		/**
		 * Constructor.
		 *
		 */
		private TransactionItem() {
			this(null, 0, 0, null, null);
		}

		/**
		 * Constructor (quantity = 1).
		 *
		 * @param id item identifier
		 * @param price item price
		 */
		public TransactionItem(String id, double price) {
			this(id, price, 1, null, null);
		}

		/**
		 * Constructor.
		 *
		 * @param id item identifier
		 * @param price item price
		 * @param quantity item quantity
		 */
		public TransactionItem(String id, double price, int quantity) {
			this(id, price, quantity, null, null);
		}

		/**
		 * Constructor.
		 *
		 * @param id item identifier
		 * @param price item price
		 * @param quantity item quantity
		 * @param name item name
		 */
		public TransactionItem(String id, double price, int quantity, String name) {
			this(id, price, quantity, name, null);
		}

		/**
		 * Constructor.
		 *
		 * @param id item identifier
		 * @param price item price
		 * @param quantity item quantity
		 * @param name item name
		 * @param widget facilitator widget
		 */
		public TransactionItem(String id, double price, int quantity, String name, String widget) {
			this.id = id;
			this.price = price;
			this.quantity = quantity;
			this.name = name;
			this.widget = widget;
		}


		/**
		 * Compute a hash code for this object.
		 *
		 * @return hash code
		 */
		@Override
		public int hashCode() {
			int hash = 11;

			hash = 31 * hash + id.hashCode();
			return hash;
		}

		/**
		 * Test if given object is equal to this.
		 *
		 * @param o other object to test
		 * @return true if other object is equal (id)
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof TransactionItem) {
				TransactionItem i = (TransactionItem)o;

				return (
					id.equals(i.id) &&
					price == i.price &&
					quantity == i.quantity &&
					((name != null && name.equals(i.name)) || (name == null && i.name == null)) &&
					((widget != null && widget.equals(i.widget)) || (widget == null && i.widget == null))
				);
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
				"{id=" + id +
				",price=" + price +
				",quantity=" + quantity +
				",name=" + name +
				",widget=" + widget + "}"
			);
		}


		/**
		 * Convert to JSON format.
		 *
		 * @return JSON format
		 */
		private JsonObject asJson() {
			JsonObject json = new JsonObject();

			json.setString("id", id);
			json.setDouble("price", price);
			json.setInteger("quantity", quantity);
			if (name != null) {
				json.setString("name", name);
			}
			if (widget != null) {
				json.setString("widget", widget);
			}
			return json;
		}
	}


	/** API url */
	private final String url;

	/** Debug output (print exceptions to stderr) */
	public boolean debug = true;


	/**
	 * Constructor.
	 *
	 */
	private ApiClient() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param url api url
	 */
	public ApiClient(String url) {
		super();
		this.url = url;
	}


	/**
	 * Proxy request.
	 *
	 * @param uri remote uri
	 * @param context page context
	 */
	public void proxy(String uri, PageContext context) {
		proxy(uri, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Proxy request.
	 *
	 * @param uri remote uri
	 * @param request http request
	 * @param response http response
	 */
	public void proxy(String uri, HttpServletRequest request, HttpServletResponse response) {
		try {
			// build proxied headers
			Header [] headers = fetchProxyHeaders(request);
			URL _url = new URL(url);

			headers = add(Header.class, headers, new Header("Host", _url.getHost()));
			headers = add(Header.class, headers, new Header("Via", "1.1 (Proxy)"));

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
				headers,
				request.getInputStream(),
				new ProxyCallback(response)
			);
		} catch (Exception e) {
			if (debug) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Load page.
	 *
	 * @param uri page uri
	 * @param context page context
	 * @return page content
	 */
	public Page loadPage(String uri, PageContext context) {
		return loadPage(uri, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}

	/**
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param context page context
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, PageContext context) {
		return loadPageAsync(uri, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}


	/**
	 * Load page.
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
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param request optional http request
	 * @param response optional http response
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, HttpServletRequest request, HttpServletResponse response) {
		return loadPageAsync(uri, new LinkedHashMap<String, String[]>(), request, response);
	}


	/**
	 * Load page.
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
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param context page context
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, String [][] parameters, PageContext context) {
		return loadPageAsync(uri, parameters, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
	}


	/**
	 * Load page.
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
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param request optional http request
	 * @param response optional http response
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, String [][] parameters, HttpServletRequest request, HttpServletResponse response) {
		Map<String, String[]> args = new LinkedHashMap<String, String[]>();

		if (parameters != null) {
			for (String [] parameter : parameters) {
				for (int i = 1; i < parameter.length; i++) {
					add(String.class, args, parameter[0], parameter[i]);
				}
			}
		}
		return loadPageAsync(uri, args, request, response);
	}


	/**
	 * Load page.
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
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param context page context
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, Map<String, String[]> parameters, PageContext context) {
		return loadPageAsync(uri, parameters, (HttpServletRequest)context.getRequest(), (HttpServletResponse)context.getResponse());
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
	public Page loadPage(String uri, Map<String, String[]> parameters, final HttpServletRequest request, final HttpServletResponse response) {
		applyRequest(request, parameters);
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
			if (debug) {
				e.printStackTrace();
			}
		}
		return new Page();
	}

	/**
	 * Load page (async).
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @param request optional http request
	 * @param response optional http response
	 * @return future page content
	 */
	public Future<Page> loadPageAsync(String uri, final Map<String, String[]> parameters, final HttpServletRequest request, final HttpServletResponse response) {
		applyRequest(request, parameters);
		parameters.put("uri", new String [] { uri });

		FutureTask<Page> task = new FutureTask<Page>(
			new Callable<Page>() {
				@Override
				public Page call() throws Exception {
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
								throw new RuntimeException(e);
							}
						}
					);
					return page.get();
				}
			}
		);
		Thread thread = new Thread(task, "com.boxalino.cem.client.ApiClient.loadPageAsync");

		thread.setDaemon(true);
		thread.start();
		return task;
	}


	/**
	 * Track when a category is viewed.
	 *
	 * @param categoryId category identifier
	 * @param categoryName optional category name
	 * @param context page context
	 * @return true on success
	 */
	public boolean trackCategoryView(String categoryId, String categoryName, PageContext context) {
		return trackCategoryView(categoryId, categoryName, (HttpServletRequest)context.getRequest());
	}

	/**
	 * Track when a category is viewed (async).
	 *
	 * @param categoryId category identifier
	 * @param categoryName optional category name
	 * @param context page context
	 * @return true on success
	 */
	public Future<Boolean> trackCategoryViewAsync(String categoryId, String categoryName, PageContext context) {
		return trackCategoryViewAsync(categoryId, categoryName, (HttpServletRequest)context.getRequest());
	}


	/**
	 * Track when a category is viewed.
	 *
	 * @param categoryId category identifier
	 * @param categoryName optional category name
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackCategoryView(String categoryId, String categoryName, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("id", categoryId);
		if (categoryName != null) {
			description.put("name", categoryName);
		}
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEvent("categoryView", description, request);
	}

	/**
	 * Track when a category is viewed (async).
	 *
	 * @param categoryId category identifier
	 * @param categoryName optional category name
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackCategoryViewAsync(String categoryId, String categoryName, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("id", categoryId);
		if (categoryName != null) {
			description.put("name", categoryName);
		}
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEventAsync("categoryView", description, request);
	}


	/**
	 * Track when an item is viewed.
	 *
	 * @param itemId item identifier
	 * @param itemName optional item name
	 * @param context page context
	 * @return true on success
	 */
	public boolean trackProductView(String itemId, String itemName, PageContext context) {
		return trackProductView(itemId, itemName, (HttpServletRequest)context.getRequest());
	}

	/**
	 * Track when an item is viewed (async).
	 *
	 * @param itemId item identifier
	 * @param itemName optional item name
	 * @param context page context
	 * @return true on success
	 */
	public Future<Boolean> trackProductViewAsync(String itemId, String itemName, PageContext context) {
		return trackProductViewAsync(itemId, itemName, (HttpServletRequest)context.getRequest());
	}


	/**
	 * Track when an item is viewed.
	 *
	 * @param itemId item identifier
	 * @param itemName optional item name
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackProductView(String itemId, String itemName, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("id", itemId);
		if (itemName != null) {
			description.put("name", itemName);
		}
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEvent("productView", description, request);
	}

	/**
	 * Track when an item is viewed (async).
	 *
	 * @param itemId item identifier
	 * @param itemName optional item name
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackProductViewAsync(String itemId, String itemName, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("id", itemId);
		if (itemName != null) {
			description.put("name", itemName);
		}
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEventAsync("productView", description, request);
	}


	/**
	 * Track when an item is added to the basket.
	 *
	 * @param item item descriptor
	 * @param context page context
	 * @return true on success
	 */
	public boolean trackAddToBasket(TransactionItem item, PageContext context) {
		return trackAddToBasket(item, (HttpServletRequest)context.getRequest());
	}

	/**
	 * Track when an item is added to the basket (async).
	 *
	 * @param item item descriptor
	 * @param context page context
	 * @return true on success
	 */
	public Future<Boolean> trackAddToBasketAsync(TransactionItem item, PageContext context) {
		return trackAddToBasketAsync(item, (HttpServletRequest)context.getRequest());
	}


	/**
	 * Track when an item is added to the basket.
	 *
	 * @param item item descriptor
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackAddToBasket(TransactionItem item, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("item", item.asJson().toJson(true));
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEvent("addToBasket", description, request);
	}

	/**
	 * Track when an item is added to the basket (async).
	 *
	 * @param item item descriptor
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackAddToBasketAsync(TransactionItem item, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();

		description.put("item", item.asJson().toJson(true));
		if (request != null && request.getParameter("widget") != null) {
			description.put("widget", request.getParameter("widget"));
		}
		return trackEventAsync("addToBasket", description, request);
	}


	/**
	 * Track when a checkout is completed.
	 *
	 * @param success success status (true: successful, false: failed)
	 * @param amount transaction total amount
	 * @param items items descriptors
	 * @param context page context
	 * @return true on success
	 */
	public boolean trackPurchase(boolean success, double amount, List<TransactionItem> items, PageContext context) {
		return trackPurchase(success, amount, items, (HttpServletRequest)context.getRequest());
	}

	/**
	 * Track when a checkout is completed (async).
	 *
	 * @param success success status (true: successful, false: failed)
	 * @param amount transaction total amount
	 * @param items items descriptors
	 * @param context page context
	 * @return true on success
	 */
	public Future<Boolean> trackPurchaseAsync(boolean success, double amount, List<TransactionItem> items, PageContext context) {
		return trackPurchaseAsync(success, amount, items, (HttpServletRequest)context.getRequest());
	}


	/**
	 * Track when a checkout is completed.
	 *
	 * @param success success status (true: successful, false: failed)
	 * @param amount transaction total amount
	 * @param items items descriptors
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackPurchase(boolean success, double amount, List<TransactionItem> items, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();
		JsonArray json = new JsonArray();

		for (TransactionItem item : items) {
			json.addJson(item.asJson());
		}
		description.put("status", success ? "1" : "0");
		description.put("amount", Double.toString(amount));
		description.put("items", json.toJson(true));
		return trackEvent("purchaseDone", description, request);
	}

	/**
	 * Track when a checkout is completed (async).
	 *
	 * @param success success status (true: successful, false: failed)
	 * @param amount transaction total amount
	 * @param items items descriptors
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackPurchaseAsync(boolean success, double amount, List<TransactionItem> items, HttpServletRequest request) {
		Map<String, String> description = new LinkedHashMap<String, String>();
		JsonArray json = new JsonArray();

		for (TransactionItem item : items) {
			json.addJson(item.asJson());
		}
		description.put("status", success ? "1" : "0");
		description.put("amount", Double.toString(amount));
		description.put("items", json.toJson(true));
		return trackEventAsync("purchaseDone", description, request);
	}


	/**
	 * Track an analytics event.
	 *
	 * @param name event name
	 * @param description event description (map)
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackEvent(String name, Map<String, String> description, HttpServletRequest request) {
		String [] list = new String[description.size()];
		int i = 0;

		for (Map.Entry<String, String> item : description.entrySet()) {
			try {
				list[i++] = URLEncoder.encode(item.getKey(), "UTF-8") + ":" + URLEncoder.encode(item.getValue(), "UTF-8");
			} catch (Exception e) {
				if (debug) {
					e.printStackTrace();
				}
			}
		}
		return trackEvent(name, list, request);
	}

	/**
	 * Track an analytics event (async).
	 *
	 * @param name event name
	 * @param description event description (map)
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackEventAsync(String name, Map<String, String> description, HttpServletRequest request) {
		String [] list = new String[description.size()];
		int i = 0;

		for (Map.Entry<String, String> item : description.entrySet()) {
			try {
				list[i++] = URLEncoder.encode(item.getKey(), "UTF-8") + ":" + URLEncoder.encode(item.getValue(), "UTF-8");
			} catch (Exception e) {
				if (debug) {
					e.printStackTrace();
				}
			}
		}
		return trackEventAsync(name, list, request);
	}


	/**
	 * Track an analytics event.
	 *
	 * @param name event name
	 * @param description event description (list)
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackEvent(String name, String [] description, HttpServletRequest request) {
		StringBuilder buffer = new StringBuilder();

		for (String item : description) {
			if (buffer.length() > 0) {
				buffer.append(' ');
			}
			buffer.append(item);
		}
		return trackEvent(name, buffer.toString(), request);
	}

	/**
	 * Track an analytics event (async).
	 *
	 * @param name event name
	 * @param description event description (list)
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackEventAsync(String name, String [] description, HttpServletRequest request) {
		StringBuilder buffer = new StringBuilder();

		for (String item : description) {
			if (buffer.length() > 0) {
				buffer.append(' ');
			}
			buffer.append(item);
		}
		return trackEventAsync(name, buffer.toString(), request);
	}


	/**
	 * Track an analytics event.
	 *
	 * @param name event name
	 * @param description event description (raw)
	 * @param request optional http request
	 * @return true on success
	 */
	public boolean trackEvent(String name, String description, HttpServletRequest request) {
		Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();
		final AtomicBoolean success = new AtomicBoolean();

		applyRequest(request, parameters);
		parameters.put("eventName", new String [] { name });
		parameters.put("eventDescription", new String [] { description });
		try {
			postFields(
				url + "/analytics",
				parameters,
				"UTF-8",
				null,
				new Callback() {
					@Override
					public void beginResponse() throws Exception {
						success.set(getCode() == 200);
					}

					@Override
					public void parseResponse(InputStream is) throws Exception {
					}

					@Override
					public void error(Exception e) {
					}
				}
			);
		} catch (Exception e) {
			if (debug) {
				e.printStackTrace();
			}
		}
		return success.get();
	}

	/**
	 * Track an analytics event (async).
	 *
	 * @param name event name
	 * @param description event description (raw)
	 * @param request optional http request
	 * @return true on success
	 */
	public Future<Boolean> trackEventAsync(String name, String description, HttpServletRequest request) {
		final Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();

		applyRequest(request, parameters);
		parameters.put("eventName", new String [] { name });
		parameters.put("eventDescription", new String [] { description });

		FutureTask<Boolean> task = new FutureTask<Boolean>(
			new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					final AtomicBoolean success = new AtomicBoolean();

					postFields(
						url + "/analytics",
						parameters,
						"UTF-8",
						null,
						new Callback() {
							@Override
							public void beginResponse() throws Exception {
								success.set(getCode() == 200);
							}

							@Override
							public void parseResponse(InputStream is) throws Exception {
							}

							@Override
							public void error(Exception e) {
							}
						}
					);
					return success.get();
				}
			}
		);
		Thread thread = new Thread(task, "com.boxalino.cem.client.ApiClient.trackEventAsync");

		thread.setDaemon(true);
		thread.start();
		return task;
	}


	/**
	 * Apply request parameters.
	 *
	 * @param request optional http request
	 * @param parameters request parameters
	 */
	@SuppressWarnings("unchecked")
	private void applyRequest(HttpServletRequest request, Map<String, String[]> parameters) {
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
	}

	/**
	 * Fetch proxy headers from request.
	 *
	 * @param request http request
	 * @return proxy headers
	 */
	@SuppressWarnings("unchecked")
	private Header [] fetchProxyHeaders(HttpServletRequest request) {
		List<Header> list = new ArrayList<Header>();
		Enumeration<String> headerNames = (Enumeration<String>)request.getHeaderNames();

		while (headerNames != null && headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();

			if (!hiddenProxyHeaders.contains(name.toLowerCase())) {
				Enumeration<String> headerValues = (Enumeration<String>)request.getHeaders(name);

				while (headerValues != null && headerValues.hasMoreElements()) {
					String value = headerValues.nextElement();

					list.add(new Header(name, value));
				}
			}
		}
		return list.toArray(new Header[list.size()]);
	}


	/**
	 * Visit an xml "array" element.
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
	 * Visit an xml "map" element.
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
	 * Visit an xml element and return all direct textual content.
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
	 * Proxy callback writing to response.
	 *
	 * @author nitro
	 */
	private class ProxyCallback implements Callback {
		/** Underlying response */
		private final HttpServletResponse response;


		/**
		 * Constructor.
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
