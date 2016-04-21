package com.tars.util.exceptions;

/**
 * WTFException represents an unrecoverable error, a logical error that is not considered to ever happen in the system.
 */
@Deprecated
class WTFException extends RuntimeException {

  WTFException() {
    super();
  }

  WTFException(String message) {
    super(message);
  }

  WTFException(String message, Throwable cause) {
    super(message, cause);
  }

  WTFException(Throwable cause) {
    super(cause);
  }
}
