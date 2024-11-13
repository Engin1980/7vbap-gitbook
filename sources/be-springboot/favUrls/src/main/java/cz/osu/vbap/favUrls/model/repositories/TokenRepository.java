package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
  Optional<Token> findByAppUser(AppUser appUser);

  Optional<Token> findByValue(String value);

  /**
   * Deletes a token by its value. Thread-safe option.
   *
   * @param value the value of the token to delete
   */
  @Query(value = "delete from Token where value = ?1", nativeQuery = true)
  void deleteByValue(String value);
  void deleteByAppUser(AppUser appUser);
}
