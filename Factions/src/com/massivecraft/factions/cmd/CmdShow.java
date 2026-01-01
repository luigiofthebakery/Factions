package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import java.util.Collection;

public class CmdShow extends FCommand {
   public CmdShow() {
      this.aliases.add("show");
      this.aliases.add("who");
      this.optionalArgs.put("faction tag", "yours");
      this.permission = Permission.SHOW.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      Faction faction = this.myFaction;
      if (this.argIsSet(0)) {
         faction = this.argAsFaction(0);
         if (faction == null) {
            return;
         }
      }

      if (this.payForCommand(Conf.econCostShow, "to show faction information", "for showing faction information")) {
         Collection<FPlayer> admins = faction.getFPlayersWhereRole(Role.ADMIN);
         Collection<FPlayer> mods = faction.getFPlayersWhereRole(Role.MODERATOR);
         Collection<FPlayer> normals = faction.getFPlayersWhereRole(Role.NORMAL);
         Collection<FPlayer> trustedPlayers = faction.getTrustedFPlayers();
         this.msg(this.p.txt.titleize(faction.getTag(this.fme)), new Object[0]);
         this.msg("<a>Description: <i>%s", new Object[]{faction.getDescription()});
         if (faction.isNormal()) {
            String peaceStatus = "";
            if (faction.isPeaceful()) {
               peaceStatus = "     " + Conf.colorNeutral + "This faction is Peaceful";
            }

            this.msg("<a>Joining: <i>" + (faction.getOpen() ? "no invitation is needed" : "invitation is required") + peaceStatus, new Object[0]);
            double powerBoost = faction.getPowerBoost();
            String boost = powerBoost == 0.0 ? "" : (powerBoost > 0.0 ? " (bonus: " : " (penalty: ") + powerBoost + ")";
            this.msg(
               "<a>Land / Power / Maxpower: <i> %d/%d/%d %s",
               new Object[]{faction.getLandRounded(), faction.getPowerRounded(), faction.getPowerMaxRounded(), boost}
            );
            if (faction.isPermanent()) {
               this.msg("<a>This faction is permanent, remaining even with no members.", new Object[0]);
            }

            if (Econ.shouldBeUsed()) {
               double value = Econ.calculateTotalLandValue(faction.getLandRounded());
               double refund = value * Conf.econClaimRefundMultiplier;
               if (value > 0.0) {
                  String stringValue = Econ.moneyString(value);
                  String stringRefund = refund > 0.0 ? " (" + Econ.moneyString(refund) + " depreciated)" : "";
                  this.msg("<a>Total land value: <i>" + stringValue + stringRefund, new Object[0]);
               }

               if (Conf.bankEnabled) {
                  this.msg("<a>Bank contains: <i>" + Econ.moneyString(Econ.getBalance(faction.getAccountId())), new Object[0]);
               }
            }

            String allyList = this.p.txt.parse("<a>Allies: ");
            String enemyList = this.p.txt.parse("<a>Enemies: ");

            for (Faction otherFaction : Factions.i.get()) {
               if (otherFaction != faction) {
                  Relation rel = otherFaction.getRelationTo(faction);
                  if (rel.isAlly() || rel.isEnemy()) {
                     String listpart = otherFaction.getTag(this.fme) + this.p.txt.parse("<i>") + ", ";
                     if (rel.isAlly()) {
                        allyList = allyList + listpart;
                     } else if (rel.isEnemy()) {
                        enemyList = enemyList + listpart;
                     }
                  }
               }
            }

            if (allyList.endsWith(", ")) {
               allyList = allyList.substring(0, allyList.length() - 2);
            }

            if (enemyList.endsWith(", ")) {
               enemyList = enemyList.substring(0, enemyList.length() - 2);
            }

            this.sendMessage(allyList);
            this.sendMessage(enemyList);
            String onlineList = this.p.txt.parse("<a>") + "Members online: ";
            String offlineList = this.p.txt.parse("<a>") + "Members offline: ";
            StringBuilder trustedPlayerListBuilder = new StringBuilder().append(this.p.txt.parse("<a>")).append("Trusted players: ");

            for (FPlayer follower : admins) {
               String listpart = follower.getNameAndTitle(this.fme) + this.p.txt.parse("<i>") + ", ";
               if (follower.isOnlineAndVisibleTo(this.me)) {
                  onlineList = onlineList + listpart;
               } else {
                  offlineList = offlineList + listpart;
               }
            }

            for (FPlayer followerx : mods) {
               String listpart = followerx.getNameAndTitle(this.fme) + this.p.txt.parse("<i>") + ", ";
               if (followerx.isOnlineAndVisibleTo(this.me)) {
                  onlineList = onlineList + listpart;
               } else {
                  offlineList = offlineList + listpart;
               }
            }

            for (FPlayer followerxx : normals) {
               String listpart = followerxx.getNameAndTitle(this.fme) + this.p.txt.parse("<i>") + ", ";
               if (followerxx.isOnlineAndVisibleTo(this.me)) {
                  onlineList = onlineList + listpart;
               } else {
                  offlineList = offlineList + listpart;
               }
            }

            if (onlineList.endsWith(", ")) {
               onlineList = onlineList.substring(0, onlineList.length() - 2);
            }

            if (offlineList.endsWith(", ")) {
               offlineList = offlineList.substring(0, offlineList.length() - 2);
            }

            this.sendMessage(onlineList);
            this.sendMessage(offlineList);
            if (Conf.trustEnabled) {
               for (FPlayer trustedPlayer : trustedPlayers) {
                  trustedPlayerListBuilder
                     .append(trustedPlayer.getColorTo(fme))
                     .append(trustedPlayer.getName())
                     .append(p.txt.parse("<i>")).append(", ");
               }

               String trustedPlayerList = trustedPlayerListBuilder.toString();

               if (trustedPlayerList.endsWith(", ")) {
                  trustedPlayerList = trustedPlayerList.substring(0, trustedPlayerList.length() - 2);
               }

               if (Conf.trustEnabled && !trustedPlayers.isEmpty()) {
                  sendMessage(trustedPlayerList);
               }
            }
         }
      }
   }
}
