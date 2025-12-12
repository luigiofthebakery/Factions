package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.struct.Permission;

public class CmdBoom extends FCommand {
   public CmdBoom() {
      this.aliases.add("noboom");
      this.optionalArgs.put("on/off", "flip");
      this.permission = Permission.NO_BOOM.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (!this.myFaction.isPeaceful()) {
         this.fme.msg("<b>This command is only usable by factions which are specially designated as peaceful.");
      } else if (this.payForCommand(Conf.econCostNoBoom, "to toggle explosions", "for toggling explosions")) {
         this.myFaction.setPeacefulExplosionsEnabled(this.argAsBool(0, !this.myFaction.getPeacefulExplosionsEnabled()));
         String enabled = this.myFaction.noExplosionsInTerritory() ? "disabled" : "enabled";
         this.myFaction.msg("%s<i> has " + enabled + " explosions in your faction's territory.", this.fme.describeTo(this.myFaction));
      }
   }
}
