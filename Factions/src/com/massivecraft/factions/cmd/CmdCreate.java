package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class CmdCreate extends FCommand {
   public CmdCreate() {
      this.aliases.add("create");
      this.requiredArgs.add("faction tag");
      this.permission = Permission.CREATE.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      String tag = this.argAsString(0);
      if (this.fme.hasFaction()) {
         this.msg("<b>You must leave your current faction first.", new Object[0]);
      } else if (Factions.i.isTagTaken(tag)) {
         this.msg("<b>That tag is already in use.", new Object[0]);
      } else {
         ArrayList<String> tagValidationErrors = Factions.validateTag(tag);
         if (tagValidationErrors.size() > 0) {
            this.sendMessage(tagValidationErrors);
         } else if (this.canAffordCommand(Conf.econCostCreate, "to create a new faction")) {
            FactionCreateEvent createEvent = new FactionCreateEvent(this.me, tag);
            Bukkit.getServer().getPluginManager().callEvent(createEvent);
            if (!createEvent.isCancelled()) {
               if (this.payForCommand(Conf.econCostCreate, "to create a new faction", "for creating a new faction")) {
                  Faction faction = Factions.i.create();
                  if (faction == null) {
                     this.msg("<b>There was an internal error while trying to create your faction. Please try again.", new Object[0]);
                  } else {
                     faction.setTag(tag);
                     FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.i.get(this.me), faction, FPlayerJoinEvent.PlayerJoinReason.CREATE);
                     Bukkit.getServer().getPluginManager().callEvent(joinEvent);
                     this.fme.setRole(Role.ADMIN);
                     this.fme.setFaction(faction);

                     for (FPlayer follower : FPlayers.i.getOnline()) {
                        follower.msg("%s<i> created a new faction %s", this.fme.describeTo(follower, true), faction.getTag(follower));
                     }

                     this.msg("<i>You should now: %s", new Object[]{this.p.cmdBase.cmdDescription.getUseageTemplate()});
                     if (Conf.logFactionCreate) {
                        P.p.log(this.fme.getName() + " created a new faction: " + tag + " (" + faction.getId() + ")");
                     }
                  }
               }
            }
         }
      }
   }
}
