package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.struct.NotificationPosition;
import com.massivecraft.factions.struct.Permission;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;

import com.massivecraft.factions.struct.Role;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CmdConfig extends FCommand {
   private static HashMap<String, String> properFieldNames = new HashMap<>();

   public CmdConfig() {
      this.aliases.add("config");
      this.requiredArgs.add("setting");
      this.requiredArgs.add("value");
      this.errorOnToManyArgs = false;
      this.permission = Permission.CONFIG.node;
      this.disableOnLock = true;
      this.senderMustBePlayer = false;
      this.senderMustBeMember = false;
      this.senderMustBeModerator = false;
      this.senderMustBeAdmin = false;
   }

   @Override
   public void perform() {
      if (properFieldNames.isEmpty()) {
         Field[] fields = Conf.class.getDeclaredFields();

         for (int i = 0; i < fields.length; i++) {
            properFieldNames.put(fields[i].getName().toLowerCase(), fields[i].getName());
         }
      }

      String field = this.argAsString(0).toLowerCase();
      if (field.startsWith("\"") && field.endsWith("\"")) {
         field = field.substring(1, field.length() - 1);
      }

      String fieldName = properFieldNames.get(field);
      if (fieldName != null && !fieldName.isEmpty()) {
         String success = "";
         String value = this.args.get(1);

         for (int i = 2; i < this.args.size(); i++) {
            value = value + ' ' + this.args.get(i);
         }

         try {
            Field target = Conf.class.getField(fieldName);
            if (target.getType() == boolean.class) {
               boolean targetValue = this.strAsBool(value);
               target.setBoolean(null, targetValue);
               if (targetValue) {
                  success = "\"" + fieldName + "\" option set to true (enabled).";
               } else {
                  success = "\"" + fieldName + "\" option set to false (disabled).";
               }
            } else if (target.getType() == int.class) {
               try {
                  int intVal = Integer.parseInt(value);
                  target.setInt(null, intVal);
                  success = "\"" + fieldName + "\" option set to " + intVal + ".";
               } catch (NumberFormatException var14) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": integer (whole number) value required.");
                  return;
               }
            } else if (target.getType() == double.class) {
               try {
                  double doubleVal = Double.parseDouble(value);
                  target.setDouble(null, doubleVal);
                  success = "\"" + fieldName + "\" option set to " + doubleVal + ".";
               } catch (NumberFormatException var13) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": double (numeric) value required.");
                  return;
               }
            } else if (target.getType() == float.class) {
               try {
                  float floatVal = Float.parseFloat(value);
                  target.setFloat(null, floatVal);
                  success = "\"" + fieldName + "\" option set to " + floatVal + ".";
               } catch (NumberFormatException var12) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": float (numeric) value required.");
                  return;
               }
            } else if (target.getType() == String.class) {
               target.set(null, value);
               success = "\"" + fieldName + "\" option set to \"" + value + "\".";
            } else if (target.getType() == ChatColor.class) {
               ChatColor newColor = null;

               try {
                  newColor = ChatColor.valueOf(value.toUpperCase());
               } catch (IllegalArgumentException var11) {
               }

               if (newColor == null) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": \"" + value.toUpperCase() + "\" is not a valid color.");
                  return;
               }

               target.set(null, newColor);
               success = "\"" + fieldName + "\" color option set to \"" + value.toUpperCase() + "\".";
            } else if (target.getType() == NotificationPosition.class) {
               NotificationPosition newPosition = null;

               try {
                  newPosition = NotificationPosition.valueOf(value.toUpperCase());
               } catch (IllegalArgumentException e) {}

               if (newPosition == null) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": \"" + value.toUpperCase() + "\" is not a valid notification position.");
                  return;
               }

               target.set(null, newPosition);
               success = "\"" + fieldName + "\" notification position option set to \"" + value.toUpperCase() + "\".";
            } else if (target.getType() == Role.class) {
               Role newRole = null;

               try {
                  newRole = Role.valueOf(value.toUpperCase());
               } catch (IllegalArgumentException e) {}

               if (newRole == null) {
                  this.sendMessage("Cannot set \"" + fieldName + "\": \"" + value.toUpperCase() + "\" is not a valid notification position.");
                  return;
               }

               target.set(null, newRole);
               success = "\"" + fieldName + "\" role option set to \"" + value.toUpperCase() + "\".";
            } else {
               if (!(target.getGenericType() instanceof ParameterizedType)) {
                  this.sendMessage("\"" + fieldName + "\" is not a data type which can be modified with this command.");
                  return;
               }

               ParameterizedType targSet = (ParameterizedType)target.getGenericType();
               Type innerType = targSet.getActualTypeArguments()[0];
               if (targSet.getRawType() != Set.class) {
                  this.sendMessage("\"" + fieldName + "\" is not a data collection type which can be modified with this command.");
                  return;
               }

               if (innerType == Material.class) {
                  Material newMat = null;

                  try {
                     newMat = Material.valueOf(value.toUpperCase());
                  } catch (IllegalArgumentException var10) {
                  }

                  if (newMat == null) {
                     this.sendMessage("Cannot change \"" + fieldName + "\" set: \"" + value.toUpperCase() + "\" is not a valid material.");
                     return;
                  }

                  Set<Material> matSet = (Set<Material>)target.get(null);
                  if (matSet.contains(newMat)) {
                     matSet.remove(newMat);
                     target.set(null, matSet);
                     success = "\"" + fieldName + "\" set: Material \"" + value.toUpperCase() + "\" removed.";
                  } else {
                     matSet.add(newMat);
                     target.set(null, matSet);
                     success = "\"" + fieldName + "\" set: Material \"" + value.toUpperCase() + "\" added.";
                  }
               } else {
                  if (innerType != String.class) {
                     this.sendMessage("\"" + fieldName + "\" is not a data type set which can be modified with this command.");
                     return;
                  }

                  Set<String> stringSet = (Set<String>)target.get(null);
                  if (stringSet.contains(value)) {
                     stringSet.remove(value);
                     target.set(null, stringSet);
                     success = "\"" + fieldName + "\" set: \"" + value + "\" removed.";
                  } else {
                     stringSet.add(value);
                     target.set(null, stringSet);
                     success = "\"" + fieldName + "\" set: \"" + value + "\" added.";
                  }
               }
            }
         } catch (NoSuchFieldException var15) {
            this.sendMessage("Configuration setting \"" + fieldName + "\" couldn't be matched, though it should be... please report this error.");
            return;
         } catch (IllegalAccessException var16) {
            this.sendMessage("Error setting configuration setting \"" + fieldName + "\" to \"" + value + "\".");
            return;
         }

         if (!success.isEmpty()) {
            this.sendMessage(success);
            if (this.sender instanceof Player) {
               P.p.log(success + " Command was run by " + this.fme.getName() + ".");
            }
         }

         Conf.save();
         SpoutFeatures.updateAppearances();
      } else {
         this.msg("<b>No configuration setting \"<h>%s<b>\" exists.", new Object[]{field});
      }
   }
}
