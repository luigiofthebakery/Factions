package com.massivecraft.factions.zcore.util;

import com.massivecraft.factions.zcore.MPlugin;
import java.io.File;
import java.lang.reflect.Type;
import java.util.logging.Level;

public class Persist {
   private MPlugin p;

   public Persist(MPlugin p) {
      this.p = p;
   }

   public static String getName(Class<?> clazz) {
      return clazz.getSimpleName().toLowerCase();
   }

   public static String getName(Object o) {
      return getName(o.getClass());
   }

   public static String getName(Type type) {
      return getName(type.getClass());
   }

   public File getFile(String name) {
      return new File(this.p.getDataFolder(), name + ".json");
   }

   public File getFile(Class<?> clazz) {
      return this.getFile(getName(clazz));
   }

   public File getFile(Object obj) {
      return this.getFile(getName(obj));
   }

   public File getFile(Type type) {
      return this.getFile(getName(type));
   }

   public <T> T loadOrSaveDefault(T def, Class<T> clazz) {
      return this.loadOrSaveDefault(def, clazz, this.getFile(clazz));
   }

   public <T> T loadOrSaveDefault(T def, Class<T> clazz, String name) {
      return this.loadOrSaveDefault(def, clazz, this.getFile(name));
   }

   public <T> T loadOrSaveDefault(T def, Class<T> clazz, File file) {
      if (!file.exists()) {
         this.p.log("Creating default: " + file);
         this.save(def, file);
         return def;
      } else {
         T loaded = this.load(clazz, file);
         if (loaded == null) {
            this.p.log(Level.WARNING, "Using default as I failed to load: " + file);
            File backup = new File(file.getPath() + "_bad");
            if (backup.exists()) {
               backup.delete();
            }

            this.p.log(Level.WARNING, "Backing up copy of bad file to: " + backup);
            file.renameTo(backup);
            return def;
         } else {
            return loaded;
         }
      }
   }

   public boolean save(Object instance) {
      return this.save(instance, this.getFile(instance));
   }

   public boolean save(Object instance, String name) {
      return this.save(instance, this.getFile(name));
   }

   public boolean save(Object instance, File file) {
      return DiscUtil.writeCatch(file, this.p.gson.toJson(instance));
   }

   public <T> T load(Class<T> clazz) {
      return this.load(clazz, this.getFile(clazz));
   }

   public <T> T load(Class<T> clazz, String name) {
      return this.load(clazz, this.getFile(name));
   }

   public <T> T load(Class<T> clazz, File file) {
      String content = DiscUtil.readCatch(file);
      if (content == null) {
         return null;
      } else {
         try {
            return this.p.gson.fromJson(content, clazz);
         } catch (Exception var5) {
            this.p.log(Level.WARNING, var5.getMessage());
            return null;
         }
      }
   }

   public <T> T load(Type typeOfT, String name) {
      return this.load(typeOfT, this.getFile(name));
   }

   public <T> T load(Type typeOfT, File file) {
      String content = DiscUtil.readCatch(file);
      return content == null ? null : this.p.gson.fromJson(content, typeOfT);
   }
}
