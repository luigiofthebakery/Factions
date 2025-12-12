package com.massivecraft.factions.zcore.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoadHack {
   private static URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();

   public static boolean load(String filename) {
      return load(new File(filename));
   }

   public static boolean load(File file) {
      try {
         return load(file.toURI().toURL());
      } catch (MalformedURLException var2) {
         return false;
      }
   }

   public static boolean load(URL url) {
      for (URL otherUrl : sysloader.getURLs()) {
         if (otherUrl.sameFile(url)) {
            return true;
         }
      }

      try {
         Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
         addURLMethod.setAccessible(true);
         addURLMethod.invoke(sysloader, url);
         return true;
      } catch (Exception var5) {
         return false;
      }
   }
}
