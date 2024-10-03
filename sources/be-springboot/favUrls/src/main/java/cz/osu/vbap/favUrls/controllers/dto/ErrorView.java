package cz.osu.vbap.favUrls.controllers.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorView {
  public final String message;
  public final LocalDateTime createdAt = LocalDateTime.now();
}
