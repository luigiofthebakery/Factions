package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class FactionsBlockListener implements Listener {
   public P p;

   public FactionsBlockListener(P p) {
      this.p = p;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
         if (event.canBuild()) {
            if (event.getBlockPlaced().getType() != Material.FIRE) {
               if (!playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "build", false)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onBlockBreak(BlockBreakEvent event) {
      if (!event.isCancelled()) {
         if (!playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "destroy", false)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onBlockDamage(BlockDamageEvent event) {
      if (!event.isCancelled()) {
         if (event.getInstaBreak() && !playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "destroy", false)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      if (!event.isCancelled()) {
         if (Conf.pistonProtectionThroughDenyBuild) {
            Faction pistonFaction = Board.getFactionAt(new FLocation(event.getBlock()));
            Block targetBlock = event.getBlock().getRelative(event.getDirection(), event.getLength() + 1);
            if ((targetBlock.isEmpty() || targetBlock.isLiquid()) && !this.canPistonMoveBlock(pistonFaction, targetBlock.getLocation())) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      if (!event.isCancelled() && event.isSticky() && Conf.pistonProtectionThroughDenyBuild) {
         Location targetLoc = event.getRetractLocation();
         if (!targetLoc.getBlock().isEmpty() && !targetLoc.getBlock().isLiquid()) {
            Faction pistonFaction = Board.getFactionAt(new FLocation(event.getBlock()));
            if (!this.canPistonMoveBlock(pistonFaction, targetLoc)) {
               event.setCancelled(true);
            }
         }
      }
   }

   private boolean canPistonMoveBlock(Faction pistonFaction, Location target) {
      Faction otherFaction = Board.getFactionAt(new FLocation(target));
      if (pistonFaction == otherFaction) {
         return true;
      } else if (otherFaction.isNone()) {
         return !Conf.wildernessDenyBuild || Conf.worldsNoWildernessProtection.contains(target.getWorld().getName());
      } else if (otherFaction.isSafeZone()) {
         return !Conf.safeZoneDenyBuild;
      } else if (otherFaction.isWarZone()) {
         return !Conf.warZoneDenyBuild;
      } else {
         Relation rel = pistonFaction.getRelationTo(otherFaction);
         return !rel.confDenyBuild(otherFaction.hasPlayersOnline());
      }
   }

   public static boolean playerCanBuildDestroyBlock(Player player, Location location, String action, boolean justCheck) {
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
            if (otherFaction.isNone()) {
               if (Conf.wildernessDenyBuild && !Conf.worldsNoWildernessProtection.contains(location.getWorld().getName())) {
                  if (!justCheck) {
                     me.msg("<b>You can't " + action + " in the wilderness.");
                  }

                  return false;
               } else {
                  return true;
               }
            } else if (otherFaction.isSafeZone()) {
               if (Conf.worldGuardBuildPriority && Worldguard.playerCanBuild(player, location)) {
                  return true;
               } else if (Conf.safeZoneDenyBuild && !Permission.MANAGE_SAFE_ZONE.has(player)) {
                  if (!justCheck) {
                     me.msg("<b>You can't " + action + " in a safe zone.");
                  }

                  return false;
               } else {
                  return true;
               }
            } else if (otherFaction.isWarZone()) {
               if (Conf.warZoneDenyBuild && !Permission.MANAGE_WAR_ZONE.has(player)) {
                  if (!justCheck) {
                     me.msg("<b>You can't " + action + " in a war zone.");
                  }

                  return false;
               } else {
                  return true;
               }
            } else {
               Faction myFaction = me.getFaction();
               Relation rel = myFaction.getRelationTo(otherFaction);
               boolean online = otherFaction.hasPlayersOnline();
               boolean pain = !justCheck && rel.confPainBuild(online);
               boolean deny = rel.confDenyBuild(online);
               boolean trusted = Conf.trustEnabled && otherFaction.trustsPlayer(me);
               if (pain) {
                  player.damage(Conf.actionDeniedPainAmount);
                  if (!deny) {
                     me.msg("<b>It is painful to try to " + action + " in the territory of " + otherFaction.getTag(myFaction));
                  }
               }

               if (deny && !trusted) {
                  if (!justCheck) {
                     me.msg("<b>You can't " + action + " in the territory of " + otherFaction.getTag(myFaction));
                  }

                  return false;
               } else {
                  if (Conf.ownedAreasEnabled && (Conf.ownedAreaDenyBuild || Conf.ownedAreaPainBuild) && !otherFaction.playerHasOwnershipRights(me, loc)) {
                     if (!pain && Conf.ownedAreaPainBuild && !justCheck) {
                        player.damage(Conf.actionDeniedPainAmount);
                        if (!Conf.ownedAreaDenyBuild) {
                           me.msg("<b>It is painful to try to " + action + " in this territory, it is owned by: " + otherFaction.getOwnerListString(loc));
                        }
                     }

                     if (Conf.ownedAreaDenyBuild) {
                        if (!justCheck) {
                           me.msg("<b>You can't " + action + " in this territory, it is owned by: " + otherFaction.getOwnerListString(loc));
                        }

                        return false;
                     }
                  }

                  if (trusted && Conf.trustDenyBuild) {
                     return false;
                  }

                  return true;
               }
            }
         }
      }
   }
}