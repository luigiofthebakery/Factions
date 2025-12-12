package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Bukkit;

public class CmdKick extends FCommand {
   public CmdKick() {
      this.aliases.add("kick");
      this.requiredArgs.add("player name");
      this.permission = Permission.KICK.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = true;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer you = this.argAsBestFPlayerMatch(0);
      if (you != null) {
         if (this.fme == you) {
            this.msg("<b>You cannot kick yourself.", new Object[0]);
            this.msg("<i>You might want to: %s", new Object[]{this.p.cmdBase.cmdLeave.getUseageTemplate(false)});
         } else {
            Faction yourFaction = you.getFaction();
            if (!Permission.KICK_ANY.has(this.sender)) {
               if (yourFaction != this.myFaction) {
                  this.msg("%s<b> is not a member of %s", new Object[]{you.describeTo(this.fme, true), this.myFaction.describeTo(this.fme)});
                  return;
               }

               if (you.getRole().value >= this.fme.getRole().value) {
                  this.msg("<b>Your rank is too low to kick this player.", new Object[0]);
                  return;
               }

               if (!Conf.canLeaveWithNegativePower && you.getPower() < 0.0) {
                  this.msg("<b>You cannot kick that member until their power is positive.", new Object[0]);
                  return;
               }
            }

            if (this.canAffordCommand(Conf.econCostKick, "to kick someone from the faction")) {
               FPlayerLeaveEvent event = new FPlayerLeaveEvent(you, you.getFaction(), FPlayerLeaveEvent.PlayerLeaveReason.KICKED);
               Bukkit.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  if (this.payForCommand(Conf.econCostKick, "to kick someone from the faction", "for kicking someone from the faction")) {
                     yourFaction.msg("%s<i> kicked %s<i> from the faction! :O", this.fme.describeTo(yourFaction, true), you.describeTo(yourFaction, true));
                     you.msg("%s<i> kicked you from %s<i>! :O", this.fme.describeTo(you, true), yourFaction.describeTo(you));
                     if (yourFaction != this.myFaction) {
                        this.fme.msg("<i>You kicked %s<i> from the faction %s<i>!", you.describeTo(this.fme), yourFaction.describeTo(this.fme));
                     }

                     if (Conf.logFactionKick) {
                        P.p
                           .log(
                              (
                                    this.senderIsConsole
                                       ? "CONSOLE kicked " + you.getName() + " from the faction: " + yourFaction.getTag() + " (" + yourFaction.getId() + ")"
                                       : this.fme.getName()
                                 )
                                 + " kicked "
                                 + you.getName()
                                 + " from the faction: "
                                 + yourFaction.getTag()
                                 + " ("
                                 + yourFaction.getId()
                                 + ")"
                           );
                     }

                     if (you.getRole() == Role.ADMIN) {
                        yourFaction.promoteNewLeader();
                     }

                     yourFaction.deinvite(you);
                     you.resetFactionData();
                  }
               }
            }
         }
      }
   }
}
