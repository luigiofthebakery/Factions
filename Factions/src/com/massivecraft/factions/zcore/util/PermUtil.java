package com.massivecraft.factions.zcore.util;

import com.massivecraft.factions.zcore.MPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class PermUtil {
   public Map<String, String> permissionDescriptions = new HashMap<>();
   protected MPlugin p;

   public PermUtil(MPlugin p) {
      this.p = p;
      this.setup();
   }

   public String getForbiddenMessage(String perm) {
      return this.p.txt.parse("<b>You don't have permission to %s.", this.getPermissionDescription(perm));
   }

   public final void setup() {
      for (Permission permission : this.p.getDescription().getPermissions()) {
         this.permissionDescriptions.put(permission.getName(), permission.getDescription());
      }
   }

   public String getPermissionDescription(String perm) {
      String desc = this.permissionDescriptions.get(perm);
      return desc == null ? "do that" : desc;
   }

   public boolean has(CommandSender me, String perm) {
      if (me == null) {
         return false;
      } else {
         return !(me instanceof Player) ? me.hasPermission(perm) : me.hasPermission(perm);
      }
   }

   public boolean has(CommandSender me, String perm, boolean informSenderIfNot) {
      if (this.has(me, perm)) {
         return true;
      } else {
         if (informSenderIfNot && me != null) {
            me.sendMessage(this.getForbiddenMessage(perm));
         }

         return false;
      }
   }

   public <T> T pickFirstVal(CommandSender me, Map<String, T> perm2val) {
      if (perm2val == null) {
         return null;
      } else {
         T ret = null;

         for (Entry<String, T> entry : perm2val.entrySet()) {
            ret = entry.getValue();
            if (this.has(me, entry.getKey())) {
               break;
            }
         }

         return ret;
      }
   }
}
