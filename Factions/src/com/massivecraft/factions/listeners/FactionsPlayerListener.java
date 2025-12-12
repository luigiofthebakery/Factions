package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TextUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.NumberConversions;

public class FactionsPlayerListener implements Listener {
   public P p;
   private Map<String, FactionsPlayerListener.InteractAttemptSpam> interactSpammers = new HashMap<>();

   public FactionsPlayerListener(P p) {
      this.p = p;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      FPlayer me = FPlayers.i.get(event.getPlayer());
      me.setLastLoginTime(System.currentTimeMillis());
      me.setLastStoodAt(new FLocation(event.getPlayer().getLocation()));
      if (!SpoutFeatures.updateTerritoryDisplay(me)) {
         me.sendFactionHereMessage();
      }

      SpoutFeatures.updateAppearancesShortly(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      FPlayer me = FPlayers.i.get(event.getPlayer());
      me.getPower();
      me.setLastLoginTime(System.currentTimeMillis());
      Faction myFaction = me.getFaction();
      if (myFaction != null) {
         myFaction.memberLoggedOff();
      }

      SpoutFeatures.playerDisconnect(me);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (!event.isCancelled()) {
         if (event.getFrom().getBlockX() >> 4 != event.getTo().getBlockX() >> 4
            || event.getFrom().getBlockZ() >> 4 != event.getTo().getBlockZ() >> 4
            || event.getFrom().getWorld() != event.getTo().getWorld()) {
            Player player = event.getPlayer();
            FPlayer me = FPlayers.i.get(player);
            FLocation from = me.getLastStoodAt();
            FLocation to = new FLocation(event.getTo());
            if (!from.equals(to)) {
               me.setLastStoodAt(to);
               boolean spoutClient = SpoutFeatures.availableFor(player);
               Faction factionFrom = Board.getFactionAt(from);
               Faction factionTo = Board.getFactionAt(to);
               boolean changedFaction = factionFrom != factionTo;
               if (changedFaction && SpoutFeatures.updateTerritoryDisplay(me)) {
                  changedFaction = false;
               }

               if (me.isMapAutoUpdating()) {
                  me.sendMessage(Board.getMap(me.getFaction(), to, player.getLocation().getYaw()));
                  if (spoutClient && Conf.spoutTerritoryOwnersShow) {
                     SpoutFeatures.updateOwnerList(me);
                  }
               } else {
                  Faction myFaction = me.getFaction();
                  String ownersTo = myFaction.getOwnerListString(to);
                  if (changedFaction) {
                     me.sendFactionHereMessage();
                     if (Conf.ownedAreasEnabled
                        && Conf.ownedMessageOnBorder
                        && (!spoutClient || !Conf.spoutTerritoryOwnersShow)
                        && myFaction == factionTo
                        && !ownersTo.isEmpty()) {
                        me.sendMessage(Conf.ownedLandMessage + ownersTo);
                     }
                  } else if (spoutClient && Conf.spoutTerritoryOwnersShow) {
                     SpoutFeatures.updateOwnerList(me);
                  } else if (Conf.ownedAreasEnabled && Conf.ownedMessageInsideTerritory && factionFrom == factionTo && myFaction == factionTo) {
                     String ownersFrom = myFaction.getOwnerListString(from);
                     if (Conf.ownedMessageByChunk || !ownersFrom.equals(ownersTo)) {
                        if (!ownersTo.isEmpty()) {
                           me.sendMessage(Conf.ownedLandMessage + ownersTo);
                        } else if (!Conf.publicLandMessage.isEmpty()) {
                           me.sendMessage(Conf.publicLandMessage);
                        }
                     }
                  }
               }

               if (me.getAutoClaimFor() != null) {
                  me.attemptClaim(me.getAutoClaimFor(), event.getTo(), true);
               } else if (me.isAutoSafeClaimEnabled()) {
                  if (!Permission.MANAGE_SAFE_ZONE.has(player)) {
                     me.setIsAutoSafeClaimEnabled(false);
                  } else if (!Board.getFactionAt(to).isSafeZone()) {
                     Board.setFactionAt(Factions.i.getSafeZone(), to);
                     me.msg("<i>This land is now a safe zone.");
                  }
               } else if (me.isAutoWarClaimEnabled()) {
                  if (!Permission.MANAGE_WAR_ZONE.has(player)) {
                     me.setIsAutoWarClaimEnabled(false);
                  } else if (!Board.getFactionAt(to).isWarZone()) {
                     Board.setFactionAt(Factions.i.getWarZone(), to);
                     me.msg("<i>This land is now a war zone.");
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (!event.isCancelled()) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            if (block != null) {
               if (!canPlayerUseBlock(player, block, false)) {
                  event.setCancelled(true);
                  if (Conf.handleExploitInteractionSpam) {
                     String name = player.getName();
                     FactionsPlayerListener.InteractAttemptSpam attempt = this.interactSpammers.get(name);
                     if (attempt == null) {
                        attempt = new FactionsPlayerListener.InteractAttemptSpam();
                        this.interactSpammers.put(name, attempt);
                     }

                     int count = attempt.increment();
                     if (count >= 10) {
                        FPlayer me = FPlayers.i.get(name);
                        me.msg("<b>Ouch, that is starting to hurt. You should give it a rest.");
                        player.damage(NumberConversions.floor(count / 10.0));
                     }
                  }
               } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                  if (!playerCanUseItemHere(player, block.getLocation(), event.getMaterial(), false)) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   public static boolean playerCanUseItemHere(Player player, Location location, Material material, boolean justCheck) {
      String name = player.getName();
      if (Conf.playersWhoBypassAllProtection.contains(name)) {
         return true;
      } else {
         FPlayer me = FPlayers.i.get(name);
         if (me.isAdminBypassing()) {
            return true;
         } else {
            FLocation loc = new FLocation(location);
            Faction otherFaction = Board.getFactionAt(loc);
            if (otherFaction.hasPlayersOnline()) {
               if (!Conf.territoryDenyUseageMaterials.contains(material)) {
                  return true;
               }
            } else if (!Conf.territoryDenyUseageMaterialsWhenOffline.contains(material)) {
               return true;
            }

            if (otherFaction.isNone()) {
               if (Conf.wildernessDenyUseage && !Conf.worldsNoWildernessProtection.contains(location.getWorld().getName())) {
                  if (!justCheck) {
                     me.msg("<b>You can't use <h>%s<b> in the wilderness.", TextUtil.getMaterialName(material));
                  }

                  return false;
               } else {
                  return true;
               }
            } else if (otherFaction.isSafeZone()) {
               if (Conf.safeZoneDenyUseage && !Permission.MANAGE_SAFE_ZONE.has(player)) {
                  if (!justCheck) {
                     me.msg("<b>You can't use <h>%s<b> in a safe zone.", TextUtil.getMaterialName(material));
                  }

                  return false;
               } else {
                  return true;
               }
            } else if (otherFaction.isWarZone()) {
               if (Conf.warZoneDenyUseage && !Permission.MANAGE_WAR_ZONE.has(player)) {
                  if (!justCheck) {
                     me.msg("<b>You can't use <h>%s<b> in a war zone.", TextUtil.getMaterialName(material));
                  }

                  return false;
               } else {
                  return true;
               }
            } else {
               Faction myFaction = me.getFaction();
               Relation rel = myFaction.getRelationTo(otherFaction);
               if (rel.confDenyUseage()) {
                  if (!justCheck) {
                     me.msg("<b>You can't use <h>%s<b> in the territory of <h>%s<b>.", TextUtil.getMaterialName(material), otherFaction.getTag(myFaction));
                  }

                  return false;
               } else if (Conf.ownedAreasEnabled && Conf.ownedAreaDenyUseage && !otherFaction.playerHasOwnershipRights(me, loc)) {
                  if (!justCheck) {
                     me.msg(
                        "<b>You can't use <h>%s<b> in this territory, it is owned by: %s<b>.",
                        TextUtil.getMaterialName(material),
                        otherFaction.getOwnerListString(loc)
                     );
                  }

                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   public static boolean canPlayerUseBlock(Player player, Block block, boolean justCheck) {
      String name = player.getName();
      if (Conf.playersWhoBypassAllProtection.contains(name)) {
         return true;
      } else {
         FPlayer me = FPlayers.i.get(name);
         if (me.isAdminBypassing()) {
            return true;
         } else {
            Material material = block.getType();
            FLocation loc = new FLocation(block);
            Faction otherFaction = Board.getFactionAt(loc);
            if (!otherFaction.isNormal()) {
               return true;
            } else {
               if (otherFaction.hasPlayersOnline()) {
                  if (!Conf.territoryProtectedMaterials.contains(material)) {
                     return true;
                  }
               } else if (!Conf.territoryProtectedMaterialsWhenOffline.contains(material)) {
                  return true;
               }

               Faction myFaction = me.getFaction();
               Relation rel = myFaction.getRelationTo(otherFaction);
               if (rel.isNeutral() || rel.isEnemy() && Conf.territoryEnemyProtectMaterials || rel.isAlly() && Conf.territoryAllyProtectMaterials) {
                  if (!justCheck) {
                     me.msg(
                        "<b>You can't %s <h>%s<b> in the territory of <h>%s<b>.",
                        material == Material.FARMLAND ? "trample" : "use",
                        TextUtil.getMaterialName(material),
                        otherFaction.getTag(myFaction)
                     );
                  }

                  return false;
               } else if (Conf.ownedAreasEnabled && Conf.ownedAreaProtectMaterials && !otherFaction.playerHasOwnershipRights(me, loc)) {
                  if (!justCheck) {
                     me.msg(
                        "<b>You can't use <h>%s<b> in this territory, it is owned by: %s<b>.",
                        TextUtil.getMaterialName(material),
                        otherFaction.getOwnerListString(loc)
                     );
                  }

                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      FPlayer me = FPlayers.i.get(event.getPlayer());
      me.getPower();
      Location home = me.getFaction().getHome();
      if (Conf.homesEnabled
         && Conf.homesTeleportToOnDeath
         && home != null
         && (Conf.homesRespawnFromNoPowerLossWorlds || !Conf.worldsNoPowerLoss.contains(event.getPlayer().getWorld().getName()))) {
         event.setRespawnLocation(home);
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      if (!event.isCancelled()) {
         Block block = event.getBlockClicked();
         Player player = event.getPlayer();
         if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerBucketFill(PlayerBucketFillEvent event) {
      if (!event.isCancelled()) {
         Block block = event.getBlockClicked();
         Player player = event.getPlayer();
         if (!playerCanUseItemHere(player, block.getLocation(), event.getBucket(), false)) {
            event.setCancelled(true);
         }
      }
   }

   public static boolean preventCommand(String fullCmd, Player player) {
      if (Conf.territoryNeutralDenyCommands.isEmpty() && Conf.territoryEnemyDenyCommands.isEmpty() && Conf.permanentFactionMemberDenyCommands.isEmpty()) {
         return false;
      } else {
         fullCmd = fullCmd.toLowerCase();
         FPlayer me = FPlayers.i.get(player);
         String shortCmd;
         if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
         } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
         }

         if (me.hasFaction()
            && !me.isAdminBypassing()
            && !Conf.permanentFactionMemberDenyCommands.isEmpty()
            && me.getFaction().isPermanent()
            && isCommandInList(fullCmd, shortCmd, Conf.permanentFactionMemberDenyCommands.iterator())) {
            me.msg("<b>You can't use the command \"" + fullCmd + "\" because you are in a permanent faction.");
            return true;
         } else if (!me.isInOthersTerritory()) {
            return false;
         } else {
            Relation rel = me.getRelationToLocation();
            if (rel.isAtLeast(Relation.ALLY)) {
               return false;
            } else if (rel.isNeutral()
               && !Conf.territoryNeutralDenyCommands.isEmpty()
               && !me.isAdminBypassing()
               && isCommandInList(fullCmd, shortCmd, Conf.territoryNeutralDenyCommands.iterator())) {
               me.msg("<b>You can't use the command \"" + fullCmd + "\" in neutral territory.");
               return true;
            } else if (rel.isEnemy()
               && !Conf.territoryEnemyDenyCommands.isEmpty()
               && !me.isAdminBypassing()
               && isCommandInList(fullCmd, shortCmd, Conf.territoryEnemyDenyCommands.iterator())) {
               me.msg("<b>You can't use the command \"" + fullCmd + "\" in enemy territory.");
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static boolean isCommandInList(String fullCmd, String shortCmd, Iterator<String> iter) {
      while (iter.hasNext()) {
         String cmdCheck = iter.next();
         if (cmdCheck == null) {
            iter.remove();
         } else {
            cmdCheck = cmdCheck.toLowerCase();
            if (fullCmd.startsWith(cmdCheck) || shortCmd.startsWith(cmdCheck)) {
               return true;
            }
         }
      }

      return false;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerKick(PlayerKickEvent event) {
      if (!event.isCancelled()) {
         FPlayer badGuy = FPlayers.i.get(event.getPlayer());
         if (badGuy != null) {
            SpoutFeatures.playerDisconnect(badGuy);
            if (Conf.removePlayerDataWhenBanned && event.getReason().equals("Banned by admin.")) {
               if (badGuy.getRole() == Role.ADMIN) {
                  badGuy.getFaction().promoteNewLeader();
               }

               badGuy.leave(false);
               badGuy.detach();
            }
         }
      }
   }

   private static class InteractAttemptSpam {
      private int attempts = 0;
      private long lastAttempt = System.currentTimeMillis();

      private InteractAttemptSpam() {
      }

      public int increment() {
         long Now = System.currentTimeMillis();
         if (Now > this.lastAttempt + 2000L) {
            this.attempts = 1;
         } else {
            this.attempts++;
         }

         this.lastAttempt = Now;
         return this.attempts;
      }
   }
}
