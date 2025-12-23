package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.AutoTriggerType;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.SpiralTask;
import com.massivecraft.factions.zcore.util.PermUtil;

import java.util.Collections;
import java.util.List;

public class CmdClaim extends AutomatableCommand {
   public CmdClaim() {
      this.aliases.add("claim");
      this.optionalArgs.put("faction", "your");
      this.optionalArgs.put("radius", "1");
      this.permission = Permission.CLAIM.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;

      this.priority = 100;
      this.autoPermission = Permission.CLAIM_AUTO.node;
      this.autoTriggerType = AutoTriggerType.CHUNK_BOUNDARY;
      this.incompatibleWith = Collections.singletonList(
         CmdUnclaim.class
      );
   }

   @Override
   public void perform() {
      final Faction forFaction = this.argAsFaction(0, this.myFaction);
      int radius = this.argAsInt(1, 1);
      if (radius < 1) {
         this.msg("<b>If you specify a radius, it must be at least 1.", new Object[0]);
      } else if (radius < 2) {
         this.fme.attemptClaim(forFaction, this.fme.getLastStoodAt(), true);
      } else if (this.fme.isAdminBypassing() || assertMinRole(Conf.claimRadiusMinRole)) {
         if (radius < 8) {
            new SpiralTask(this.fme.getLastStoodAt(), radius) {
               private int failCount = 0;
               private final int limit = Conf.radiusClaimFailureLimit - 1;

               @Override
               public boolean work() {
                  boolean success = CmdClaim.this.fme.attemptClaim(forFaction, this.currentLocation(), true);
                  if (success) {
                     this.failCount = 0;
                  } else if (!success && this.failCount++ >= this.limit) {
                     this.stop();
                     return false;
                  }

                  return true;
               }
            };
         } else {
            this.msg("<b>You cannot claim more than 150 chunks in one instance.", new Object[0]);
         }
      }
   }

   @Override
   public boolean onAutoEnable(FPlayer player) {
      boolean preCheck = super.onAutoEnable(player);

      if (!preCheck) {
         return false;
      }

      if (!fme.isAdminBypassing() && (!this.assertHasFaction() || !this.assertMinRole(Conf.autoClaimMinRole))) {
         return false;
      }

      final Faction forFaction = this.argAsFaction(0, this.myFaction);

      if (!player.canClaimForFaction(forFaction)) {
         String factionName = forFaction.describeTo(player);
         player.msg("<b>You can't claim land for %s<b>.", factionName);
         return false;
      }

      player.msg("<i>Auto-claim for %s<i> enabled.", forFaction.describeTo(fme));
      return true;
   }

   @Override
   public boolean onAutoDisable(FPlayer player) {
      final Faction forFaction = this.argAsFaction(0, this.myFaction);
      player.msg("<i>Auto-claim for %s<i> disabled.", forFaction.describeTo(fme));
      return true;
   }

   public boolean doArgsMatch(List<String> args1, List<String> args2) {
      final Faction args1faction = !args1.isEmpty() ? this.strAsFaction(args1.get(0), this.myFaction, false) : this.myFaction;
      final Faction args2faction = !args2.isEmpty() ? this.strAsFaction(args2.get(0), this.myFaction, false) : this.myFaction;

      final int args1radius = args1.size() > 1 ? this.strAsInt(args1.get(1), 1) : 1;
      final int args2radius = args2.size() > 1 ? this.strAsInt(args2.get(1), 1) : 1;

      return args1faction.equals(args2faction) && (args1radius == args2radius);
   }
}
