package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.P;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.MCommand;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class FCommand extends MCommand<P> {
   public boolean disableOnLock = true;
   public FPlayer fme;
   public Faction myFaction;
   public boolean senderMustBeMember;
   public boolean senderMustBeModerator;
   public boolean senderMustBeAdmin;
   public boolean isMoneyCommand = false;

   public FCommand() {
      super(P.p);
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void execute(CommandSender sender, List<String> args, List<MCommand<?>> commandChain) {
      if (sender instanceof Player) {
         this.fme = FPlayers.i.get((Player)sender);
         this.myFaction = this.fme.getFaction();
      } else {
         this.fme = null;
         this.myFaction = null;
      }

      super.execute(sender, args, commandChain);
   }

   public void prepare(CommandSender sender, List<String> args, List<MCommand<?>> commandChain) {
      if (sender instanceof Player) {
         this.fme = FPlayers.i.get((Player)sender);
         this.myFaction = this.fme.getFaction();
      } else {
         this.fme = null;
         this.myFaction = null;
      }

      super.prepare(sender, args, commandChain);
   }

   @Override
   public boolean isEnabled() {
      if (this.p.getLocked() && this.disableOnLock) {
         this.msg("<b>Factions was locked by an admin. Please try again later.", new Object[0]);
         return false;
      } else if (this.isMoneyCommand && !Conf.econEnabled) {
         this.msg("<b>Faction economy features are disabled on this server.", new Object[0]);
         return false;
      } else if (this.isMoneyCommand && !Conf.bankEnabled) {
         this.msg("<b>The faction bank system is disabled on this server.", new Object[0]);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean validSenderType(CommandSender sender, boolean informSenderIfNot) {
      boolean superValid = super.validSenderType(sender, informSenderIfNot);
      if (!superValid) {
         return false;
      } else if (!this.senderMustBeMember && !this.senderMustBeModerator && !this.senderMustBeAdmin) {
         return true;
      } else if (!(sender instanceof Player)) {
         return false;
      } else {
         FPlayer fplayer = FPlayers.i.get((Player)sender);
         if (!fplayer.hasFaction()) {
            sender.sendMessage(this.p.txt.parse("<b>You are not member of any faction."));
            return false;
         } else if (this.senderMustBeModerator && !fplayer.getRole().isAtLeast(Role.MODERATOR)) {
            sender.sendMessage(this.p.txt.parse("<b>Only faction moderators can %s.", this.getHelpShort()));
            return false;
         } else if (this.senderMustBeAdmin && !fplayer.getRole().isAtLeast(Role.ADMIN)) {
            sender.sendMessage(this.p.txt.parse("<b>Only faction admins can %s.", this.getHelpShort()));
            return false;
         } else {
            return true;
         }
      }
   }

   public boolean assertHasFaction() {
      if (this.me == null) {
         return true;
      } else if (!this.fme.hasFaction()) {
         this.sendMessage("You are not member of any faction.");
         return false;
      } else {
         return true;
      }
   }

   public boolean assertMinRole(Role role) {
      if (this.me == null) {
         return true;
      } else if (this.fme.getRole().value < role.value) {
         this.msg("<b>You must be <h>" + role + "<b> to " + this.getHelpShort() + ".", new Object[0]);
         return false;
      } else {
         return true;
      }
   }

   public FPlayer strAsFPlayer(String name, FPlayer def, boolean msg) {
      FPlayer ret = def;
      if (name != null) {
         FPlayer fplayer = FPlayers.i.get(name);
         if (fplayer != null) {
            ret = fplayer;
         }
      }

      if (msg && ret == null) {
         this.msg("<b>No player \"<p>%s<b>\" could be found.", new Object[]{name});
      }

      return ret;
   }

   public FPlayer argAsFPlayer(int idx, FPlayer def, boolean msg) {
      return this.strAsFPlayer(this.argAsString(idx), def, msg);
   }

   public FPlayer argAsFPlayer(int idx, FPlayer def) {
      return this.argAsFPlayer(idx, def, true);
   }

   public FPlayer argAsFPlayer(int idx) {
      return this.argAsFPlayer(idx, null);
   }

   public FPlayer strAsBestFPlayerMatch(String name, FPlayer def, boolean msg) {
      FPlayer ret = def;
      if (name != null) {
         FPlayer fplayer = FPlayers.i.getBestIdMatch(name);
         if (fplayer != null) {
            ret = fplayer;
         }
      }

      if (msg && ret == null) {
         this.msg("<b>No player match found for \"<p>%s<b>\".", new Object[]{name});
      }

      return ret;
   }

   public FPlayer argAsBestFPlayerMatch(int idx, FPlayer def, boolean msg) {
      return this.strAsBestFPlayerMatch(this.argAsString(idx), def, msg);
   }

   public FPlayer argAsBestFPlayerMatch(int idx, FPlayer def) {
      return this.argAsBestFPlayerMatch(idx, def, true);
   }

   public FPlayer argAsBestFPlayerMatch(int idx) {
      return this.argAsBestFPlayerMatch(idx, null);
   }

   public Faction strAsFaction(String name, Faction def, boolean msg) {
      Faction ret = def;
      if (name != null) {
         Faction faction = null;
         if (faction == null) {
            faction = Factions.i.getByTag(name);
         }

         if (faction == null) {
            faction = Factions.i.getBestTagMatch(name);
         }

         if (faction == null) {
            FPlayer fplayer = FPlayers.i.getBestIdMatch(name);
            if (fplayer != null) {
               faction = fplayer.getFaction();
            }
         }

         if (faction != null) {
            ret = faction;
         }
      }

      if (msg && ret == null) {
         this.msg("<b>The faction or player \"<p>%s<b>\" could not be found.", new Object[]{name});
      }

      return ret;
   }

   public Faction argAsFaction(int idx, Faction def, boolean msg) {
      return this.strAsFaction(this.argAsString(idx), def, msg);
   }

   public Faction argAsFaction(int idx, Faction def) {
      return this.argAsFaction(idx, def, true);
   }

   public Faction argAsFaction(int idx) {
      return this.argAsFaction(idx, null);
   }

   public boolean canIAdministerYou(FPlayer i, FPlayer you) {
      if (!i.getFaction().equals(you.getFaction())) {
         i.sendMessage(this.p.txt.parse("%s <b>is not in the same faction as you.", you.describeTo(i, true)));
         return false;
      } else if (i.getRole().value <= you.getRole().value && !i.getRole().equals(Role.ADMIN)) {
         if (you.getRole().equals(Role.ADMIN)) {
            i.sendMessage(this.p.txt.parse("<b>Only the faction admin can do that."));
         } else if (i.getRole().equals(Role.MODERATOR)) {
            if (i == you) {
               return true;
            }

            i.sendMessage(this.p.txt.parse("<b>Moderators can't control each other..."));
         } else {
            i.sendMessage(this.p.txt.parse("<b>You must be a faction moderator to do that."));
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean payForCommand(double cost, String toDoThis, String forDoingThis) {
      if (!Econ.shouldBeUsed() || this.fme == null || cost == 0.0 || this.fme.isAdminBypassing()) {
         return true;
      } else {
         return Conf.bankEnabled && Conf.bankFactionPaysCosts && this.fme.hasFaction()
            ? Econ.modifyMoney(this.myFaction, -cost, toDoThis, forDoingThis)
            : Econ.modifyMoney(this.fme, -cost, toDoThis, forDoingThis);
      }
   }

   public boolean canAffordCommand(double cost, String toDoThis) {
      if (!Econ.shouldBeUsed() || this.fme == null || cost == 0.0 || this.fme.isAdminBypassing()) {
         return true;
      } else {
         return Conf.bankEnabled && Conf.bankFactionPaysCosts && this.fme.hasFaction()
            ? Econ.hasAtLeast(this.myFaction, cost, toDoThis)
            : Econ.hasAtLeast(this.fme, cost, toDoThis);
      }
   }
}
