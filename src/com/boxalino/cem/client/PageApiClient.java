package com.boxalino.cem.client;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;


/**
 * Page API connector
 *
 * @author nitro
 */
public class PageApiClient extends HttpClient {
	/** Document builder factory */
	protected static final DocumentBuilderFactory dbf;

	/** Document builder factory */
	protected static final DocumentBuilder db;

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
	}


	/**
	 * Page content
	 *
	 * @author nitro
	 */
	public class Page {
		public final String apiVersion;
		public final boolean apiStatus;
		public final int apiTime;
		public final String context;
		private String query = "";
		private int resultOffset = 0;
		private int resultTotal = 0;
		private int resultPageIndex = 0;
		private int resultPageCount = 0;
		private int resultPageSize = 0;
		private List<String> results = new ArrayList<String>();
		private List<String> recommendations = new ArrayList<String>();
		private Map<String, String> blocks = new LinkedHashMap<String, String>();


		private Page(Element element) throws Exception {
			this.apiVersion = element.getAttribute("version");
			this.apiStatus = Boolean.parseBoolean(element.getAttribute("status"));
			this.apiTime = Integer.parseInt(element.getAttribute("totalTime"));
			// cryptoKey / cryptoIV

			NodeList children = element.getChildNodes();
			String context = "";

			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);

				switch (child.getNodeType()) {
				case Node.ELEMENT_NODE:
					if ("context".equals(child.getNodeName())) {
						context = visitTextNodes((Element)child);
					}
					break;
				}
			}
			this.context = context;
		}
	}


	/** API url */
	public String url;

	/** Last page content */
	private Page lastPage;


	/**
	 * Constructor
	 *
	 * @param url api url
	 * @param connectTimeout connection timeout
	 * @param readTimeout read timeout
	 * @param connectMaxTries max. connection tries
	 */
	public PageApiClient(String url, int connectTimeout, int readTimeout, int connectMaxTries) {
		super();
		this.url = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.connectMaxTries = connectMaxTries;
	}


	/**
	 * Load page
	 *
	 * @param uri page uri
	 * @param parameters page parameters
	 * @return page content or null on error
	 */
	public Page load(String uri, String [][] parameters) {
		Map<String, String[]> args = new LinkedHashMap<String, String[]>();

		for (String [] parameter : parameters) {
			for (int i = 1; i < parameter.length; i++) {
				add(String.class, args, parameter[0], parameter[i]);
			}
		}
		lastPage = null;
		try {
			postFields(url, args, "UTF-8", null);
		} catch (Exception e) {
			return null;
		}
		return lastPage;
	}

	/**
	 * Called to parse response
	 *
	 * @param is body stream
	 * @throws Exception if any error occurs
	 */
	@Override
	public void parseResponse(InputStream is) throws Exception {
		// parse xml request
		Element element = db.parse(new InputSource(is)).getDocumentElement();

		// validate element
		if (!element.getNodeName().equals("cem")) {
			throw new IllegalStateException("invalid xml element: " + element.getNodeName());
		}
		lastPage = new Page(element);
	}


	/**
	 * Visit an xml element and return all direct textual content
	 *
	 * @param element xml element
	 * @return all textual content
	 * @throws Exception if any error occurs
	 */
	private static String visitTextNodes(Element element) throws Exception {
		StringBuffer buffer = new StringBuffer();
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				buffer.append(child.getNodeValue());
				break;

			default:
				throw new IllegalStateException("invalid xml node type: " + child.getNodeType());
			}
		}
		return buffer.toString().trim();
	}
}
