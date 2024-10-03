package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrlRepository extends JpaRepository<Url, Integer> {
    List<Url> findByAppUser(AppUser appUser);
}
