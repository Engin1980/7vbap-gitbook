package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;

public class InvalidOrExpiredCredentialsException extends AppServiceException {
  public InvalidOrExpiredCredentialsException(AppService source) {
    super(source, "Invalid or expired credentials.");
  }
}
