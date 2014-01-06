package com.sothr.imagetools.util;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by drew on 1/5/14.
 */
public class FileLoader {

    private static final FileLoader instance = new FileLoader();

    private FileLoader() {}

    public static FileLoader get() {
        return instance;
    }

    public URL getResource(String location) {
        return Thread.currentThread().getContextClassLoader().getResource(location);
    }

    public InputStream getResourceStream(String location) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    }

}