package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FPlayerLeaveEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private FPlayerLeaveEvent.PlayerLeaveReason reason;
   FPlayer FPlayer;
   Faction Faction;
   boolean cancelled = false;

   public FPlayerLeaveEvent(FPlayer p, Faction f, FPlayerLeaveEvent.PlayerLeaveReason r) {
      this.FPlayer = p;
      this.Faction = f;
      this.reason = r;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public FPlayerLeaveEvent.PlayerLeaveReason getReason() {
      return this.reason;
   }

   public FPlayer getFPlayer() {
      return this.FPlayer;
   }

   public Faction getFaction() {
      return this.Faction;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean c) {
      if (this.reason != FPlayerLeaveEvent.PlayerLeaveReason.DISBAND && this.reason != FPlayerLeaveEvent.PlayerLeaveReason.RESET) {
         this.cancelled = c;
      } else {
         this.cancelled = false;
      }
   }

   public static enum PlayerLeaveReason {
      KICKED,
      DISBAND,
      RESET,
      JOINOTHER,
      LEAVE;
   }
}
