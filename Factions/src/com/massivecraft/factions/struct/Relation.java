package com.massivecraft.factions.struct;

import com.massivecraft.factions.Conf;
import org.bukkit.ChatColor;

public enum Relation {
   MEMBER(3, "member"),
   ALLY(2, "ally"),
   NEUTRAL(1, "neutral"),
   ENEMY(0, "enemy");

   public final int value;
   public final String nicename;

   private Relation(int value, String nicename) {
      this.value = value;
      this.nicename = nicename;
   }

   @Override
   public String toString() {
      return this.nicename;
   }

   public boolean isMember() {
      return this == MEMBER;
   }

   public boolean isAlly() {
      return this == ALLY;
   }

   public boolean isNeutral() {
      return this == NEUTRAL;
   }

   public boolean isEnemy() {
      return this == ENEMY;
   }

   public boolean isAtLeast(Relation relation) {
      return this.value >= relation.value;
   }

   public boolean isAtMost(Relation relation) {
      return this.value <= relation.value;
   }

   public ChatColor getColor() {
      if (this == MEMBER) {
         return Conf.colorMember;
      } else if (this == ALLY) {
         return Conf.colorAlly;
      } else {
         return this == NEUTRAL ? Conf.colorNeutral : Conf.colorEnemy;
      }
   }

   public boolean confDenyBuild(boolean online) {
      if (this.isMember()) {
         return false;
      } else if (online) {
         if (this.isEnemy()) {
            return Conf.territoryEnemyDenyBuild;
         } else {
            return this.isAlly() ? Conf.territoryAllyDenyBuild : Conf.territoryDenyBuild;
         }
      } else if (this.isEnemy()) {
         return Conf.territoryEnemyDenyBuildWhenOffline;
      } else {
         return this.isAlly() ? Conf.territoryAllyDenyBuildWhenOffline : Conf.territoryDenyBuildWhenOffline;
      }
   }

   public boolean confPainBuild(boolean online) {
      if (this.isMember()) {
         return false;
      } else if (online) {
         if (this.isEnemy()) {
            return Conf.territoryEnemyPainBuild;
         } else {
            return this.isAlly() ? Conf.territoryAllyPainBuild : Conf.territoryPainBuild;
         }
      } else if (this.isEnemy()) {
         return Conf.territoryEnemyPainBuildWhenOffline;
      } else {
         return this.isAlly() ? Conf.territoryAllyPainBuildWhenOffline : Conf.territoryPainBuildWhenOffline;
      }
   }

   public boolean confDenyUseage() {
      if (this.isMember()) {
         return false;
      } else if (this.isEnemy()) {
         return Conf.territoryEnemyDenyUseage;
      } else {
         return this.isAlly() ? Conf.territoryAllyDenyUseage : Conf.territoryDenyUseage;
      }
   }

   public double getRelationCost() {
      if (this.isEnemy()) {
         return Conf.econCostEnemy;
      } else {
         return this.isAlly() ? Conf.econCostAlly : Conf.econCostNeutral;
      }
   }
}
