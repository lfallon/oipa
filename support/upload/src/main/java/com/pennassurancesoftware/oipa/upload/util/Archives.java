package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import com.jcabi.immutable.Array;

public abstract class Archives {
   public static abstract class Support {
      public static Temp temp( File source ) {
         return new Temp( source );
      }

      public static class Temp {
         private final File source;

         public Temp( File source ) {
            this.source = source;
         }

         private String prefix() {
            return FilenameUtils.getBaseName( source.getName() );
         }

         private String suffix() {
            return String.format( ".%s", FilenameUtils.getExtension( source.getName() ) );
         }

         public File create() {
            try {
               return File.createTempFile( prefix(), suffix() );
            }
            catch( Throwable exception ) {
               throw new RuntimeException( "Failed to create temp file", exception );
            }
         }
      }
   }

   public static Default of( File file ) {
      return new Default( file );
   }

   public static class Default {
      private final File file;

      public Default( File file ) {
         this.file = file;
      }

      public Transformer transformer() {
         return new Transformer( file );
      }

      public Ls ls() {
         return new Ls( file );
      }

      public File file() {
         return file;
      }
   }

   public static class Ls {
      private final File file;
      private final Array<Predicate<String>> filters;

      public Ls( File file ) {
         this( file, new Array<>() );
      }

      public Ls( File file, Iterable<Predicate<String>> filters ) {
         this.file = file;
         this.filters = new Array<>( filters );
      }

      public Ls filterBy( Predicate<String> filter ) {
         return new Ls( file, filters.with( filter ) );
      }

      private boolean included( ArchiveEntry entry ) {
         return filters.stream().allMatch( p -> p.test( entry.getName() ) );
      }

      public Stream<String> entries() {
         try {
            final List<String> result = new ArrayList<>();
            final FileInputStream fis = new FileInputStream( file );
            final ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( ArchiveStreamFactory.ZIP, fis );
            try {
               ArchiveEntry entry = input.getNextEntry();
               while( entry != null ) {
                  if( included( entry ) ) {
                     result.add( entry.getName() );
                  }
                  entry = input.getNextEntry();
               }
            }
            finally {
               input.close();
            }
            fis.close();

            Collections.sort( result );
            return result.stream();
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to read archived file: %s", file.getAbsolutePath() ), exception );
         }
      }

      @Override
      public String toString() {
         return entries().collect( Collectors.joining( "\n" ) );
      }
   }

   public static class Transformer {
      private final File source;
      private final File dest;
      private final Array<Predicate<String>> filters;

      public Transformer( File source ) {
         this( source, Support.temp( source ).create(), new Array<>() );
      }

      public Transformer( File source, File dest, Iterable<Predicate<String>> filters ) {
         this.source = source;
         this.dest = dest;
         this.filters = new Array<>( filters );
      }

      public Transformer withDest( File dest ) {
         return new Transformer( source, dest, filters );
      }

      public Transformer filterBy( Predicate<String> filter ) {
         return new Transformer( source, dest, filters.with( filter ) );
      }

      private File dest() {
         return dest;
      }

      public Archives.Default transform() {
         try {
            final OutputStream os = new FileOutputStream( dest() );
            final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream( ArchiveStreamFactory.ZIP, os );
            copy( aos );
            aos.finish();
            os.close();
            return Archives.of( dest() );
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to create zip: %s", dest().getAbsolutePath() ), exception );
         }
      }

      private void copy( ArchiveOutputStream aos ) {
         try {
            final FileInputStream fis = new FileInputStream( source );
            final ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( ArchiveStreamFactory.ZIP, fis );
            try {
               ArchiveEntry entry = input.getNextEntry();
               while( entry != null ) {
                  if( included( entry ) ) {
                     aos.putArchiveEntry( entry );
                     IOUtils.copy( input, aos );
                     aos.closeArchiveEntry();
                  }
                  entry = input.getNextEntry();
               }
            }
            finally {
               input.close();
            }
            fis.close();

         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to read archived file: %s", source.getAbsolutePath() ), exception );
         }
      }

      private boolean included( ArchiveEntry entry ) {
         return filters.stream().allMatch( p -> p.test( entry.getName() ) );
      }
   }
}
