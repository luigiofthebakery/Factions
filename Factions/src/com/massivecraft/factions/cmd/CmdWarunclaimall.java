package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;

public class CmdWarunclaimall extends FCommand {
   public CmdWarunclaimall() {
      this.aliases.add("warunclaimall");
      this.aliases.add("wardeclaimall");
      this.permission = Permission.WARUNCLAIMALL.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
      this.setHelpShort("unclaim all warzone land");
   }

   @Override
   public void perform() {
      Board.unclaimAll(Factions.i.getWarZone().getId());
      this.msg("<i>You unclaimed ALL war zone land.", new Object[0]);
      if (Conf.logLandUnclaims) {
         P.p.log(this.fme.getName() + " unclaimed all war zones.");
      }
   }
}
