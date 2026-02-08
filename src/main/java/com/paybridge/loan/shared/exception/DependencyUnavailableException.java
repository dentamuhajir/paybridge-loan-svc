package com.paybridge.loan.shared.exception;

public class DependencyUnavailableException extends RuntimeException {
  public DependencyUnavailableException(String message) {
    super(message);
  }
}
