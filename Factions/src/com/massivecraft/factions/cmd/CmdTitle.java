package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TextUtil;

public class CmdTitle extends FCommand {
   public CmdTitle() {
      this.aliases.add("title");
      this.requiredArgs.add("player name");
      this.optionalArgs.put("title", "");
      this.permission = Permission.TITLE.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer you = this.argAsBestFPlayerMatch(0);
      if (you != null) {
         this.args.remove(0);
         String title = TextUtil.implode(this.args, " ");
         if (this.canIAdministerYou(this.fme, you)) {
            if (this.payForCommand(Conf.econCostTitle, "to change a players title", "for changing a players title")) {
               you.setTitle(title);
               this.myFaction.msg("%s<i> changed a title: %s", this.fme.describeTo(this.myFaction, true), you.describeTo(this.myFaction, true));
               if (Conf.spoutFactionTitlesOverNames) {
                  SpoutFeatures.updateAppearances(this.me);
               }
            }
         }
      }
   }
}
