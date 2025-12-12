package com.massivecraft.factions.zcore.util;

import com.massivecraft.factions.zcore.MPlugin;
import java.io.File;

public class LibLoader {
   MPlugin p;

   public LibLoader(MPlugin p) {
      this.p = p;
      new File("./lib").mkdirs();
   }

   public boolean require(String filename, String url) {
      if (!this.include(filename, url)) {
         this.p.log("Failed to load the required library " + filename);
         this.p.suicide();
         return false;
      } else {
         return true;
      }
   }

   public boolean include(String filename, String url) {
      File file = getFile(filename);
      if (!file.exists()) {
         this.p.log("Downloading library " + filename);
         if (!DiscUtil.downloadUrl(url, file)) {
            this.p.log("Failed to download " + filename);
            return false;
         }
      }

      return ClassLoadHack.load(file);
   }

   private static File getFile(String filename) {
      return new File("./lib/" + filename);
   }
}
