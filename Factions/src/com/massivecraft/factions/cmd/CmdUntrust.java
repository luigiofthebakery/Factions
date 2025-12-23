package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.RelationUtil;

public class CmdUntrust extends FCommand {
   public CmdUntrust() {
      super();
      this.aliases.add("untrust");

      this.requiredArgs.add("player name");

      this.permission = Permission.UNTRUST.node;
      this.disableOnLock = true;

      senderMustBePlayer = true;
      senderMustBeMember = false;
      senderMustBeModerator = false;
      senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (!Conf.trustEnabled) {
         msg("<b>Trust is currently disabled.");
         return;
      }

      boolean hasBypass = fme.isAdminBypassing();

      if (!assertHasFaction()) {
         return;
      }

      if (!hasBypass && !assertMinRole(Conf.untrustMinRole)) {
         return;
      }

      FPlayer target = this.argAsFPlayer(0);
      if (target == null) return;

      Faction faction = myFaction;

      if (!faction.trustsPlayer(target)) {
         msg(String.format("%s <i>already doesn't trust %s <i>with their land.",
            faction.describeTo(fme, true),
            target.describeTo(fme)));
      } else {
         faction.removeTrustedPlayer(target);
         msg(String.format("%s <i>no longer trusts %s <i>with their land.",
            faction.describeTo(fme, true),
            target.describeTo(fme)));
         target.sendMessage(this.p.txt.parse(
            String.format("%s <i>no longer trusts %s <i>with their land.",
               faction.describeTo(target, true),
               target.describeTo(target))
         ));
      }
   }
}
