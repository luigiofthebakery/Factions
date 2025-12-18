package com.massivecraft.factions.struct;

public enum NotificationPosition {
   DEFAULT(0, "default"),
   CHAT(1, "chat"),
   ACTIONBAR(2, "actionbar"),
   HIDDEN(3, "hidden");

   public final int value;
   public final String name;

   NotificationPosition(int value, String name) {
      this.value = value;
      this.name = name;
   }
}
