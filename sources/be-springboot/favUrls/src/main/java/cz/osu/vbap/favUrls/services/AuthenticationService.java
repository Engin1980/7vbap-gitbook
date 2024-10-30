package cz.osu.vbap.favUrls.services;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Token;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import cz.osu.vbap.favUrls.model.repositories.TokenRepository;
import cz.osu.vbap.favUrls.security.JwtTokenUtil;
import cz.osu.vbap.favUrls.services.exceptions.AppServiceException;
import cz.osu.vbap.favUrls.services.exceptions.BadRequestException;
import cz.osu.vbap.favUrls.services.exceptions.InternalException;
import cz.osu.vbap.favUrls.services.exceptions.InvalidOrExpiredCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService extends AppService {
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private TokenRepository tokenRepository;
  @Autowired
  private AppUserRepository appUserRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;


  public record LoginResponse(String refreshToken, String accessToken, AppUser appUser) {
  }

  public LoginResponse login(String email, String password) throws AppServiceException {
    LoginResponse ret;
    Optional<AppUser> appUserOpt = tryInvoke(() -> appUserRepository.findByEmail(email));

    if (appUserOpt.isEmpty() || !isValidCredentials(appUserOpt.get(), password))
      throw new BadRequestException(this, "Invalid credentials.");

    AppUser appUser = appUserOpt.get();
    String refreshToken = jwtTokenUtil.generateRefreshToken(appUser.getEmail(), appUser.getAppUserId());
    String accessToken = jwtTokenUtil.generateAccessToken(refreshToken);
    try {
      storeRefreshToken(appUser, refreshToken);
    } catch (Exception e) {
      throw new InternalException(this, "Failed to login", e);
    }
    ret = new LoginResponse(refreshToken, accessToken, appUser);

    return ret;
  }

  public AppUser register(String email, String password) throws AppServiceException {
    if (tryInvoke(() -> appUserRepository.findByEmail(email)).isPresent())
      throw new BadRequestException(this, "User already exists.");

    AppUser user = new AppUser(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    tryInvoke(() -> appUserRepository.save(user));

    return user;
  }

  private boolean isValidCredentials(AppUser appUser, String password) {
    return passwordEncoder.matches(password, appUser.getPasswordHash());
  }

  private void deleteRefreshToken(String refreshToken) {
    tokenRepository.findByValue(refreshToken).ifPresent(tokenRepository::delete);
  }

  public void logout(String refreshToken) throws InternalException {
    tryInvoke(() -> deleteRefreshToken(refreshToken));
  }

  public String refreshAccessToken(String refreshToken) throws AppServiceException {
    String ret;

    Optional<Token> tokenOpt = tryInvoke(() -> tokenRepository.findByValue(refreshToken));

    if (tokenOpt.isEmpty() || !jwtTokenUtil.isValid(refreshToken))
      throw new InvalidOrExpiredCredentialsException(this);

    ret = jwtTokenUtil.generateAccessToken(refreshToken);

    return ret;
  }

  private void storeRefreshToken(AppUser appUser, String refreshToken) {
    // exceptions handled at upper level
    tokenRepository.findByAppUser(appUser).ifPresent(tokenRepository::delete);
    Token token = new Token(appUser, refreshToken);
    tokenRepository.save(token);
  }
}
