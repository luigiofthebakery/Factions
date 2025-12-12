package com.massivecraft.factions.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LazyLocation {
   private Location location = null;
   private String worldName;
   private double x;
   private double y;
   private double z;
   private float pitch;
   private float yaw;

   public LazyLocation(Location loc) {
      this.setLocation(loc);
   }

   public LazyLocation(String worldName, double x, double y, double z) {
      this(worldName, x, y, z, 0.0F, 0.0F);
   }

   public LazyLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
      this.worldName = worldName;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public final Location getLocation() {
      this.initLocation();
      return this.location;
   }

   public final void setLocation(Location loc) {
      this.location = loc;
      this.worldName = loc.getWorld().getName();
      this.x = loc.getX();
      this.y = loc.getY();
      this.z = loc.getZ();
      this.yaw = loc.getYaw();
      this.pitch = loc.getPitch();
   }

   private void initLocation() {
      if (this.location == null) {
         World world = Bukkit.getWorld(this.worldName);
         if (world != null) {
            this.location = new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
         }
      }
   }

   public final String getWorldName() {
      return this.worldName;
   }

   public final double getX() {
      return this.x;
   }

   public final double getY() {
      return this.y;
   }

   public final double getZ() {
      return this.z;
   }

   public final double getPitch() {
      return this.pitch;
   }

   public final double getYaw() {
      return this.yaw;
   }
}
