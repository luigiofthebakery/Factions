package com.massivecraft.factions.integration;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SpoutMainListener implements Listener {
   private static transient Map<String, GenericLabel> territoryLabels = new ConcurrentHashMap<>();
   private static transient Map<String, SpoutMainListener.NoticeLabel> territoryChangeLabels = new ConcurrentHashMap<>();
   private static transient Map<String, GenericLabel> ownerLabels = new ConcurrentHashMap<>();
   private static final int SCREEN_WIDTH = 427;

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
      FPlayer me = FPlayers.i.get(event.getPlayer());
      SpoutFeatures.updateAppearances(me.getPlayer());
      this.updateTerritoryDisplay(me, true);
   }

   public boolean updateTerritoryDisplay(FPlayer player, boolean notify) {
      Player p = player.getPlayer();
      if (p == null) {
         return false;
      } else {
         SpoutPlayer sPlayer = SpoutManager.getPlayer(p);
         if (sPlayer.isSpoutCraftEnabled() && (!(Conf.spoutTerritoryDisplaySize <= 0.0F) || Conf.spoutTerritoryNoticeShow)) {
            this.doLabels(player, sPlayer, notify);
            return true;
         } else {
            return false;
         }
      }
   }

   public void updateOwnerList(FPlayer player) {
      SpoutPlayer sPlayer = SpoutManager.getPlayer(player.getPlayer());
      if (sPlayer.isSpoutCraftEnabled() && (!(Conf.spoutTerritoryDisplaySize <= 0.0F) || Conf.spoutTerritoryNoticeShow)) {
         FLocation here = player.getLastStoodAt();
         Faction factionHere = Board.getFactionAt(here);
         this.doOwnerList(player, sPlayer, here, factionHere);
      }
   }

   public void removeTerritoryLabels(String playerName) {
      territoryLabels.remove(playerName);
      territoryChangeLabels.remove(playerName);
      ownerLabels.remove(playerName);
   }

   private void doLabels(FPlayer player, SpoutPlayer sPlayer, boolean notify) {
      FLocation here = player.getLastStoodAt();
      Faction factionHere = Board.getFactionAt(here);
      String tag = factionHere.getColorTo(player).toString() + factionHere.getTag();
      if (Conf.spoutTerritoryDisplayPosition > 0 && Conf.spoutTerritoryDisplaySize > 0.0F) {
         GenericLabel label;
         if (territoryLabels.containsKey(player.getName())) {
            label = territoryLabels.get(player.getName());
         } else {
            label = new GenericLabel();
            label.setWidth(1).setHeight(1);
            label.setScale(Conf.spoutTerritoryDisplaySize);
            sPlayer.getMainScreen().attachWidget(P.p, label);
            territoryLabels.put(player.getName(), label);
         }

         String msg = tag;
         if (Conf.spoutTerritoryDisplayShowDescription && !factionHere.getDescription().isEmpty()) {
            msg = tag + " - " + factionHere.getDescription();
         }

         label.setText(msg);
         this.alignLabel(label, msg);
         label.setDirty(true);
      }

      if (notify && Conf.spoutTerritoryNoticeShow && Conf.spoutTerritoryNoticeSize > 0.0F) {
         SpoutMainListener.NoticeLabel labelx;
         if (territoryChangeLabels.containsKey(player.getName())) {
            labelx = territoryChangeLabels.get(player.getName());
         } else {
            labelx = new SpoutMainListener.NoticeLabel(Conf.spoutTerritoryNoticeLeaveAfterSeconds);
            labelx.setWidth(1).setHeight(1);
            labelx.setScale(Conf.spoutTerritoryNoticeSize);
            labelx.setY(Conf.spoutTerritoryNoticeTop);
            sPlayer.getMainScreen().attachWidget(P.p, labelx);
            territoryChangeLabels.put(player.getName(), labelx);
         }

         String msg = tag;
         if (Conf.spoutTerritoryNoticeShowDescription && !factionHere.getDescription().isEmpty()) {
            msg = tag + " - " + factionHere.getDescription();
         }

         labelx.setText(msg);
         this.alignLabel(labelx, msg, 2);
         labelx.resetNotice();
         labelx.setDirty(true);
      }

      this.doOwnerList(player, sPlayer, here, factionHere);
   }

   private void doOwnerList(FPlayer player, SpoutPlayer sPlayer, FLocation here, Faction factionHere) {
      if (Conf.spoutTerritoryDisplayPosition > 0 && Conf.spoutTerritoryDisplaySize > 0.0F && Conf.spoutTerritoryOwnersShow && Conf.ownedAreasEnabled) {
         GenericLabel label;
         if (ownerLabels.containsKey(player.getName())) {
            label = ownerLabels.get(player.getName());
         } else {
            label = new GenericLabel();
            label.setWidth(1).setHeight(1);
            label.setScale(Conf.spoutTerritoryDisplaySize);
            label.setY((int)(10.0F * Conf.spoutTerritoryDisplaySize));
            sPlayer.getMainScreen().attachWidget(P.p, label);
            ownerLabels.put(player.getName(), label);
         }

         String msg = "";
         if (player.getFaction() == factionHere) {
            msg = factionHere.getOwnerListString(here);
            if (!msg.isEmpty()) {
               msg = Conf.ownedLandMessage + msg;
            }
         }

         label.setText(msg);
         this.alignLabel(label, msg);
         label.setDirty(true);
      }
   }

   public void alignLabel(GenericLabel label, String text) {
      this.alignLabel(label, text, Conf.spoutTerritoryDisplayPosition);
   }

   public void alignLabel(GenericLabel label, String text, int alignment) {
      int labelWidth = (int)(GenericLabel.getStringWidth(text) * Conf.spoutTerritoryDisplaySize);
      if (labelWidth > 427) {
         label.setX(0);
      } else {
         switch (alignment) {
            case 1:
               label.setX(0);
               break;
            case 2:
               label.setX((427 - labelWidth) / 2);
               break;
            default:
               label.setX(427 - labelWidth);
         }
      }
   }

   private static class NoticeLabel extends GenericLabel {
      private int initial;
      private int countdown;

      public NoticeLabel(float secondsOfLife) {
         this.initial = (int)(secondsOfLife * 20.0F);
         this.resetNotice();
      }

      public final void resetNotice() {
         this.countdown = this.initial;
      }

      public void onTick() {
         if (this.countdown > 0) {
            this.countdown--;
            if (this.countdown <= 0) {
               this.setText("");
               this.setDirty(true);
            }
         }
      }
   }
}
