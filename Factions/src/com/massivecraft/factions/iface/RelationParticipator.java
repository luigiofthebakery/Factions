package com.massivecraft.factions.iface;

import com.massivecraft.factions.struct.Relation;
import org.bukkit.ChatColor;

public interface RelationParticipator {
   String describeTo(RelationParticipator var1);

   String describeTo(RelationParticipator var1, boolean var2);

   Relation getRelationTo(RelationParticipator var1);

   Relation getRelationTo(RelationParticipator var1, boolean var2);

   ChatColor getColorTo(RelationParticipator var1);
}
