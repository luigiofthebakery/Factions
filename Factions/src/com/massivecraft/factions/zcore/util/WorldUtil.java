package com.massivecraft.factions.zcore.util;

import java.io.File;
import org.bukkit.Bukkit;

public class WorldUtil {
   public static boolean isWorldLoaded(String name) {
      return Bukkit.getServer().getWorld(name) != null;
   }

   public static boolean doesWorldExist(String name) {
      return new File(name, "level.dat").exists();
   }
}
