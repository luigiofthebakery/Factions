package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Permission;

public class CmdChat extends FCommand {
   public CmdChat() {
      this.aliases.add("c");
      this.aliases.add("chat");
      this.optionalArgs.put("mode", "next");
      this.permission = Permission.CHAT.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = true;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (!Conf.factionOnlyChat) {
         this.msg("<b>The built in chat chat channels are disabled on this server.", new Object[0]);
      } else {
         String modeString = this.argAsString(0);
         ChatMode modeTarget = this.fme.getChatMode().getNext();
         if (modeString != null) {
            modeString.toLowerCase();
            if (modeString.startsWith("p")) {
               modeTarget = ChatMode.PUBLIC;
            } else if (modeString.startsWith("a")) {
               modeTarget = ChatMode.ALLIANCE;
            } else {
               if (!modeString.startsWith("f")) {
                  this.msg("<b>Unrecognised chat mode. <i>Please enter either 'a','f' or 'p'", new Object[0]);
                  return;
               }

               modeTarget = ChatMode.FACTION;
            }
         }

         this.fme.setChatMode(modeTarget);
         if (this.fme.getChatMode() == ChatMode.PUBLIC) {
            this.msg("<i>Public chat mode.", new Object[0]);
         } else if (this.fme.getChatMode() == ChatMode.ALLIANCE) {
            this.msg("<i>Alliance only chat mode.", new Object[0]);
         } else {
            this.msg("<i>Faction only chat mode.", new Object[0]);
         }
      }
   }
}
