package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TextUtil;

public class CmdDescription extends FCommand {
   public CmdDescription() {
      this.aliases.add("desc");
      this.requiredArgs.add("desc");
      this.errorOnToManyArgs = false;
      this.permission = Permission.DESCRIPTION.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (this.payForCommand(Conf.econCostDesc, "to change faction description", "for changing faction description")) {
         this.myFaction.setDescription(TextUtil.implode(this.args, " ").replaceAll("(&([a-f0-9]))", "& $2"));
         if (!Conf.broadcastDescriptionChanges) {
            this.fme.msg("You have changed the description for <h>%s<i> to:", this.myFaction.describeTo(this.fme));
            this.fme.sendMessage(this.myFaction.getDescription());
         } else {
            for (FPlayer fplayer : FPlayers.i.getOnline()) {
               fplayer.msg("<i>The faction %s<i> changed their description to:", this.myFaction.describeTo(fplayer));
               fplayer.sendMessage(this.myFaction.getDescription());
            }
         }
      }
   }
}
