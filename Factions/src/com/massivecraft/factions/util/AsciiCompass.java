package com.massivecraft.factions.util;

import java.util.ArrayList;
import org.bukkit.ChatColor;

public class AsciiCompass {
   public static AsciiCompass.Point getCompassPointForDirection(double inDegrees) {
      double degrees = (inDegrees - 180.0) % 360.0;
      if (degrees < 0.0) {
         degrees += 360.0;
      }

      if (0.0 <= degrees && degrees < 22.5) {
         return AsciiCompass.Point.N;
      } else if (22.5 <= degrees && degrees < 67.5) {
         return AsciiCompass.Point.NE;
      } else if (67.5 <= degrees && degrees < 112.5) {
         return AsciiCompass.Point.E;
      } else if (112.5 <= degrees && degrees < 157.5) {
         return AsciiCompass.Point.SE;
      } else if (157.5 <= degrees && degrees < 202.5) {
         return AsciiCompass.Point.S;
      } else if (202.5 <= degrees && degrees < 247.5) {
         return AsciiCompass.Point.SW;
      } else if (247.5 <= degrees && degrees < 292.5) {
         return AsciiCompass.Point.W;
      } else if (292.5 <= degrees && degrees < 337.5) {
         return AsciiCompass.Point.NW;
      } else {
         return 337.5 <= degrees && degrees < 360.0 ? AsciiCompass.Point.N : null;
      }
   }

   public static ArrayList<String> getAsciiCompass(AsciiCompass.Point point, ChatColor colorActive, String colorDefault) {
      ArrayList<String> ret = new ArrayList<>();
      String row = "";
      row = row + AsciiCompass.Point.NW.toString(AsciiCompass.Point.NW == point, colorActive, colorDefault);
      row = row + AsciiCompass.Point.N.toString(AsciiCompass.Point.N == point, colorActive, colorDefault);
      row = row + AsciiCompass.Point.NE.toString(AsciiCompass.Point.NE == point, colorActive, colorDefault);
      ret.add(row);
      row = "";
      row = row + AsciiCompass.Point.W.toString(AsciiCompass.Point.W == point, colorActive, colorDefault);
      row = row + colorDefault + "+";
      row = row + AsciiCompass.Point.E.toString(AsciiCompass.Point.E == point, colorActive, colorDefault);
      ret.add(row);
      row = "";
      row = row + AsciiCompass.Point.SW.toString(AsciiCompass.Point.SW == point, colorActive, colorDefault);
      row = row + AsciiCompass.Point.S.toString(AsciiCompass.Point.S == point, colorActive, colorDefault);
      row = row + AsciiCompass.Point.SE.toString(AsciiCompass.Point.SE == point, colorActive, colorDefault);
      ret.add(row);
      return ret;
   }

   public static ArrayList<String> getAsciiCompass(double inDegrees, ChatColor colorActive, String colorDefault) {
      return getAsciiCompass(getCompassPointForDirection(inDegrees), colorActive, colorDefault);
   }

   public static enum Point {
      N('N'),
      NE('/'),
      E('E'),
      SE('\\'),
      S('S'),
      SW('/'),
      W('W'),
      NW('\\');

      public final char asciiChar;

      private Point(char asciiChar) {
         this.asciiChar = asciiChar;
      }

      @Override
      public String toString() {
         return String.valueOf(this.asciiChar);
      }

      public String toString(boolean isActive, ChatColor colorActive, String colorDefault) {
         return (isActive ? colorActive : colorDefault) + String.valueOf(this.asciiChar);
      }
   }
}
