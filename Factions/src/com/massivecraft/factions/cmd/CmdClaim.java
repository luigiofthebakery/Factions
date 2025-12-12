package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.SpiralTask;

public class CmdClaim extends FCommand {
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
   }

   @Override
   public void perform() {
      final Faction forFaction = this.argAsFaction(0, this.myFaction);
      int radius = this.argAsInt(1, 1);
      if (radius < 1) {
         this.msg("<b>If you specify a radius, it must be at least 1.", new Object[0]);
      } else {
         if (radius < 2) {
            this.fme.attemptClaim(forFaction, this.me.getLocation(), true);
         }

         if (radius < 8) {
            new SpiralTask(new FLocation(this.me), radius) {
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
}
