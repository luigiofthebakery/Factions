package com.massivecraft.factions.struct;

public enum NotificationType {
   UNSPECIFIED(0, "unspecified"),
   CHUNK_BOUNDARY_TRANSITION(0, "chunk_boundary_transition");

   public final int value;
   public final String name;

   NotificationType(int value, String name) {
      this.value = value;
      this.name = name;
   }
}
