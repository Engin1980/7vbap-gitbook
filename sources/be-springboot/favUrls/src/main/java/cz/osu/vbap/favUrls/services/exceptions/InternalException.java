package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;

public class InternalException extends AppServiceException {
  public InternalException(AppService source, String message, Throwable cause) {
    super(source, message, cause);
  }
}
