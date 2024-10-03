package cz.osu.vbap.favUrls.services;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Url;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import cz.osu.vbap.favUrls.model.repositories.UrlRepository;
import cz.osu.vbap.favUrls.services.exceptions.AppServiceException;
import cz.osu.vbap.favUrls.services.exceptions.BadDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrlService extends AppService {

  @Autowired
  private UrlRepository urlRepository;
  @Autowired
  private AppUserRepository appUserRepository;

  public Url create(int appUserId, String title, String address) throws AppServiceException {
    AppUser appUser = tryInvoke(() -> appUserRepository.findById(appUserId))
            .orElseThrow(() -> new BadDataException(this, "User not found."));

    Url url = new Url(appUser, title, address);
    tryInvoke(() -> urlRepository.save(url));
    return url;
  }

  public void delete(int urlId) throws AppServiceException {
    tryInvoke(() -> urlRepository.deleteById(urlId));
  }

  public List<Url> getByUser(int appUserId) throws AppServiceException {
    AppUser appUser = tryInvoke(() -> appUserRepository.findById(appUserId))
            .orElseThrow(() -> new BadDataException(this, "User not found."));
    List<Url> ret = tryInvoke(() -> urlRepository.findByAppUser(appUser));
    return ret;
  }
}
