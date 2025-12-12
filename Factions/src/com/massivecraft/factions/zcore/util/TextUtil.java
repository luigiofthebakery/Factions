package com.massivecraft.factions.zcore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TextUtil {
   public Map<String, String> tags = new HashMap<>();
   public static final transient Pattern patternTag = Pattern.compile("<([a-zA-Z0-9_]*)>");
   private static final String titleizeLine = repeat("_", 52);
   private static final int titleizeBalance = -1;
   public static final long millisPerSecond = 1000L;
   public static final long millisPerMinute = 60000L;
   public static final long millisPerHour = 3600000L;
   public static final long millisPerDay = 86400000L;
   public static final long millisPerWeek = 604800000L;
   public static final long millisPerMonth = 2678400000L;
   public static final long millisPerYear = 31536000000L;

   public String parse(String str, Object... args) {
      return String.format(this.parse(str), args);
   }

   public String parse(String str) {
      return this.parseTags(parseColor(str));
   }

   public String parseTags(String str) {
      return replaceTags(str, this.tags);
   }

   public static String replaceTags(String str, Map<String, String> tags) {
      StringBuffer ret = new StringBuffer();
      Matcher matcher = patternTag.matcher(str);

      while (matcher.find()) {
         String tag = matcher.group(1);
         String repl = tags.get(tag);
         if (repl == null) {
            matcher.appendReplacement(ret, "<" + tag + ">");
         } else {
            matcher.appendReplacement(ret, repl);
         }
      }

      matcher.appendTail(ret);
      return ret.toString();
   }

   public static String parseColor(String string) {
      string = parseColorAmp(string);
      string = parseColorAcc(string);
      return parseColorTags(string);
   }

   public static String parseColorAmp(String string) {
      string = string.replaceAll("(§([a-z0-9]))", "§$2");
      string = string.replaceAll("(&([a-z0-9]))", "§$2");
      return string.replace("&&", "&");
   }

   public static String parseColorAcc(String string) {
      return string.replace("`e", "")
         .replace("`r", ChatColor.RED.toString())
         .replace("`R", ChatColor.DARK_RED.toString())
         .replace("`y", ChatColor.YELLOW.toString())
         .replace("`Y", ChatColor.GOLD.toString())
         .replace("`g", ChatColor.GREEN.toString())
         .replace("`G", ChatColor.DARK_GREEN.toString())
         .replace("`a", ChatColor.AQUA.toString())
         .replace("`A", ChatColor.DARK_AQUA.toString())
         .replace("`b", ChatColor.BLUE.toString())
         .replace("`B", ChatColor.DARK_BLUE.toString())
         .replace("`p", ChatColor.LIGHT_PURPLE.toString())
         .replace("`P", ChatColor.DARK_PURPLE.toString())
         .replace("`k", ChatColor.BLACK.toString())
         .replace("`s", ChatColor.GRAY.toString())
         .replace("`S", ChatColor.DARK_GRAY.toString())
         .replace("`w", ChatColor.WHITE.toString());
   }

   public static String parseColorTags(String string) {
      return string.replace("<empty>", "")
         .replace("<black>", "§0")
         .replace("<navy>", "§1")
         .replace("<green>", "§2")
         .replace("<teal>", "§3")
         .replace("<red>", "§4")
         .replace("<purple>", "§5")
         .replace("<gold>", "§6")
         .replace("<silver>", "§7")
         .replace("<gray>", "§8")
         .replace("<blue>", "§9")
         .replace("<lime>", "§a")
         .replace("<aqua>", "§b")
         .replace("<rose>", "§c")
         .replace("<pink>", "§d")
         .replace("<yellow>", "§e")
         .replace("<white>", "§f");
   }

   public static String upperCaseFirst(String string) {
      return string.substring(0, 1).toUpperCase() + string.substring(1);
   }

   public static String implode(List<String> list, String glue) {
      StringBuilder ret = new StringBuilder();

      for (int i = 0; i < list.size(); i++) {
         if (i != 0) {
            ret.append(glue);
         }

         ret.append(list.get(i));
      }

      return ret.toString();
   }

   public static String repeat(String s, int times) {
      return times <= 0 ? "" : s + repeat(s, times - 1);
   }

   public static String getMaterialName(Material material) {
      return material.toString().replace('_', ' ').toLowerCase();
   }

   public String titleize(String str) {
      String center = ".[ " + this.parseTags("<l>") + str + this.parseTags("<a>") + " ].";
      int centerlen = ChatColor.stripColor(center).length();
      int pivot = titleizeLine.length() / 2;
      int eatLeft = centerlen / 2 + 1;
      int eatRight = centerlen - eatLeft - 1;
      return eatLeft < pivot
         ? this.parseTags("<a>") + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight)
         : this.parseTags("<a>") + center;
   }

   public ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title) {
      ArrayList<String> ret = new ArrayList<>();
      int pageZeroBased = pageHumanBased - 1;
      int pageheight = 9;
      int pagecount = lines.size() / 9 + 1;
      ret.add(this.titleize(title + " " + pageHumanBased + "/" + pagecount));
      if (pagecount == 0) {
         ret.add(this.parseTags("<i>Sorry. No Pages available."));
         return ret;
      } else if (pageZeroBased >= 0 && pageHumanBased <= pagecount) {
         int from = pageZeroBased * 9;
         int to = from + 9;
         if (to > lines.size()) {
            to = lines.size();
         }

         ret.addAll(lines.subList(from, to));
         return ret;
      } else {
         ret.add(this.parseTags("<i>Invalid page. Must be between 1 and " + pagecount));
         return ret;
      }
   }

   public static String getTimeDeltaDescriptionRelNow(long millis) {
      double absmillis = Math.abs(millis);
      String agofromnow = "from now";
      if (millis <= 0L) {
         agofromnow = "ago";
      }

      String unit;
      long num;
      if (absmillis < 3000.0) {
         unit = "milliseconds";
         num = (long)absmillis;
      } else if (absmillis < 180000.0) {
         unit = "seconds";
         num = (long)(absmillis / 1000.0);
      } else if (absmillis < 1.08E7) {
         unit = "minutes";
         num = (long)(absmillis / 60000.0);
      } else if (absmillis < 2.592E8) {
         unit = "hours";
         num = (long)(absmillis / 3600000.0);
      } else if (absmillis < 1.8144E9) {
         unit = "days";
         num = (long)(absmillis / 8.64E7);
      } else if (absmillis < 8.0352E9) {
         unit = "weeks";
         num = (long)(absmillis / 6.048E8);
      } else if (absmillis < 9.4608E10) {
         unit = "months";
         num = (long)(absmillis / 2.6784E9);
      } else {
         unit = "years";
         num = (long)(absmillis / 3.1536E10);
      }

      return num + " " + unit + " " + agofromnow;
   }

   public static String getBestStartWithCI(Collection<String> candidates, String start) {
      String ret = null;
      int best = 0;
      start = start.toLowerCase();
      int minlength = start.length();

      for (String candidate : candidates) {
         if (candidate.length() >= minlength && candidate.toLowerCase().startsWith(start)) {
            int lendiff = candidate.length() - minlength;
            if (lendiff == 0) {
               return candidate;
            }

            if (lendiff < best || best == 0) {
               best = lendiff;
               ret = candidate;
            }
         }
      }

      return ret;
   }
}
