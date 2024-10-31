package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
  Optional<Token> findByAppUser(AppUser appUser);

  Optional<Token> findByValue(String value);

  void deleteByValue(String value);
  void deleteByAppUser(AppUser appUser);
}
