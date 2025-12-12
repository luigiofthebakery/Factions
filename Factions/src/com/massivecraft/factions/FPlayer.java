package com.massivecraft.factions;

import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.zcore.persist.PlayerEntity;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FPlayer extends PlayerEntity implements EconomyParticipator {
   private transient FLocation lastStoodAt = new FLocation();
   private String factionId;
   private Role role;
   private String title;
   private double power;
   private double powerBoost;
   private long lastPowerUpdateTime;
   private long lastLoginTime;
   private transient boolean mapAutoUpdating;
   private transient Faction autoClaimFor;
   private transient boolean autoSafeZoneEnabled;
   private transient boolean autoWarZoneEnabled;
   private transient boolean isAdminBypassing = false;
   private transient boolean loginPvpDisabled;
   private transient boolean deleteMe;
   private ChatMode chatMode;
   private transient boolean spyingChat;

   public Faction getFaction() {
      return this.factionId == null ? null : Factions.i.get(this.factionId);
   }

   public String getFactionId() {
      return this.factionId;
   }

   public boolean hasFaction() {
      return !this.factionId.equals("0");
   }

   public void setFaction(Faction faction) {
      Faction oldFaction = this.getFaction();
      if (oldFaction != null) {
         oldFaction.removeFPlayer(this);
      }

      faction.addFPlayer(this);
      this.factionId = faction.getId();
      SpoutFeatures.updateAppearances(this.getPlayer());
   }

   public Role getRole() {
      return this.role;
   }

   public void setRole(Role role) {
      this.role = role;
      SpoutFeatures.updateAppearances(this.getPlayer());
   }

   public double getPowerBoost() {
      return this.powerBoost;
   }

   public void setPowerBoost(double powerBoost) {
      this.powerBoost = powerBoost;
   }

   public Faction getAutoClaimFor() {
      return this.autoClaimFor;
   }

   public void setAutoClaimFor(Faction faction) {
      this.autoClaimFor = faction;
      if (this.autoClaimFor != null) {
         this.autoSafeZoneEnabled = false;
         this.autoWarZoneEnabled = false;
      }
   }

   public boolean isAutoSafeClaimEnabled() {
      return this.autoSafeZoneEnabled;
   }

   public void setIsAutoSafeClaimEnabled(boolean enabled) {
      this.autoSafeZoneEnabled = enabled;
      if (enabled) {
         this.autoClaimFor = null;
         this.autoWarZoneEnabled = false;
      }
   }

   public boolean isAutoWarClaimEnabled() {
      return this.autoWarZoneEnabled;
   }

   public void setIsAutoWarClaimEnabled(boolean enabled) {
      this.autoWarZoneEnabled = enabled;
      if (enabled) {
         this.autoClaimFor = null;
         this.autoSafeZoneEnabled = false;
      }
   }

   public boolean isAdminBypassing() {
      return this.isAdminBypassing;
   }

   public void setIsAdminBypassing(boolean val) {
      this.isAdminBypassing = val;
   }

   public void setChatMode(ChatMode chatMode) {
      this.chatMode = chatMode;
   }

   public ChatMode getChatMode() {
      if (this.factionId.equals("0") || !Conf.factionOnlyChat) {
         this.chatMode = ChatMode.PUBLIC;
      }

      return this.chatMode;
   }

   public void setSpyingChat(boolean chatSpying) {
      this.spyingChat = chatSpying;
   }

   public boolean isSpyingChat() {
      return this.spyingChat;
   }

   @Override
   public String getAccountId() {
      return this.getId();
   }

   public FPlayer() {
      this.resetFactionData(this.spyingChat = false);
      this.power = Conf.powerPlayerStarting;
      this.lastPowerUpdateTime = System.currentTimeMillis();
      this.lastLoginTime = System.currentTimeMillis();
      this.mapAutoUpdating = false;
      this.autoClaimFor = null;
      this.autoSafeZoneEnabled = false;
      this.autoWarZoneEnabled = false;
      this.loginPvpDisabled = Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0;
      this.deleteMe = false;
      this.powerBoost = 0.0;
      if (!Conf.newPlayerStartingFactionID.equals("0") && Factions.i.exists(Conf.newPlayerStartingFactionID)) {
         this.factionId = Conf.newPlayerStartingFactionID;
      }
   }

   public final void resetFactionData(boolean doSpoutUpdate) {
      if (Factions.i.exists(this.getFactionId())) {
         Faction currentFaction = this.getFaction();
         currentFaction.removeFPlayer(this);
         if (currentFaction.isNormal()) {
            currentFaction.clearClaimOwnership(this.getId());
         }
      }

      this.factionId = "0";
      this.chatMode = ChatMode.PUBLIC;
      this.role = Role.NORMAL;
      this.title = "";
      this.autoClaimFor = null;
      if (doSpoutUpdate) {
         SpoutFeatures.updateAppearances(this.getPlayer());
      }
   }

   public void resetFactionData() {
      this.resetFactionData(true);
   }

   public long getLastLoginTime() {
      return this.lastLoginTime;
   }

   public void setLastLoginTime(long lastLoginTime) {
      this.losePowerFromBeingOffline();
      this.lastLoginTime = lastLoginTime;
      this.lastPowerUpdateTime = lastLoginTime;
      if (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) {
         this.loginPvpDisabled = true;
      }
   }

   public boolean isMapAutoUpdating() {
      return this.mapAutoUpdating;
   }

   public void setMapAutoUpdating(boolean mapAutoUpdating) {
      this.mapAutoUpdating = mapAutoUpdating;
   }

   public boolean hasLoginPvpDisabled() {
      return this.loginPvpDisabled
         && (this.lastLoginTime + Conf.noPVPDamageToOthersForXSecondsAfterLogin * 1000 >= System.currentTimeMillis() || (this.loginPvpDisabled = false));
   }

   public FLocation getLastStoodAt() {
      return this.lastStoodAt;
   }

   public void setLastStoodAt(FLocation flocation) {
      this.lastStoodAt = flocation;
   }

   public void markForDeletion(boolean delete) {
      this.deleteMe = delete;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getName() {
      return this.getId();
   }

   public String getTag() {
      return !this.hasFaction() ? "" : this.getFaction().getTag();
   }

   public String getNameAndSomething(String something) {
      String ret = this.role.getPrefix();
      if (something.length() > 0) {
         ret = ret + something + " ";
      }

      return ret + this.getName();
   }

   public String getNameAndTitle() {
      return this.getNameAndSomething(this.getTitle());
   }

   public String getNameAndTag() {
      return this.getNameAndSomething(this.getTag());
   }

   public String getNameAndTitle(Faction faction) {
      return this.getColorTo(faction) + this.getNameAndTitle();
   }

   public String getNameAndTitle(FPlayer fplayer) {
      return this.getColorTo(fplayer) + this.getNameAndTitle();
   }

   public String getChatTag() {
      return !this.hasFaction() ? "" : String.format(Conf.chatTagFormat, this.role.getPrefix() + this.getTag());
   }

   public String getChatTag(Faction faction) {
      return !this.hasFaction() ? "" : this.getRelationTo(faction).getColor() + this.getChatTag();
   }

   public String getChatTag(FPlayer fplayer) {
      return !this.hasFaction() ? "" : this.getColorTo(fplayer) + this.getChatTag();
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

   public Relation getRelationToLocation() {
      return Board.getFactionAt(new FLocation(this)).getRelationTo(this);
   }

   @Override
   public ChatColor getColorTo(RelationParticipator rp) {
      return RelationUtil.getColorOfThatToMe(this, rp);
   }

   public void heal(int amnt) {
      Player player = this.getPlayer();
      if (player != null) {
         player.setHealth(player.getHealth() + amnt);
      }
   }

   public double getPower() {
      this.updatePower();
      return this.power;
   }

   protected void alterPower(double delta) {
      this.power += delta;
      if (this.power > this.getPowerMax()) {
         this.power = this.getPowerMax();
      } else if (this.power < this.getPowerMin()) {
         this.power = this.getPowerMin();
      }
   }

   public double getPowerMax() {
      return Conf.powerPlayerMax + this.powerBoost;
   }

   public double getPowerMin() {
      return Conf.powerPlayerMin + this.powerBoost;
   }

   public int getPowerRounded() {
      return (int)Math.round(this.getPower());
   }

   public int getPowerMaxRounded() {
      return (int)Math.round(this.getPowerMax());
   }

   public int getPowerMinRounded() {
      return (int)Math.round(this.getPowerMin());
   }

   protected void updatePower() {
      if (this.isOffline()) {
         this.losePowerFromBeingOffline();
         if (!Conf.powerRegenOffline) {
            return;
         }
      }

      long now = System.currentTimeMillis();
      long millisPassed = now - this.lastPowerUpdateTime;
      this.lastPowerUpdateTime = now;
      Player thisPlayer = this.getPlayer();
      if (thisPlayer == null || !thisPlayer.isDead()) {
         int millisPerMinute = 60000;
         this.alterPower(millisPassed * Conf.powerPerMinute / 60000.0);
      }
   }

   protected void losePowerFromBeingOffline() {
      if (Conf.powerOfflineLossPerDay > 0.0 && this.power > Conf.powerOfflineLossLimit) {
         long now = System.currentTimeMillis();
         long millisPassed = now - this.lastPowerUpdateTime;
         this.lastPowerUpdateTime = now;
         double loss = millisPassed * Conf.powerOfflineLossPerDay / 8.64E7;
         if (this.power - loss < Conf.powerOfflineLossLimit) {
            loss = this.power;
         }

         this.alterPower(-loss);
      }
   }

   public void onDeath() {
      this.updatePower();
      this.alterPower(-Conf.powerPerDeath);
   }

   public boolean isInOwnTerritory() {
      return Board.getFactionAt(new FLocation(this)) == this.getFaction();
   }

   public boolean isInOthersTerritory() {
      Faction factionHere = Board.getFactionAt(new FLocation(this));
      return factionHere != null && factionHere.isNormal() && factionHere != this.getFaction();
   }

   public boolean isInAllyTerritory() {
      return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isAlly();
   }

   public boolean isInNeutralTerritory() {
      return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isNeutral();
   }

   public boolean isInEnemyTerritory() {
      return Board.getFactionAt(new FLocation(this)).getRelationTo(this).isEnemy();
   }

   public void sendFactionHereMessage() {
      if (!SpoutFeatures.updateTerritoryDisplay(this)) {
         Faction factionHere = Board.getFactionAt(this.getLastStoodAt());
         String msg = P.p.txt.parse("<i>") + " ~ " + factionHere.getTag(this);
         if (factionHere.getDescription().length() > 0) {
            msg = msg + " - " + factionHere.getDescription();
         }

         this.sendMessage(msg);
      }
   }

   public void leave(boolean makePay) {
      Faction myFaction = this.getFaction();
      makePay = makePay && Econ.shouldBeUsed() && !this.isAdminBypassing();
      if (myFaction == null) {
         this.resetFactionData();
      } else {
         boolean perm = myFaction.isPermanent();
         if (!perm && this.getRole() == Role.ADMIN && myFaction.getFPlayers().size() > 1) {
            this.msg("<b>You must give the admin role to someone else first.");
         } else if (!Conf.canLeaveWithNegativePower && this.getPower() < 0.0) {
            this.msg("<b>You cannot leave until your power is positive.");
         } else if (!makePay || Econ.hasAtLeast(this, Conf.econCostLeave, "to leave your faction.")) {
            FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this, myFaction, FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
            Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
            if (!leaveEvent.isCancelled()) {
               if (!makePay || Econ.modifyMoney(this, -Conf.econCostLeave, "to leave your faction.", "for leaving your faction.")) {
                  if (myFaction.getFPlayers().size() == 1 && Econ.shouldBeUsed()) {
                     Econ.transferMoney(this, myFaction, this, Econ.getBalance(myFaction.getAccountId()));
                  }

                  if (myFaction.isNormal()) {
                     for (FPlayer fplayer : myFaction.getFPlayersWhereOnline(true)) {
                        fplayer.msg("%s<i> left %s<i>.", this.describeTo(fplayer, true), myFaction.describeTo(fplayer));
                     }

                     if (Conf.logFactionLeave) {
                        P.p.log(this.getName() + " left the faction: " + myFaction.getTag() + " (" + myFaction.getId() + ")");
                     }
                  }

                  this.resetFactionData();
                  if (myFaction.isNormal() && !perm && myFaction.getFPlayers().isEmpty()) {
                     for (FPlayer fplayer : FPlayers.i.getOnline()) {
                        fplayer.msg("<i>%s<i> was disbanded.", myFaction.describeTo(fplayer, true));
                     }

                     myFaction.detach();
                     if (Conf.logFactionDisband) {
                        P.p
                           .log(
                              "The faction "
                                 + myFaction.getTag()
                                 + " ("
                                 + myFaction.getId()
                                 + ") was disbanded due to the last player ("
                                 + this.getName()
                                 + ") leaving."
                           );
                     }
                  }
               }
            }
         }
      }
   }

   public boolean canClaimForFaction(Faction forFaction) {
      return !forFaction.isNone()
         && (
            this.isAdminBypassing()
               || forFaction == this.getFaction() && this.getRole().isAtLeast(Role.MODERATOR)
               || forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(this.getPlayer())
               || forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(this.getPlayer())
         );
   }

   public boolean canClaimForFactionAtLocation(Faction forFaction, Location location, boolean notifyFailure) {
      String error = null;
      FLocation flocation = new FLocation(location);
      Faction myFaction = this.getFaction();
      Faction currentFaction = Board.getFactionAt(flocation);
      int ownedLand = forFaction.getLandRounded();
      if (Conf.worldGuardChecking && Worldguard.checkForRegionsInChunk(location)) {
         error = P.p.txt.parse("<b>This land is protected");
      } else if (Conf.worldsNoClaiming.contains(flocation.getWorldName())) {
         error = P.p.txt.parse("<b>Sorry, this world has land claiming disabled.");
      } else {
         if (this.isAdminBypassing()) {
            return true;
         }

         if (forFaction.isSafeZone() && Permission.MANAGE_SAFE_ZONE.has(this.getPlayer())) {
            return true;
         }

         if (forFaction.isWarZone() && Permission.MANAGE_WAR_ZONE.has(this.getPlayer())) {
            return true;
         }

         if (myFaction != forFaction) {
            error = P.p.txt.parse("<b>You can't claim land for <h>%s<b>.", forFaction.describeTo(this));
         } else if (forFaction == currentFaction) {
            error = P.p.txt.parse("%s<i> already own this land.", forFaction.describeTo(this, true));
         } else if (this.getRole().value < Role.MODERATOR.value) {
            error = P.p.txt.parse("<b>You must be <h>%s<b> to claim land.", Role.MODERATOR.toString());
         } else if (forFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
            error = P.p.txt.parse("Factions must have at least <h>%s<b> members to claim land.", Conf.claimsRequireMinFactionMembers);
         } else if (currentFaction.isSafeZone()) {
            error = P.p.txt.parse("<b>You can not claim a Safe Zone.");
         } else if (currentFaction.isWarZone()) {
            error = P.p.txt.parse("<b>You can not claim a War Zone.");
         } else if (ownedLand >= forFaction.getPowerRounded()) {
            error = P.p.txt.parse("<b>You can't claim more land! You need more power!");
         } else if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && forFaction.isNormal()) {
            error = P.p.txt.parse("<b>Limit reached. You can't claim more land!");
         } else if (currentFaction.getRelationTo(forFaction) == Relation.ALLY) {
            error = P.p.txt.parse("<b>You can't claim the land of your allies.");
         } else if (Conf.claimsMustBeConnected
            && !this.isAdminBypassing()
            && myFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0
            && !Board.isConnectedLocation(flocation, myFaction)
            && (!Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction || !currentFaction.isNormal())) {
            if (Conf.claimsCanBeUnconnectedIfOwnedByOtherFaction) {
               error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim or controlled by another faction!");
            } else {
               error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim!");
            }
         } else if (currentFaction.isNormal()) {
            if (myFaction.isPeaceful()) {
               error = P.p
                  .txt
                  .parse("%s<i> owns this land. Your faction is peaceful, so you cannot claim land from other factions.", currentFaction.getTag(this));
            } else if (currentFaction.isPeaceful()) {
               error = P.p.txt.parse("%s<i> owns this land, and is a peaceful faction. You cannot claim land from them.", currentFaction.getTag(this));
            } else if (!currentFaction.hasLandInflation()) {
               error = P.p.txt.parse("%s<i> owns this land and is strong enough to keep it.", currentFaction.getTag(this));
            } else if (!Board.isBorderLocation(flocation)) {
               error = P.p.txt.parse("<b>You must start claiming land at the border of the territory.");
            }
         }
      }

      if (notifyFailure && error != null) {
         this.msg(error);
      }

      return error == null;
   }

   public boolean attemptClaim(Faction forFaction, Location location, boolean notifyFailure) {
      FLocation flocation = new FLocation(location);
      Faction currentFaction = Board.getFactionAt(flocation);
      int ownedLand = forFaction.getLandRounded();
      if (!this.canClaimForFactionAtLocation(forFaction, location, notifyFailure)) {
         return false;
      } else {
         boolean mustPay = Econ.shouldBeUsed() && !this.isAdminBypassing() && !forFaction.isSafeZone() && !forFaction.isWarZone();
         double cost = 0.0;
         EconomyParticipator payee = null;
         if (mustPay) {
            cost = Econ.calculateClaimCost(ownedLand, currentFaction.isNormal());
            if (Conf.econClaimUnconnectedFee != 0.0
               && forFaction.getLandRoundedInWorld(flocation.getWorldName()) > 0
               && !Board.isConnectedLocation(flocation, forFaction)) {
               cost += Conf.econClaimUnconnectedFee;
            }

            if (Conf.bankEnabled && Conf.bankFactionPaysLandCosts && this.hasFaction()) {
               payee = this.getFaction();
            } else {
               payee = this;
            }

            if (!Econ.hasAtLeast(payee, cost, "to claim this land")) {
               return false;
            }
         }

         LandClaimEvent claimEvent = new LandClaimEvent(flocation, forFaction, this);
         Bukkit.getServer().getPluginManager().callEvent(claimEvent);
         if (claimEvent.isCancelled()) {
            return false;
         } else if (mustPay && !Econ.modifyMoney(payee, -cost, "to claim this land", "for claiming this land")) {
            return false;
         } else {
            if (LWCFeatures.getEnabled() && forFaction.isNormal() && Conf.onCaptureResetLwcLocks) {
               LWCFeatures.clearOtherChests(flocation, this.getFaction());
            }

            Set<FPlayer> informTheseFPlayers = new HashSet<>();
            informTheseFPlayers.add(this);
            informTheseFPlayers.addAll(forFaction.getFPlayersWhereOnline(true));

            for (FPlayer fp : informTheseFPlayers) {
               fp.msg("<h>%s<i> claimed land for <h>%s<i> from <h>%s<i>.", this.describeTo(fp, true), forFaction.describeTo(fp), currentFaction.describeTo(fp));
            }

            Board.setFactionAt(forFaction, flocation);
            SpoutFeatures.updateTerritoryDisplayLoc(flocation);
            if (Conf.logLandClaims) {
               P.p
                  .log(
                     this.getName() + " claimed land at " + flocation.toString() + " for the faction: " + forFaction.getTag() + " (" + forFaction.getId() + ")"
                  );
            }

            return true;
         }
      }
   }

   @Override
   public boolean shouldBeSaved() {
      return (this.hasFaction() || this.getPowerRounded() != this.getPowerMaxRounded() && this.getPowerRounded() != (int)Math.round(Conf.powerPlayerStarting))
         && !this.deleteMe;
   }

   @Override
   public void msg(String str, Object... args) {
      this.sendMessage(P.p.txt.parse(str, args));
   }
}
