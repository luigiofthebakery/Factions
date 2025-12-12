package com.massivecraft.factions.event;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LandUnclaimEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled = false;
   private FLocation location;
   private Faction faction;
   private FPlayer fplayer;

   public LandUnclaimEvent(FLocation loc, Faction f, FPlayer p) {
      this.location = loc;
      this.faction = f;
      this.fplayer = p;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public FLocation getLocation() {
      return this.location;
   }

   public Faction getFaction() {
      return this.faction;
   }

   public String getFactionId() {
      return this.faction.getId();
   }

   public String getFactionTag() {
      return this.faction.getTag();
   }

   public FPlayer getFPlayer() {
      return this.fplayer;
   }

   public Player getPlayer() {
      return this.fplayer.getPlayer();
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean c) {
      this.cancelled = c;
   }
}
