package com.massivecraft.factions.integration;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SpoutFeatures {
   private static transient boolean spoutMe = false;
   private static transient SpoutMainListener mainListener;
   private static transient boolean listenersHooked;

   public static void setup() {
      Plugin test = Bukkit.getServer().getPluginManager().getPlugin("Spout");
      if (test != null && test.isEnabled()) {
         setAvailable(true, test.getDescription().getFullName());
      }
   }

   public static void setAvailable(boolean enable, String pluginName) {
      spoutMe = enable;
      if (spoutMe) {
         P.p.log("Found and will use features of " + pluginName);
         if (!listenersHooked) {
            listenersHooked = true;
            mainListener = new SpoutMainListener();
            Bukkit.getServer().getPluginManager().registerEvents(mainListener, P.p);
         }
      }
   }

   public static boolean enabled() {
      return spoutMe;
   }

   public static boolean availableFor(Player player) {
      return spoutMe && SpoutManager.getPlayer(player).isSpoutCraftEnabled();
   }

   public static void updateTerritoryDisplayLoc(FLocation fLoc) {
      if (enabled()) {
         for (FPlayer player : FPlayers.i.getOnline()) {
            if (fLoc == null) {
               mainListener.updateTerritoryDisplay(player, false);
            } else if (player.getLastStoodAt().equals(fLoc)) {
               mainListener.updateTerritoryDisplay(player, true);
            }
         }
      }
   }

   public static boolean updateTerritoryDisplay(FPlayer player) {
      return !enabled() ? false : mainListener.updateTerritoryDisplay(player, true);
   }

   public static void updateOwnerListLoc(FLocation fLoc) {
      if (enabled()) {
         for (FPlayer player : FPlayers.i.getOnline()) {
            if (fLoc == null || player.getLastStoodAt().equals(fLoc)) {
               mainListener.updateOwnerList(player);
            }
         }
      }
   }

   public static void updateOwnerList(FPlayer player) {
      if (enabled()) {
         mainListener.updateOwnerList(player);
      }
   }

   public static void playerDisconnect(FPlayer player) {
      if (enabled()) {
         mainListener.removeTerritoryLabels(player.getName());
      }
   }

   public static void updateAppearances() {
      if (enabled()) {
         Set<FPlayer> players = FPlayers.i.getOnline();

         for (FPlayer playerA : players) {
            for (FPlayer playerB : players) {
               updateSingle(playerB, playerA);
            }
         }
      }
   }

   public static void updateAppearances(Player player) {
      if (enabled() && player != null) {
         Set<FPlayer> players = FPlayers.i.getOnline();
         FPlayer playerA = FPlayers.i.get(player);

         for (FPlayer playerB : players) {
            updateSingle(playerB, playerA);
            updateSingle(playerA, playerB);
         }
      }
   }

   public static void updateAppearancesShortly(final Player player) {
      if (enabled()) {
         player.getScheduler().runDelayed(P.p, (ScheduledTask t) -> {
               SpoutFeatures.updateAppearances(player);
         }, null, 100L);
      }
   }

   public static void updateAppearances(Faction faction) {
      if (enabled() && faction != null) {
         Set<FPlayer> players = FPlayers.i.getOnline();

         for (FPlayer playerA : players) {
            Faction factionA = playerA.getFaction();

            for (FPlayer playerB : players) {
               if (factionA == faction || playerB.getFaction() == faction) {
                  updateSingle(playerB, playerA);
               }
            }
         }
      }
   }

   public static void updateAppearances(Faction factionA, Faction factionB) {
      if (enabled() && factionA != null && factionB != null) {
         for (FPlayer playerA : factionA.getFPlayersWhereOnline(true)) {
            for (FPlayer playerB : factionB.getFPlayersWhereOnline(true)) {
               updateSingle(playerB, playerA);
               updateSingle(playerA, playerB);
            }
         }
      }
   }

   private static void updateSingle(FPlayer viewer, FPlayer viewed) {
      if (viewer != null && viewed != null) {
         Faction viewedFaction = viewed.getFaction();
         if (viewedFaction != null) {
            if (viewer.getPlayer() != null && viewed.getPlayer() != null) {
               SpoutPlayer pViewer = SpoutManager.getPlayer(viewer.getPlayer());
               SpoutPlayer pViewed = SpoutManager.getPlayer(viewed.getPlayer());
               if (pViewed != null && pViewer != null) {
                  String viewedTitle = viewed.getTitle();
                  Role viewedRole = viewed.getRole();
                  if ((Conf.spoutFactionTagsOverNames || Conf.spoutFactionTitlesOverNames) && viewer != viewed) {
                     if (viewedFaction.isNormal()) {
                        String addTag = "";
                        if (Conf.spoutFactionTagsOverNames) {
                           addTag = addTag + viewedFaction.getTag(viewed.getColorTo(viewer).toString() + "[") + "]";
                        }

                        String rolePrefix = viewedRole.getPrefix();
                        if (Conf.spoutFactionTitlesOverNames && (!viewedTitle.isEmpty() || !rolePrefix.isEmpty())) {
                           addTag = addTag + (addTag.isEmpty() ? "" : " ") + viewedRole.getPrefix() + viewedTitle;
                        }

                        pViewed.setTitleFor(pViewer, addTag + "\n" + pViewed.getDisplayName());
                     } else {
                        pViewed.setTitleFor(pViewer, pViewed.getDisplayName());
                     }
                  }

                  if ((!Conf.spoutFactionAdminCapes || !viewedRole.equals(Role.ADMIN))
                     && (!Conf.spoutFactionModeratorCapes || !viewedRole.equals(Role.MODERATOR))) {
                     if (Conf.spoutFactionAdminCapes || Conf.spoutFactionModeratorCapes) {
                        pViewed.resetCapeFor(pViewer);
                     }
                  } else {
                     Relation relation = viewer.getRelationTo(viewed);
                     String cape = "";
                     if (viewedFaction.isNormal()) {
                        if (viewedFaction.isPeaceful()) {
                           cape = Conf.capePeaceful;
                        } else if (relation.isNeutral()) {
                           cape = Conf.capeNeutral;
                        } else if (relation.isMember()) {
                           cape = Conf.capeMember;
                        } else if (relation.isEnemy()) {
                           cape = Conf.capeEnemy;
                        } else if (relation.isAlly()) {
                           cape = Conf.capeAlly;
                        }
                     }

                     if (cape.isEmpty()) {
                        pViewed.resetCapeFor(pViewer);
                     } else {
                        pViewed.setCapeFor(pViewer, cape);
                     }
                  }
               }
            }
         }
      }
   }

   protected static Color getSpoutColor(ChatColor inColor, int alpha) {
      if (inColor == null) {
         return SpoutFixedColor(191, 191, 191, alpha);
      } else {
         switch (inColor.getChar()) {
            case '\u0001':
               return SpoutFixedColor(0, 0, 191, alpha);
            case '\u0002':
               return SpoutFixedColor(0, 191, 0, alpha);
            case '\u0003':
               return SpoutFixedColor(0, 191, 191, alpha);
            case '\u0004':
               return SpoutFixedColor(191, 0, 0, alpha);
            case '\u0005':
               return SpoutFixedColor(191, 0, 191, alpha);
            case '\u0006':
               return SpoutFixedColor(191, 191, 0, alpha);
            case '\u0007':
               return SpoutFixedColor(191, 191, 191, alpha);
            case '\b':
               return SpoutFixedColor(64, 64, 64, alpha);
            case '\t':
               return SpoutFixedColor(64, 64, 255, alpha);
            case '\n':
               return SpoutFixedColor(64, 255, 64, alpha);
            case '\u000b':
               return SpoutFixedColor(64, 255, 255, alpha);
            case '\f':
               return SpoutFixedColor(255, 64, 64, alpha);
            case '\r':
               return SpoutFixedColor(255, 64, 255, alpha);
            case '\u000e':
               return SpoutFixedColor(255, 255, 64, alpha);
            case '\u000f':
               return SpoutFixedColor(255, 255, 255, alpha);
            default:
               return SpoutFixedColor(0, 0, 0, alpha);
         }
      }
   }

   private static Color SpoutFixedColor(int r, int g, int b, int a) {
      return new Color(r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F);
   }
}
