package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Bukkit;

public class CmdUnclaim extends FCommand {
   public CmdUnclaim() {
      this.aliases.add("unclaim");
      this.aliases.add("declaim");
      this.permission = Permission.UNCLAIM.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FLocation flocation = new FLocation(this.fme);
      Faction otherFaction = Board.getFactionAt(flocation);
      if (otherFaction.isSafeZone()) {
         if (Permission.MANAGE_SAFE_ZONE.has(this.sender)) {
            Board.removeAt(flocation);
            SpoutFeatures.updateTerritoryDisplayLoc(flocation);
            this.msg("<i>Safe zone was unclaimed.", new Object[0]);
            if (Conf.logLandUnclaims) {
               P.p.log(this.fme.getName() + " unclaimed land at (" + flocation.getCoordString() + ") from the faction: " + otherFaction.getTag());
            }
         } else {
            this.msg("<b>This is a safe zone. You lack permissions to unclaim.", new Object[0]);
         }
      } else if (otherFaction.isWarZone()) {
         if (Permission.MANAGE_WAR_ZONE.has(this.sender)) {
            Board.removeAt(flocation);
            SpoutFeatures.updateTerritoryDisplayLoc(flocation);
            this.msg("<i>War zone was unclaimed.", new Object[0]);
            if (Conf.logLandUnclaims) {
               P.p.log(this.fme.getName() + " unclaimed land at (" + flocation.getCoordString() + ") from the faction: " + otherFaction.getTag());
            }
         } else {
            this.msg("<b>This is a war zone. You lack permissions to unclaim.", new Object[0]);
         }
      } else if (this.fme.isAdminBypassing()) {
         Board.removeAt(flocation);
         SpoutFeatures.updateTerritoryDisplayLoc(flocation);
         otherFaction.msg("%s<i> unclaimed some of your land.", this.fme.describeTo(otherFaction, true));
         this.msg("<i>You unclaimed this land.", new Object[0]);
         if (Conf.logLandUnclaims) {
            P.p.log(this.fme.getName() + " unclaimed land at (" + flocation.getCoordString() + ") from the faction: " + otherFaction.getTag());
         }
      } else if (this.assertHasFaction()) {
         if (this.assertMinRole(Role.MODERATOR)) {
            if (this.myFaction != otherFaction) {
               this.msg("<b>You don't own this land.", new Object[0]);
            } else {
               LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(flocation, otherFaction, this.fme);
               Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
               if (!unclaimEvent.isCancelled()) {
                  if (Econ.shouldBeUsed()) {
                     double refund = Econ.calculateClaimRefund(this.myFaction.getLandRounded());
                     if (Conf.bankEnabled && Conf.bankFactionPaysLandCosts) {
                        if (!Econ.modifyMoney(this.myFaction, refund, "to unclaim this land", "for unclaiming this land")) {
                           return;
                        }
                     } else if (!Econ.modifyMoney(this.fme, refund, "to unclaim this land", "for unclaiming this land")) {
                        return;
                     }
                  }

                  Board.removeAt(flocation);
                  SpoutFeatures.updateTerritoryDisplayLoc(flocation);
                  this.myFaction.msg("%s<i> unclaimed some land.", this.fme.describeTo(this.myFaction, true));
                  if (Conf.logLandUnclaims) {
                     P.p
                        .log(
                           this.fme.getName()
                              + " unclaimed land at "
                              + flocation.toString()
                              + " from the faction: "
                              + otherFaction.getTag()
                              + " ("
                              + otherFaction.getId()
                              + ")"
                        );
                  }
               }
            }
         }
      }
   }
}
