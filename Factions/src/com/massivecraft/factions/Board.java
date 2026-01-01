package com.massivecraft.factions;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.util.AsciiCompass;
import com.massivecraft.factions.zcore.util.DiscUtil;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

public class Board {
   private static transient File file = new File(P.p.getDataFolder(), "board.json");
   private static transient Map<FLocation, String> flocationIds = new ConcurrentHashMap<>();

   public static String getIdAt(FLocation flocation) {
      return !flocationIds.containsKey(flocation) ? "0" : flocationIds.get(flocation);
   }

   public static Faction getFactionAt(FLocation flocation) {
      return Factions.i.get(getIdAt(flocation));
   }

   public static void setIdAt(String id, FLocation flocation) {
      clearOwnershipAt(flocation);
      if (id == "0") {
         removeAt(flocation);
      }

      flocationIds.put(flocation, id);
   }

   public static void setFactionAt(Faction faction, FLocation flocation) {
      setIdAt(faction.getId(), flocation);
   }

   public static void removeAt(FLocation flocation) {
      clearOwnershipAt(flocation);
      flocationIds.remove(flocation);
   }

   public static void clearOwnershipAt(FLocation flocation) {
      Faction faction = getFactionAt(flocation);
      if (faction != null && faction.isNormal()) {
         faction.clearClaimOwnership(flocation);
      }
   }

   public static void unclaimAll(String factionId) {
      Faction faction = Factions.i.get(factionId);
      if (faction != null && faction.isNormal()) {
         faction.clearAllClaimOwnership();
      }

      Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<FLocation, String> entry = iter.next();
         if (entry.getValue().equals(factionId)) {
            if (Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled()) {
               LWCFeatures.clearAllChests(entry.getKey());
            }

            iter.remove();
         }
      }
   }

   public static boolean isBorderLocation(FLocation flocation) {
      Faction faction = getFactionAt(flocation);
      FLocation a = flocation.getRelative(1, 0);
      FLocation b = flocation.getRelative(-1, 0);
      FLocation c = flocation.getRelative(0, 1);
      FLocation d = flocation.getRelative(0, -1);
      return faction != getFactionAt(a) || faction != getFactionAt(b) || faction != getFactionAt(c) || faction != getFactionAt(d);
   }

   public static boolean isConnectedLocation(FLocation flocation, Faction faction) {
      FLocation a = flocation.getRelative(1, 0);
      FLocation b = flocation.getRelative(-1, 0);
      FLocation c = flocation.getRelative(0, 1);
      FLocation d = flocation.getRelative(0, -1);
      return faction == getFactionAt(a) || faction == getFactionAt(b) || faction == getFactionAt(c) || faction == getFactionAt(d);
   }

   public static void clean() {
      Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<FLocation, String> entry = iter.next();
         if (!Factions.i.exists(entry.getValue())) {
            if (Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled()) {
               LWCFeatures.clearAllChests(entry.getKey());
            }

            P.p.log("Board cleaner removed " + entry.getValue() + " from " + entry.getKey());
            iter.remove();
         }
      }
   }

   public static int getFactionCoordCount(String factionId) {
      int ret = 0;

      for (String thatFactionId : flocationIds.values()) {
         if (thatFactionId.equals(factionId)) {
            ret++;
         }
      }

      return ret;
   }

   public static int getFactionCoordCount(Faction faction) {
      return getFactionCoordCount(faction.getId());
   }

   public static int getFactionCoordCountInWorld(Faction faction, String worldName) {
      String factionId = faction.getId();
      int ret = 0;

      for (Entry<FLocation, String> entry : flocationIds.entrySet()) {
         if (entry.getValue().equals(factionId) && entry.getKey().getWorldName().equals(worldName)) {
            ret++;
         }
      }

      return ret;
   }

   public static ArrayList<String> getMap(RelationParticipator player, FLocation flocation, double inDegrees) {
      Faction faction;
      if (player instanceof FPlayer) {
         faction = ((FPlayer)player).getFaction();
      } else if (player instanceof Faction) {
         faction = (Faction)player;
      } else {
         return new ArrayList<>();
      }

      ArrayList<String> ret = new ArrayList<>();
      Faction factionLoc = getFactionAt(flocation);
      ret.add(P.p.txt.titleize("(" + flocation.getCoordString() + ") " + factionLoc.getTag(faction)));
      int halfWidth = Conf.mapWidth / 2;
      int halfHeight = Conf.mapHeight / 2;
      FLocation topLeft = flocation.getRelative(-halfWidth, -halfHeight);
      int width = halfWidth * 2 + 1;
      int height = halfHeight * 2 + 1;
      if (Conf.showMapFactionKey) {
         height--;
      }

      Map<String, Character> fList = new HashMap<>();
      int chrIdx = 0;

      for (int dz = 0; dz < height; dz++) {
         String row = "";

         for (int dx = 0; dx < width; dx++) {
            if (dx == halfWidth && dz == halfHeight) {
               row = row + ChatColor.AQUA + "+";
            } else {
               FLocation flocationHere = topLeft.getRelative(dx, dz);
               Faction factionHere = getFactionAt(flocationHere);
               Relation relation = player.getRelationTo(factionHere);
               if (factionHere.isNone()) {
                  row = row + ChatColor.GRAY + "-";
               } else if (factionHere.isSafeZone()) {
                  row = row + Conf.colorPeaceful + "+";
               } else if (factionHere.isWarZone()) {
                  row = row + ChatColor.DARK_RED + "+";
               } else if (factionHere == faction
                  || factionHere == factionLoc
                  || relation.isAtLeast(Relation.ALLY)
                  || Conf.showNeutralFactionsOnMap && relation.equals(Relation.NEUTRAL)
                  || Conf.showEnemyFactionsOnMap && relation.equals(Relation.ENEMY)) {
                  if (!fList.containsKey(factionHere.getTag())) {
                     fList.put(factionHere.getTag(), Conf.mapKeyChrs[chrIdx++]);
                  }

                  char tag = fList.get(factionHere.getTag());
                  row = row + factionHere.getColorTo(player) + "" + tag;
               } else {
                  row = row + ChatColor.GRAY + "-";
               }
            }
         }

         ret.add(row);
      }

      ArrayList<String> asciiCompass = AsciiCompass.getAsciiCompass(inDegrees, ChatColor.RED, P.p.txt.parse("<a>"));
      ret.set(1, asciiCompass.get(0) + ret.get(1).substring(9));
      ret.set(2, asciiCompass.get(1) + ret.get(2).substring(9));
      ret.set(3, asciiCompass.get(2) + ret.get(3).substring(9));
      if (Conf.showMapFactionKey) {
         String fRow = "";

         for (String key : fList.keySet()) {
            fRow = fRow + String.format("%s%s: %s ", ChatColor.GRAY, fList.get(key), key);
         }

         ret.add(fRow);
      }

      return ret;
   }

   public static Map<String, Map<String, String>> dumpAsSaveFormat() {
      Map<String, Map<String, String>> worldCoordIds = new HashMap<>();

      for (Entry<FLocation, String> entry : flocationIds.entrySet()) {
         String worldName = entry.getKey().getWorldName();
         String coords = entry.getKey().getCoordString();
         String id = entry.getValue();
         if (!worldCoordIds.containsKey(worldName)) {
            worldCoordIds.put(worldName, new TreeMap<>());
         }

         worldCoordIds.get(worldName).put(coords, id);
      }

      return worldCoordIds;
   }

   public static void loadFromSaveFormat(Map<String, Map<String, String>> worldCoordIds) {
      flocationIds.clear();

      for (Entry<String, Map<String, String>> entry : worldCoordIds.entrySet()) {
         String worldName = entry.getKey();

         for (Entry<String, String> entry2 : entry.getValue().entrySet()) {
            String[] coords = entry2.getKey().trim().split("[,\\s]+");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);
            String factionId = entry2.getValue();
            flocationIds.put(new FLocation(worldName, x, z), factionId);
         }
      }
   }

   public static boolean save() {
      try {
         DiscUtil.write(file, P.p.gson.toJson(dumpAsSaveFormat()));
         return true;
      } catch (Exception var1) {
         var1.printStackTrace();
         P.p.log("Failed to save the board to disk.");
         return false;
      }
   }

   public static boolean load() {
      P.p.log("Loading board from disk");
      if (!file.exists()) {
         P.p.log("No board to load from disk. Creating new file.");
         save();
         return true;
      } else {
         try {
            Type type = (new TypeToken<Map<String, Map<String, String>>>() {}).getType();
            Map<String, Map<String, String>> worldCoordIds = P.p.gson.fromJson(DiscUtil.read(file), type);
            loadFromSaveFormat(worldCoordIds);
            return true;
         } catch (Exception var2) {
            var2.printStackTrace();
            P.p.log("Failed to load the board from disk.");
            return false;
         }
      }
   }
}
