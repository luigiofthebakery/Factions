package com.massivecraft.factions.zcore.persist;

import java.util.function.Consumer;

import com.massivecraft.factions.zcore.MPlugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class SaveTask implements Consumer<ScheduledTask> {
   MPlugin p;

   public SaveTask(MPlugin p) {
      this.p = p;
   }

   @Override
   public void accept(ScheduledTask t) {
      if (this.p.getAutoSave()) {
         this.p.preAutoSave();
         EM.saveAllToDisc();
         this.p.postAutoSave();
      }
   }
}
