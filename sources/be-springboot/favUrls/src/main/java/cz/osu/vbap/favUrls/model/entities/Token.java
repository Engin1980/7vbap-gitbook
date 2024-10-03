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
        @UniqueConstraint(columnNames = {"app_user_id", "type"}, name = "UQ_token_app_user_type")
})
public class Token {

  @Getter
  @AllArgsConstructor
  public enum Type {
    REFRESH('R'), PASSWORD_RESET('P');
    private final char code;
  }

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int tokenId;
  @Column(unique = true, nullable = false)
  private String value;
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Type type;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_token_app_user"))
  private AppUser appUser;

  @Contract(pure = true)
  public Token(AppUser appUser, Type type, String value) {
    ArgVal.notNull(appUser, "appUser");
    ArgVal.notNull(type, "type");
    ArgVal.notWhitespace(value, "value");

    this.type = type;
    this.value = value;
    this.createdAt = LocalDateTime.now();
    this.appUser = appUser;
  }
}
