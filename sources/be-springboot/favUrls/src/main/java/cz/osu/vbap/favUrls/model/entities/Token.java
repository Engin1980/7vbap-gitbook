package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"app_user_id"}, name = "UQ_token_app_user")
})
public class Token {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int tokenId;
  @Column(unique = true, nullable = false)
  private String value;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_token_app_user"))
  private AppUser appUser;

  @Contract(pure = true)
  public Token(AppUser appUser, String value) {
    ArgVal.notNull(appUser, "appUser");
    ArgVal.notWhitespace(value, "value");

    this.value = value;
    this.appUser = appUser;
  }
}
