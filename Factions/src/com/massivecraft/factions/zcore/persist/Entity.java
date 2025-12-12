package com.massivecraft.factions.zcore.persist;

public abstract class Entity {
   protected transient String id = null;

   public String getId() {
      return this.id;
   }

   protected void setId(String id) {
      this.id = id;
   }

   public boolean shouldBeSaved() {
      return true;
   }

   public void attach() {
      EM.attach(this);
   }

   public void detach() {
      EM.detach(this);
   }

   public boolean attached() {
      return EM.attached(this);
   }

   public boolean detached() {
      return EM.detached(this);
   }

   public void preDetach() {
   }

   public void postDetach() {
   }
}
