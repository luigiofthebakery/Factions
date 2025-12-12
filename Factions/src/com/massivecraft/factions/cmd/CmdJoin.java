package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.struct.Permission;
import org.bukkit.Bukkit;

public class CmdJoin extends FCommand {
   public CmdJoin() {
      this.aliases.add("join");
      this.requiredArgs.add("faction name");
      this.optionalArgs.put("player", "you");
      this.permission = Permission.JOIN.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      Faction faction = this.argAsFaction(0);
      if (faction != null) {
         FPlayer fplayer = this.argAsBestFPlayerMatch(1, this.fme, false);
         boolean samePlayer = fplayer == this.fme;
         if (!samePlayer && !Permission.JOIN_OTHERS.has(this.sender, false)) {
            this.msg("<b>You do not have permission to move other players into a faction.", new Object[0]);
         } else if (!faction.isNormal()) {
            this.msg("<b>Players may only join normal factions. This is a system faction.", new Object[0]);
         } else if (faction == fplayer.getFaction()) {
            this.msg("<b>%s %s already a member of %s", new Object[]{fplayer.describeTo(this.fme, true), samePlayer ? "are" : "is", faction.getTag(this.fme)});
         } else if (Conf.factionMemberLimit > 0 && faction.getFPlayers().size() >= Conf.factionMemberLimit) {
            this.msg(
               " <b>!<white> The faction %s is at the limit of %d members, so %s cannot currently join.",
               new Object[]{faction.getTag(this.fme), Conf.factionMemberLimit, fplayer.describeTo(this.fme, false)}
            );
         } else if (fplayer.hasFaction()) {
            this.msg("<b>%s must leave %s current faction first.", new Object[]{fplayer.describeTo(this.fme, true), samePlayer ? "your" : "their"});
         } else if (!Conf.canLeaveWithNegativePower && fplayer.getPower() < 0.0) {
            this.msg("<b>%s cannot join a faction with a negative power level.", new Object[]{fplayer.describeTo(this.fme, true)});
         } else if (!faction.getOpen() && !faction.isInvited(fplayer) && !this.fme.isAdminBypassing() && !Permission.JOIN_ANY.has(this.sender, false)) {
            this.msg("<i>This faction requires invitation.", new Object[0]);
            if (samePlayer) {
               faction.msg("%s<i> tried to join your faction.", fplayer.describeTo(faction, true));
            }
         } else if (!samePlayer || this.canAffordCommand(Conf.econCostJoin, "to join a faction")) {
            FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.i.get(this.me), faction, FPlayerJoinEvent.PlayerJoinReason.COMMAND);
            Bukkit.getServer().getPluginManager().callEvent(joinEvent);
            if (!joinEvent.isCancelled()) {
               if (!samePlayer || this.payForCommand(Conf.econCostJoin, "to join a faction", "for joining a faction")) {
                  this.fme.msg("<i>%s successfully joined %s.", fplayer.describeTo(this.fme, true), faction.getTag(this.fme));
                  if (!samePlayer) {
                     fplayer.msg("<i>%s moved you into the faction %s.", this.fme.describeTo(fplayer, true), faction.getTag(fplayer));
                  }

                  faction.msg("<i>%s joined your faction.", fplayer.describeTo(faction, true));
                  fplayer.resetFactionData();
                  fplayer.setFaction(faction);
                  faction.deinvite(fplayer);
                  if (Conf.logFactionJoin) {
                     if (samePlayer) {
                        P.p.log("%s joined the faction %s (%s).", new Object[]{fplayer.getName(), faction.getTag(), faction.getId()});
                     } else {
                        P.p
                           .log(
                              "%s moved the player %s into the faction %s (%s).",
                              new Object[]{this.fme.getName(), fplayer.getName(), faction.getTag(), faction.getId()}
                           );
                     }
                  }
               }
            }
         }
      }
   }
}
