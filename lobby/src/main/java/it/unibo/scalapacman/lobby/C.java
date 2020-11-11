package it.unibo.scalapacman.lobby;

public class C {

  public static class HTTP{

    public static class ResponseCode {
      public static final int OK = 200;
      public static final int CREATED = 201;
      public static final int NOT_FOUND = 404;
    }

    public static class HeaderElement {
      public static final String CONTENT_TYPE = "content-type";

      public static class ContentType {
        public static final String APPLICATION_JSON = "application/json; charset=utf-8";
      }
    }
  }
}
