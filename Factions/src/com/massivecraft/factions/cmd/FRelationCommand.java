package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public abstract class FRelationCommand extends FCommand {
   public Relation targetRelation;

   public FRelationCommand() {
      this.requiredArgs.add("faction tag");
      this.permission = Permission.RELATION.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = true;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      boolean hasMinRole = true;
      if (this.targetRelation == Relation.NEUTRAL) {
         hasMinRole = this.assertMinRole(Conf.neutralMinRole);
      } else if (this.targetRelation == Relation.ALLY) {
         hasMinRole = this.assertMinRole(Conf.allyMinRole);
      } else if (this.targetRelation == Relation.ENEMY) {
         hasMinRole = this.assertMinRole(Conf.enemyMinRole);
      }

      if (!hasMinRole) {
         return;
      }

      Faction them = this.argAsFaction(0);
      if (them != null) {
         if (!them.isNormal()) {
            this.msg("<b>Nope! You can't.", new Object[0]);
         } else if (them == this.myFaction) {
            this.msg("<b>Nope! You can't declare a relation to yourself :)", new Object[0]);
         } else if (this.myFaction.getRelationWish(them) == this.targetRelation) {
            this.msg("<b>You already have that relation wish set with %s.", new Object[]{them.getTag()});
         } else if (this.payForCommand(this.targetRelation.getRelationCost(), "to change a relation wish", "for changing a relation wish")) {
            Relation oldRelation = this.myFaction.getRelationTo(them, true);
            this.myFaction.setRelationWish(them, this.targetRelation);
            Relation currentRelation = this.myFaction.getRelationTo(them, true);
            ChatColor currentRelationColor = currentRelation.getColor();
            if (this.targetRelation.value == currentRelation.value) {
               FactionRelationEvent relationEvent = new FactionRelationEvent(this.myFaction, them, oldRelation, currentRelation);
               Bukkit.getServer().getPluginManager().callEvent(relationEvent);
               them.msg(
                  "<i>Your faction is now "
                     + currentRelationColor
                     + this.targetRelation.toString()
                     + "<i> to "
                     + currentRelationColor
                     + this.myFaction.getTag()
               );
               this.myFaction
                  .msg("<i>Your faction is now " + currentRelationColor + this.targetRelation.toString() + "<i> to " + currentRelationColor + them.getTag());
            } else {
               them.msg(
                  currentRelationColor + this.myFaction.getTag() + "<i> wishes to be your " + this.targetRelation.getColor() + this.targetRelation.toString()
               );
               them.msg("<i>Type <c>/" + Conf.baseCommandAliases.get(0) + " " + this.targetRelation + " " + this.myFaction.getTag() + "<i> to accept.");
               this.myFaction
                  .msg(currentRelationColor + them.getTag() + "<i> were informed that you wish to be " + this.targetRelation.getColor() + this.targetRelation);
            }

            if (!this.targetRelation.isNeutral() && them.isPeaceful()) {
               them.msg("<i>This will have no effect while your faction is peaceful.");
               this.myFaction.msg("<i>This will have no effect while their faction is peaceful.");
            }

            if (!this.targetRelation.isNeutral() && this.myFaction.isPeaceful()) {
               them.msg("<i>This will have no effect while their faction is peaceful.");
               this.myFaction.msg("<i>This will have no effect while your faction is peaceful.");
            }

            SpoutFeatures.updateAppearances(this.myFaction, them);
            SpoutFeatures.updateTerritoryDisplayLoc(null);
         }
      }
   }
}
