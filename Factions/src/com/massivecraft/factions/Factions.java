package com.massivecraft.factions;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.zcore.persist.EntityCollection;
import com.massivecraft.factions.zcore.util.TextUtil;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.bukkit.ChatColor;

public class Factions extends EntityCollection<Faction> {
   public static Factions i = new Factions();
   P p;

   private Factions() {
      super(Faction.class, new CopyOnWriteArrayList<>(), new ConcurrentHashMap<>(), new File(P.p.getDataFolder(), "factions.json"), P.p.gson);
      this.p = P.p;
   }

   @Override
   public Type getMapType() {
      return (new TypeToken<Map<String, Faction>>() {}).getType();
   }

   @Override
   public boolean loadFromDisc() {
      if (!super.loadFromDisc()) {
         return false;
      } else {
         if (!this.exists("0")) {
            Faction faction = this.create("0");
            faction.setTag(ChatColor.DARK_GREEN + "Wilderness");
            faction.setDescription("");
         }

         if (!this.exists("-1")) {
            Faction faction = this.create("-1");
            faction.setTag("SafeZone");
            faction.setDescription("Free from PVP and monsters");
         } else {
            Faction faction = this.getSafeZone();
            if (faction.getTag().contains(" ")) {
               faction.setTag("SafeZone");
            }
         }

         if (!this.exists("-2")) {
            Faction factionx = this.create("-2");
            factionx.setTag("WarZone");
            factionx.setDescription("Not the safest place to be");
         } else {
            Faction factionx = this.getWarZone();
            if (factionx.getTag().contains(" ")) {
               factionx.setTag("WarZone");
            }
         }

         for (Faction factionxx : i.get()) {
            factionxx.refreshFPlayers();
         }

         return true;
      }
   }

   public Faction get(String id) {
      if (!this.exists(id)) {
         this.p.log(Level.WARNING, "Non existing factionId " + id + " requested! Issuing cleaning!");
         Board.clean();
         FPlayers.i.clean();
      }

      return (Faction)super.get(id);
   }

   public Faction getNone() {
      return this.get("0");
   }

   public Faction getSafeZone() {
      return this.get("-1");
   }

   public Faction getWarZone() {
      return this.get("-2");
   }

   public static ArrayList<String> validateTag(String str) {
      ArrayList<String> errors = new ArrayList<>();
      if (MiscUtil.getComparisonString(str).length() < Conf.factionTagLengthMin) {
         errors.add(P.p.txt.parse("<i>The faction tag can't be shorter than <h>%s<i> chars.", Conf.factionTagLengthMin));
      }

      if (str.length() > Conf.factionTagLengthMax) {
         errors.add(P.p.txt.parse("<i>The faction tag can't be longer than <h>%s<i> chars.", Conf.factionTagLengthMax));
      }

      for (char c : str.toCharArray()) {
         if (!MiscUtil.substanceChars.contains(String.valueOf(c))) {
            errors.add(P.p.txt.parse("<i>Faction tag must be alphanumeric. \"<h>%s<i>\" is not allowed.", c));
         }
      }

      return errors;
   }

   public Faction getByTag(String str) {
      String compStr = MiscUtil.getComparisonString(str);

      for (Faction faction : this.get()) {
         if (faction.getComparisonTag().equals(compStr)) {
            return faction;
         }
      }

      return null;
   }

   public Faction getBestTagMatch(String searchFor) {
      Map<String, Faction> tag2faction = new HashMap<>();

      for (Faction faction : this.get()) {
         tag2faction.put(ChatColor.stripColor(faction.getTag()), faction);
      }

      String tag = TextUtil.getBestStartWithCI(tag2faction.keySet(), searchFor);
      return tag == null ? null : tag2faction.get(tag);
   }

   public boolean isTagTaken(String str) {
      return this.getByTag(str) != null;
   }
}
