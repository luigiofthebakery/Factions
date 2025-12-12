package com.massivecraft.factions.zcore.persist;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.massivecraft.factions.zcore.util.DiscUtil;
import com.massivecraft.factions.zcore.util.TextUtil;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public abstract class EntityCollection<E extends Entity> {
   private Collection<E> entities;
   protected Map<String, E> id2entity;
   private boolean creative;
   private int nextId;
   private Class<E> entityClass;
   private Gson gson;
   private File file;

   public boolean isCreative() {
      return this.creative;
   }

   public void setCreative(boolean creative) {
      this.creative = creative;
   }

   public abstract Type getMapType();

   public Gson getGson() {
      return this.gson;
   }

   public void setGson(Gson gson) {
      this.gson = gson;
   }

   public File getFile() {
      return this.file;
   }

   public void setFile(File file) {
      this.file = file;
   }

   public EntityCollection(Class<E> entityClass, Collection<E> entities, Map<String, E> id2entity, File file, Gson gson, boolean creative) {
      this.entityClass = entityClass;
      this.entities = entities;
      this.id2entity = id2entity;
      this.file = file;
      this.gson = gson;
      this.creative = creative;
      this.nextId = 1;
      EM.setEntitiesCollectionForEntityClass(this.entityClass, this);
   }

   public EntityCollection(Class<E> entityClass, Collection<E> entities, Map<String, E> id2entity, File file, Gson gson) {
      this(entityClass, entities, id2entity, file, gson, false);
   }

   public Collection<E> get() {
      return this.entities;
   }

   public Map<String, E> getMap() {
      return this.id2entity;
   }

   public E get(String id) {
      return this.creative ? this.getCreative(id) : this.id2entity.get(id);
   }

   public E getCreative(String id) {
      E e = this.id2entity.get(id);
      return e != null ? e : this.create(id);
   }

   public boolean exists(String id) {
      return id == null ? false : this.id2entity.get(id) != null;
   }

   public E getBestIdMatch(String pattern) {
      String id = TextUtil.getBestStartWithCI(this.id2entity.keySet(), pattern);
      return id == null ? null : this.id2entity.get(id);
   }

   public synchronized E create() {
      return this.create(this.getNextId());
   }

   public synchronized E create(String id) {
      if (!this.isIdFree(id)) {
         return null;
      } else {
         E e = null;

         try {
            e = this.entityClass.newInstance();
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         e.setId(id);
         this.entities.add(e);
         this.id2entity.put(e.getId(), e);
         this.updateNextIdForId(id);
         return e;
      }
   }

   public void attach(E entity) {
      if (entity.getId() == null) {
         entity.setId(this.getNextId());
         this.entities.add(entity);
         this.id2entity.put(entity.getId(), entity);
      }
   }

   public void detach(E entity) {
      entity.preDetach();
      this.entities.remove(entity);
      this.id2entity.remove(entity.getId());
      entity.postDetach();
   }

   public void detach(String id) {
      E entity = this.id2entity.get(id);
      if (entity != null) {
         this.detach(entity);
      }
   }

   public boolean attached(E entity) {
      return this.entities.contains(entity);
   }

   public boolean detached(E entity) {
      return !this.attached(entity);
   }

   public boolean saveToDisc() {
      Map<String, E> entitiesThatShouldBeSaved = new HashMap<>();

      for (E entity : this.entities) {
         if (entity.shouldBeSaved()) {
            entitiesThatShouldBeSaved.put(entity.getId(), entity);
         }
      }

      return this.saveCore(entitiesThatShouldBeSaved);
   }

   private boolean saveCore(Map<String, E> entities) {
      return DiscUtil.writeCatch(this.file, this.gson.toJson(entities));
   }

   public boolean loadFromDisc() {
      Map<String, E> id2entity = this.loadCore();
      if (id2entity == null) {
         return false;
      } else {
         this.entities.clear();
         this.entities.addAll(id2entity.values());
         this.id2entity.clear();
         this.id2entity.putAll(id2entity);
         this.fillIds();
         return true;
      }
   }

   private Map<String, E> loadCore() {
      if (!this.file.exists()) {
         return new HashMap<>();
      } else {
         String content = DiscUtil.readCatch(this.file);
         if (content == null) {
            return null;
         } else {
            Type type = this.getMapType();

            try {
               return this.gson.fromJson(content, type);
            } catch (JsonSyntaxException var5) {
               Bukkit.getLogger().log(Level.WARNING, "JSON error encountered loading \"" + this.file + "\": " + var5.getLocalizedMessage());
               File backup = new File(this.file.getPath() + "_bad");
               if (backup.exists()) {
                  backup.delete();
               }

               Bukkit.getLogger().log(Level.WARNING, "Backing up copy of bad file to: " + backup);
               this.file.renameTo(backup);
               return null;
            }
         }
      }
   }

   public String getNextId() {
      while (!this.isIdFree(this.nextId)) {
         this.nextId++;
      }

      return Integer.toString(this.nextId);
   }

   public boolean isIdFree(String id) {
      return !this.id2entity.containsKey(id);
   }

   public boolean isIdFree(int id) {
      return this.isIdFree(Integer.toString(id));
   }

   protected synchronized void fillIds() {
      this.nextId = 1;

      for (Entry<String, E> entry : this.id2entity.entrySet()) {
         String id = entry.getKey();
         E entity = entry.getValue();
         entity.id = id;
         this.updateNextIdForId(id);
      }
   }

   protected synchronized void updateNextIdForId(int id) {
      if (this.nextId < id) {
         this.nextId = id + 1;
      }
   }

   protected void updateNextIdForId(String id) {
      try {
         int idAsInt = Integer.parseInt(id);
         this.updateNextIdForId(idAsInt);
      } catch (Exception var3) {
      }
   }
}
