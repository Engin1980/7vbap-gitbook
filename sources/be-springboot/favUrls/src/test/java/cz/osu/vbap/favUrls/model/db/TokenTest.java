package cz.osu.vbap.favUrls.model.db;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Token;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import cz.osu.vbap.favUrls.model.repositories.TokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest()
@TestPropertySource(locations =
        {"classpath:application.properties", "classpath:test.properties"})
public class TokenTest {

  @Autowired
  AppUserRepository appUserRepository;
  @Autowired
  TokenRepository tokenRepository;

  @Test
  void tokenValueUnique(){
    AppUser a = new AppUser("tokenTest@seznam.cz");
    appUserRepository.save(a);

    Token token;
    token = new Token(a, Token.Type.PASSWORD_RESET, "duplicityTest");
    tokenRepository.save(token);

    token = new Token(a, Token.Type.PASSWORD_RESET, "duplicityTest");
    try {
      tokenRepository.save(token);
      fail("Successfully saved duplicate token value, what should not be permitted");
    } catch (Exception e) {
    }
  }

  @Test
  void tokenUserAndTypeUnique(){
    AppUser a = new AppUser("anotherTokenTest@seznam.cz");
    appUserRepository.save(a);

    Token token;
    token = new Token(a, Token.Type.PASSWORD_RESET, "otherDuplicityTest");
    tokenRepository.save(token);

    token = new Token(a, Token.Type.PASSWORD_RESET, "anotherDuplicityTest");
    try {
      tokenRepository.save(token);
      fail("Successfully saved duplicate token value, what should not be permitted");
    } catch (Exception e) {
    }
  }
}
