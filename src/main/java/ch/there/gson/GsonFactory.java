package ch.there.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonFactory {
  private static class ClassSerializer implements JsonSerializer<Class<?>> {
    @Override
    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getName());
    }
  }

  private static class ClassDeserializer implements JsonDeserializer<Class<?>> {
    @Override
    public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return Class.forName(json.getAsJsonPrimitive().getAsString());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static Gson getGson() {
    GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(Class.class, new ClassSerializer());
    gson.registerTypeAdapter(Class.class, new ClassDeserializer());
    return gson.create();
  }
}
