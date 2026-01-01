package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.RelationUtil;

public class CmdTrust extends FCommand {
   public CmdTrust() {
      super();
      this.aliases.add("trust");

      this.requiredArgs.add("player name");

      this.permission = Permission.TRUST.node;
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

      if (!hasBypass && !assertMinRole(Conf.trustMinRole)) {
         return;
      }

      FPlayer target = this.argAsFPlayer(0);
      if (target == null) return;

      Faction faction = myFaction;

      if (faction.trustsPlayer(target)) {
         msg(String.format("%s <i>already trusts %s <i>with their land.",
            faction.describeTo(fme, true),
            target.describeTo(fme)));
      } else {
         faction.addTrustedPlayer(target);
         faction.msg(String.format("%s <i>now trusts %s <i>with their land.",
            faction.describeTo(fme, true),
            target.describeTo(fme)));

         if (!target.getFaction().equals(faction)) {
            target.sendMessage(this.p.txt.parse(
               String.format("%s <i>now trusts %s <i>with their land.",
                  faction.describeTo(target, true),
                  target.describeTo(target))
            ));
         }
      }
   }
}
