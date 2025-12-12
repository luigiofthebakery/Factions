package com.massivecraft.factions.util;

import java.util.function.Consumer;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.P;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class AutoLeaveTask implements Consumer<ScheduledTask> {
   double rate = Conf.autoLeaveRoutineRunsEveryXMinutes;

   @Override
   public void accept( ScheduledTask t) {
      FPlayers.i.autoLeaveOnInactivityRoutine();
      if (this.rate != Conf.autoLeaveRoutineRunsEveryXMinutes) {
         P.p.startAutoLeaveTask(true);
      }
   }
}
