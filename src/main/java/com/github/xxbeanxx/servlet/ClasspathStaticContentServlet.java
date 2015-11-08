package com.github.xxbeanxx.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xxbeanxx.servlet.util.MimeUtils;

/**
 * A servlet for classpath-based static content.
 * <p>
 * This class works by mapping the request path to the packages that are
 * configured in it's init-params. If no class paths are configured via
 * init-param, then no classpaths will be searched and a 404 will always be
 * returned.
 * <p>
 * <b>As a security precaution, this servlet will not allow mapping to the root
 * classpath ('/').</b>
 * <p>
 * There are three initialization parameters that can be used to configure the
 * servlet:
 * 
 * <p>
 * <ul>
 * <li><b>{@value #PARAM_DISABLE_BROWSER_CACHE}</b> &ndash; set to
 * <code>true</code> to sent the HTTP headers instructing the browser to not
 * cache the content
 * 
 * <li><b>{@value #PARAM_EXPIRES}</b> &ndash; sets the "Expires" header in the
 * response; the actual header value will be {@code time-now + expires-seconds}
 * 
 * <li><b>{@value #PARAM_ENCODING}</b> &ndash; sets the default <a href=
 * "http://docs.oracle.com/javase/6/docs/api/java/lang/package-summary.html#charenc">
 * character encoding</a>
 * 
 * <li><b>{@value #PARAM_PACKAGES}</b> &ndash; a comma or space separated list
 * of classpath packages to scan
 * </ul>
 * <p>
 * 
 * @author Greg Baker
 */
@SuppressWarnings("serial")
public class ClasspathStaticContentServlet extends HttpServlet {

	public static final String PARAM_DISABLE_BROWSER_CACHE = "disable-cache"; 
	
	public static final String PARAM_ENCODING = "encoding";

	public static final String PARAM_EXPIRES = "expires";

	public static final String PARAM_PACKAGES = "packages";

	/** The last modified date. Defaults to the application's deployment time. */
	private static final long LAST_MODIFIED_MILLIS = Calendar.getInstance().getTimeInMillis();

	private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathStaticContentServlet.class);

	private static final String PACKAGES_DELIMITER = ",; \t\n";
	
	private static final int OUTPUT_BUFFER_SIZE = 4096;

	/** If true, sends no-cache headers in the response */
	protected boolean disableBrowserCache;
	
	/** A list of classpaths that will be scanned to find the requested resource. */
	protected List<String> packages;
	
	/** The output character encoding. */
	protected String encoding = "UTF-8";

	/** The time delta (from now) used to expire the content. **/
	protected int expiresDelta = 365 * 24 * 60 * 60;

	@Override
	public void init() throws ServletException {
		this.packages = tokenizeToList(getInitParameter(PARAM_PACKAGES), ClasspathStaticContentServlet.PACKAGES_DELIMITER);
		
		if (this.packages.contains("/")) {
			throw new ServletException("Scanning of root classpath is disallowed");
		}

		this.disableBrowserCache = Boolean.parseBoolean(getInitParameter(PARAM_DISABLE_BROWSER_CACHE));

		if (getInitParameter(PARAM_ENCODING) != null) {
			this.encoding = getInitParameter(PARAM_ENCODING);
		}
		
		if (getInitParameter(PARAM_EXPIRES) != null) {
			try {
				this.expiresDelta = Integer.valueOf(getInitParameter(PARAM_EXPIRES));
			}
			catch (final NumberFormatException numberFormatException) {
				LOGGER.warn("Caught exception converting init-parameter '{}' to long. Using default of {}", PARAM_EXPIRES, this.expiresDelta);
			}
		}
		
		LOGGER.info("Servlet initialized: " + dumpInitParams());
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String servletPath = request.getServletPath();
		final String path = request.getPathInfo();
		LOGGER.debug("Servicing GET request for {}{}", servletPath, path);
		
		for (final String pkg : packages) {
			final String qualifiedPath = buildQualifiedPath(path, pkg);
			final URL resourceUrl = findResourceUrl(qualifiedPath);
			
			if (resourceUrl != null) {
				LOGGER.debug("Resource found at location {}", resourceUrl);
				
				InputStream inputStream = null;
				
				try {
					if (resourceUrl.getFile().endsWith(qualifiedPath)) {
						inputStream = resourceUrl.openStream();
					}
				}
				catch (final IOException ioException) {
					LOGGER.debug("Caught exception opening input stream", ioException);
				}

				// the request is intentionally processed outside of the try/catch above 
				if (inputStream != null) {
					processRequest(inputStream, path, request, response);
					LOGGER.debug("Successfully processed request for resource {}", path);
					return;
				}
			}
		}
	}
	
	protected void processRequest(InputStream inputStream, String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
		final Calendar cal = Calendar.getInstance();
		final long now = cal.getTimeInMillis();
		cal.add(Calendar.SECOND, expiresDelta);
		final long expires = cal.getTimeInMillis();
		LOGGER.debug("'Expires' response header is {}", expires);

		final long ifModifiedSince = getIfModifiedSinceHeader(request);
		LOGGER.debug("'If-Modified-Since' request header is {}", ifModifiedSince);

		if (ifModifiedSince > 0 && ifModifiedSince <= LAST_MODIFIED_MILLIS) {
			LOGGER.debug("Resource was not modified, sending {}", HttpServletResponse.SC_NOT_MODIFIED);
			response.setDateHeader("Expires", expires);
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			inputStream.close();
			return;
		}

		final String contentType = MimeUtils.guessMimeTypeFromFilename(path);
		LOGGER.debug("Guessed content-type from filename: {}", contentType);
		
		if (contentType != null) {
			response.setContentType(contentType);
		}

		if (disableBrowserCache == true) {
			LOGGER.debug("Init-param disableBrowserCache is set to true, adding no-cache headers");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "-1");
		}
		else {
			response.setDateHeader("Date", now);
			response.setDateHeader("Expires", expires);
			response.setDateHeader("Retry-After", expires);
			response.setHeader("Cache-Control", "public");
			response.setDateHeader("Last-Modified", LAST_MODIFIED_MILLIS);
		}

		try {
			copyStreams(inputStream, response.getOutputStream());
		}
		finally {
			inputStream.close();
		}
	}

	private long getIfModifiedSinceHeader(HttpServletRequest request) {
		try {
			return request.getDateHeader("If-Modified-Since");
		}
		catch (final IllegalArgumentException illegalArgumentException) {
			return 0L;
		}
	}

    protected void copyStreams(InputStream input, OutputStream output) throws IOException {
        final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];

        int n, total = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            total += n;
        }
        
        LOGGER.debug("Wrote {} bytes to output stream", total);
        output.flush();
    }
    
	protected URL findResourceUrl(String path) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return (url != null) ? url  : getClass().getClassLoader().getResource(path);
	}

	protected String buildQualifiedPath(String contentPath, String pkg) throws UnsupportedEncodingException {
        if (pkg.endsWith("/") && contentPath.startsWith("/")) {
            return URLDecoder.decode(pkg + contentPath.substring(1), encoding);
        }
        else {
            return URLDecoder.decode(pkg + contentPath, encoding);
        }
	}

	protected List<String> tokenizeToList(String string, String delimiters) {
		final List<String> list = new ArrayList<String>();
		
		if (string != null) {
			final StringTokenizer stringTokenizer = new StringTokenizer(string, delimiters);
			
			while (stringTokenizer.hasMoreTokens()) {
				final String token = stringTokenizer.nextToken().trim();
				list.add(token);
			}
		}
		
		return list;
	}

	private String dumpInitParams() {
		return "ClasspathStaticContentServlet [\n"
				+ "\t disableBrowserCache=" + disableBrowserCache + "\n"
				+ "\t encoding=" + encoding + "\n"
				+ "\t expiresDelta=" + expiresDelta + "\n"
				+ "\t lastModifiedMillis=" + LAST_MODIFIED_MILLIS + "\n"
				+ "\t packages=" + packages + "\n]";
	}
	
}
