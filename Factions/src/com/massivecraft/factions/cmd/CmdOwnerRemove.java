package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdOwnerRemove extends FCommand {
   public CmdOwnerRemove() {
      this.aliases.add("remove");
      this.optionalArgs.put("player name", "you");
      this.permission = Permission.OWNER_REMOVE.node;
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
               if (target.getFaction() != factionHere) {
                  this.fme.msg("%s<i> %s not a member of %s<i>.", target.describeTo(fme), target.equals(fme) ? "are" : "is", factionHere.describeTo(fme));
               } else if (factionHere.isPlayerInOwnerList(playerName, flocation)) {
                  factionHere.removePlayerAsOwner(playerName, flocation);
                  SpoutFeatures.updateOwnerListLoc(flocation);
                  this.fme.msg("<i>You have removed ownership of this claimed land from %s<i>.", target.describeTo(fme));
               } else {
                  this.fme.msg("%s<i> %s not an owner of this land<i>.", target.describeTo(fme), target.equals(fme) ? "are" : "is");
               }
            }
         }
      }
   }
}
