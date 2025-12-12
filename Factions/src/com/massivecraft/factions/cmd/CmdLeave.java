package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.Permission;

public class CmdLeave extends FCommand {
   public CmdLeave() {
      this.aliases.add("leave");
      this.permission = Permission.LEAVE.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = true;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      this.fme.leave(true);
   }
}
