package cz.osu.vbap.favUrls.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.vbap.favUrls.model.entities.AppUser;
import lombok.Data;

@Data
public class AppUserView {
  @JsonIgnoreProperties({"urls", "tags", "tokens", "passwordHash"})
  private static class MixIn{
  }

  public static AppUserView of(AppUser appUser) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(AppUser.class, MixIn.class);
    AppUserView ret = mapper.convertValue(appUser, AppUserView.class);
    return ret;
  }

  private int appUserId;
  private String email;
}
