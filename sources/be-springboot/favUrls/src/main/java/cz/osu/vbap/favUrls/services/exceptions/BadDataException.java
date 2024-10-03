package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;

public class BadDataException extends BadRequestException {
  public BadDataException (AppService service, String message) {
    super(service, message);
  }
}
