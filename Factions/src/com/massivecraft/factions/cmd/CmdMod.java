package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdMod extends FCommand {
   public CmdMod() {
      this.aliases.add("mod");
      this.requiredArgs.add("player name");
      this.permission = Permission.MOD.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer you = this.argAsBestFPlayerMatch(0);
      if (you != null) {
         boolean permAny = Permission.MOD_ANY.has(this.sender, false);
         Faction targetFaction = you.getFaction();
         if (targetFaction != this.myFaction && !permAny) {
            this.msg("%s<b> is not a member in your faction.", new Object[]{you.describeTo(this.fme, true)});
         } else if (this.fme != null && this.fme.getRole() != Role.ADMIN && !permAny) {
            this.msg("<b>You are not the faction admin.", new Object[0]);
         } else if (you == this.fme && !permAny) {
            this.msg("<b>The target player musn't be yourself.", new Object[0]);
         } else if (you.getRole() == Role.ADMIN) {
            this.msg("<b>The target player is a faction admin. Demote them first.", new Object[0]);
         } else {
            if (you.getRole() == Role.MODERATOR) {
               you.setRole(Role.NORMAL);
               targetFaction.msg("%s<i> is no longer moderator in your faction.", you.describeTo(targetFaction, true));
               this.msg("<i>You have removed moderator status from %s<i>.", new Object[]{you.describeTo(this.fme, true)});
            } else {
               you.setRole(Role.MODERATOR);
               targetFaction.msg("%s<i> was promoted to moderator in your faction.", you.describeTo(targetFaction, true));
               this.msg("<i>You have promoted %s<i> to moderator.", new Object[]{you.describeTo(this.fme, true)});
            }
         }
      }
   }
}
