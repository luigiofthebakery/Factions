package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdOwnerAdd extends FCommand {
   public CmdOwnerAdd() {
      this.aliases.add("add");
      this.optionalArgs.put("player name", "you");
      this.permission = Permission.OWNER_ADD.node;
      this.disableOnLock = false;
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

            FPlayer target = this.argAsBestFPlayerMatch(0, fme);
            if (target != null) {
               String playerName = target.getName();
               if (target.getFaction() != factionHere) {
                  this.fme.msg("%s<i> %s not a member of %s<i>.", target.describeTo(fme), target.equals(fme) ? "are" : "is", factionHere.describeTo(fme));
               } else if (factionHere.isPlayerInOwnerList(playerName, flocation)) {
                  this.fme.msg("%s<i> %s already an owner of this land<i>.", target.describeTo(fme), target.equals(fme) ? "are" : "is");
               } else if (this.payForCommand(Conf.econCostOwner, "to set ownership of claimed land", "for setting ownership of claimed land")) {
                  factionHere.setPlayerAsOwner(playerName, flocation);
                  SpoutFeatures.updateOwnerListLoc(flocation);
                  this.fme.msg("<i>You have added %s<i> to the owner list for this claimed land.", target.describeTo(fme));
               }
            }
         }
      }
   }
}
