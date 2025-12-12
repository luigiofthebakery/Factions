package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Location;
import org.bukkit.World;

public class CmdSethome extends FCommand {
   public CmdSethome() {
      this.aliases.add("sethome");
      this.optionalArgs.put("faction tag", "mine");
      this.permission = Permission.SETHOME.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (!Conf.homesEnabled) {
         this.fme.msg("<b>Sorry, Faction homes are disabled on this server.");
      } else {
         Faction faction = this.argAsFaction(0, this.myFaction);
         if (faction != null) {
            if (faction == this.myFaction) {
               if (!Permission.SETHOME_ANY.has(this.sender) && !this.assertMinRole(Role.MODERATOR)) {
                  return;
               }
            } else if (!Permission.SETHOME_ANY.has(this.sender, true)) {
               return;
            }

            if (!Permission.BYPASS.has(this.me) && Conf.homesMustBeInClaimedTerritory && Board.getFactionAt(new FLocation(this.me)) != faction) {
               this.fme.msg("<b>Sorry, your faction home can only be set inside your own claimed territory.");
            } else if (this.payForCommand(Conf.econCostSethome, "to set the faction home", "for setting the faction home")) {
               Location loc = this.me.getLocation();
               faction.setHome(loc);
               int x = loc.getBlockX();
               int y = loc.getBlockY();
               int z = loc.getBlockZ();
               World w = loc.getWorld();
               P.p.log("FACTIONSETHOME issued for " + faction.getTag() + " (" + faction.getId() + ") at (" + x + "," + y + "," + z + ")@" + w.getName());
               faction.msg("%s<i> set the home for your faction. You can now use:", this.fme.describeTo(this.myFaction, true));
               faction.sendMessage(this.p.cmdBase.cmdHome.getUseageTemplate());
               if (faction != this.myFaction) {
                  this.fme.msg("<b>You have set the home for the " + faction.getTag(this.fme) + "<i> faction.");
               }
            }
         }
      }
   }
}
