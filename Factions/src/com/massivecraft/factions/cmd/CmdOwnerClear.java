package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdOwnerClear extends FCommand {
   public CmdOwnerClear() {
      this.aliases.add("clear");
      this.aliases.add("clearlist");
      this.permission = Permission.OWNER_CLEAR.node;
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

            if (factionHere.doesLocationHaveOwnersSet(flocation)) {
               factionHere.clearClaimOwnership(flocation);
               SpoutFeatures.updateOwnerListLoc(flocation);
               this.fme.msg("<i>You have cleared ownership for this claimed area.");
            } else {
               this.fme.msg("<i>This claimed area does not have any owners.");
            }
         }
      }
   }
}
