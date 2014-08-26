package com.sothr.imagetools.engine.errors;

/**
 * Simple Exception
 *
 * Created by drew on 12/31/13.
 */
public class ImageToolsException extends Exception {

    public ImageToolsException() { super(); }
    public ImageToolsException(String message) { super(message); }
    public ImageToolsException(String message, Throwable cause) { super(message, cause); }
    public ImageToolsException(Throwable cause) { super(cause); }

}
