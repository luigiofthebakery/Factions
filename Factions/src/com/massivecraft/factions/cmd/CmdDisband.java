package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Bukkit;

public class CmdDisband extends FCommand {
   public CmdDisband() {
      this.aliases.add("disband");
      this.optionalArgs.put("faction tag", "yours");
      this.permission = Permission.DISBAND.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      Faction faction = this.argAsFaction(0, this.fme == null ? null : this.myFaction);
      if (faction != null) {
         boolean isMyFaction = this.fme == null ? false : faction == this.myFaction;
         if (isMyFaction) {
            if (!this.assertMinRole(Role.ADMIN)) {
               return;
            }
         } else if (!Permission.DISBAND_ANY.has(this.sender, true)) {
            return;
         }

         if (!faction.isNormal()) {
            this.msg("<i>You cannot disband the Wilderness, SafeZone, or WarZone.", new Object[0]);
         } else if (faction.isPermanent()) {
            this.msg("<i>This faction is designated as permanent, so you cannot disband it.", new Object[0]);
         } else {
            FactionDisbandEvent disbandEvent = new FactionDisbandEvent(this.me, faction.getId());
            Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
            if (!disbandEvent.isCancelled()) {
               for (FPlayer fplayer : faction.getFPlayers()) {
                  Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, faction, FPlayerLeaveEvent.PlayerLeaveReason.DISBAND));
               }

               for (FPlayer fplayer : FPlayers.i.getOnline()) {
                  String who = this.senderIsConsole ? "A server admin" : this.fme.describeTo(fplayer);
                  if (fplayer.getFaction() == faction) {
                     fplayer.msg("<h>%s<i> disbanded your faction.", who);
                  } else {
                     fplayer.msg("<h>%s<i> disbanded the faction %s.", who, faction.getTag(fplayer));
                  }
               }

               if (Conf.logFactionDisband) {
                  P.p
                     .log(
                        "The faction "
                           + faction.getTag()
                           + " ("
                           + faction.getId()
                           + ") was disbanded by "
                           + (this.senderIsConsole ? "console command" : this.fme.getName())
                           + "."
                     );
               }

               if (Econ.shouldBeUsed() && !this.senderIsConsole) {
                  double amount = Econ.getBalance(faction.getAccountId());
                  Econ.transferMoney(this.fme, faction, this.fme, amount, false);
                  if (amount > 0.0) {
                     String amountString = Econ.moneyString(amount);
                     this.msg("<i>You have been given the disbanded faction's bank, totaling %s.", new Object[]{amountString});
                     P.p.log(this.fme.getName() + " has been given bank holdings of " + amountString + " from disbanding " + faction.getTag() + ".");
                  }
               }

               faction.detach();
               SpoutFeatures.updateAppearances();
            }
         }
      }
   }
}
