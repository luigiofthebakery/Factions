package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;

public class CmdSafeunclaimall extends FCommand {
   public CmdSafeunclaimall() {
      this.aliases.add("safeunclaimall");
      this.aliases.add("safedeclaimall");
      this.permission = Permission.SAFEUNCLAIMALL.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
      this.setHelpShort("Unclaim all safezone land");
   }

   @Override
   public void perform() {
      Board.unclaimAll(Factions.i.getSafeZone().getId());
      this.msg("<i>You unclaimed ALL safe zone land.", new Object[0]);
      if (Conf.logLandUnclaims) {
         P.p.log(this.fme.getName() + " unclaimed all safe zones.");
      }
   }
}
