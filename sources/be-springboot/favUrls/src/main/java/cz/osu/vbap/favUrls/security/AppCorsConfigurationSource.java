package cz.osu.vbap.favUrls.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Component
public class AppCorsConfigurationSource implements CorsConfigurationSource {
  @Override
  public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
    CorsConfiguration ret = new CorsConfiguration();

    ret.addAllowedOrigin("http://localhost:3000");
    ret.addAllowedMethod(HttpMethod.GET);
    ret.addAllowedMethod(HttpMethod.POST);
    ret.addAllowedMethod(HttpMethod.PATCH);
    ret.addAllowedMethod(HttpMethod.DELETE);
    ret.addAllowedHeader("*");
    ret.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", ret);

    return ret;
  }
}
