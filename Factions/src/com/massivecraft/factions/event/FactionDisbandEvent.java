package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionDisbandEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled = false;
   private String id;
   private Player sender;

   public FactionDisbandEvent(Player sender, String factionId) {
      this.sender = sender;
      this.id = factionId;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Faction getFaction() {
      return Factions.i.get(this.id);
   }

   public FPlayer getFPlayer() {
      return FPlayers.i.get(this.sender);
   }

   public Player getPlayer() {
      return this.sender;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean c) {
      this.cancelled = c;
   }
}
