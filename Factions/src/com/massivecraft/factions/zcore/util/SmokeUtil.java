package com.massivecraft.factions.zcore.util;

import java.util.Collection;
import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;

public class SmokeUtil {
   public static Random random = new Random();

   public static void spawnSingle(Location location, int direction) {
      if (location != null) {
         location.getWorld().playEffect(location.clone(), Effect.SMOKE, direction);
      }
   }

   public static void spawnSingle(Location location) {
      spawnSingle(location, 4);
   }

   public static void spawnSingleRandom(Location location) {
      spawnSingle(location, random.nextInt(9));
   }

   public static void spawnCloudSimple(Location location) {
      for (int i = 0; i <= 8; i++) {
         spawnSingle(location, i);
      }
   }

   public static void spawnCloudSimple(Collection<Location> locations) {
      for (Location location : locations) {
         spawnCloudSimple(location);
      }
   }

   public static void spawnCloudRandom(Location location, float thickness) {
      int singles = (int)Math.floor(thickness * 9.0F);

      for (int i = 0; i < singles; i++) {
         spawnSingleRandom(location.clone());
      }
   }

   public static void spawnCloudRandom(Collection<Location> locations, float thickness) {
      for (Location location : locations) {
         spawnCloudRandom(location, thickness);
      }
   }
}
