package com.massivecraft.factions.zcore.persist;

import java.util.List;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerEntity extends Entity {
   public Player getPlayer() {
      return Bukkit.getPlayerExact(this.getId());
   }

   public boolean isOnline() {
      return this.getPlayer() != null;
   }

   public boolean isOnlineAndVisibleTo(Player player) {
      Player target = this.getPlayer();
      return target != null && player.canSee(target);
   }

   public boolean isOffline() {
      return !this.isOnline();
   }

   public void sendMessage(String msg) {
      Player player = this.getPlayer();
      if (player != null) {
         player.sendMessage(msg);
      }
   }

   public void sendMessage(List<String> msgs) {
      for (String msg : msgs) {
         this.sendMessage(msg);
      }
   }

   public void sendActionBarMessage(String msg) {
      Player player = this.getPlayer();
      if (player != null) {
         player.sendActionBar(Component.text(msg));
      }
   }
}
