package it.unibo.scalapacman.lobby.util.exception;

public class APIException extends RuntimeException {
  private final int code;

  public APIException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
