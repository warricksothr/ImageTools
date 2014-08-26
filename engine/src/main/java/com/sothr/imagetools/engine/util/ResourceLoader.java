package com.sothr.imagetools.engine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Seamlessly handle resource loading
 * <p/>
 * Created by drew on 1/5/14.
 */
public class ResourceLoader {

  private static final ResourceLoader instance = new ResourceLoader();

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private ResourceLoader() {
    logger.info("Created Resource Loader");
  }

  public static ResourceLoader get() {
    return instance;
  }

  public URL getResource(String location) {
    logger.debug(String.format("Attempting to load resource: %s", location));
    return Thread.currentThread().getContextClassLoader().getResource(location);
  }

  public InputStream getResourceStream(String location) {
    logger.debug(String.format("Attempting to get stream for resource: %s", location));
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
  }

}
