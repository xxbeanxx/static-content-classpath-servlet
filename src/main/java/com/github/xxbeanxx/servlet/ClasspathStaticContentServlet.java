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

import com.github.xxbeanxx.servlet.util.MimeUtils;

/**
 * @author Greg Baker
 */
public class ClasspathStaticContentServlet extends HttpServlet {

	private static final long serialVersionUID = 6266640446993571859L;

	private static final String PACKAGES_DELIMITER = ",; \t\n";

	protected List<String> packages;
	
	protected final Calendar lastModifiedCal = Calendar.getInstance();
	
	protected String encoding = "UTF-8";

	protected boolean serveStaticBrowserCache = true;

	@Override
	public void init() throws ServletException {
		this.packages = tokenizeToList(getInitParameter("packages"), ClasspathStaticContentServlet.PACKAGES_DELIMITER);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String contentPath = request.getPathInfo();
		
		for (final String pkg : packages) {
			final String pathEnding = buildPath(contentPath, pkg);
			final URL resourceUrl = findResource(pathEnding);
			
			if (resourceUrl != null) {
				InputStream is = null;
				
				try {
					//check that the resource path is under the pathPrefix path
					if (resourceUrl.getFile().endsWith(pathEnding)) {
						is = resourceUrl.openStream();
					}
				} catch (IOException ex) {
					// just ignore it
					continue;
				}

				//not inside the try block, as this could throw IOExceptions also
				if (is != null) {
					process(is, contentPath, request, response);
					return;
				}

			}
		}
	}
	
	protected void process(InputStream is, String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (is != null) {
			Calendar cal = Calendar.getInstance();

			// check for if-modified-since, prior to any other headers
			long ifModifiedSince = 0;
			
			try {
				ifModifiedSince = request.getDateHeader("If-Modified-Since");
			} catch (Exception e) {
				// ignore
			}
			
			long lastModifiedMillis = lastModifiedCal.getTimeInMillis();
			long now = cal.getTimeInMillis();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			long expires = cal.getTimeInMillis();

			if (ifModifiedSince > 0 && ifModifiedSince <= lastModifiedMillis) {
				// not modified, content is not sent - only basic
				// headers and status SC_NOT_MODIFIED
				response.setDateHeader("Expires", expires);
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				is.close();
				return;
			}

			final String contentType = MimeUtils.guessMimeTypeFromFilename(path);
			if (contentType != null) {
				response.setContentType(contentType);
			}

			if (serveStaticBrowserCache ) {
				// set heading information for caching static content
				response.setDateHeader("Date", now);
				response.setDateHeader("Expires", expires);
				response.setDateHeader("Retry-After", expires);
				response.setHeader("Cache-Control", "public");
				response.setDateHeader("Last-Modified", lastModifiedMillis);
			} else {
				response.setHeader("Cache-Control", "no-cache");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "-1");
			}

			try {
				copy(is, response.getOutputStream());
			} finally {
				is.close();
			}
		}
	}

    protected void copy(InputStream input, OutputStream output) throws IOException {
        final byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        output.flush();
    }
    
	protected URL findResource(String path) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return (url != null) ? url  : getClass().getClassLoader().getResource(path);
	}

	protected String buildPath(String contentPath, String pkg) throws UnsupportedEncodingException {
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
	
}
