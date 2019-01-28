package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public interface ExplodedArchive {

   File folder();

   void clean();

   public static class Folder implements ExplodedArchive {
      private final File file;

      public Folder( File file ) {
         this.file = file;
      }

      @Override
      public File folder() {
         return file;
      }

      @Override
      public void clean() {}
   }

   public static class Cached implements ExplodedArchive {
      private final transient ConcurrentHashMap<Long, File> cache = new ConcurrentHashMap<>();
      private final ExplodedArchive archive;

      public Cached( Archive archive ) {
         this.archive = archive;
      }

      @Override
      public File folder() {
         return cache.computeIfAbsent( 1L, ( key ) -> archive.folder() );
      }

      @Override
      public void clean() {
         archive.clean();
      }

   }

   public static class Archive implements ExplodedArchive {
      private final File file;
      private final ConcurrentHashMap<Long, File> cache = new ConcurrentHashMap<Long, File>();

      public Archive( File file ) {
         this.file = file;
      }

      private File get() {
         return cache.computeIfAbsent( 1L, ( key ) -> new Unzip.Temp( file ).unzip() );
      }

      @Override
      public File folder() {
         return get();
      }

      @Override
      public void clean() {
         new DeleteFile.Quietly( get() );
      }
   }

}
