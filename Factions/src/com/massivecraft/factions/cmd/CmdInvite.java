package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.struct.Permission;

public class CmdInvite extends FCommand {
   public CmdInvite() {
      this.aliases.add("invite");
      this.aliases.add("inv");
      this.requiredArgs.add("player name");
      this.permission = Permission.INVITE.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer you = this.argAsBestFPlayerMatch(0);
      if (you != null) {
         if (you.getFaction() == this.myFaction) {
            this.msg("%s<i> is already a member of %s", new Object[]{you.getName(), this.myFaction.getTag()});
            this.msg("<i>You might want to: " + this.p.cmdBase.cmdKick.getUseageTemplate(false), new Object[0]);
         } else if (this.payForCommand(Conf.econCostInvite, "to invite someone", "for inviting someone")) {
            this.myFaction.invite(you);
            you.msg("%s<i> invited you to %s", this.fme.describeTo(you, true), this.myFaction.describeTo(you));
            this.myFaction.msg("%s<i> invited %s<i> to your faction.", this.fme.describeTo(this.myFaction, true), you.describeTo(this.myFaction));
         }
      }
   }
}
