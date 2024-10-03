package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;

public class BadRequestException extends AppServiceException {
  public BadRequestException(AppService service, String message) {
    super(service, message);
  }
}
