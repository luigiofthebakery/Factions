package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FPlayerJoinEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   FPlayer fplayer;
   Faction faction;
   FPlayerJoinEvent.PlayerJoinReason reason;
   boolean cancelled = false;

   public FPlayerJoinEvent(FPlayer fp, Faction f, FPlayerJoinEvent.PlayerJoinReason r) {
      this.fplayer = fp;
      this.faction = f;
      this.reason = r;
   }

   public FPlayer getFPlayer() {
      return this.fplayer;
   }

   public Faction getFaction() {
      return this.faction;
   }

   public FPlayerJoinEvent.PlayerJoinReason getReason() {
      return this.reason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean c) {
      this.cancelled = c;
   }

   public static enum PlayerJoinReason {
      CREATE,
      LEADER,
      COMMAND;
   }
}
