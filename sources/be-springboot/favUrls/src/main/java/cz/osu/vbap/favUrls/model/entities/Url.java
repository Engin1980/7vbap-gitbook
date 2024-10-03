package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Url {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int urlId;
  @Column(nullable = false, length = 256)
  private String title;
  @Column(nullable = false)
  private String address;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_url_app_user"))
  private AppUser user;

  @ManyToMany
  @JoinTable(name = "url_tag",
          joinColumns = @JoinColumn(name = "url_id", foreignKey = @ForeignKey(name = "FK_url_tag_url")),
          inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "FK_url_tag_tag")))
  private Collection<Tag> tags;

  @Contract(pure = true)
  public Url(AppUser user, String title, String address, Tag... tags) {
    ArgVal.notNull(user, "user");
    ArgVal.notWhitespace(title, "title");
    ArgVal.isTrue(() -> title.length() <= 256, "Title must have 256 characters at most.");
    ArgVal.notWhitespace(address, "address");

    this.user = user;
    this.title = title;
    this.address = address;
    if (tags.length > 0) {
      this.tags = List.of(tags);
    }
  }
}
