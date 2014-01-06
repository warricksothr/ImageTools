package com.sothr.imagetools.util;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by drew on 1/5/14.
 */
public class ResourceLoader {

    private static final ResourceLoader instance = new ResourceLoader();

    private ResourceLoader() {}

    public static ResourceLoader get() {
        return instance;
    }

    public URL getResource(String location) {
        return Thread.currentThread().getContextClassLoader().getResource(location);
    }

    public InputStream getResourceStream(String location) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    }

}