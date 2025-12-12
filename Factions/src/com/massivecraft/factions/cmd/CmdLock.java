package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.Permission;

public class CmdLock extends FCommand {
   public CmdLock() {
      this.aliases.add("lock");
      this.optionalArgs.put("on/off", "flip");
      this.permission = Permission.LOCK.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      this.p.setLocked(this.argAsBool(0, !this.p.getLocked()));
      if (this.p.getLocked()) {
         this.msg("<i>Factions is now locked", new Object[0]);
      } else {
         this.msg("<i>Factions in now unlocked", new Object[0]);
      }
   }
}
