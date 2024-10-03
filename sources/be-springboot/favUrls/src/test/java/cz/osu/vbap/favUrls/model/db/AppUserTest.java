package cz.osu.vbap.favUrls.model.db;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@TestPropertySource(locations =
        {"classpath:application.properties", "classpath:test.properties"})
public class AppUserTest {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  AppUserRepository appUserRepository;

  @Test
  void targetDbTest(){
    // only for demonstration purposes
    System.out.println("DB URL: " + dbUrl);
  }

  @Test()
  void duplicateUser() {

    AppUser a = new AppUser("john.doe@osu.cz");
    appUserRepository.save(a);

    AppUser b = new AppUser("jane.doe@osu.cz");
    appUserRepository.save(b);

    AppUser c = new AppUser("jane.doe@osu.cz");
    try {
      appUserRepository.save(c);
      fail("Duplicate email should not be saved");
    } catch (DataIntegrityViolationException ex) {
      assertTrue(ex.getMessage().toLowerCase().contains("duplicate"));
      assertTrue(ex.getMessage().toLowerCase().contains("jane.doe@osu.cz"));
    }
  }

  @Test()
  void emailStoredLowerCase(){
    String email = "MIKE.WHITE@OSU.CZ";
    AppUser a = new AppUser(email);
    appUserRepository.save(a);
    int aId = a.getAppUserId();

    Optional<AppUser> oa = appUserRepository.findById(aId);
    assertTrue(oa.isPresent());
    assertEquals(oa.get().getEmail(), email.toLowerCase());
  }
}
