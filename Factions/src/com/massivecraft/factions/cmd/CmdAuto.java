package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.AutoActionDetails;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.MCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CmdAuto extends FCommand {
   public List<AutomatableCommand> actions = new CopyOnWriteArrayList<>();

   public CmdAuto() {
      this.aliases.add("auto");
      this.optionalArgs.put("command", "clear");
      this.permission = Permission.AUTO.node;

      this.disableOnLock = true;
      this.errorOnToManyArgs = false;

      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      String arg = this.argAsString(0, "clear");

      if (arg.equals("clear")) {
         if (this.fme.getAutoActions().isEmpty()) {
            this.msg("<i>There aren't any automatic actions to turn off.");
            return;
         }
         this.fme.removeAllAutoActions();
         this.msg("<i>Turned off all automatic actions.");
         return;
      }

      for (AutomatableCommand command: actions ) {
         for (String alias : command.aliases) {
            if (!alias.equals(arg)) continue;

            if (args.size() > 1) {
               String possibleSubAlias = args.get(1);
               for (MCommand<?> subcommand : command.subCommands) {
                  for (String subAlias : subcommand.aliases) {
                     if (!(subcommand instanceof AutomatableCommand) || !subAlias.equals(possibleSubAlias))
                        continue;

                     startAutoCommand((AutomatableCommand) subcommand, this.args.subList(2, args.size()));

                     return;
                  }
               }
            }

            startAutoCommand(command, this.args.subList(1, args.size()));

            return;
         }
      }
      msg("<b>No command found matching: %s", arg);
   }

   public void startAutoCommand(AutomatableCommand command) {
      this.startAutoCommand(command, this.args, this.commandChain);
   }

   public void startAutoCommand(AutomatableCommand command, List<String> args) {
      this.startAutoCommand(command, args, this.commandChain);
   }

   public void startAutoCommand(AutomatableCommand command, List<String> args, List<MCommand<?>> commandChain) {
      AutoActionDetails details = new AutoActionDetails((Player)sender, command, new ArrayList<>(args), commandChain);

      if (fme.hasAutoActionFor(command)) {
         AutoActionDetails existing = fme.getAutoActionFor(command);
         if (command.doArgsMatch(args, existing.getArgs())) {
            fme.toggleAutoAction(details);
         } else {
            fme.removeAutoAction(existing);
            fme.addAutoAction(details);
         }
      } else {
         fme.toggleAutoAction(details);
      }
   }

   public void registerAction(AutomatableCommand command) {
      this.actions.add(command);
   }
}
