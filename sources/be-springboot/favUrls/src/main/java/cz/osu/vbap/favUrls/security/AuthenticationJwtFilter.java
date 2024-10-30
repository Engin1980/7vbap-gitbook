package cz.osu.vbap.favUrls.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AuthenticationJwtFilter extends OncePerRequestFilter {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final String APP_USER_ID_REQUEST_ATTRIBUTE_NAME = "__appUserId";

  private enum TokenState {
    NO_TOKEN,
    VALID,
    INVALID,
    ERROR
  }

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationJwtFilter.class);

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain)
          throws ServletException, IOException {

    logger.debug("AuthenticationJwtFilter invoked");

    String jwt = tryExtractJwtFromRequest(request);
    TokenState state;
    if (jwt == null || jwt.isEmpty()) {
      state = TokenState.NO_TOKEN;
    } else if (jwtTokenUtil.isValid(jwt)) {
      state = TokenState.VALID;
    } else {
      state = TokenState.INVALID;
    }

    if (state == TokenState.VALID) {
      try {
        processValidToken(request, jwt);
      } catch (Exception ex) {
        logger.error("Failed to process authentication procedure: {}", ex.toString());
        state = TokenState.ERROR;
      }
    }

    logger.info("JWT for {} is : {}", request.getRequestURL(), state);

    switch (state) {
      case ERROR:
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        break;
      case INVALID:
      case VALID:
      case NO_TOKEN:
        filterChain.doFilter(request, response);
        break;
      default:
        throw new java.lang.EnumConstantNotPresentException(TokenState.class, state.toString());
    }
  }

  private void processValidToken(HttpServletRequest request, String jwt) {
    String email = jwtTokenUtil.getSubject(jwt);

    AppUserDetails userDetails = new AppUserDetails(email);
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    int appUserId = jwtTokenUtil.getAppUserId(jwt);
    request.setAttribute(APP_USER_ID_REQUEST_ATTRIBUTE_NAME, appUserId);
  }

  private String tryExtractJwtFromRequest(HttpServletRequest request) {
    String ret = null;
    if (request.getCookies() != null) {
      ret = Arrays.stream(request.getCookies())
              .filter(q -> q.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
              .findFirst()
              .map(q -> q.getValue()).orElse(null);
    }
    return ret;
  }
}
