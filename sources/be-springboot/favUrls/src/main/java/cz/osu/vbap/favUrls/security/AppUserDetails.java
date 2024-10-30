package cz.osu.vbap.favUrls.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;

@Getter
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {
  private final Collection<? extends GrantedAuthority> authorities = new HashSet<>();
  private final String username;
  private final String password = null;
}
