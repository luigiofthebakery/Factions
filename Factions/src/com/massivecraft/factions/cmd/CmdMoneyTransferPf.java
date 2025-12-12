package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import org.bukkit.ChatColor;

public class CmdMoneyTransferPf extends FCommand {
   public CmdMoneyTransferPf() {
      this.aliases.add("pf");
      this.requiredArgs.add("amount");
      this.requiredArgs.add("player");
      this.requiredArgs.add("faction");
      this.permission = Permission.MONEY_P2F.node;
      this.setHelpShort("transfer p -> f");
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      double amount = this.argAsDouble(0, 0.0);
      EconomyParticipator from = this.argAsBestFPlayerMatch(1);
      if (from != null) {
         EconomyParticipator to = this.argAsFaction(2);
         if (to != null) {
            boolean success = Econ.transferMoney(this.fme, from, to, amount);
            if (success && Conf.logMoneyTransactions) {
               P.p
                  .log(
                     ChatColor.stripColor(
                        P.p
                           .txt
                           .parse(
                              "%s transferred %s from the player \"%s\" to the faction \"%s\"",
                              this.fme.getName(),
                              Econ.moneyString(amount),
                              from.describeTo(null),
                              to.describeTo(null)
                           )
                     )
                  );
            }
         }
      }
   }
}
