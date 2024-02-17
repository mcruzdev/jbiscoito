package dev.matheuscruz;

public class NoStackTraceException extends RuntimeException {
  public NoStackTraceException(final String message) {
    super(message);
  }
}
