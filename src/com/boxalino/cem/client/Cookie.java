package com.boxalino.cem.client;


/**
 * Cookie wrapper
 *
 * @author nitro
 */
public class Cookie {
	/** Netscape draft */
	public static final int VERSION_0 = 0;

	/** RFC 2109/2965 */
	public static final int VERSION_1 = 1;


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
	 * @param name cookie name
	 * @param value cookie value
	 */
	public Cookie(String name, String value) {
		this(name, value, null, null, null, null, null, VERSION_0, -1, false, false);
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 11;

		hash = 31 * hash + name.hashCode();
		hash = 31 * hash + domain.hashCode();
		hash = 31 * hash + path.hashCode();
		hash = 31 * hash + portList.hashCode();
		return hash;
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
}
