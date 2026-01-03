package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.AutoTriggerType;
import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

import java.util.Arrays;
import java.util.List;

public class CmdOwnerAdd extends AutomatableCommand {
   public CmdOwnerAdd() {
      this.aliases.add("add");
      this.optionalArgs.put("player name", "you");
      this.permission = Permission.OWNER_ADD.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;

      this.priority = 90;
      this.autoPermission = Permission.OWNER_ADD_AUTO.node;
      this.autoTriggerType = AutoTriggerType.CHUNK_BOUNDARY;
      this.incompatibleWith = Arrays.asList(
         CmdUnclaim.class,
         CmdOwnerRemove.class,
         CmdOwnerClear.class
      );
   }

   @Override
   public void perform() {
      boolean hasBypass = this.fme.isAdminBypassing();
      if (hasBypass || this.assertHasFaction()) {
         if (!Conf.ownedAreasEnabled) {
            this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         } else if (!hasBypass && Conf.ownedAreasLimitPerFaction > 0 && this.myFaction.getCountOfClaimsWithOwners() >= Conf.ownedAreasLimitPerFaction) {
            this.fme.msg("<b>Sorry, but you have reached the server's <h>limit of %d <b>owned areas per faction.", Conf.ownedAreasLimitPerFaction);
         } else if (hasBypass || this.assertMinRole(Conf.ownerAddMinRole)) {
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

            FPlayer target = this.argAsBestFPlayerMatch(0, fme);
            if (target != null) {
               String playerName = target.getName();
               if (!target.canHaveOwnershipInFaction(factionHere)) {
                  this.fme.msg("%s<i> can't own land in %s<i>.", target.describeTo(fme), factionHere.describeTo(fme));
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

   @Override
   public boolean onAutoEnable(FPlayer player) {
      boolean preCheck = super.onAutoEnable(player);

      if (!preCheck) {
         return false;
      }

      boolean hasBypass = player.isAdminBypassing();

      if (!Conf.ownedAreasEnabled) {
         player.msg("<b>Sorry, but owned areas are disabled on this server.");
         return false;
      } else if (!hasBypass && (!this.assertHasFaction() || !this.assertMinRole(Conf.autoOwnerAddMinRole))) {
         return false;
      }  else if (!hasBypass && Conf.ownedAreasLimitPerFaction > 0 && this.myFaction.getCountOfClaimsWithOwners() >= Conf.ownedAreasLimitPerFaction) {
         player.msg("<b>Sorry, but you have reached the server's <h>limit of %d <b>owned areas per faction.", Conf.ownedAreasLimitPerFaction);
         return false;
      }

      FPlayer target = this.argAsBestFPlayerMatch(0, player);
      player.msg("<i>Automatically trying to add %s<i> to ownerlist.", target.describeTo(player));
      return true;
   }

   @Override
   public boolean onAutoDisable(FPlayer player) {
      FPlayer target = this.argAsBestFPlayerMatch(0, player);
      player.msg("<i>No longer automatically trying to add %s<i> to ownerlist.", target.describeTo(player));
      return true;
   }

   @Override
   public boolean doArgsMatch(List<String> args1, List<String> args2) {
      if (args1.size() != args2.size()) return false;

      FPlayer target1 = !args1.isEmpty() ? this.strAsBestFPlayerMatch(args1.get(0), fme, false) : fme;
      FPlayer target2 = !args1.isEmpty() ? this.strAsBestFPlayerMatch(args2.get(0), fme, false) : fme;

      return target1.equals(target2);
   }
}
