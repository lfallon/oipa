package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.util.Optional;


public interface DeleteFile {
   boolean delete();

   public class Default implements DeleteFile {
      private final Optional<File> file;

      public Default( File file ) {
         this( Optional.ofNullable( file ) );
      }

      public Default( Optional<File> file ) {
         this.file = file;
      }

      @Override
      public boolean delete() {
         boolean result = true;
         if( file.isPresent() ) {
            if( file.get().isDirectory() ) {
               new CleanDirectory.Default( file ).clean();
            }
            result = file.get().delete();
         }
         else {
            result = false;
         }
         return result;
      }
   }

   public class Quietly implements DeleteFile {
      private final Optional<File> file;

      public Quietly( File file ) {
         this( Optional.ofNullable( file ) );
      }

      public Quietly( Optional<File> file ) {
         this.file = file;
      }

      @Override
      public boolean delete() {
         boolean result = true;
         if( file.isPresent() ) {
            if( file.get().isDirectory() ) {
               try {
                  new CleanDirectory.Default( file ).clean();
               }
               catch( Exception ignored ) {}
            }
            try {
               result = file.get().delete();
            }
            catch( Exception ignored ) {
               result = false;
            }
         }
         else {
            result = false;
         }
         return result;
      }

   }
}
