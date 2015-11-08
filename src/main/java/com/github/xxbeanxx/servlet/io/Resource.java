package com.github.xxbeanxx.servlet.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Greg Baker
 */
public class Resource {
	
	/** The last modified date */
	private static final long LAST_MODIFIED_MILLIS = Calendar.getInstance().getTimeInMillis();

	private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

	/** ClassLoader used to load the resource */
	private final ClassLoader classLoader;

	/** Path to the resource to load */
	private final String path;

	public Resource(String path) {
		this(path, (ClassLoader) null);
	}

	public Resource(String path, ClassLoader classLoader) {
		if (path == null) {
			throw new IllegalArgumentException("Path must not be null");
		}
		
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		this.path = path;
		this.classLoader = (classLoader != null ? classLoader : getDefaultClassLoader());
	}

	private ClassLoader getDefaultClassLoader() {
		ClassLoader classLoader = null;
		
		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		catch (final Throwable throwable) {
			LOGGER.debug("Caught exception acquiring thread context classloader", throwable);
		}
		
		if (classLoader == null) {
			classLoader = Resource.class.getClassLoader();
		}
			
		if (classLoader == null) {
			LOGGER.debug("Bootstrap context classloader detected, attempting to acquire system context classloader");
			classLoader = ClassLoader.getSystemClassLoader();
		}
		
		if (classLoader == null) {
			throw new NullPointerException("Could not acquire application classloader");
		}
		
		return classLoader;
	}

	public boolean exists() {
		return this.classLoader.getResource(this.path) != null;
	}

	public InputStream getInputStream() throws IOException {
		final InputStream inputStream = this.classLoader.getResourceAsStream(this.path);
		
		if (inputStream == null) {
			throw new FileNotFoundException(this.path + " cannot be opened because it does not exist");
		}
		
		return inputStream;
	}

	public long getLastModified() {
		return Resource.LAST_MODIFIED_MILLIS;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public URL getUrl() {
		return this.classLoader.getResource(this.path);
	}

}
