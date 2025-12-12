package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.Relation;

public class CmdRelationAlly extends FRelationCommand {
   public CmdRelationAlly() {
      this.aliases.add("ally");
      this.targetRelation = Relation.ALLY;
   }
}
