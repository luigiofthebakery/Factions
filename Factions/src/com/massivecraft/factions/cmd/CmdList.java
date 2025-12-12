package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CmdList extends FCommand {
   public CmdList() {
      this.aliases.add("list");
      this.aliases.add("ls");
      this.optionalArgs.put("page", "1");
      this.permission = Permission.LIST.node;
      this.disableOnLock = false;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (this.payForCommand(Conf.econCostList, "to list the factions", "for listing the factions")) {
         ArrayList<Faction> factionList = new ArrayList<>(Factions.i.get());
         factionList.remove(Factions.i.getNone());
         factionList.remove(Factions.i.getSafeZone());
         factionList.remove(Factions.i.getWarZone());
         Collections.sort(factionList, new Comparator<Faction>() {
            public int compare(Faction f1, Faction f2) {
               int f1Size = f1.getFPlayers().size();
               int f2Size = f2.getFPlayers().size();
               if (f1Size < f2Size) {
                  return 1;
               } else {
                  return f1Size > f2Size ? -1 : 0;
               }
            }
         });
         Collections.sort(factionList, new Comparator<Faction>() {
            public int compare(Faction f1, Faction f2) {
               int f1Size = f1.getFPlayersWhereOnline(true).size();
               int f2Size = f2.getFPlayersWhereOnline(true).size();
               if (f1Size < f2Size) {
                  return 1;
               } else {
                  return f1Size > f2Size ? -1 : 0;
               }
            }
         });
         ArrayList<String> lines = new ArrayList<>();
         factionList.add(0, Factions.i.getNone());
         int pageheight = 9;
         int pagenumber = this.argAsInt(0, 1);
         int pagecount = factionList.size() / 9 + 1;
         if (pagenumber > pagecount) {
            pagenumber = pagecount;
         } else if (pagenumber < 1) {
            pagenumber = 1;
         }

         int start = (pagenumber - 1) * 9;
         int end = start + 9;
         if (end > factionList.size()) {
            end = factionList.size();
         }

         lines.add(this.p.txt.titleize("Faction List " + pagenumber + "/" + pagecount));

         for (Faction faction : factionList.subList(start, end)) {
            if (faction.isNone()) {
               lines.add(this.p.txt.parse("<i>Factionless<i> %d online", Factions.i.getNone().getFPlayersWhereOnline(true).size()));
            } else {
               lines.add(
                  this.p
                     .txt
                     .parse(
                        "%s<i> %d/%d online, %d/%d/%d",
                        faction.getTag(this.fme),
                        faction.getFPlayersWhereOnline(true).size(),
                        faction.getFPlayers().size(),
                        faction.getLandRounded(),
                        faction.getPowerRounded(),
                        faction.getPowerMaxRounded()
                     )
               );
            }
         }

         this.sendMessage(lines);
      }
   }
}
