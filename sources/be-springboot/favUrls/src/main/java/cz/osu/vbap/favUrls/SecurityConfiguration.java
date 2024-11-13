package cz.osu.vbap.favUrls;

import cz.osu.vbap.favUrls.security.AuthenticationJwtFilter;
import cz.osu.vbap.favUrls.security.CsrfCookieFilter;
import cz.osu.vbap.favUrls.security.SpaCsrfTokenRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Autowired private AuthenticationJwtFilter authenticationJwtFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    CookieCsrfTokenRepository cookieCsrfTokenRepository = new CookieCsrfTokenRepository();
    cookieCsrfTokenRepository.setCookieCustomizer(q -> {
      q.httpOnly(false);
      q.sameSite("Strict");
      q.secure(true);
    });
    http.csrf(q -> q
            .csrfTokenRepository(cookieCsrfTokenRepository)
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()));
    http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

    http.cors(q -> {
      CorsConfiguration corsConfiguration = new CorsConfiguration();
      corsConfiguration.addAllowedOrigin("http://localhost:3000");
      corsConfiguration.addAllowedMethod("*");
      corsConfiguration.addAllowedHeader("*");
      corsConfiguration.setAllowCredentials(true);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", corsConfiguration);

      q.configurationSource(source);
    });

    http.authorizeHttpRequests(q -> q
            .requestMatchers("/v1/appUser/login").permitAll()
            .requestMatchers("v1/appUser/refresh").permitAll()
            .requestMatchers("v1/appUser/logout").permitAll()
            .requestMatchers("/v1/appUser/register").permitAll()
            .requestMatchers("/**").authenticated());
    http.addFilterBefore(authenticationJwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
