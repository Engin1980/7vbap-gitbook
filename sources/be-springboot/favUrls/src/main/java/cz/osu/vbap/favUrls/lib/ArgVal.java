package cz.osu.vbap.favUrls.lib;

import org.jetbrains.annotations.Contract;

import java.util.function.Function;
import java.util.function.Supplier;

public class ArgVal {
  @Contract(pure = true)
  public static void notNull(Object value, String argName) {
    if (value == null) {
      throw new IllegalArgumentException(argName + " must not be null");
    }
  }

  @Contract(pure = true)
  public static void matchRegex(String text, String regex, String argName) {
    if (text == null || !text.matches(regex)) {
      throw new IllegalArgumentException(argName + " must match regex " + regex + ". Invalid value: " + text);
    }
  }

  @Contract(pure = true)
  public static void notWhitespace(String text, String argName) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException(argName + " must not be empty.");
    }

  }

  public static void isTrue(Supplier<Boolean> validator, String argName) {
    boolean res;
    try{
      res = validator.get();
      if (!res) throw new IllegalArgumentException(argName + " failed to pass 'isTrue' validation");
    }catch (Exception ex){
      throw new IllegalArgumentException(argName + " crashed when passing 'isTrue' validation");
    }
  }
}
