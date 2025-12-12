package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Permission;

public class CmdOpen extends FCommand {
   public CmdOpen() {
      this.aliases.add("open");
      this.optionalArgs.put("yes/no", "flip");
      this.permission = Permission.OPEN.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (this.payForCommand(Conf.econCostOpen, "to open or close the faction", "for opening or closing the faction")) {
         this.myFaction.setOpen(this.argAsBool(0, !this.myFaction.getOpen()));
         String open = this.myFaction.getOpen() ? "open" : "closed";
         this.myFaction.msg("%s<i> changed the faction to <h>%s<i>.", this.fme.describeTo(this.myFaction, true), open);

         for (Faction faction : Factions.i.get()) {
            if (faction != this.myFaction) {
               faction.msg("<i>The faction %s<i> is now %s", this.myFaction.getTag(faction), open);
            }
         }
      }
   }
}
