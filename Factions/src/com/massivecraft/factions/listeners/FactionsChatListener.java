package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import java.util.UnknownFormatConversionException;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FactionsChatListener implements Listener {
   public P p;

   public FactionsChatListener(P p) {
      this.p = p;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerEarlyChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled()) {
         Player talkingPlayer = event.getPlayer();
         String msg = event.getMessage();
         FPlayer me = FPlayers.i.get(talkingPlayer);
         ChatMode chat = me.getChatMode();
         if (chat != ChatMode.PUBLIC && this.p.handleCommand(talkingPlayer, msg, false, true)) {
            if (Conf.logPlayerCommands) {
               Bukkit.getLogger().log(Level.INFO, "[PLAYER_COMMAND] " + talkingPlayer.getName() + ": " + msg);
            }

            event.setCancelled(true);
         } else if (chat == ChatMode.FACTION) {
            Faction myFaction = me.getFaction();
            String message = String.format(Conf.factionChatFormat, me.describeTo(myFaction), msg);
            myFaction.sendMessage(message);
            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("FactionChat " + myFaction.getTag() + ": " + message));

            for (FPlayer fplayer : FPlayers.i.getOnline()) {
               if (fplayer.isSpyingChat() && fplayer.getFaction() != myFaction) {
                  fplayer.sendMessage("[FCspy] " + myFaction.getTag() + ": " + message);
               }
            }

            event.setCancelled(true);
         } else if (chat == ChatMode.ALLIANCE) {
            Faction myFaction = me.getFaction();
            String message = String.format(Conf.allianceChatFormat, ChatColor.stripColor(me.getNameAndTag()), msg);
            myFaction.sendMessage(message);

            for (FPlayer fplayerx : FPlayers.i.getOnline()) {
               if (myFaction.getRelationTo(fplayerx) == Relation.ALLY) {
                  fplayerx.sendMessage(message);
               } else if (fplayerx.isSpyingChat()) {
                  fplayerx.sendMessage("[ACspy]: " + message);
               }
            }

            Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor("AllianceChat: " + message));
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled()) {
         if (Conf.chatTagEnabled && !Conf.chatTagHandledByAnotherPlugin) {
            Player talkingPlayer = event.getPlayer();
            String msg = event.getMessage();
            String eventFormat = event.getFormat();
            FPlayer me = FPlayers.i.get(talkingPlayer);
            int InsertIndex = 0;
            if (!Conf.chatTagReplaceString.isEmpty() && eventFormat.contains(Conf.chatTagReplaceString)) {
               if (eventFormat.contains("[FACTION_TITLE]")) {
                  eventFormat = eventFormat.replace("[FACTION_TITLE]", me.getTitle());
               }

               InsertIndex = eventFormat.indexOf(Conf.chatTagReplaceString);
               eventFormat = eventFormat.replace(Conf.chatTagReplaceString, "");
               Conf.chatTagPadAfter = false;
               Conf.chatTagPadBefore = false;
            } else if (!Conf.chatTagInsertAfterString.isEmpty() && eventFormat.contains(Conf.chatTagInsertAfterString)) {
               InsertIndex = eventFormat.indexOf(Conf.chatTagInsertAfterString) + Conf.chatTagInsertAfterString.length();
            } else if (!Conf.chatTagInsertBeforeString.isEmpty() && eventFormat.contains(Conf.chatTagInsertBeforeString)) {
               InsertIndex = eventFormat.indexOf(Conf.chatTagInsertBeforeString);
            } else {
               InsertIndex = Conf.chatTagInsertIndex;
               if (InsertIndex > eventFormat.length()) {
                  return;
               }
            }

            String formatStart = eventFormat.substring(0, InsertIndex) + (Conf.chatTagPadBefore && !me.getChatTag().isEmpty() ? " " : "");
            String formatEnd = (Conf.chatTagPadAfter && !me.getChatTag().isEmpty() ? " " : "") + eventFormat.substring(InsertIndex);
            String nonColoredMsgFormat = formatStart + me.getChatTag().trim() + formatEnd;
            if (Conf.chatTagRelationColored) {
               event.setCancelled(true);

               for (Player listeningPlayer : event.getRecipients()) {
                  FPlayer you = FPlayers.i.get(listeningPlayer);
                  String yourFormat = formatStart + me.getChatTag(you).trim() + formatEnd;

                  try {
                     listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
                  } catch (UnknownFormatConversionException var15) {
                     Conf.chatTagInsertIndex = 0;
                     P.p.log(Level.SEVERE, "Critical error in chat message formatting!");
                     P.p.log(Level.SEVERE, "NOTE: This has been automatically fixed right now by setting chatTagInsertIndex to 0.");
                     P.p
                        .log(
                           Level.SEVERE,
                           "For a more proper fix, please read this regarding chat configuration: http://massivecraft.com/plugins/factions/config#Chat_configuration"
                        );
                     return;
                  }
               }

               String nonColoredMsg = ChatColor.stripColor(String.format(nonColoredMsgFormat, talkingPlayer.getDisplayName(), msg));
               Bukkit.getLogger().log(Level.INFO, nonColoredMsg);
            } else {
               event.setFormat(nonColoredMsgFormat);
            }
         }
      }
   }
}
