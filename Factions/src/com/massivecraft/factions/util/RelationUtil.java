package com.massivecraft.factions.util;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.util.TextUtil;
import org.bukkit.ChatColor;

public class RelationUtil {
   public static String describeThatToMe(RelationParticipator that, RelationParticipator me, boolean ucfirst) {
      String ret = "";
      Faction thatFaction = getFaction(that);
      if (thatFaction == null) {
         return "ERROR";
      } else {
         Faction myFaction = getFaction(me);
         if (that instanceof Faction) {
            if (me instanceof FPlayer && myFaction == thatFaction) {
               ret = "your faction";
            } else {
               ret = thatFaction.getTag();
            }
         } else if (that instanceof FPlayer) {
            FPlayer fplayerthat = (FPlayer)that;
            if (that == me) {
               ret = "you";
            } else if (thatFaction == myFaction) {
               ret = fplayerthat.getNameAndTitle();
            } else {
               ret = fplayerthat.getNameAndTag();
            }
         }

         if (ucfirst) {
            ret = TextUtil.upperCaseFirst(ret);
         }

         return "" + getColorOfThatToMe(that, me) + ret;
      }
   }

   public static String describeThatToMe(RelationParticipator that, RelationParticipator me) {
      return describeThatToMe(that, me, false);
   }

   public static Relation getRelationTo(RelationParticipator me, RelationParticipator that) {
      return getRelationTo(that, me, false);
   }

   public static Relation getRelationTo(RelationParticipator me, RelationParticipator that, boolean ignorePeaceful) {
      Faction fthat = getFaction(that);
      if (fthat == null) {
         return Relation.NEUTRAL;
      } else {
         Faction fme = getFaction(me);
         if (fme == null) {
            return Relation.NEUTRAL;
         } else if (!fthat.isNormal() || !fme.isNormal()) {
            return Relation.NEUTRAL;
         } else if (fthat.equals(fme)) {
            return Relation.MEMBER;
         } else if (ignorePeaceful || !fme.isPeaceful() && !fthat.isPeaceful()) {
            return fme.getRelationWish(fthat).value >= fthat.getRelationWish(fme).value ? fthat.getRelationWish(fme) : fme.getRelationWish(fthat);
         } else {
            return Relation.NEUTRAL;
         }
      }
   }

   public static FPlayer getPlayer(RelationParticipator rp)
   {
      if (rp instanceof FPlayer) {
         return (FPlayer) rp;
      }

      return null;
   }

   public static Faction getFaction(RelationParticipator rp) {
      if (rp instanceof Faction) {
         return (Faction)rp;
      } else {
         return rp instanceof FPlayer ? ((FPlayer)rp).getFaction() : null;
      }
   }

   public static ChatColor getColorOfThatToMe(RelationParticipator that, RelationParticipator me) {
      Faction thatFaction = getFaction(that);
      Faction thisFaction = getFaction(me);

      FPlayer thatPlayer = getPlayer(that);
      FPlayer thisPlayer = getPlayer(me);

      Relation relation = getRelationTo(that, me);

      if (thisPlayer != null && thisPlayer.equals(thatPlayer)) {
         if (thisFaction.isNone()) {
            return Conf.colorNeutral;
         } else {
            return Conf.colorMember;
         }
      }

      if (!relation.equals(Relation.MEMBER)) {
         if (Conf.trustEnabled) {
            boolean isTrustedToFaction = thatPlayer == null && thisPlayer != null && thatFaction != null && thatFaction.trustsPlayer(thisPlayer);
            boolean isFactionTrustingPlayer = thisFaction != null && thatPlayer != null && thisFaction.trustsPlayer(thatPlayer);

            if (isTrustedToFaction || isFactionTrustingPlayer) {
               return Conf.colorTrusted;
            }
         }
      }

      if (thatFaction != null) {
         if (thatFaction.isPeaceful() && thatFaction != getFaction(me)) {
            return Conf.colorPeaceful;
         }

         if (thatFaction.isSafeZone() && thatFaction != getFaction(me)) {
            return Conf.colorPeaceful;
         }

         if (thatFaction.isWarZone() && thatFaction != getFaction(me)) {
            return Conf.colorWar;
         }
      }

      return getRelationTo(that, me).getColor();
   }
}
