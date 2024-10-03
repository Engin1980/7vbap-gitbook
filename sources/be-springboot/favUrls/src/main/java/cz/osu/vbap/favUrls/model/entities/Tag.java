package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.util.Collection;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Tag {
  private final static int COLOR_LENGTH = 3;

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int tagId;
  @Column(nullable = false)
  private String title;
  @Column(nullable = false, length = COLOR_LENGTH)
  @Size(min = COLOR_LENGTH, max = COLOR_LENGTH)
  private String color;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_tag_app_user"))
  private AppUser appUser;

  @ManyToMany(mappedBy = "tags")
  private Collection<Url> urls;

  @Contract(pure = true)
  public Tag(AppUser appUser, String title, String color) {
    ArgVal.notNull(appUser, "user");
    ArgVal.notWhitespace(title, "title");
    ArgVal.matchRegex(color, "[0-9a-fA-F]{" + COLOR_LENGTH + "}", "color");

    this.title = title;
    this.appUser = appUser;
    this.color = color;
  }
}
