package com.massivecraft.factions.util;

import com.massivecraft.factions.P;
import java.lang.reflect.Type;
import java.util.logging.Level;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MyLocationTypeAdapter implements JsonDeserializer<LazyLocation>, JsonSerializer<LazyLocation> {
   private static final String WORLD = "world";
   private static final String X = "x";
   private static final String Y = "y";
   private static final String Z = "z";
   private static final String YAW = "yaw";
   private static final String PITCH = "pitch";

   public LazyLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      try {
         JsonObject obj = json.getAsJsonObject();
         String worldName = obj.get("world").getAsString();
         double x = obj.get("x").getAsDouble();
         double y = obj.get("y").getAsDouble();
         double z = obj.get("z").getAsDouble();
         float yaw = obj.get("yaw").getAsFloat();
         float pitch = obj.get("pitch").getAsFloat();
         return new LazyLocation(worldName, x, y, z, yaw, pitch);
      } catch (Exception var14) {
         var14.printStackTrace();
         P.p.log(Level.WARNING, "Error encountered while deserializing a LazyLocation.");
         return null;
      }
   }

   public JsonElement serialize(LazyLocation src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();

      try {
         obj.addProperty("world", src.getWorldName());
         obj.addProperty("x", src.getX());
         obj.addProperty("y", src.getY());
         obj.addProperty("z", src.getZ());
         obj.addProperty("yaw", src.getYaw());
         obj.addProperty("pitch", src.getPitch());
         return obj;
      } catch (Exception var6) {
         var6.printStackTrace();
         P.p.log(Level.WARNING, "Error encountered while serializing a LazyLocation.");
         return obj;
      }
   }
}
