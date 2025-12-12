package com.massivecraft.factions.zcore;

import com.massivecraft.factions.zcore.util.TextUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class MCommand<T extends MPlugin> {
   public T p;
   public List<MCommand<?>> subCommands;
   public List<String> aliases;
   public boolean allowNoSlashAccess;
   public List<String> requiredArgs;
   public LinkedHashMap<String, String> optionalArgs;
   public boolean errorOnToManyArgs = true;
   private String helpShort;
   public List<String> helpLong;
   public CommandVisibility visibility;
   public boolean senderMustBePlayer;
   public String permission;
   public CommandSender sender;
   public Player me;
   public boolean senderIsConsole;
   public List<String> args;
   public List<MCommand<?>> commandChain = new ArrayList<>();

   public void addSubCommand(MCommand<?> subCommand) {
      subCommand.commandChain.addAll(this.commandChain);
      subCommand.commandChain.add(this);
      this.subCommands.add(subCommand);
   }

   public void setHelpShort(String val) {
      this.helpShort = val;
   }

   public String getHelpShort() {
      if (this.helpShort == null) {
         String pdesc = this.p.perm.getPermissionDescription(this.permission);
         return pdesc != null ? pdesc : "*info unavailable*";
      } else {
         return this.helpShort;
      }
   }

   public MCommand(T p) {
      this.p = p;
      this.permission = null;
      this.allowNoSlashAccess = false;
      this.subCommands = new ArrayList<>();
      this.aliases = new ArrayList<>();
      this.requiredArgs = new ArrayList<>();
      this.optionalArgs = new LinkedHashMap<>();
      this.helpShort = null;
      this.helpLong = new ArrayList<>();
      this.visibility = CommandVisibility.VISIBLE;
   }

   public void execute(CommandSender sender, List<String> args, List<MCommand<?>> commandChain) {
      this.sender = sender;
      if (sender instanceof Player) {
         this.me = (Player)sender;
         this.senderIsConsole = false;
      } else {
         this.me = null;
         this.senderIsConsole = true;
      }

      this.args = args;
      this.commandChain = commandChain;
      if (args.size() > 0) {
         for (MCommand<?> subCommand : this.subCommands) {
            if (subCommand.aliases.contains(args.get(0))) {
               args.remove(0);
               commandChain.add(this);
               subCommand.execute(sender, args, commandChain);
               return;
            }
         }
      }

      if (this.validCall(this.sender, this.args)) {
         if (this.isEnabled()) {
            this.perform();
         }
      }
   }

   public void execute(CommandSender sender, List<String> args) {
      this.execute(sender, args, new ArrayList<>());
   }

   public abstract void perform();

   public boolean validCall(CommandSender sender, List<String> args) {
      if (!this.validSenderType(sender, true)) {
         return false;
      } else {
         return !this.validSenderPermissions(sender, true) ? false : this.validArgs(args, sender);
      }
   }

   public boolean isEnabled() {
      return true;
   }

   public boolean validSenderType(CommandSender sender, boolean informSenderIfNot) {
      if (this.senderMustBePlayer && !(sender instanceof Player)) {
         if (informSenderIfNot) {
            this.msg("<b>This command can only be used by ingame players.");
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean validSenderPermissions(CommandSender sender, boolean informSenderIfNot) {
      return this.permission == null ? true : this.p.perm.has(sender, this.permission, informSenderIfNot);
   }

   public boolean validArgs(List<String> args, CommandSender sender) {
      if (args.size() < this.requiredArgs.size()) {
         if (sender != null) {
            this.msg("<b>To few arguments. <i>Use like this:");
            sender.sendMessage(this.getUseageTemplate());
         }

         return false;
      } else if (args.size() > this.requiredArgs.size() + this.optionalArgs.size() && this.errorOnToManyArgs) {
         if (sender != null) {
            List<String> theToMany = args.subList(this.requiredArgs.size() + this.optionalArgs.size(), args.size());
            this.msg("<b>Strange argument \"<p>%s<b>\". <i>Use the command like this:", TextUtil.implode(theToMany, " "));
            sender.sendMessage(this.getUseageTemplate());
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean validArgs(List<String> args) {
      return this.validArgs(args, null);
   }

   public String getUseageTemplate(List<MCommand<?>> commandChain, boolean addShortHelp) {
      StringBuilder ret = new StringBuilder();
      ret.append(this.p.txt.parseTags("<c>"));
      ret.append('/');

      for (MCommand<?> mc : commandChain) {
         ret.append(TextUtil.implode(mc.aliases, ","));
         ret.append(' ');
      }

      ret.append(TextUtil.implode(this.aliases, ","));
      List<String> args = new ArrayList<>();

      for (String requiredArg : this.requiredArgs) {
         args.add("<" + requiredArg + ">");
      }

      for (Entry<String, String> optionalArg : this.optionalArgs.entrySet()) {
         String val = optionalArg.getValue();
         if (val == null) {
            val = "";
         } else {
            val = "=" + val;
         }

         args.add("[" + optionalArg.getKey() + val + "]");
      }

      if (args.size() > 0) {
         ret.append(this.p.txt.parseTags("<p> "));
         ret.append(TextUtil.implode(args, " "));
      }

      if (addShortHelp) {
         ret.append(this.p.txt.parseTags(" <i>"));
         ret.append(this.getHelpShort());
      }

      return ret.toString();
   }

   public String getUseageTemplate(boolean addShortHelp) {
      return this.getUseageTemplate(this.commandChain, addShortHelp);
   }

   public String getUseageTemplate() {
      return this.getUseageTemplate(false);
   }

   public void msg(String str, Object... args) {
      this.sender.sendMessage(this.p.txt.parse(str, args));
   }

   public void sendMessage(String msg) {
      this.sender.sendMessage(msg);
   }

   public void sendMessage(List<String> msgs) {
      for (String msg : msgs) {
         this.sendMessage(msg);
      }
   }

   public boolean argIsSet(int idx) {
      return this.args.size() >= idx + 1;
   }

   public String argAsString(int idx, String def) {
      return this.args.size() < idx + 1 ? def : this.args.get(idx);
   }

   public String argAsString(int idx) {
      return this.argAsString(idx, null);
   }

   public Integer strAsInt(String str, Integer def) {
      if (str == null) {
         return def;
      } else {
         try {
            return Integer.parseInt(str);
         } catch (Exception var4) {
            return def;
         }
      }
   }

   public Integer argAsInt(int idx, Integer def) {
      return this.strAsInt(this.argAsString(idx), def);
   }

   public Integer argAsInt(int idx) {
      return this.argAsInt(idx, null);
   }

   public Double strAsDouble(String str, Double def) {
      if (str == null) {
         return def;
      } else {
         try {
            return Double.parseDouble(str);
         } catch (Exception var4) {
            return def;
         }
      }
   }

   public Double argAsDouble(int idx, Double def) {
      return this.strAsDouble(this.argAsString(idx), def);
   }

   public Double argAsDouble(int idx) {
      return this.argAsDouble(idx, null);
   }

   public Boolean strAsBool(String str) {
      str = str.toLowerCase();
      return !str.startsWith("y") && !str.startsWith("t") && !str.startsWith("on") && !str.startsWith("+") && !str.startsWith("1") ? false : true;
   }

   public Boolean argAsBool(int idx, boolean def) {
      String str = this.argAsString(idx);
      return str == null ? def : this.strAsBool(str);
   }

   public Boolean argAsBool(int idx) {
      return this.argAsBool(idx, false);
   }

   public Player strAsPlayer(String name, Player def, boolean msg) {
      Player ret = def;
      if (name != null) {
         Player player = Bukkit.getServer().getPlayer(name);
         if (player != null) {
            ret = player;
         }
      }

      if (msg && ret == null) {
         this.msg("<b>No player \"<p>%s<b>\" could not be found.", name);
      }

      return ret;
   }

   public Player argAsPlayer(int idx, Player def, boolean msg) {
      return this.strAsPlayer(this.argAsString(idx), def, msg);
   }

   public Player argAsPlayer(int idx, Player def) {
      return this.argAsPlayer(idx, def, true);
   }

   public Player argAsPlayer(int idx) {
      return this.argAsPlayer(idx, null);
   }

   public Player strAsBestPlayerMatch(String name, Player def, boolean msg) {
      Player ret = def;
      if (name != null) {
         List<Player> players = Bukkit.getServer().matchPlayer(name);
         if (players.size() > 0) {
            ret = players.get(0);
         }
      }

      if (msg && ret == null) {
         this.msg("<b>No player match found for \"<p>%s<b>\".", name);
      }

      return ret;
   }

   public Player argAsBestPlayerMatch(int idx, Player def, boolean msg) {
      return this.strAsBestPlayerMatch(this.argAsString(idx), def, msg);
   }

   public Player argAsBestPlayerMatch(int idx, Player def) {
      return this.argAsBestPlayerMatch(idx, def, true);
   }

   public Player argAsBestPlayerMatch(int idx) {
      return this.argAsPlayer(idx, null);
   }
}
