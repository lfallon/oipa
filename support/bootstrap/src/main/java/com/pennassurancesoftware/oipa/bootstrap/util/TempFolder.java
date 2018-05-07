package com.pennassurancesoftware.oipa.bootstrap.util;

import java.io.File;

public interface TempFolder {
   File create();

   public class Default implements TempFolder {
      private final String name;

      public Default() {
         this( "temp" );
      }

      public Default( String name ) {
         this.name = name;
      }

      @Override
      public File create() {
         try {
            final File temp = File.createTempFile( name, "" );
            temp.delete();
            temp.mkdir();
            return temp;
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to create temp dirctory: %s", name ), exception );
         }
      }

   }
}
