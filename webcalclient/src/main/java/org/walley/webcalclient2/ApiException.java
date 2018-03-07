package org.walley.webcalclient2;

public class ApiException extends Exception {
  public ApiException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public ApiException(String detailMessage) {
    super(detailMessage);
  }
}
