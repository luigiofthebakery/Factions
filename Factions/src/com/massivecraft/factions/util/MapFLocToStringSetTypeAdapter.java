package com.massivecraft.factions.util;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.P;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MapFLocToStringSetTypeAdapter implements JsonDeserializer<Map<FLocation, Set<String>>>, JsonSerializer<Map<FLocation, Set<String>>> {
   public Map<FLocation, Set<String>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      try {
         JsonObject obj = json.getAsJsonObject();
         if (obj == null) {
            return null;
         } else {
            Map<FLocation, Set<String>> locationMap = new ConcurrentHashMap<>();

            for (Entry<String, JsonElement> entry : obj.entrySet()) {
               String worldName = entry.getKey();

               for (Entry<String, JsonElement> entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                  String[] coords = entry2.getKey().trim().split("[,\\s]+");
                  int x = Integer.parseInt(coords[0]);
                  int z = Integer.parseInt(coords[1]);
                  Set<String> nameSet = new HashSet<>();
                  Iterator<JsonElement> iter = entry2.getValue().getAsJsonArray().iterator();

                  while (iter.hasNext()) {
                     nameSet.add(iter.next().getAsString());
                  }

                  locationMap.put(new FLocation(worldName, x, z), nameSet);
               }
            }

            return locationMap;
         }
      } catch (Exception var16) {
         var16.printStackTrace();
         P.p.log(Level.WARNING, "Error encountered while deserializing a Map of FLocations to String Sets.");
         return null;
      }
   }

   public JsonElement serialize(Map<FLocation, Set<String>> src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();

      try {
         if (src != null) {
            for (Entry<FLocation, Set<String>> entry : src.entrySet()) {
               FLocation loc = entry.getKey();
               String locWorld = loc.getWorldName();
               Set<String> nameSet = entry.getValue();
               if (nameSet != null && !nameSet.isEmpty()) {
                  JsonArray nameArray = new JsonArray();
                  Iterator<String> iter = nameSet.iterator();

                  while (iter.hasNext()) {
                     JsonPrimitive nameElement = new JsonPrimitive(iter.next());
                     nameArray.add(nameElement);
                  }

                  if (!obj.has(locWorld)) {
                     obj.add(locWorld, new JsonObject());
                  }

                  obj.get(locWorld).getAsJsonObject().add(loc.getCoordString(), nameArray);
               }
            }
         }

         return obj;
      } catch (Exception var13) {
         var13.printStackTrace();
         P.p.log(Level.WARNING, "Error encountered while serializing a Map of FLocations to String Sets.");
         return obj;
      }
   }
}
