package cz.osu.vbap.favUrls.model.converters;

import cz.osu.vbap.favUrls.model.entities.Token;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class TokenTypeConverter
        implements AttributeConverter<Token.Type, Character> {
  @Override
  public Character convertToDatabaseColumn(Token.Type type) {
    Character ret = type == null ? null : type.getCode();
    return ret;
  }

  @Override
  public Token.Type convertToEntityAttribute(Character character) {
    Token.Type ret;
    if (character == null)
      ret = null;
    else
      ret = Arrays.stream(Token.Type.values())
              .filter(t -> t.getCode() == character)
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Unknown token type: " + character));
    return ret;
  }
}
