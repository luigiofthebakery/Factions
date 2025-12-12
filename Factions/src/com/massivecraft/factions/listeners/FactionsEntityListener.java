package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.util.MiscUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FactionsEntityListener implements Listener {
   public P p;
   private static final Set<PotionEffectType> badPotionEffects = new LinkedHashSet<>(
      Arrays.asList(
         PotionEffectType.BLINDNESS,
         PotionEffectType.NAUSEA,
         PotionEffectType.INSTANT_DAMAGE,
         PotionEffectType.HUNGER,
         PotionEffectType.POISON,
         PotionEffectType.SLOWNESS,
         PotionEffectType.MINING_FATIGUE,
         PotionEffectType.WEAKNESS,
         PotionEffectType.WITHER
      )
   );

   public FactionsEntityListener(P p) {
      this.p = p;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityDeath(EntityDeathEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof Player) {
         Player player = (Player)entity;
         FPlayer fplayer = FPlayers.i.get(player);
         Faction faction = Board.getFactionAt(new FLocation(player.getLocation()));
         PowerLossEvent powerLossEvent = new PowerLossEvent(faction, fplayer);
         if (faction.isWarZone()) {
            if (!Conf.warZonePowerLoss) {
               powerLossEvent.setMessage("<i>You didn't lose any power since you were in a war zone.");
               powerLossEvent.setCancelled(true);
            }

            if (Conf.worldsNoPowerLoss.contains(player.getWorld().getName())) {
               powerLossEvent.setMessage(
                  "<b>The world you are in has power loss normally disabled, but you still lost power since you were in a war zone.\n<i>Your power is now <h>%d / %d"
               );
            }
         } else if (faction.isNone() && !Conf.wildernessPowerLoss && !Conf.worldsNoWildernessProtection.contains(player.getWorld().getName())) {
            powerLossEvent.setMessage("<i>You didn't lose any power since you were in the wilderness.");
            powerLossEvent.setCancelled(true);
         } else if (Conf.worldsNoPowerLoss.contains(player.getWorld().getName())) {
            powerLossEvent.setMessage("<i>You didn't lose any power due to the world you died in.");
            powerLossEvent.setCancelled(true);
         } else if (Conf.peacefulMembersDisablePowerLoss && fplayer.hasFaction() && fplayer.getFaction().isPeaceful()) {
            powerLossEvent.setMessage("<i>You didn't lose any power since you are in a peaceful faction.");
            powerLossEvent.setCancelled(true);
         } else {
            powerLossEvent.setMessage("<i>Your power is now <h>%d / %d");
         }

         Bukkit.getPluginManager().callEvent(powerLossEvent);
         if (!powerLossEvent.isCancelled()) {
            fplayer.onDeath();
         }

         String msg = powerLossEvent.getMessage();
         if (msg != null && !msg.isEmpty()) {
            fplayer.msg(msg, fplayer.getPowerRounded(), fplayer.getPowerMaxRounded());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (!event.isCancelled()) {
         if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
            if (!this.canDamagerHurtDamagee(sub, true)) {
               event.setCancelled(true);
            }
         } else if (Conf.safeZonePreventAllDamageToPlayers && this.isPlayerInSafeZone(event.getEntity())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (!event.isCancelled()) {
         Location loc = event.getLocation();
         Entity boomer = event.getEntity();
         Faction faction = Board.getFactionAt(new FLocation(loc));
         if (faction.noExplosionsInTerritory()) {
            event.setCancelled(true);
         } else {
            boolean online = faction.hasPlayersOnline();
            if (boomer instanceof Creeper) {
               if (faction.isNone() && Conf.wildernessBlockCreepers && !Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName())
                  || faction.isNormal() && (online ? Conf.territoryBlockCreepers : Conf.territoryBlockCreepersWhenOffline)) {
                  event.setCancelled(true);
                  return;
               }

               if (!faction.isWarZone() || !Conf.warZoneBlockCreepers) {
                  faction.isSafeZone();
               }
            }

            if (boomer instanceof Fireball || boomer instanceof WitherSkull || boomer instanceof Wither) {
               if (faction.isNone() && Conf.wildernessBlockFireballs && !Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName())
                  || faction.isNormal() && (online ? Conf.territoryBlockFireballs : Conf.territoryBlockFireballsWhenOffline)) {
                  event.setCancelled(true);
                  return;
               }

               if (!faction.isWarZone() || !Conf.warZoneBlockFireballs) {
                  faction.isSafeZone();
               }
            }

            if (boomer instanceof TNTPrimed) {
               if (faction.isNone() && Conf.wildernessBlockTNT && !Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName())
                  || faction.isNormal() && (online ? Conf.territoryBlockTNT : Conf.territoryBlockTNTWhenOffline)) {
                  event.setCancelled(true);
                  return;
               }

               if ((!faction.isWarZone() || !Conf.warZoneBlockTNT) && faction.isSafeZone()) {
                  boolean center = Conf.safeZoneBlockTNT;
               }
            }

            if (boomer instanceof TNTPrimed && Conf.handleExploitTNTWaterlog) {
               Block center = loc.getBlock();
               if (center.isLiquid()) {
                  List<Block> targets = new ArrayList<>();
                  targets.add(center.getRelative(0, 0, 1));
                  targets.add(center.getRelative(0, 0, -1));
                  targets.add(center.getRelative(0, 1, 0));
                  targets.add(center.getRelative(0, -1, 0));
                  targets.add(center.getRelative(1, 0, 0));
                  targets.add(center.getRelative(-1, 0, 0));

                  for (Block target : targets) {
                     Material id = target.getType();
                     if (id != Material.AIR
                        && id != Material.BEDROCK
                        && id != Material.WATER
                        && id != Material.LAVA
                        && id != Material.OBSIDIAN
                        && id != Material.NETHER_PORTAL
                        && id != Material.ENCHANTING_TABLE
                        && id != Material.END_PORTAL
                        && id != Material.END_PORTAL_FRAME
                        && id != Material.ENDER_CHEST) {
                        target.breakNaturally();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
      if (!event.isCancelled()) {
         EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(), DamageCause.FIRE, 0.0);
         if (!this.canDamagerHurtDamagee(sub, false)) {
            event.setCancelled(true);
         }

         sub = null;
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPotionSplashEvent(PotionSplashEvent event) {
      if (!event.isCancelled()) {
         boolean badjuju = false;

         for (PotionEffect effect : event.getPotion().getEffects()) {
            if (badPotionEffects.contains(effect.getType())) {
               badjuju = true;
               break;
            }
         }

         if (badjuju) {
            Entity thrower = (Entity)event.getPotion().getShooter();

            for (LivingEntity target : event.getAffectedEntities()) {
               EntityDamageByEntityEvent sub = new EntityDamageByEntityEvent(thrower, target, DamageCause.CUSTOM, 0.0);
               if (!this.canDamagerHurtDamagee(sub, true)) {
                  event.setIntensity(target, 0.0);
               }

               sub = null;
            }
         }
      }
   }

   public boolean isPlayerInSafeZone(Entity damagee) {
      return damagee instanceof Player && Board.getFactionAt(new FLocation(damagee.getLocation())).isSafeZone();
   }

   public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub) {
      return this.canDamagerHurtDamagee(sub, true);
   }

   public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub, boolean notify) {
      Entity damager = sub.getDamager();
      Entity damagee = sub.getEntity();
      double damage = sub.getDamage();
      if (!(damagee instanceof Player)) {
         return true;
      } else {
         FPlayer defender = FPlayers.i.get((Player)damagee);
         if (defender != null && defender.getPlayer() != null) {
            Location defenderLoc = defender.getPlayer().getLocation();
            Faction defLocFaction = Board.getFactionAt(new FLocation(defenderLoc));
            if (damager instanceof Projectile) {
               damager = (Entity)((Projectile)damager).getShooter();
            }

            if (damager == damagee) {
               return true;
            } else if (defLocFaction.noPvPInTerritory()) {
               if (damager instanceof Player) {
                  if (notify) {
                     FPlayer attacker = FPlayers.i.get((Player)damager);
                     attacker.msg("<i>You can't hurt other players in " + (defLocFaction.isSafeZone() ? "a SafeZone." : "peaceful territory."));
                  }

                  return false;
               } else {
                  return !defLocFaction.noMonstersInTerritory();
               }
            } else if (!(damager instanceof Player)) {
               return true;
            } else {
               FPlayer attacker = FPlayers.i.get((Player)damager);
               if (attacker != null && attacker.getPlayer() != null) {
                  if (Conf.playersWhoBypassAllProtection.contains(attacker.getName())) {
                     return true;
                  } else if (attacker.hasLoginPvpDisabled()) {
                     if (notify) {
                        attacker.msg("<i>You can't hurt other players for " + Conf.noPVPDamageToOthersForXSecondsAfterLogin + " seconds after logging in.");
                     }

                     return false;
                  } else {
                     Faction locFaction = Board.getFactionAt(new FLocation(attacker));
                     if (locFaction.noPvPInTerritory()) {
                        if (notify) {
                           attacker.msg("<i>You can't hurt other players while you are in " + (locFaction.isSafeZone() ? "a SafeZone." : "peaceful territory."));
                        }

                        return false;
                     } else if (locFaction.isWarZone() && Conf.warZoneFriendlyFire) {
                        return true;
                     } else if (Conf.worldsIgnorePvP.contains(defenderLoc.getWorld().getName())) {
                        return true;
                     } else {
                        Faction defendFaction = defender.getFaction();
                        Faction attackFaction = attacker.getFaction();
                        if (attackFaction.isNone() && Conf.disablePVPForFactionlessPlayers) {
                           if (notify) {
                              attacker.msg("<i>You can't hurt other players until you join a faction.");
                           }

                           return false;
                        } else {
                           if (defendFaction.isNone()) {
                              if (defLocFaction == attackFaction && Conf.enablePVPAgainstFactionlessInAttackersLand) {
                                 return true;
                              }

                              if (Conf.disablePVPForFactionlessPlayers) {
                                 if (notify) {
                                    attacker.msg("<i>You can't hurt players who are not currently in a faction.");
                                 }

                                 return false;
                              }
                           }

                           if (defendFaction.isPeaceful()) {
                              if (notify) {
                                 attacker.msg("<i>You can't hurt players who are in a peaceful faction.");
                              }

                              return false;
                           } else if (attackFaction.isPeaceful()) {
                              if (notify) {
                                 attacker.msg("<i>You can't hurt players while you are in a peaceful faction.");
                              }

                              return false;
                           } else {
                              Relation relation = defendFaction.getRelationTo(attackFaction);
                              if (Conf.disablePVPBetweenNeutralFactions && relation.isNeutral()) {
                                 if (notify) {
                                    attacker.msg("<i>You can't hurt neutral factions. Declare them as an enemy.");
                                 }

                                 return false;
                              } else if (!defender.hasFaction()) {
                                 return true;
                              } else if (!relation.isMember() && !relation.isAlly()) {
                                 boolean ownTerritory = defender.isInOwnTerritory();
                                 if (ownTerritory && relation.isNeutral()) {
                                    if (notify) {
                                       attacker.msg(
                                          "<i>You can't hurt %s<i> in their own territory unless you declare them as an enemy.", defender.describeTo(attacker)
                                       );
                                       defender.msg("%s<i> tried to hurt you.", attacker.describeTo(defender, true));
                                    }

                                    return false;
                                 } else {
                                    if (damage > 0.0 && ownTerritory && Conf.territoryShieldFactor > 0.0) {
                                       int newDamage = (int)Math.ceil(damage * (1.0 - Conf.territoryShieldFactor));
                                       sub.setDamage(newDamage);
                                       if (notify) {
                                          String perc = MessageFormat.format("{0,number,#%}", Conf.territoryShieldFactor);
                                          defender.msg("<i>Enemy damage reduced by <rose>%s<i>.", perc);
                                       }
                                    }

                                    return true;
                                 }
                              } else {
                                 if (notify) {
                                    attacker.msg("<i>You can't hurt %s<i>.", defender.describeTo(attacker));
                                 }

                                 return false;
                              }
                           }
                        }
                     }
                  }
               } else {
                  return true;
               }
            }
         } else {
            return true;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      if (!event.isCancelled() && event.getLocation() != null) {
         if (Conf.safeZoneNerfedCreatureTypes.contains(event.getEntityType()) && Board.getFactionAt(new FLocation(event.getLocation())).noMonstersInTerritory()
            )
          {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityTarget(EntityTargetEvent event) {
      if (!event.isCancelled()) {
         Entity target = event.getTarget();
         if (target != null) {
            if (Conf.safeZoneNerfedCreatureTypes.contains(MiscUtil.creatureTypeFromEntity(event.getEntity()))) {
               if (Board.getFactionAt(new FLocation(target.getLocation())).noMonstersInTerritory()) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPaintingBreak(HangingBreakEvent event) {
      if (!event.isCancelled()) {
         if (event.getCause() == RemoveCause.EXPLOSION) {
            Location loc = event.getEntity().getLocation();
            Faction faction = Board.getFactionAt(new FLocation(loc));
            if (faction.noExplosionsInTerritory()) {
               event.setCancelled(true);
               return;
            }

            boolean online = faction.hasPlayersOnline();
            if ((
                  !faction.isNone()
                     || Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName())
                     || !Conf.wildernessBlockCreepers && !Conf.wildernessBlockFireballs && !Conf.wildernessBlockTNT
               )
               && (
                  !faction.isNormal()
                     || (
                        online
                           ? !Conf.territoryBlockCreepers || !Conf.territoryBlockFireballs || !Conf.territoryBlockTNT
                           : !Conf.territoryBlockCreepersWhenOffline && !Conf.territoryBlockFireballsWhenOffline && !Conf.territoryBlockTNTWhenOffline
                     )
               )) {
               if (!faction.isWarZone() || !Conf.warZoneBlockCreepers && !Conf.warZoneBlockFireballs && !Conf.warZoneBlockTNT) {
                  faction.isSafeZone();
               }
            } else {
               event.setCancelled(true);
            }
         }

         if (event instanceof HangingBreakByEntityEvent) {
            Entity breaker = ((HangingBreakByEntityEvent)event).getRemover();
            if (breaker instanceof Player) {
               if (!FactionsBlockListener.playerCanBuildDestroyBlock((Player)breaker, event.getEntity().getLocation(), "remove paintings", false)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPaintingPlace(HangingPlaceEvent event) {
      if (!event.isCancelled()) {
         if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getLocation(), "place paintings", false)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (!event.isCancelled()) {
         Entity entity = event.getEntity();
         if (entity instanceof Enderman || entity instanceof Wither) {
            Location loc = event.getBlock().getLocation();
            if (entity instanceof Enderman) {
               if (this.stopEndermanBlockManipulation(loc)) {
                  event.setCancelled(true);
               }
            } else if (entity instanceof Wither) {
               Faction faction = Board.getFactionAt(new FLocation(loc));
               if ((!faction.isNone() || !Conf.wildernessBlockFireballs || Conf.worldsNoWildernessProtection.contains(loc.getWorld().getName()))
                  && (!faction.isNormal() || (faction.hasPlayersOnline() ? !Conf.territoryBlockFireballs : !Conf.territoryBlockFireballsWhenOffline))) {
                  if (!faction.isWarZone() || !Conf.warZoneBlockFireballs) {
                     faction.isSafeZone();
                  }

                  return;
               }

               event.setCancelled(true);
            }
         }
      }
   }

   private boolean stopEndermanBlockManipulation(Location loc) {
      if (loc == null) {
         return false;
      } else if (Conf.wildernessDenyEndermanBlocks
         && Conf.territoryDenyEndermanBlocks
         && Conf.territoryDenyEndermanBlocksWhenOffline
         && Conf.safeZoneDenyEndermanBlocks
         && Conf.warZoneDenyEndermanBlocks) {
         return true;
      } else {
         FLocation fLoc = new FLocation(loc);
         Faction claimFaction = Board.getFactionAt(fLoc);
         if (claimFaction.isNone()) {
            return Conf.wildernessDenyEndermanBlocks;
         } else if (claimFaction.isNormal()) {
            return claimFaction.hasPlayersOnline() ? Conf.territoryDenyEndermanBlocks : Conf.territoryDenyEndermanBlocksWhenOffline;
         } else {
            return claimFaction.isSafeZone() ? Conf.safeZoneDenyEndermanBlocks : claimFaction.isWarZone() && Conf.warZoneDenyEndermanBlocks;
         }
      }
   }
}
