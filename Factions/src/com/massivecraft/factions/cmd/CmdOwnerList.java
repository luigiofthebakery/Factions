package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.struct.Permission;

public class CmdOwnerList extends FCommand {
   public CmdOwnerList() {
      this.aliases.add("ownerlist");
      this.permission = Permission.OWNERLIST.node;
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
            this.fme.msg("<b>Owned areas are disabled on this server.");
         } else {
            FLocation flocation = new FLocation(this.fme);
            if (Board.getFactionAt(flocation) != this.myFaction) {
               if (!hasBypass) {
                  this.fme.msg("<b>This land is not claimed by your faction.");
                  return;
               }

               this.myFaction = Board.getFactionAt(flocation);
               if (!this.myFaction.isNormal()) {
                  this.fme.msg("<i>This land is not claimed by any faction, thus no owners.");
                  return;
               }
            }

            String owners = this.myFaction.getOwnerListString(flocation);
            if (owners != null && !owners.isEmpty()) {
               this.fme.msg("<i>Current owner(s) of this land: %s", owners);
            } else {
               this.fme.msg("<i>No owners are set here; everyone in the faction has access.");
            }
         }
      }
   }
}
