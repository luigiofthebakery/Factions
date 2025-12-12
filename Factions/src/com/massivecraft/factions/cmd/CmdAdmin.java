package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Bukkit;

public class CmdAdmin extends FCommand {
   public CmdAdmin() {
      this.aliases.add("admin");
      this.requiredArgs.add("player name");
      this.permission = Permission.ADMIN.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      FPlayer fyou = this.argAsBestFPlayerMatch(0);
      if (fyou != null) {
         boolean permAny = Permission.ADMIN_ANY.has(this.sender, false);
         Faction targetFaction = fyou.getFaction();
         if (targetFaction != this.myFaction && !permAny) {
            this.msg("%s<i> is not a member in your faction.", new Object[]{fyou.describeTo(this.fme, true)});
         } else if (this.fme != null && this.fme.getRole() != Role.ADMIN && !permAny) {
            this.msg("<b>You are not the faction admin.", new Object[0]);
         } else if (fyou == this.fme && !permAny) {
            this.msg("<b>The target player musn't be yourself.", new Object[0]);
         } else {
            if (fyou.getFaction() != targetFaction) {
               FPlayerJoinEvent event = new FPlayerJoinEvent(FPlayers.i.get(this.me), targetFaction, FPlayerJoinEvent.PlayerJoinReason.LEADER);
               Bukkit.getServer().getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  return;
               }
            }

            FPlayer admin = targetFaction.getFPlayerAdmin();
            if (fyou == admin) {
               targetFaction.promoteNewLeader();
               this.msg("<i>You have demoted %s<i> from the position of faction admin.", new Object[]{fyou.describeTo(this.fme, true)});
               fyou.msg(
                  "<i>You have been demoted from the position of faction admin by %s<i>.",
                  this.senderIsConsole ? "a server admin" : this.fme.describeTo(fyou, true)
               );
            } else {
               if (admin != null) {
                  admin.setRole(Role.MODERATOR);
               }

               fyou.setRole(Role.ADMIN);
               this.msg("<i>You have promoted %s<i> to the position of faction admin.", new Object[]{fyou.describeTo(this.fme, true)});

               for (FPlayer fplayer : FPlayers.i.getOnline()) {
                  fplayer.msg(
                     "%s<i> gave %s<i> the leadership of %s<i>.",
                     this.senderIsConsole ? "A server admin" : this.fme.describeTo(fplayer, true),
                     fyou.describeTo(fplayer),
                     targetFaction.describeTo(fplayer)
                  );
               }
            }
         }
      }
   }
}
