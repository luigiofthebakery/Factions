package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import org.bukkit.Bukkit;

public class CmdUnclaimall extends FCommand {
   public CmdUnclaimall() {
      this.aliases.add("unclaimall");
      this.aliases.add("declaimall");
      this.permission = Permission.UNCLAIM_ALL.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = true;
   }

   @Override
   public void perform() {
      if (Econ.shouldBeUsed()) {
         double refund = Econ.calculateTotalLandRefund(this.myFaction.getLandRounded());
         if (Conf.bankEnabled && Conf.bankFactionPaysLandCosts) {
            if (!Econ.modifyMoney(this.myFaction, refund, "to unclaim all faction land", "for unclaiming all faction land")) {
               return;
            }
         } else if (!Econ.modifyMoney(this.fme, refund, "to unclaim all faction land", "for unclaiming all faction land")) {
            return;
         }
      }

      LandUnclaimAllEvent unclaimAllEvent = new LandUnclaimAllEvent(this.myFaction, this.fme);
      Bukkit.getServer().getPluginManager().callEvent(unclaimAllEvent);
      Board.unclaimAll(this.myFaction.getId());
      this.myFaction.msg("%s<i> unclaimed ALL of your faction's land.", this.fme.describeTo(this.myFaction, true));
      SpoutFeatures.updateTerritoryDisplayLoc(null);
      if (Conf.logLandUnclaims) {
         P.p
            .log(String.valueOf(this.fme.getName()) + " unclaimed everything for the faction: " + this.myFaction.getTag() + " (" + this.myFaction.getId() + ")");
      }
   }
}
