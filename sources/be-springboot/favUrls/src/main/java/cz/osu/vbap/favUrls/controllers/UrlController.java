package cz.osu.vbap.favUrls.controllers;

import cz.osu.vbap.favUrls.controllers.dto.UrlView;
import cz.osu.vbap.favUrls.controllers.exceptions.ForbiddenException;
import cz.osu.vbap.favUrls.model.entities.Url;
import cz.osu.vbap.favUrls.services.UrlService;
import cz.osu.vbap.favUrls.services.exceptions.AppServiceException;
import cz.osu.vbap.favUrls.services.exceptions.InvalidOrExpiredCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/v1/url")
public class UrlController  {

  @Autowired
  private UrlService urlService;

  @PostMapping
  public UrlView createUrl(int appUserId, String title, String address) throws AppServiceException {
    Url url = urlService.create(appUserId, title, address);
    UrlView ret = UrlView.of(url);
    return ret;
  }

  @GetMapping("/{appUserId}")
  public Collection<UrlView> getByUser(@PathVariable int appUserId, HttpServletRequest request) throws AppServiceException{

    int loggedAppUserId = (int) request.getAttribute("__appUserId");
    if (loggedAppUserId != appUserId) throw new ForbiddenException();

    List<Url> urls = urlService.getByUser(appUserId);
    List<UrlView> ret = urls.stream()
            .map(UrlView::of)
            .toList();
    return ret;
  }

  @DeleteMapping("/{urlId}")
  public void deleteUrl(@PathVariable int urlId) throws AppServiceException {
    urlService.delete(urlId);
  }
}
