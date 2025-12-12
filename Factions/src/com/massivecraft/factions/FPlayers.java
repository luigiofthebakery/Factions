package com.massivecraft.factions;

import com.massivecraft.factions.zcore.persist.PlayerEntityCollection;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import com.google.gson.reflect.TypeToken;

public class FPlayers extends PlayerEntityCollection<FPlayer> {
   public static FPlayers i = new FPlayers();
   P p;

   private FPlayers() {
      super(
         FPlayer.class,
         new CopyOnWriteArrayList<>(),
         new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER),
         new File(P.p.getDataFolder(), "players.json"),
         P.p.gson
      );
      this.p = P.p;
      this.setCreative(true);
   }

   @Override
   public Type getMapType() {
      return (new TypeToken<Map<String, FPlayer>>() {}).getType();
   }

   public void clean() {
      for (FPlayer fplayer : this.get()) {
         if (!Factions.i.exists(fplayer.getFactionId())) {
            this.p.log("Reset faction data (invalid faction) for player " + fplayer.getName());
            fplayer.resetFactionData(false);
         }
      }
   }

   public void autoLeaveOnInactivityRoutine() {
      if (!(Conf.autoLeaveAfterDaysOfInactivity <= 0.0)) {
         P.p.log("Running auto database cleanup routine...");
         
            for (FPlayer fplayer : FPlayers.i.get()) {
                  if (fplayer.isOffline() && fplayer.getFactionId().equalsIgnoreCase("0")) {
                     fplayer.leave(false);
                     fplayer.detach();
                     P.p.log("Player " + fplayer.getName() + " was auto-removed due to not being in any faction.");
                  }
               }
      }
   }
}
