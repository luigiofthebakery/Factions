package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;

import java.util.ArrayList;
import java.util.List;

public class CmdOwnerList extends FCommand {
   public CmdOwnerList() {
      this.aliases.add("list");
      this.permission = Permission.OWNER_LIST.node;
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
         } else {
            FLocation flocation = new FLocation(this.fme);
            Faction factionHere = Board.getFactionAt(flocation);
            if (factionHere != this.myFaction) {
               if (!hasBypass) {
                  this.fme.msg("<b>This land is not claimed by your faction.");
                  return;
               }

               if (!factionHere.isNormal()) {
                  this.fme.msg("<i>This land is not claimed by any faction, thus no owners.");
                  return;
               }
            }

            String owners = factionHere.getOwnerListString(flocation);
            if (owners != null && !owners.isEmpty()) {
               this.fme.msg("<i>Current owner(s) of this land: %s", owners);
            } else {
               this.fme.msg("<i>No owners are set here; everyone in the faction has access.");
            }
         }
      }
   }
}
