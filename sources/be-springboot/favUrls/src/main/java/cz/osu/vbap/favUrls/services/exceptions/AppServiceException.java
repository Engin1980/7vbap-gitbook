package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;
import lombok.Getter;

@Getter
public abstract class AppServiceException extends Exception {
  private final AppService source;

  public AppServiceException(AppService source, String message) {
    super(message);
    this.source = source;
  }

  public AppServiceException(AppService source, String message, Throwable cause) {
    super(message, cause);
    this.source = source;
  }
}
