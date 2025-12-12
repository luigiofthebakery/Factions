package com.massivecraft.factions.cmd;

import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;

public class CmdBypass extends FCommand {
   public CmdBypass() {
      this.aliases.add("bypass");
      this.optionalArgs.put("on/off", "flip");
      this.permission = Permission.BYPASS.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      this.fme.setIsAdminBypassing(this.argAsBool(0, !this.fme.isAdminBypassing()));
      if (this.fme.isAdminBypassing()) {
         this.fme.msg("<i>You have enabled admin bypass mode. You will be able to build or destroy anywhere.");
         P.p.log(this.fme.getName() + " has ENABLED admin bypass mode.");
      } else {
         this.fme.msg("<i>You have disabled admin bypass mode.");
         P.p.log(this.fme.getName() + " DISABLED admin bypass mode.");
      }
   }
}
