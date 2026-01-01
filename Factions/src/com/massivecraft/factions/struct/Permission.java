package com.massivecraft.factions.struct;

import com.massivecraft.factions.P;
import org.bukkit.command.CommandSender;

public enum Permission {
   MANAGE_SAFE_ZONE("managesafezone"),
   MANAGE_WAR_ZONE("managewarzone"),
   SAFEUNCLAIMALL("safeunclaimall"),
   WARUNCLAIMALL("warunclaimall"),
   OWNERSHIP_BYPASS("ownershipbypass"),
   ADMIN("admin"),
   ADMIN_ANY("admin.any"),
   BYPASS("bypass"),
   CHAT("chat"),
   CHATSPY("chatspy"),
   CLEANUP("cleanup"),
   CLAIM("claim"),
   CONFIG("config"),
   CREATE("create"),
   DEINVITE("deinvite"),
   DESCRIPTION("description"),
   DISBAND("disband"),
   DISBAND_ANY("disband.any"),
   HELP("help"),
   HOME("home"),
   INVITE("invite"),
   JOIN("join"),
   JOIN_ANY("join.any"),
   JOIN_OTHERS("join.others"),
   KICK("kick"),
   KICK_ANY("kick.any"),
   LEAVE("leave"),
   LIST("list"),
   LOCK("lock"),
   MAP("map"),
   MOD("mod"),
   MOD_ANY("mod.any"),
   MONEY_BALANCE("money.balance"),
   MONEY_BALANCE_ANY("money.balance.any"),
   MONEY_DEPOSIT("money.deposit"),
   MONEY_WITHDRAW("money.withdraw"),
   MONEY_WITHDRAW_ANY("money.withdraw.any"),
   MONEY_F2F("money.f2f"),
   MONEY_F2P("money.f2p"),
   MONEY_P2F("money.p2f"),
   NO_BOOM("noboom"),
   OPEN("open"),
   OWNER("owner"),
   OWNER_ADD("owner.add"),
   OWNER_REMOVE("owner.remove"),
   OWNER_LIST("owner.list"),
   OWNER_CLEAR("owner.clear"),
   SET_PEACEFUL("setpeaceful"),
   SET_PERMANENT("setpermanent"),
   SET_PERMANENTPOWER("setpermanentpower"),
   POWERBOOST("powerboost"),
   POWER("power"),
   POWER_ANY("power.any"),
   RELATION("relation"),
   RELOAD("reload"),
   SAVE("save"),
   SETHOME("sethome"),
   SETHOME_ANY("sethome.any"),
   SHOW("show"),
   TAG("tag"),
   TITLE("title"),
   UNCLAIM("unclaim"),
   UNCLAIM_ALL("unclaimall"),
   VERSION("version"),
   TRUST("trust"),
   UNTRUST("untrust"),
   AUTO("auto"),
   CLAIM_AUTO("auto.claim"),
   UNCLAIM_AUTO("auto.unclaim"),
   OWNER_AUTO("auto.owner"),
   OWNER_ADD_AUTO("auto.owner.add"),
   OWNER_REMOVE_AUTO("auto.owner.remove"),
   OWNER_LIST_AUTO("auto.owner.list"),
   OWNER_CLEAR_AUTO("auto.owner.clear");

   public final String node;

   private Permission(String node) {
      this.node = "factions." + node;
   }

   public boolean has(CommandSender sender, boolean informSenderIfNot) {
      return P.p.perm.has(sender, this.node, informSenderIfNot);
   }

   public boolean has(CommandSender sender) {
      return this.has(sender, false);
   }
}
