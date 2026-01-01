package com.massivecraft.factions.zcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.zcore.persist.EM;
import com.massivecraft.factions.zcore.persist.SaveTask;
import com.massivecraft.factions.zcore.util.LibLoader;
import com.massivecraft.factions.zcore.util.PermUtil;
import com.massivecraft.factions.zcore.util.Persist;
import com.massivecraft.factions.zcore.util.TextUtil;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class MPlugin extends JavaPlugin {
   public Persist persist;
   public TextUtil txt;
   public LibLoader lib;
   public PermUtil perm;
   public Gson gson;
   private ScheduledTask saveTask = null;
   private boolean autoSave = true;
   protected boolean loadSuccessful = false;
   public String refCommand = "";
   private MPluginSecretPlayerListener mPluginSecretPlayerListener;

   private List<MCommand<?>> baseCommands = new ArrayList<>();
   private long timeEnableStart;
   public Map<String, String> rawTags = new LinkedHashMap<>();

   public boolean getAutoSave() {
      return this.autoSave;
   }

   public void setAutoSave(boolean val) {
      this.autoSave = val;
   }

   public List<MCommand<?>> getBaseCommands() {
      return this.baseCommands;
   }

   public boolean preEnable() {
      this.log("=== ENABLE START ===");
      this.timeEnableStart = System.currentTimeMillis();
      this.getDataFolder().mkdirs();
      this.perm = new PermUtil(this);
      this.persist = new Persist(this);
      this.lib = new LibLoader(this);
      this.gson = this.getGsonBuilder().create();
      this.txt = new TextUtil();
      this.initTXT();

      try {
         Map<String, Map<String, Object>> refCmd = this.getDescription().getCommands();
         if (refCmd != null && !refCmd.isEmpty()) {
            this.refCommand = (String)refCmd.keySet().toArray()[0];
         }
      } catch (ClassCastException var3) {
      }

      this.mPluginSecretPlayerListener = new MPluginSecretPlayerListener(this);

      this.getServer().getPluginManager().registerEvents(this.mPluginSecretPlayerListener, this);

      long saveTicks = 30L;
      if (this.saveTask == null) {
         this.saveTask = Bukkit.getAsyncScheduler().runAtFixedRate(this, new SaveTask(this), saveTicks, saveTicks, TimeUnit.MINUTES);
      }

      this.loadSuccessful = true;
      return true;
   }

   public void postEnable() {
      this.log("=== ENABLE DONE (Took " + (System.currentTimeMillis() - this.timeEnableStart) + "ms) ===");
   }

   public void onDisable() {
      if (this.saveTask != null) {
         this.saveTask.cancel();
         this.saveTask = null;
      }

      if (this.loadSuccessful) {
         EM.saveAllToDisc();
      }

      this.log("Disabled");
   }

   public void suicide() {
      this.log("Now I suicide!");
      this.getServer().getPluginManager().disablePlugin(this);
   }

   public GsonBuilder getGsonBuilder() {
      return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().excludeFieldsWithModifiers(128, 64);
   }

   public void addRawTags() {
      this.rawTags.put("l", "<green>");
      this.rawTags.put("a", "<gold>");
      this.rawTags.put("n", "<silver>");
      this.rawTags.put("i", "<yellow>");
      this.rawTags.put("g", "<lime>");
      this.rawTags.put("b", "<rose>");
      this.rawTags.put("h", "<pink>");
      this.rawTags.put("c", "<aqua>");
      this.rawTags.put("p", "<teal>");
   }

   public void initTXT() {
      this.addRawTags();
      Type type = (new TypeToken<Map<String, String>>() {}).getType();
      Map<String, String> tagsFromFile = this.persist.load(type, "tags");
      if (tagsFromFile != null) {
         this.rawTags.putAll(tagsFromFile);
      }

      this.persist.save(this.rawTags, "tags");

      for (Entry<String, String> rawTag : this.rawTags.entrySet()) {
         this.txt.tags.put(rawTag.getKey(), TextUtil.parseColor(rawTag.getValue()));
      }
   }

   public boolean logPlayerCommands() {
      return true;
   }

   public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly) {
      return this.handleCommand(sender, commandString, testOnly, false);
   }

   public boolean handleCommand(final CommandSender sender, String commandString, boolean testOnly, boolean async) {
      boolean noSlash = true;
      if (commandString.startsWith("/")) {
         noSlash = false;
         commandString = commandString.substring(1);
      }

      for (final MCommand<?> command : this.getBaseCommands()) {
         if (!noSlash || command.allowNoSlashAccess) {
            for (String alias : command.aliases) {
               if (commandString.startsWith(alias + "  ")) {
                  return false;
               }

               if (commandString.startsWith(alias + " ") || commandString.equals(alias)) {
                  final List<String> args = new ArrayList<>(Arrays.asList(commandString.split("\\s+")));
                  args.remove(0);
                  if (testOnly) {
                     return true;
                  } else {
                     if (async && sender instanceof Player) {
                        ((Player)sender).getScheduler().execute(this, new Runnable() {
                           @Override
                           public void run() {
                              command.execute(sender, args);
                           }
                        }, null, 1L);
                     } else if (async) {
                        Bukkit.getAsyncScheduler().runNow(this, (ScheduledTask t) -> {
                           command.execute(sender, args);
                        });
                     } else {
                        command.execute(sender, args);
                     }

                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean handleCommand(CommandSender sender, String commandString) {
      return this.handleCommand(sender, commandString, false);
   }

   public void preAutoSave() {
   }

   public void postAutoSave() {
   }

   public void log(Object msg) {
      this.log(Level.INFO, msg);
   }

   public void log(String str, Object... args) {
      this.log(Level.INFO, this.txt.parse(str, args));
   }

   public void log(Level level, String str, Object... args) {
      this.log(level, this.txt.parse(str, args));
   }

   public void log(Level level, Object msg) {
      Bukkit.getLogger().log(level, "[" + this.getDescription().getFullName() + "] " + msg);
   }
}
