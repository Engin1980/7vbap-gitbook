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
  @Column(unique = true, nullable = false, length = 64)
  private String email;
  private String passwordHash;

  @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
  private Collection<Url> urls;

  @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
  private Collection<Tag> tags;

  @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
  private Collection<Token> tokens;

  @Contract(pure = true)
  public AppUser(@NonNull String email) {
    ArgVal.matchRegex(email, ".+@.+", "email");
    this.email = email;
  }

  @PrePersist
  private void prePersistCheck(){
    if (email != null)
      email = email.toLowerCase();
  }
}
