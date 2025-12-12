package com.massivecraft.factions.zcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DiscUtil {
   private static final String UTF8 = "UTF-8";

   public static byte[] readBytes(File file) throws IOException {
      int length = (int)file.length();
      byte[] output = new byte[length];
      InputStream in = new FileInputStream(file);
      int offset = 0;

      while (offset < length) {
         offset += in.read(output, offset, length - offset);
      }

      in.close();
      return output;
   }

   public static void writeBytes(File file, byte[] bytes) throws IOException {
      FileOutputStream out = new FileOutputStream(file);
      out.write(bytes);
      out.close();
   }

   public static void write(File file, String content) throws IOException {
      writeBytes(file, utf8(content));
   }

   public static String read(File file) throws IOException {
      return utf8(readBytes(file));
   }

   public static boolean writeCatch(File file, String content) {
      try {
         write(file, content);
         return true;
      } catch (Exception var3) {
         return false;
      }
   }

   public static String readCatch(File file) {
      try {
         return read(file);
      } catch (IOException var2) {
         return null;
      }
   }

   public static boolean downloadUrl(String urlstring, File file) {
      try {
         URL url = new URL(urlstring);
         ReadableByteChannel rbc = Channels.newChannel(url.openStream());
         FileOutputStream fos = new FileOutputStream(file);
         fos.getChannel().transferFrom(rbc, 0L, 16777216L);
         return true;
      } catch (Exception var5) {
         var5.printStackTrace();
         return false;
      }
   }

   public static boolean downloadUrl(String urlstring, String filename) {
      return downloadUrl(urlstring, new File(filename));
   }

   public static boolean deleteRecursive(File path) throws FileNotFoundException {
      if (!path.exists()) {
         throw new FileNotFoundException(path.getAbsolutePath());
      } else {
         boolean ret = true;
         if (path.isDirectory()) {
            for (File f : path.listFiles()) {
               ret = ret && deleteRecursive(f);
            }
         }

         return ret && path.delete();
      }
   }

   public static byte[] utf8(String string) {
      try {
         return string.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static String utf8(byte[] bytes) {
      try {
         return new String(bytes, "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         var2.printStackTrace();
         return null;
      }
   }
}
