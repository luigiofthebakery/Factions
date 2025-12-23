package com.massivecraft.factions.struct;

import com.google.gson.Gson;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.MCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoActionDetails {
   private final Player player;
   private final AutomatableCommand command;
   private final List<String> args;
   private final List<MCommand<?>> commandChain;

   public AutoActionDetails(Player player, AutomatableCommand command, List<String> args, List<MCommand<?>> commandChain) {
      this.player = player;
      this.command = command;
      this.args = new ArrayList<>(args);
      this.commandChain = new ArrayList<>(commandChain);
   }

   public AutomatableCommand getCommand() {
      return this.command;
   }

   public List<String> getArgs() {
      return new ArrayList<>(this.args);
   }

   public int getPriority() {
      return this.command.getPriority();
   }

   public void doAction() {
      this.command.execute(player, new ArrayList<>(args), new ArrayList<>(commandChain));
   }

   public AutoTriggerType getTriggerType() {
      return this.command.getTriggerType();
   }

   public void remove() {
      FPlayers.i.get(player).removeAutoAction(this);
   }

   public boolean onEnable() {
      FPlayer fplayer = FPlayers.i.get(player);
      fplayer.removeAutoActions(this.command.incompatibleWith);

      command.prepare(player, new ArrayList<>(args), new ArrayList<>(commandChain));
      return this.command.onAutoEnable(fplayer);
   }

   public boolean onDisable() {
      FPlayer fplayer = FPlayers.i.get(player);

      command.prepare(player, new ArrayList<>(args), new ArrayList<>(commandChain));
      return this.command.onAutoDisable(fplayer);
   }
}
