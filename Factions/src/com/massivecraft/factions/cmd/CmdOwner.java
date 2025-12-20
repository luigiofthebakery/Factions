package com.massivecraft.factions.cmd;

import com.massivecraft.factions.*;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TextUtil;

import java.util.List;

public class CmdOwner extends FCommand {
   private final CmdOwnerList cmdOwnerList;
   private final CmdOwnerAdd cmdOwnerAdd;
   private final CmdOwnerRemove cmdOwnerRemove;
   private final CmdOwnerClear cmdOwnerClear;

   public CmdOwner() {
      this.aliases.add("owner");

      this.requiredArgs.add("add/remove/clear/list");
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
   }

   @Override
   public void perform() {
      if (!Conf.ownedAreasEnabled) {
         this.fme.msg("<b>Sorry, but owned areas are disabled on this server.");
         return;
      }

      String invalidArg = args.get(0);
      this.msg("<b>Strange argument \"<p>%s<b>\". <i>Use the command like this:", invalidArg);
      sender.sendMessage(this.getUseageTemplate());
   }
}
