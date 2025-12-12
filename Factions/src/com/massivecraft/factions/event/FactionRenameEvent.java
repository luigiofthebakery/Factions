package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionRenameEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;
   private FPlayer fplayer;
   private Faction faction;
   private String tag;

   public FactionRenameEvent(FPlayer sender, String newTag) {
      this.fplayer = sender;
      this.faction = sender.getFaction();
      this.tag = newTag;
      this.cancelled = false;
   }

   public Faction getFaction() {
      return this.faction;
   }

   public FPlayer getFPlayer() {
      return this.fplayer;
   }

   public Player getPlayer() {
      return this.fplayer.getPlayer();
   }

   public String getOldFactionTag() {
      return this.faction.getTag();
   }

   public String getFactionTag() {
      return this.tag;
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
