package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
}
