package it.unibo.scalapacman.lobby.util.exception;

import it.unibo.scalapacman.lobby.C;

public class ConflictException extends APIException {
  public ConflictException(String message) {
    super(C.HTTP.ResponseCode.CONFLICT, message);
  }
}
