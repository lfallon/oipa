package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.util.Optional;

public interface CleanDirectory {
   void clean();

   public static class Default implements CleanDirectory {
      private final Optional<File> directory;

      public Default( File directory ) {
         this( Optional.ofNullable( directory ) );
      }

      public Default( Optional<File> directory ) {
         this.directory = directory;
      }

      @Override
      public void clean() {
         if( directory.isPresent() ) {
            if( !directory.get().exists() ) {
               throw new IllegalArgumentException( String.format( "%s  does not exist", directory.get().getAbsolutePath() ) );
            }
            if( !directory.get().isDirectory() ) {
               throw new IllegalArgumentException( String.format( "%s is not a directory", directory.get().getAbsolutePath() ) );
            }

            final File[] files = directory.get().listFiles();
            if( files == null ) { // null if security restricted
               throw new RuntimeException( String.format( "Failed to list contents of %s", directory.get().getAbsolutePath() ) );
            }

            for( File file : files ) {
               new DeleteFile.Default( file ).delete();
            }
         }
      }

   }
}
