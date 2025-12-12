package com.massivecraft.factions.zcore.persist;

import java.util.LinkedHashMap;
import java.util.Map;

public class EM {
   public static Map<Class<? extends Entity>, EntityCollection<? extends Entity>> class2Entities = new LinkedHashMap<>();

   public static <T extends Entity> EntityCollection<T> getEntitiesCollectionForEntityClass(Class<T> entityClass) {
      return (EntityCollection<T>)class2Entities.get(entityClass);
   }

   public static void setEntitiesCollectionForEntityClass(Class<? extends Entity> entityClass, EntityCollection<? extends Entity> entities) {
      class2Entities.put(entityClass, entities);
   }

   public static <T extends Entity> void attach(T entity) {
      EntityCollection<T> ec = getEntitiesCollectionForEntityClass((Class<T>)entity.getClass());
      ec.attach(entity);
   }

   public static <T extends Entity> void detach(T entity) {
      EntityCollection<T> ec = getEntitiesCollectionForEntityClass((Class<T>)entity.getClass());
      ec.detach(entity);
   }

   public static <T extends Entity> boolean attached(T entity) {
      EntityCollection<T> ec = getEntitiesCollectionForEntityClass((Class<T>)entity.getClass());
      return ec.attached(entity);
   }

   public static <T extends Entity> boolean detached(T entity) {
      EntityCollection<T> ec = getEntitiesCollectionForEntityClass((Class<T>)entity.getClass());
      return ec.detached(entity);
   }

   public static void saveAllToDisc() {
      for (EntityCollection<? extends Entity> ec : class2Entities.values()) {
         ec.saveToDisc();
      }
   }

   public static void loadAllFromDisc() {
      for (EntityCollection<? extends Entity> ec : class2Entities.values()) {
         ec.loadFromDisc();
      }
   }
}
