package com.massivecraft.factions.iface;

public interface EconomyParticipator extends RelationParticipator {
   String getAccountId();

   void msg(String var1, Object... var2);
}
