package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdOwner extends FCommand {
   public CmdOwner() {
      this.aliases.add("owner");
      this.optionalArgs.put("player name", "you");
      this.permission = Permission.OWNER.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      boolean hasBypass = this.fme.isAdminBypassing();
      if (hasBypass || this.assertHasFaction()) {
         if (!Conf.ownedAreasEnabled) {
            this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         } else if (!hasBypass && Conf.ownedAreasLimitPerFaction > 0 && this.myFaction.getCountOfClaimsWithOwners() >= Conf.ownedAreasLimitPerFaction) {
            this.fme.msg("<b>Sorry, but you have reached the server's <h>limit of %d <b>owned areas per faction.", Conf.ownedAreasLimitPerFaction);
         } else if (hasBypass || this.assertMinRole(Conf.ownedAreasModeratorsCanSet ? Role.MODERATOR : Role.ADMIN)) {
            FLocation flocation = new FLocation(this.fme);
            Faction factionHere = Board.getFactionAt(flocation);
            if (factionHere != this.myFaction) {
               if (!hasBypass) {
                  this.fme.msg("<b>This land is not claimed by your faction, so you can't set ownership of it.");
                  return;
               }

               if (!factionHere.isNormal()) {
                  this.fme.msg("<b>This land is not claimed by a faction. Ownership is not possible.");
                  return;
               }
            }

            FPlayer target = this.argAsBestFPlayerMatch(0, this.fme);
            if (target != null) {
               String playerName = target.getName();
               if (target.getFaction() != this.myFaction) {
                  this.fme.msg("%s<i> is not a member of this faction.", playerName);
               } else if (this.args.isEmpty() && this.myFaction.doesLocationHaveOwnersSet(flocation)) {
                  this.myFaction.clearClaimOwnership(flocation);
                  SpoutFeatures.updateOwnerListLoc(flocation);
                  this.fme.msg("<i>You have cleared ownership for this claimed area.");
               } else if (this.myFaction.isPlayerInOwnerList(playerName, flocation)) {
                  this.myFaction.removePlayerAsOwner(playerName, flocation);
                  SpoutFeatures.updateOwnerListLoc(flocation);
                  this.fme.msg("<i>You have removed ownership of this claimed land from %s<i>.", playerName);
               } else if (this.payForCommand(Conf.econCostOwner, "to set ownership of claimed land", "for setting ownership of claimed land")) {
                  this.myFaction.setPlayerAsOwner(playerName, flocation);
                  SpoutFeatures.updateOwnerListLoc(flocation);
                  this.fme.msg("<i>You have added %s<i> to the owner list for this claimed land.", playerName);
               }
            }
         }
      }
   }
}
