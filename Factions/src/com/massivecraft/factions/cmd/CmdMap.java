package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.struct.Permission;

public class CmdMap extends FCommand {
   public CmdMap() {
      this.aliases.add("map");
      this.optionalArgs.put("on/off", "once");
      this.permission = Permission.MAP.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (this.argIsSet(0)) {
         if (this.argAsBool(0, !this.fme.isMapAutoUpdating())) {
            if (!this.payForCommand(Conf.econCostMap, "to show the map", "for showing the map")) {
               return;
            }

            this.fme.setMapAutoUpdating(true);
            this.msg("<i>Map auto update <green>ENABLED.", new Object[0]);
            this.showMap();
         } else {
            this.fme.setMapAutoUpdating(false);
            this.msg("<i>Map auto update <red>DISABLED.", new Object[0]);
         }
      } else {
         if (!this.payForCommand(Conf.econCostMap, "to show the map", "for showing the map")) {
            return;
         }

         this.showMap();
      }
   }

   public void showMap() {
      this.sendMessage(Board.getMap(this.myFaction, new FLocation(this.fme), this.fme.getPlayer().getLocation().getYaw()));
   }
}
