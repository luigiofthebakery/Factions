package com.massivecraft.factions.cmd;

import com.massivecraft.factions.struct.Relation;

public class CmdRelationEnemy extends FRelationCommand {
   public CmdRelationEnemy() {
      this.aliases.add("enemy");
      this.targetRelation = Relation.ENEMY;
   }
}
