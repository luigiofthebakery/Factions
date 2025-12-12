package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import org.bukkit.ChatColor;

public class CmdMoneyWithdraw extends FCommand {
   public CmdMoneyWithdraw() {
      this.aliases.add("w");
      this.aliases.add("withdraw");
      this.requiredArgs.add("amount");
      this.optionalArgs.put("faction", "yours");
      this.permission = Permission.MONEY_WITHDRAW.node;
      this.setHelpShort("withdraw money");
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      double amount = this.argAsDouble(0, 0.0);
      EconomyParticipator faction = this.argAsFaction(1, this.myFaction);
      if (faction != null) {
         boolean success = Econ.transferMoney(this.fme, faction, this.fme, amount);
         if (success && Conf.logMoneyTransactions) {
            P.p
               .log(
                  ChatColor.stripColor(
                     P.p.txt.parse("%s withdrew %s from the faction bank: %s", this.fme.getName(), Econ.moneyString(amount), faction.describeTo(null))
                  )
               );
         }
      }
   }
}
