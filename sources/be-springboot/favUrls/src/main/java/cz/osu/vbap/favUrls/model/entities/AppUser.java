package cz.osu.vbap.favUrls.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Contract;
import cz.osu.vbap.favUrls.lib.ArgVal;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AppUser {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int appUserId;
  @Column(unique = true, nullable = false)
  private String email;
  private String passwordHash;

  @OneToMany(mappedBy = "user")
  private Collection<Url> urls;

  @OneToMany(mappedBy = "user")
  private Collection<Tag> tags;

  @Contract(pure = true)
  public AppUser(@NonNull String email) {
    ArgVal.matchRegex(email, ".+@.+", "email");

    this.email = email;
  }
}
