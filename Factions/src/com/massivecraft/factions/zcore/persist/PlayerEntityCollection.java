package com.massivecraft.factions.zcore.persist;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class PlayerEntityCollection<E extends Entity> extends EntityCollection<E> {
   public PlayerEntityCollection(Class<E> entityClass, Collection<E> entities, Map<String, E> id2entity, File file, com.google.gson.Gson gson) {
      super(entityClass, entities, id2entity, file, gson, true);
   }

   public E get(Player player) {
      return this.get(player.getName());
   }

   public Set<E> getOnline() {
      Set<E> entities = new HashSet<>();

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         entities.add(this.get(player));
      }

      return entities;
   }
}
