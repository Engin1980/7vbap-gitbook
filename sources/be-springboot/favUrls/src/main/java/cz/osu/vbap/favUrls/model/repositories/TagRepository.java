package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
}
