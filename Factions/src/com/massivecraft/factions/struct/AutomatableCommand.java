package com.massivecraft.factions.struct;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.P;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.MCommand;
import com.massivecraft.factions.zcore.util.PermUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AutomatableCommand extends FCommand {
   protected AutoTriggerType autoTriggerType;
   protected List<Class<?>> incompatibleWith;
   protected String autoPermission;
   protected int priority = 0;

   public AutoTriggerType getTriggerType() {
      return this.autoTriggerType;
   }

   public List<Class<?>> getIncompatibleWith() {
      return this.incompatibleWith;
   }

   public int getPriority() {
      return this.priority;
   }

   public boolean onAutoEnable(FPlayer player) {
      boolean hasPermission = P.p.perm.has(player.getPlayer(), autoPermission);

      if (!hasPermission) {
         this.msg(P.p.perm.getForbiddenMessage(autoPermission));
         return false;
      }

      return true;
   }

   public abstract boolean onAutoDisable(FPlayer player);
   public abstract boolean doArgsMatch(List<String> args1, List<String> args2);
}
