package cz.osu.vbap.favUrls.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.vbap.favUrls.model.entities.Url;
import lombok.Data;

@Data
public class UrlView {

  @JsonIgnoreProperties({"appUser", "tags"})
  private static class MixIn{
  }

  public static UrlView of(Url url) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(Url.class, MixIn.class);
    UrlView ret = mapper.convertValue(url, UrlView.class);
    return ret;
  }

  private int urlId;
  private String title;
  private String address;
}
