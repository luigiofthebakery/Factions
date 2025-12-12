package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;

public class CmdAutoClaim extends FCommand {
   public CmdAutoClaim() {
      this.aliases.add("autoclaim");
      this.optionalArgs.put("faction", "your");
      this.permission = Permission.AUTOCLAIM.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      Faction forFaction = this.argAsFaction(0, this.myFaction);
      if (forFaction == null || forFaction == this.fme.getAutoClaimFor()) {
         this.fme.setAutoClaimFor(null);
         this.msg("<i>Auto-claiming of land disabled.", new Object[0]);
      } else if (!this.fme.canClaimForFaction(forFaction)) {
         if (this.myFaction == forFaction) {
            this.msg("<b>You must be <h>%s<b> to claim land.", new Object[]{Role.MODERATOR.toString()});
         } else {
            this.msg("<b>You can't claim land for <h>%s<b>.", new Object[]{forFaction.describeTo(this.fme)});
         }
      } else {
         this.fme.setAutoClaimFor(forFaction);
         this.msg("<i>Now auto-claiming land for <h>%s<i>.", new Object[]{forFaction.describeTo(this.fme)});
         this.fme.attemptClaim(forFaction, this.me.getLocation(), true);
      }
   }
}
