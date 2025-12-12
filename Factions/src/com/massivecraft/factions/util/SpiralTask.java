package com.massivecraft.factions.util;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.P;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class SpiralTask implements Runnable {
   private transient World world = null;
   private transient boolean readyToGo = false;
   private transient ScheduledTask taskID = null;
   private transient int limit = 0;
   private transient int x = 0;
   private transient int z = 0;
   private transient boolean isZLeg = false;
   private transient boolean isNeg = false;
   private transient int length = -1;
   private transient int current = 0;

   public SpiralTask(FLocation fLocation, int radius) {
      this.limit = (radius - 1) * 2;
      this.world = Bukkit.getWorld(fLocation.getWorldName());
      if (this.world == null) {
         P.p.log(Level.WARNING, "[SpiralTask] A valid world must be specified!");
         this.stop();
      } else {
         this.x = (int)fLocation.getX();
         this.z = (int)fLocation.getZ();
         this.readyToGo = true;
         this.setTaskID(Bukkit.getRegionScheduler().runAtFixedRate(P.p, world, this.x >> 4, this.z >> 4, (ScheduledTask t) -> this.run(), 2L, 2L));
      }
   }

   public abstract boolean work();

   public final FLocation currentFLocation() {
      return new FLocation(this.world.getName(), this.x, this.z);
   }

   public final Location currentLocation() {
      return new Location(this.world, FLocation.chunkToBlock(this.x), 65.0, FLocation.chunkToBlock(this.z));
   }

   public final int getX() {
      return this.x;
   }

   public final int getZ() {
      return this.z;
   }

   public final void setTaskID(ScheduledTask ID) {
      if (ID == null) {
         this.stop();
      }

      this.taskID = ID;
   }

   @Override
   public final void run() {
      if (this.valid() && this.readyToGo) {
         this.readyToGo = false;
         if (this.insideRadius()) {
            long loopStartTime = now();

            while (now() < loopStartTime + 20L) {
               if (!this.work()) {
                  this.finish();
                  return;
               }

               if (!this.moveToNext()) {
                  return;
               }
            }

            this.readyToGo = true;
         }
      }
   }

   public final boolean moveToNext() {
      if (!this.valid()) {
         return false;
      } else {
         if (this.current < this.length) {
            this.current++;
            if (!this.insideRadius()) {
               return false;
            }
         } else {
            this.current = 0;
            this.isZLeg ^= true;
            if (this.isZLeg) {
               this.isNeg ^= true;
               this.length++;
            }
         }

         if (this.isZLeg) {
            this.z = this.z + (this.isNeg ? -1 : 1);
         } else {
            this.x = this.x + (this.isNeg ? -1 : 1);
         }

         return true;
      }
   }

   public final boolean insideRadius() {
      boolean inside = this.current < this.limit;
      if (!inside) {
         this.finish();
      }

      return inside;
   }

   public void finish() {
      this.stop();
   }

   public final void stop() {
      if (this.valid()) {
         this.readyToGo = false;
         this.taskID.cancel();
         this.taskID = null;
      }
   }

   public final boolean valid() {
      return this.taskID != null;
   }

   private static long now() {
      return System.currentTimeMillis();
   }
}
