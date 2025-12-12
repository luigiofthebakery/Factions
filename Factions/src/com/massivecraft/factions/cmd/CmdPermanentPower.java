package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;

public class CmdPermanentPower extends FCommand {
   public CmdPermanentPower() {
      this.aliases.add("permanentpower");
      this.requiredArgs.add("faction");
      this.optionalArgs.put("power", "reset");
      this.permission = Permission.SET_PERMANENTPOWER.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      Faction targetFaction = this.argAsFaction(0);
      if (targetFaction != null) {
         Integer targetPower = this.argAsInt(1);
         targetFaction.setPermanentPower(targetPower);
         String change = "removed permanentpower status from";
         if (targetFaction.hasPermanentPower()) {
            change = "added permanentpower status to";
         }

         this.msg("<i>You %s <h>%s<i>.", new Object[]{change, targetFaction.describeTo(this.fme)});

         for (FPlayer fplayer : targetFaction.getFPlayersWhereOnline(true)) {
            fplayer.msg((this.fme == null ? "A server admin" : this.fme.describeTo(fplayer, true)) + "<i> " + change + " your faction.");
         }
      }
   }
}
