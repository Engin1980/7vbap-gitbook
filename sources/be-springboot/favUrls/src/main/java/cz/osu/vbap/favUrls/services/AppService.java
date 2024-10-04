package cz.osu.vbap.favUrls.services;

import cz.osu.vbap.favUrls.lib.ArgVal;
import cz.osu.vbap.favUrls.services.exceptions.InternalException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class AppService {

  protected void tryInvoke(Runnable runnable) throws InternalException {
    ArgVal.notNull(runnable, "runnable");
    try {
      runnable.run();
    } catch (Exception e) {
      logger.error("Error in 'tryInvoke'", e);
      throw new InternalException(this, "Error in 'tryInvoke'", e);
    }
  }

  protected <T> T tryInvoke(Supplier<T> supplier) throws InternalException {
    T ret;
    ArgVal.notNull(supplier, "supplier");
    try {
      ret = supplier.get();
    } catch (Exception e) {
      logger.error("Error in 'tryInvoke'", e);
      throw new InternalException(this, "Error in 'tryInvoke'", e);
    }
    return ret;
  }

  @Getter
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
}
