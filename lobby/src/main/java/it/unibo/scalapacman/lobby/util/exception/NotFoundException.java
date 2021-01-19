package it.unibo.scalapacman.lobby.util.exception;

import it.unibo.scalapacman.lobby.C;

public class NotFoundException extends APIException {
  public NotFoundException() {
    super(C.HTTP.ResponseCode.NOT_FOUND, "Not Found");
  }
}
