package org.walley.webcalclient2;

public class LoginException extends Exception {
  public LoginException(String detailMessage, Throwable throwable) {
    super(detailMessage, throwable);
  }

  public LoginException(String detailMessage) {
    super(detailMessage);
  }
}
