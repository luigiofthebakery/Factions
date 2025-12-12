package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionCreateEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private String factionTag;
   private Player sender;
   private boolean cancelled;

   public FactionCreateEvent(Player sender, String tag) {
      this.factionTag = tag;
      this.sender = sender;
      this.cancelled = false;
   }

   public FPlayer getFPlayer() {
      return FPlayers.i.get(this.sender);
   }

   public String getFactionId() {
      return Factions.i.getNextId();
   }

   public String getFactionTag() {
      return this.factionTag;
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
}
