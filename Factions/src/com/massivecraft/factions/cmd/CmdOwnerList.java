package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.AutoTriggerType;
import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

import java.util.Arrays;
import java.util.List;

public class CmdOwnerList extends AutomatableCommand {
   public CmdOwnerList() {
      this.aliases.add("list");
      this.permission = Permission.OWNER_LIST.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;

      this.priority = 80;
      this.autoPermission = Permission.OWNER_LIST_AUTO.node;
      this.autoMinRoleRequired = Role.NORMAL;
      this.autoTriggerType = AutoTriggerType.CHUNK_BOUNDARY;
      this.incompatibleWith = Arrays.asList(
         CmdUnclaim.class,
         CmdOwnerClear.class
      );
   }

   @Override
   public void perform() {
      boolean hasBypass = this.fme.isAdminBypassing();
      if (hasBypass || this.assertHasFaction()) {
         if (!Conf.ownedAreasEnabled) {
            this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         } else {
            FLocation flocation = this.fme.getLastStoodAt();
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

   @Override
   public boolean onAutoEnable(FPlayer player) {
      boolean preCheck = super.onAutoEnable(player);

      if (!preCheck) {
         return false;
      }

      if (!Conf.ownedAreasEnabled) {
         this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         return false;
      }

      player.msg("<i>Automatically listing owners.");
      return true;
   }

   @Override
   public boolean onAutoDisable(FPlayer player) {
      player.msg("<i>No longer automatically listing owners.");
      return true;
   }

   @Override
   public boolean doArgsMatch(List<String> args1, List<String> args2) {
      return true;
   }
}
