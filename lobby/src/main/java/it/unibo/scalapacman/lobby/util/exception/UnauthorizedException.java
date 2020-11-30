package it.unibo.scalapacman.lobby.util.exception;

import it.unibo.scalapacman.lobby.C;

public class UnauthorizedException extends APIException {
  public UnauthorizedException(String message) {
    super(C.HTTP.ResponseCode.UNAUTHORIZED, message);
  }
}
