package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import org.bukkit.ChatColor;

public class CmdMoneyDeposit extends FCommand {
   public CmdMoneyDeposit() {
      this.aliases.add("d");
      this.aliases.add("deposit");
      this.requiredArgs.add("amount");
      this.optionalArgs.put("faction", "yours");
      this.permission = Permission.MONEY_DEPOSIT.node;
      this.setHelpShort("deposit money");
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
         boolean success = Econ.transferMoney(this.fme, this.fme, faction, amount);
         if (success && Conf.logMoneyTransactions) {
            P.p
               .log(
                  ChatColor.stripColor(
                     P.p.txt.parse("%s deposited %s in the faction bank: %s", this.fme.getName(), Econ.moneyString(amount), faction.describeTo(null))
                  )
               );
         }
      }
   }
}
