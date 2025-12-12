package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.MiscUtil;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class CmdTag extends FCommand {
   public CmdTag() {
      this.aliases.add("tag");
      this.requiredArgs.add("faction tag");
      this.permission = Permission.TAG.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      String tag = this.argAsString(0);
      if (Factions.i.isTagTaken(tag) && !MiscUtil.getComparisonString(tag).equals(this.myFaction.getComparisonTag())) {
         this.msg("<b>That tag is already taken", new Object[0]);
      } else {
         ArrayList<String> errors = new ArrayList<>();
         errors.addAll(Factions.validateTag(tag));
         if (errors.size() > 0) {
            this.sendMessage(errors);
         } else if (this.canAffordCommand(Conf.econCostTag, "to change the faction tag")) {
            FactionRenameEvent renameEvent = new FactionRenameEvent(this.fme, tag);
            Bukkit.getServer().getPluginManager().callEvent(renameEvent);
            if (!renameEvent.isCancelled()) {
               if (this.payForCommand(Conf.econCostTag, "to change the faction tag", "for changing the faction tag")) {
                  String oldtag = this.myFaction.getTag();
                  this.myFaction.setTag(tag);
                  P.p.log(this.fme.getName() + " renamed the faction " + this.myFaction.getId() + " from " + oldtag + " to " + tag);
                  this.myFaction.msg("%s<i> changed your faction tag to %s", this.fme.describeTo(this.myFaction, true), this.myFaction.getTag(this.myFaction));

                  for (Faction faction : Factions.i.get()) {
                     if (faction != this.myFaction) {
                        faction.msg("<i>The faction %s<i> changed their name to %s.", this.fme.getColorTo(faction) + oldtag, this.myFaction.getTag(faction));
                     }
                  }

                  if (Conf.spoutFactionTagsOverNames) {
                     SpoutFeatures.updateAppearances(this.myFaction);
                  }
               }
            }
         }
      }
   }
}
