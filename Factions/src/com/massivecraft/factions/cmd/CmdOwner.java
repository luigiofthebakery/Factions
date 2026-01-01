package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.AutoTriggerType;
import com.massivecraft.factions.struct.AutomatableCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.MCommand;

import java.util.ArrayList;
import java.util.List;

public class CmdOwner extends AutomatableCommand {
   private final CmdOwnerList cmdOwnerList;
   private final CmdOwnerAdd cmdOwnerAdd;
   private final CmdOwnerRemove cmdOwnerRemove;
   private final CmdOwnerClear cmdOwnerClear;

   public CmdOwner() {
      this.aliases.add("owner");

      this.optionalArgs.put("add/remove/clear/list", "");
      this.optionalArgs.put("player name", "you");

      this.permission = Permission.OWNER.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;

      this.cmdOwnerList = new CmdOwnerList();
      this.cmdOwnerAdd = new CmdOwnerAdd();
      this.cmdOwnerRemove = new CmdOwnerRemove();
      this.cmdOwnerClear = new CmdOwnerClear();

      this.subCommands.add(this.cmdOwnerList);
      this.subCommands.add(this.cmdOwnerAdd);
      this.subCommands.add(this.cmdOwnerRemove);
      this.subCommands.add(this.cmdOwnerClear);

      this.autoPermission = Permission.OWNER_AUTO.node;
      this.autoTriggerType = AutoTriggerType.CHUNK_BOUNDARY;
      this.incompatibleWith = new ArrayList<>();
   }

   @Override
   public void perform() {
      if (!Conf.ownedAreasEnabled) {
         this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         return;
      }

      if (args.isEmpty()) {
         this.fme.msg("<b>To few arguments. <i>Use like this:");
         sender.sendMessage(this.getUseageTemplate());
         return;
      }

      String invalidArg = args.get(0);
      this.msg("<b>Strange argument \"<p>%s<b>\". <i>Use the command like this:", invalidArg);
      sender.sendMessage(this.getUseageTemplate());
   }

   @Override
   public boolean onAutoEnable(FPlayer player) {
      boolean preCheck = super.onAutoEnable(player);

      if (!preCheck) {
         return false;
      }

      if (!Conf.ownedAreasEnabled) {
         this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         return false;
      }

      if (!fme.isAdminBypassing() && !this.assertHasFaction()) {
         return false;
      }

      if (player.getAutoActions().stream().noneMatch(action ->
         this.subCommands.stream().anyMatch(subCommand -> subCommand.getClass().equals(action.getCommand().getClass())))) {
         player.msg("<i>There are no owner-related automatic actions to turn off.");
         player.msg(getUseageTemplate(true));
         return false;
      }

      for (MCommand<?> command : subCommands) {
         if (!(command instanceof AutomatableCommand)) continue;

         player.removeAutoAction((AutomatableCommand) command);
      }

      player.msg("<i>Turned off all owner-related automatic actions.");

      return false;
   }

   @Override
   public boolean onAutoDisable(FPlayer player) { return true; }

   @Override
   public boolean doArgsMatch(List<String> args1, List<String> args2) { return true; }
}
