package com.massivecraft.factions.cmd;

import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;

public class CmdChatSpy extends FCommand {
   public CmdChatSpy() {
      this.aliases.add("chatspy");
      this.optionalArgs.put("on/off", "flip");
      this.permission = Permission.CHATSPY.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      this.fme.setSpyingChat(this.argAsBool(0, !this.fme.isSpyingChat()));
      if (this.fme.isSpyingChat()) {
         this.fme.msg("<i>You have enabled chat spying mode.");
         P.p.log(this.fme.getName() + " has ENABLED chat spying mode.");
      } else {
         this.fme.msg("<i>You have disabled chat spying mode.");
         P.p.log(this.fme.getName() + " DISABLED chat spying mode.");
      }
   }
}
