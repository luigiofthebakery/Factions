package com.massivecraft.factions;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.cmd.CmdAutoHelp;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.EssentialsFeatures;
import com.massivecraft.factions.integration.LWCFeatures;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.integration.capi.CapiFeatures;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.listeners.FactionsChatListener;
import com.massivecraft.factions.listeners.FactionsEntityListener;
import com.massivecraft.factions.listeners.FactionsExploitListener;
import com.massivecraft.factions.listeners.FactionsPlayerListener;
import com.massivecraft.factions.listeners.FactionsServerListener;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.util.AutoLeaveTask;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.zcore.MPlugin;
import com.massivecraft.factions.zcore.util.TextUtil;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class P extends MPlugin {
   public static P p;
   public final FactionsPlayerListener playerListener;
   public final FactionsChatListener chatListener;
   public final FactionsEntityListener entityListener;
   public final FactionsExploitListener exploitListener;
   public final FactionsBlockListener blockListener;
   public final FactionsServerListener serverListener;
   private boolean locked = false;
   private ScheduledTask AutoLeaveTask = null;
   public FCmdRoot cmdBase;
   public CmdAutoHelp cmdAutoHelp;

   public boolean getLocked() {
      return this.locked;
   }

   public void setLocked(boolean val) {
      this.locked = val;
      this.setAutoSave(val);
   }

   public P() {
      p = this;
      this.playerListener = new FactionsPlayerListener(this);
      this.chatListener = new FactionsChatListener(this);
      this.entityListener = new FactionsEntityListener(this);
      this.exploitListener = new FactionsExploitListener();
      this.blockListener = new FactionsBlockListener(this);
      this.serverListener = new FactionsServerListener(this);
   }

   public void onEnable() {
      /*try {
         Class.forName("org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken");
      } catch (ClassNotFoundException var2) {
         this.log(Level.SEVERE, "GSON lib not found. Your CraftBukkit build is too old (< 1.3.2) or otherwise not compatible.");
         this.suicide();
         return;
      }*/

      if (this.preEnable()) {
         this.loadSuccessful = false;
         Conf.load();
         FPlayers.i.loadFromDisc();
         Factions.i.loadFromDisc();
         Board.load();
         this.cmdBase = new FCmdRoot();
         this.cmdAutoHelp = new CmdAutoHelp();
         this.getBaseCommands().add(this.cmdBase);
         EssentialsFeatures.setup();
         SpoutFeatures.setup();
         Econ.setup();
         CapiFeatures.setup();
         LWCFeatures.setup();
         if (Conf.worldGuardChecking || Conf.worldGuardBuildPriority) {
            Worldguard.init(this);
         }

         this.startAutoLeaveTask(false);
         this.getServer().getPluginManager().registerEvents(this.playerListener, this);
         this.getServer().getPluginManager().registerEvents(this.chatListener, this);
         this.getServer().getPluginManager().registerEvents(this.entityListener, this);
         this.getServer().getPluginManager().registerEvents(this.exploitListener, this);
         this.getServer().getPluginManager().registerEvents(this.blockListener, this);
         this.getServer().getPluginManager().registerEvents(this.serverListener, this);
         this.getCommand(this.refCommand).setExecutor(this);
         this.postEnable();
         this.loadSuccessful = true;
         Bukkit.getAsyncScheduler().runAtFixedRate(this, new Consumer<ScheduledTask>() {
            String bkpr = "backup";

            public void accept(ScheduledTask t) {
               long now = System.currentTimeMillis();

               try {
                  File dir = new File(P.p.getDataFolder() + File.separator + this.bkpr);
                  if (dir.exists() && !dir.isDirectory()) {
                     this.bkpr = "backup" + now;
                     dir = new File(P.p.getDataFolder() + File.separator + this.bkpr);
                  }

                  dir.mkdirs();
                  File[] files = dir.listFiles();
                  long mostRecent = 0L;
                  if (files != null) {
                     double total = 0.0;

                     for (File f : files) {
                        total += f.length();
                        long lastModified = f.lastModified();
                        if (lastModified > mostRecent) {
                           mostRecent = lastModified;
                        }

                        if (lastModified < now - 1209600000L) {
                           f.delete();
                        }
                     }
                  }

                  if (mostRecent > now - 86400000L) {
                     return;
                  }

                  File f = new File(dir, "factions" + now + ".zip");
                  ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
                  String[] fx = new String[]{"factions.json", "players.json", "board.json"};

                  for (String fxs : fx) {
                     ZipEntry e = new ZipEntry(fxs);
                     out.putNextEntry(e);
                     byte[] data = Files.readAllBytes(Paths.get(P.p.getDataFolder() + File.separator + fxs));
                     out.write(data, 0, data.length);
                     out.closeEntry();
                  }

                  out.close();
               } catch (IOException var16) {
                  var16.printStackTrace();
               }
            }
         }, 1000L, 81400L, TimeUnit.SECONDS);
      }
   }

   @Override
   public GsonBuilder getGsonBuilder() {
      Type mapFLocToStringSetType = (new TypeToken<Map<FLocation, Set<String>>>() {}).getType();
      return new GsonBuilder()
         .setPrettyPrinting()
         .disableHtmlEscaping()
         .excludeFieldsWithModifiers(128, 64)
         .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
         .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter());
   }

   @Override
   public void onDisable() {
      if (this.loadSuccessful) {
         Board.save();
         Conf.save();
      }

      EssentialsFeatures.unhookChat();
      if (this.AutoLeaveTask != null) {
         this.AutoLeaveTask.cancel();
         this.AutoLeaveTask = null;
      }

      super.onDisable();
   }

   public void startAutoLeaveTask(boolean restartIfRunning) {
      if (this.AutoLeaveTask != null) {
         if (!restartIfRunning) {
            return;
         }

         this.AutoLeaveTask.cancel();
      }

      if (Conf.autoLeaveRoutineRunsEveryXMinutes > 0.0) {
         long seconds = (long)(60.0 * Conf.autoLeaveRoutineRunsEveryXMinutes);
         this.AutoLeaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(this, new AutoLeaveTask(), seconds, seconds, TimeUnit.SECONDS);
      }
   }

   @Override
   public void postAutoSave() {
      Board.save();
      Conf.save();
   }

   @Override
   public boolean logPlayerCommands() {
      return Conf.logPlayerCommands;
   }

   @Override
   public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly) {
      return sender instanceof Player && FactionsPlayerListener.preventCommand(commandString, (Player)sender)
         ? true
         : super.handleCommand(sender, commandString, testOnly);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
      if (split != null && split.length != 0) {
         String cmd = Conf.baseCommandAliases.isEmpty() ? "/f" : "/" + Conf.baseCommandAliases.get(0);
         return this.handleCommand(sender, cmd + " " + TextUtil.implode(Arrays.asList(split), " "), false);
      } else {
         return true;
      }
   }

   public int hookSupportVersion() {
      return 3;
   }

   public void handleFactionTagExternally(boolean notByFactions) {
      Conf.chatTagHandledByAnotherPlugin = notByFactions;
   }

   public boolean shouldLetFactionsHandleThisChat(AsyncPlayerChatEvent event) {
      return event == null ? false : this.isPlayerFactionChatting(event.getPlayer()) || this.isFactionsCommand(event.getMessage());
   }

   public boolean isPlayerFactionChatting(Player player) {
      if (player == null) {
         return false;
      } else {
         FPlayer me = FPlayers.i.get(player);
         return me == null ? false : me.getChatMode().isAtLeast(ChatMode.ALLIANCE);
      }
   }

   public boolean isFactionsCommand(String check) {
      return check != null && !check.isEmpty() ? this.handleCommand(null, check, true) : false;
   }

   public String getPlayerFactionTag(Player player) {
      return this.getPlayerFactionTagRelation(player, null);
   }

   public String getPlayerFactionTagRelation(Player speaker, Player listener) {
      String tag = "~";
      if (speaker == null) {
         return tag;
      } else {
         FPlayer me = FPlayers.i.get(speaker);
         if (me == null) {
            return tag;
         } else {
            FPlayer you;
            tag = listener != null && Conf.chatTagRelationColored
               ? ((you = FPlayers.i.get(listener)) == null ? me.getChatTag().trim() : me.getChatTag(you).trim())
               : me.getChatTag().trim();
            if (tag.isEmpty()) {
               tag = "~";
            }

            return tag;
         }
      }
   }

   public String getPlayerTitle(Player player) {
      if (player == null) {
         return "";
      } else {
         FPlayer me = FPlayers.i.get(player);
         return me == null ? "" : me.getTitle().trim();
      }
   }

   public Set<String> getFactionTags() {
      HashSet<String> tags = new HashSet<>();

      for (Faction faction : Factions.i.get()) {
         tags.add(faction.getTag());
      }

      return tags;
   }

   public Set<String> getPlayersInFaction(String factionTag) {
      HashSet<String> players = new HashSet<>();
      Faction faction = Factions.i.getByTag(factionTag);
      if (faction != null) {
         for (FPlayer fplayer : faction.getFPlayers()) {
            players.add(fplayer.getName());
         }
      }

      return players;
   }

   public Set<String> getOnlinePlayersInFaction(String factionTag) {
      HashSet<String> players = new HashSet<>();
      Faction faction = Factions.i.getByTag(factionTag);
      if (faction != null) {
         for (FPlayer fplayer : faction.getFPlayersWhereOnline(true)) {
            players.add(fplayer.getName());
         }
      }

      return players;
   }

   public boolean isPlayerAllowedToBuildHere(Player player, Location location) {
      return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, "", true);
   }

   public boolean isPlayerAllowedToInteractWith(Player player, Block block) {
      return FactionsPlayerListener.canPlayerUseBlock(player, block, true);
   }

   public boolean isPlayerAllowedToUseThisHere(Player player, Location location, Material material) {
      return FactionsPlayerListener.playerCanUseItemHere(player, location, material, true);
   }
}
