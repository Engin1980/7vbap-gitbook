package cz.osu.vbap.favUrls;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Tag;
import cz.osu.vbap.favUrls.model.entities.Url;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import cz.osu.vbap.favUrls.model.repositories.TagRepository;
import cz.osu.vbap.favUrls.model.repositories.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FavUrlsApplication {

  public static void main(String[] args) {
    SpringApplication.run(FavUrlsApplication.class, args);
  }


  /**
   * Initializes the database with sample data.
   *
   * @param appUserRepository the repository for managing `AppUser` entities
   * @param tagRepository the repository for managing `Tag` entities
   * @param urlRepository the repository for managing `Url` entities
   * @return a `CommandLineRunner` that initializes the database
   */
  @Bean
  public CommandLineRunner initDatabase(
          @Autowired AppUserRepository appUserRepository,
          @Autowired TagRepository tagRepository,
          @Autowired UrlRepository urlRepository) {
    return _ -> {

      if (appUserRepository.findByEmail("marek.vajgl@osu.cz").isPresent())
        return; // data already exist

      AppUser user = new AppUser("marek.vajgl@osu.cz");
      appUserRepository.save(user);

      Tag privateTag = new Tag(user, "private", "F00");
      tagRepository.save(privateTag);

      Tag publicTag = new Tag(user, "public", "0F0");
      tagRepository.save(publicTag);

      Url url = new Url(user, "OU", "https://www.osu.cz", privateTag, publicTag);
      urlRepository.save(url);
    };
  }
}
