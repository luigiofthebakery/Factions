package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.AutoTriggerType;
import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

import java.util.Arrays;
import java.util.List;

public class CmdOwnerClear extends AutomatableCommand {
   public CmdOwnerClear() {
      this.aliases.add("clear");
      this.aliases.add("clearlist");
      this.permission = Permission.OWNER_CLEAR.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;

      this.priority = 90;
      this.autoPermission = Permission.OWNER_CLEAR_AUTO.node;
      this.autoMinRoleRequired = Role.ADMIN; // TODO: move to config
      this.autoTriggerType = AutoTriggerType.CHUNK_BOUNDARY;
      this.incompatibleWith = Arrays.asList(
         CmdUnclaim.class,
         CmdOwnerAdd.class,
         CmdOwnerRemove.class
      );
   }

   @Override
   public void perform() {
      boolean hasBypass = this.fme.isAdminBypassing();
      if (hasBypass || this.assertHasFaction()) {
         if (!Conf.ownedAreasEnabled) {
            this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         } else if (hasBypass || this.assertMinRole(Conf.ownedAreasModeratorsCanSet ? Role.MODERATOR : Role.ADMIN)) {
            FLocation flocation = this.fme.getLastStoodAt();
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

      player.msg("<i>Automatically clearing owners.");
      return true;
   }

   @Override
   public boolean onAutoDisable(FPlayer player) {
      player.msg("<i>No longer automatically clearing owners.");
      return true;
   }

   @Override
   public boolean doArgsMatch(List<String> args1, List<String> args2) {
      return true;
   }
}
