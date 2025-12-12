package com.massivecraft.factions;

import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.zcore.persist.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Faction extends Entity implements EconomyParticipator {
   private Map<String, Relation> relationWish;
   private Map<FLocation, Set<String>> claimOwnership = new ConcurrentHashMap<>();
   private transient Set<FPlayer> fplayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private Set<String> invites;
   private boolean open;
   private boolean peaceful;
   private boolean peacefulExplosionsEnabled;
   private boolean permanent;
   private String tag;
   private String description;
   private LazyLocation home;
   private transient long lastPlayerLoggedOffTime;
   public double money;
   private Integer permanentPower;
   private double powerBoost;

   public void invite(FPlayer fplayer) {
      this.invites.add(fplayer.getName().toLowerCase());
   }

   public void deinvite(FPlayer fplayer) {
      this.invites.remove(fplayer.getName().toLowerCase());
   }

   public boolean isInvited(FPlayer fplayer) {
      return this.invites.contains(fplayer.getName().toLowerCase());
   }

   public boolean getOpen() {
      return this.open;
   }

   public void setOpen(boolean isOpen) {
      this.open = isOpen;
   }

   public boolean isPeaceful() {
      return this.peaceful;
   }

   public void setPeaceful(boolean isPeaceful) {
      this.peaceful = isPeaceful;
   }

   public void setPeacefulExplosionsEnabled(boolean val) {
      this.peacefulExplosionsEnabled = val;
   }

   public boolean getPeacefulExplosionsEnabled() {
      return this.peacefulExplosionsEnabled;
   }

   public boolean noExplosionsInTerritory() {
      return this.peaceful && !this.peacefulExplosionsEnabled;
   }

   public boolean isPermanent() {
      return this.permanent || !this.isNormal();
   }

   public void setPermanent(boolean isPermanent) {
      this.permanent = isPermanent;
   }

   public String getTag() {
      return this.tag;
   }

   public String getTag(String prefix) {
      return prefix + this.tag;
   }

   public String getTag(Faction otherFaction) {
      return otherFaction == null ? this.getTag() : this.getTag(this.getColorTo(otherFaction).toString());
   }

   public String getTag(FPlayer otherFplayer) {
      return otherFplayer == null ? this.getTag() : this.getTag(this.getColorTo(otherFplayer).toString());
   }

   public void setTag(String str) {
      if (Conf.factionTagForceUpperCase) {
         str = str.toUpperCase();
      }

      this.tag = str;
   }

   public String getComparisonTag() {
      return MiscUtil.getComparisonString(this.tag);
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public void setHome(Location home) {
      this.home = new LazyLocation(home);
   }

   public boolean hasHome() {
      return this.getHome() != null;
   }

   public Location getHome() {
      this.confirmValidHome();
      return this.home != null ? this.home.getLocation() : null;
   }

   public void confirmValidHome() {
      if (Conf.homesMustBeInClaimedTerritory
         && this.home != null
         && (this.home.getLocation() == null || Board.getFactionAt(new FLocation(this.home.getLocation())) != this)) {
         this.msg("<b>Your faction home has been un-set since it is no longer in your territory.");
         this.home = null;
      }
   }

   @Override
   public String getAccountId() {
      String aid = "faction-" + this.getId();
      if (!Econ.hasAccount(aid)) {
         Econ.setBalance(aid, 0.0);
      }

      return aid;
   }

   public Integer getPermanentPower() {
      return this.permanentPower;
   }

   public void setPermanentPower(Integer permanentPower) {
      this.permanentPower = permanentPower;
   }

   public boolean hasPermanentPower() {
      return this.permanentPower != null;
   }

   public double getPowerBoost() {
      return this.powerBoost;
   }

   public void setPowerBoost(double powerBoost) {
      this.powerBoost = powerBoost;
   }

   public Faction() {
      this.relationWish = new HashMap<>();
      this.invites = new HashSet<>();
      this.open = Conf.newFactionsDefaultOpen;
      this.tag = "???";
      this.description = "Default faction description :(";
      this.lastPlayerLoggedOffTime = 0L;
      this.peaceful = false;
      this.peacefulExplosionsEnabled = false;
      this.permanent = false;
      this.money = 0.0;
      this.powerBoost = 0.0;
   }

   public boolean noPvPInTerritory() {
      return this.isSafeZone() || this.peaceful && Conf.peacefulTerritoryDisablePVP;
   }

   public boolean noMonstersInTerritory() {
      return this.isSafeZone() || this.peaceful && Conf.peacefulTerritoryDisableMonsters;
   }

   public boolean isNormal() {
      return !this.isNone() && !this.isSafeZone() && !this.isWarZone();
   }

   public boolean isNone() {
      return this.getId().equals("0");
   }

   public boolean isSafeZone() {
      return this.getId().equals("-1");
   }

   public boolean isWarZone() {
      return this.getId().equals("-2");
   }

   public boolean isPlayerFreeType() {
      return this.isSafeZone() || this.isWarZone();
   }

   @Override
   public String describeTo(RelationParticipator that, boolean ucfirst) {
      return RelationUtil.describeThatToMe(this, that, ucfirst);
   }

   @Override
   public String describeTo(RelationParticipator that) {
      return RelationUtil.describeThatToMe(this, that);
   }

   @Override
   public Relation getRelationTo(RelationParticipator rp) {
      return RelationUtil.getRelationTo(this, rp);
   }

   @Override
   public Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful) {
      return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
   }

   @Override
   public ChatColor getColorTo(RelationParticipator rp) {
      return RelationUtil.getColorOfThatToMe(this, rp);
   }

   public Relation getRelationWish(Faction otherFaction) {
      return this.relationWish.containsKey(otherFaction.getId()) ? this.relationWish.get(otherFaction.getId()) : Relation.NEUTRAL;
   }

   public void setRelationWish(Faction otherFaction, Relation relation) {
      if (this.relationWish.containsKey(otherFaction.getId()) && relation.equals(Relation.NEUTRAL)) {
         this.relationWish.remove(otherFaction.getId());
      } else {
         this.relationWish.put(otherFaction.getId(), relation);
      }
   }

   public double getPower() {
      if (this.hasPermanentPower()) {
         return this.getPermanentPower().intValue();
      } else {
         double ret = 0.0;

         for (FPlayer fplayer : this.fplayers) {
            ret += fplayer.getPower();
         }

         if (Conf.powerFactionMax > 0.0 && ret > Conf.powerFactionMax) {
            ret = Conf.powerFactionMax;
         }

         return ret + this.powerBoost;
      }
   }

   public double getPowerMax() {
      if (this.hasPermanentPower()) {
         return this.getPermanentPower().intValue();
      } else {
         double ret = 0.0;

         for (FPlayer fplayer : this.fplayers) {
            ret += fplayer.getPowerMax();
         }

         if (Conf.powerFactionMax > 0.0 && ret > Conf.powerFactionMax) {
            ret = Conf.powerFactionMax;
         }

         return ret + this.powerBoost;
      }
   }

   public int getPowerRounded() {
      return (int)Math.round(this.getPower());
   }

   public int getPowerMaxRounded() {
      return (int)Math.round(this.getPowerMax());
   }

   public int getLandRounded() {
      return Board.getFactionCoordCount(this);
   }

   public int getLandRoundedInWorld(String worldName) {
      return Board.getFactionCoordCountInWorld(this, worldName);
   }

   public boolean hasLandInflation() {
      return this.getLandRounded() > this.getPowerRounded();
   }

   public void refreshFPlayers() {
      this.fplayers.clear();
      if (!this.isPlayerFreeType()) {
         for (FPlayer fplayer : FPlayers.i.get()) {
            if (fplayer.getFaction() == this) {
               this.fplayers.add(fplayer);
            }
         }
      }
   }

   protected boolean addFPlayer(FPlayer fplayer) {
      return !this.isPlayerFreeType() && this.fplayers.add(fplayer);
   }

   protected boolean removeFPlayer(FPlayer fplayer) {
      return !this.isPlayerFreeType() && this.fplayers.remove(fplayer);
   }

   public Set<FPlayer> getFPlayers() {
      Set<FPlayer> ret = new HashSet<>(this.fplayers);
      return ret;
   }

   public Set<FPlayer> getFPlayersWhereOnline(boolean online) {
      Set<FPlayer> ret = new HashSet<>();

      for (FPlayer fplayer : this.fplayers) {
         if (fplayer.isOnline() == online) {
            ret.add(fplayer);
         }
      }

      return ret;
   }

   public FPlayer getFPlayerAdmin() {
      if (!this.isNormal()) {
         return null;
      } else {
         for (FPlayer fplayer : this.fplayers) {
            if (fplayer.getRole() == Role.ADMIN) {
               return fplayer;
            }
         }

         return null;
      }
   }

   public ArrayList<FPlayer> getFPlayersWhereRole(Role role) {
      ArrayList<FPlayer> ret = new ArrayList<>();
      if (!this.isNormal()) {
         return ret;
      } else {
         for (FPlayer fplayer : this.fplayers) {
            if (fplayer.getRole() == role) {
               ret.add(fplayer);
            }
         }

         return ret;
      }
   }

   public ArrayList<Player> getOnlinePlayers() {
      ArrayList<Player> ret = new ArrayList<>();
      if (this.isPlayerFreeType()) {
         return ret;
      } else {
         for (Player player : P.p.getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.i.get(player);
            if (fplayer.getFaction() == this) {
               ret.add(player);
            }
         }

         return ret;
      }
   }

   public boolean hasPlayersOnline() {
      if (this.isPlayerFreeType()) {
         return false;
      } else {
         for (Player player : P.p.getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.i.get(player);
            if (fplayer.getFaction() == this) {
               return true;
            }
         }

         return Conf.considerFactionsReallyOfflineAfterXMinutes > 0.0
            && System.currentTimeMillis() < this.lastPlayerLoggedOffTime + Conf.considerFactionsReallyOfflineAfterXMinutes * 60000.0;
      }
   }

   public void memberLoggedOff() {
      if (this.isNormal()) {
         this.lastPlayerLoggedOffTime = System.currentTimeMillis();
      }
   }

   public void promoteNewLeader() {
      if (this.isNormal()) {
         if (!this.isPermanent() || !Conf.permanentFactionsDisableLeaderPromotion) {
            FPlayer oldLeader = this.getFPlayerAdmin();
            ArrayList<FPlayer> replacements = this.getFPlayersWhereRole(Role.MODERATOR);
            if (replacements == null || replacements.isEmpty()) {
               replacements = this.getFPlayersWhereRole(Role.NORMAL);
            }

            if (replacements != null && !replacements.isEmpty()) {
               if (oldLeader != null) {
                  oldLeader.setRole(Role.NORMAL);
               }

               replacements.get(0).setRole(Role.ADMIN);
               this.msg(
                  "<i>Faction admin <h>%s<i> has been removed. %s<i> has been promoted as the new faction admin.",
                  oldLeader == null ? "" : oldLeader.getName(),
                  replacements.get(0).getName()
               );
               P.p.log("Faction " + this.getTag() + " (" + this.getId() + ") admin was removed. Replacement admin: " + replacements.get(0).getName());
            } else {
               if (this.isPermanent()) {
                  if (oldLeader != null) {
                     oldLeader.setRole(Role.NORMAL);
                  }

                  return;
               }

               if (Conf.logFactionDisband) {
                  P.p.log("The faction " + this.getTag() + " (" + this.getId() + ") has been disbanded since it has no members left.");
               }

               for (FPlayer fplayer : FPlayers.i.getOnline()) {
                  fplayer.msg("The faction %s<i> was disbanded.", this.getTag(fplayer));
               }

               this.detach();
            }
         }
      }
   }

   @Override
   public void msg(String message, Object... args) {
      message = P.p.txt.parse(message, args);

      for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
         fplayer.sendMessage(message);
      }
   }

   public void sendMessage(String message) {
      for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
         fplayer.sendMessage(message);
      }
   }

   public void sendMessage(List<String> messages) {
      for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
         fplayer.sendMessage(messages);
      }
   }

   public void clearAllClaimOwnership() {
      this.claimOwnership.clear();
   }

   public void clearClaimOwnership(FLocation loc) {
      if (Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled()) {
         LWCFeatures.clearAllChests(loc);
      }

      this.claimOwnership.remove(loc);
   }

   public void clearClaimOwnership(String playerName) {
      if (playerName != null && !playerName.isEmpty()) {
         String player = playerName.toLowerCase();

         for (Entry<FLocation, Set<String>> entry : this.claimOwnership.entrySet()) {
            Set<String> ownerData = entry.getValue();
            if (ownerData != null) {
               Iterator<String> iter = ownerData.iterator();

               while (iter.hasNext()) {
                  if (iter.next().equals(player)) {
                     iter.remove();
                  }
               }

               if (ownerData.isEmpty()) {
                  if (Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled()) {
                     LWCFeatures.clearAllChests(entry.getKey());
                  }

                  this.claimOwnership.remove(entry.getKey());
               }
            }
         }
      }
   }

   public int getCountOfClaimsWithOwners() {
      return this.claimOwnership.isEmpty() ? 0 : this.claimOwnership.size();
   }

   public boolean doesLocationHaveOwnersSet(FLocation loc) {
      if (!this.claimOwnership.isEmpty() && this.claimOwnership.containsKey(loc)) {
         Set<String> ownerData = this.claimOwnership.get(loc);
         return ownerData != null && !ownerData.isEmpty();
      } else {
         return false;
      }
   }

   public boolean isPlayerInOwnerList(String playerName, FLocation loc) {
      if (this.claimOwnership.isEmpty()) {
         return false;
      } else {
         Set<String> ownerData = this.claimOwnership.get(loc);
         return ownerData != null && ownerData.contains(playerName.toLowerCase());
      }
   }

   public void setPlayerAsOwner(String playerName, FLocation loc) {
      Set<String> ownerData = this.claimOwnership.get(loc);
      if (ownerData == null) {
         ownerData = new HashSet<>();
      }

      ownerData.add(playerName.toLowerCase());
      this.claimOwnership.put(loc, ownerData);
   }

   public void removePlayerAsOwner(String playerName, FLocation loc) {
      Set<String> ownerData = this.claimOwnership.get(loc);
      if (ownerData != null) {
         ownerData.remove(playerName.toLowerCase());
         this.claimOwnership.put(loc, ownerData);
      }
   }

   public Set<String> getOwnerList(FLocation loc) {
      return this.claimOwnership.get(loc);
   }

   public String getOwnerListString(FLocation loc) {
      Set<String> ownerData = this.claimOwnership.get(loc);
      if (ownerData != null && !ownerData.isEmpty()) {
         String ownerList = "";

         for (Iterator<String> iter = ownerData.iterator(); iter.hasNext(); ownerList = ownerList + iter.next()) {
            if (!ownerList.isEmpty()) {
               ownerList = ownerList + ", ";
            }
         }

         return ownerList;
      } else {
         return "";
      }
   }

   public boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc) {
      if (fplayer.getFaction() != this
         || !fplayer.getRole().isAtLeast(Conf.ownedAreaModeratorsBypass ? Role.MODERATOR : Role.ADMIN) && !Permission.OWNERSHIP_BYPASS.has(fplayer.getPlayer())
         )
       {
         if (this.claimOwnership.isEmpty()) {
            return true;
         } else {
            Set<String> ownerData = this.claimOwnership.get(loc);
            return ownerData == null || ownerData.isEmpty() || ownerData.contains(fplayer.getName().toLowerCase());
         }
      } else {
         return true;
      }
   }

   @Override
   public void postDetach() {
      if (Econ.shouldBeUsed()) {
         Econ.setBalance(this.getAccountId(), 0.0);
      }

      Board.clean();
      FPlayers.i.clean();
   }
}
