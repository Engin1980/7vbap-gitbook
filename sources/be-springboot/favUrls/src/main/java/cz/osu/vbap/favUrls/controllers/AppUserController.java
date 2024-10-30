package cz.osu.vbap.favUrls.controllers;

import cz.osu.vbap.favUrls.controllers.dto.AppUserView;
import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.security.AuthenticationJwtFilter;
import cz.osu.vbap.favUrls.services.AuthenticationService;
import cz.osu.vbap.favUrls.services.exceptions.AppServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/v1/appUser")
public class AppUserController {
  @Value("${app.security.refreshTokenExpirationSeconds}")
  private int refreshTokenExpirationInSeconds;
  @Value("${app.security.accessTokenExpirationSeconds}")
  private int accessTokenExpirationInSeconds;
  @Autowired
  private AuthenticationService authenticationService;

  private static final String ACCESS_TOKEN_COOKIE_NAME = AuthenticationJwtFilter.ACCESS_TOKEN_COOKIE_NAME;
  private static final String REFRESH_TOKEN_COOKIE_NAME = AuthenticationJwtFilter.REFRESH_TOKEN_COOKIE_NAME;

  private Cookie buildTokenCookie(String name, String value, int expiration) {
    final Cookie ret = new Cookie(name, value);
    ret.setHttpOnly(true);
    ret.setPath("/"); // this is super-important
    ret.setMaxAge(expiration);
    ret.setAttribute("SameSite", "Strict");
    return ret;
  }

  private void deleteTokenCookies(HttpServletResponse response) {
    final Cookie accessTokenCookie = buildTokenCookie(ACCESS_TOKEN_COOKIE_NAME, null, 0);
    response.addCookie(accessTokenCookie);
    final Cookie refreshTokenCookie = buildTokenCookie(REFRESH_TOKEN_COOKIE_NAME, null, 0);
    response.addCookie(refreshTokenCookie);
  }

  @PostMapping
  public AppUserView register(String email, String password) throws AppServiceException {
    AppUser appUser = authenticationService.register(email, password);
    AppUserView ret = AppUserView.of(appUser);
    return ret;
  }

  @PostMapping("/login")
  public AppUserView login(String email, String password, HttpServletResponse response) throws AppServiceException {
    AppUserView ret;
    AuthenticationService.LoginResponse tmp;
    try {
      tmp = authenticationService.login(email, password);

      final Cookie accessTokenCookie = buildTokenCookie(ACCESS_TOKEN_COOKIE_NAME, tmp.accessToken(), accessTokenExpirationInSeconds);
      response.addCookie(accessTokenCookie);

      final Cookie refreshTokenCookie = buildTokenCookie(REFRESH_TOKEN_COOKIE_NAME, tmp.refreshToken(), refreshTokenExpirationInSeconds);
      response.addCookie(refreshTokenCookie);

      ret = AppUserView.of(tmp.appUser());
    } catch (Exception ex) {
      deleteTokenCookies(response);
      throw ex;
    }

    return ret;
  }

  @PostMapping(path = "/refresh")
  public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws AppServiceException {
    Optional<Cookie> refreshTokenCookie = Arrays.stream(request.getCookies())
            .filter(q -> q.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
            .findFirst();
    if (refreshTokenCookie.isPresent()) {
      String accessToken = authenticationService.refreshAccessToken(refreshTokenCookie.get().getValue());

      final Cookie accessTokenCookie = buildTokenCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpirationInSeconds);
      response.addCookie(accessTokenCookie);
    } else {
      deleteTokenCookies(response);
    }
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) throws AppServiceException {
    Optional<String> optExistingRefreshToken = Arrays.stream(request.getCookies())
            .filter(q -> q.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
            .findFirst().map(q -> q.getValue());

    //TODO resolve if exception changes already set cookies
    deleteTokenCookies(response);

    if (optExistingRefreshToken.isPresent()) {
      authenticationService.logout(optExistingRefreshToken.get());
    }
  }
}
