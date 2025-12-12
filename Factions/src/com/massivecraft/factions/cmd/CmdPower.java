package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.struct.Permission;

public class CmdPower extends FCommand {
   public CmdPower() {
      this.aliases.add("power");
      this.aliases.add("pow");
      this.optionalArgs.put("player name", "you");
      this.permission = Permission.POWER.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer target = this.argAsBestFPlayerMatch(0, this.fme);
      if (target != null) {
         if (target == this.fme || Permission.POWER_ANY.has(this.sender, true)) {
            if (this.payForCommand(Conf.econCostPower, "to show player power info", "for showing player power info")) {
               double powerBoost = target.getPowerBoost();
               String boost = powerBoost == 0.0 ? "" : (powerBoost > 0.0 ? " (bonus: " : " (penalty: ") + powerBoost + ")";
               this.msg(
                  "%s<a> - Power / Maxpower: <i>%d / %d %s",
                  new Object[]{target.describeTo(this.fme, true), target.getPowerRounded(), target.getPowerMaxRounded(), boost}
               );
            }
         }
      }
   }
}
