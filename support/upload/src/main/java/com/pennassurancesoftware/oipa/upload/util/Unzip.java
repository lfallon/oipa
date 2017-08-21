package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.immutable.Array;

public interface Unzip {
   public static class Default implements Unzip {
      private final File dest;
      private final File file;
      private final Array<Filter> filters;

      public Default( File file, File dest ) {
         this( file, dest, new Array<Filter>() );
      }

      public Default( File file, File dest, Iterable<Filter> filters ) {
         this.file = file;
         this.dest = dest;
         this.filters = new Array<Filter>( filters );
      }

      @Override
      public File unzip() {
         try {
            dest.mkdirs();

            final FileInputStream fis = new FileInputStream( file );
            final ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( ArchiveStreamFactory.ZIP, fis );
            try {
               ArchiveEntry entry = input.getNextEntry();
               while( entry != null ) {
                  if( include( entry ) ) {
                     new Inflate.Entry( dest, input, entry ).inflate();
                  }
                  entry = input.getNextEntry();
               }
            }
            finally {
               input.close();
            }
            fis.close();

            return dest;
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to read unzip file: %s", file.getAbsolutePath() ), exception );
         }
      }

      public Default when( Filter filter ) {
         return new Default( file, dest, filters.with( filter ) );
      }

      private boolean include( ArchiveEntry entry ) {
         boolean result = true;
         for( Filter filter : filters ) {
            result = result && filter.accept( entry.getName() );
         }
         return result;
      }

   }

   public static interface Filter {
      public static class EndsWith implements Filter {
         private final String str;

         public EndsWith( String str ) {
            this.str = str;
         }

         @Override
         public boolean accept( String entryName ) {
            return entryName.endsWith( str );
         }
      }

      boolean accept( String entryName );
   }

   public static interface Inflate {
      public static class Entry implements Inflate {
         private static final Logger LOG = LoggerFactory.getLogger( Entry.class );

         private final File dest;
         private final transient ArchiveEntry entry;
         private final transient ArchiveInputStream input;

         public Entry( File dest, ArchiveInputStream input, ArchiveEntry entry ) {
            this.input = input;
            this.dest = dest;
            this.entry = entry;
         }

         @Override
         public File inflate() {
            try {
               final String fileName = entry.getName();
               final File newFile = new File( dest.getAbsolutePath() + File.separator + fileName );
               LOG.info( String.format( "Inflate: %s", newFile.getAbsolutePath() ) );
               if( entry.isDirectory() ) {
                  newFile.mkdirs();
               }
               else {
                  createParents( newFile );
                  final FileOutputStream fos = new FileOutputStream( newFile );
                  final byte[] buffer = new byte[1024];
                  int len;
                  while( ( len = input.read( buffer ) ) > 0 ) {
                     fos.write( buffer, 0, len );
                  }
                  fos.close();
               }
               return newFile;
            }
            catch( Exception exception ) {
               throw new RuntimeException( "Failed to inflate entry", exception );
            }
         }

         private void createParents( File file ) {
            try {
               final URI outputURI = new URI( String.format( "file:///%s",
                     file.getParent()
                           .replaceAll( " ", "%20" )
                           .replaceAll( "\\\\", "/" )
                     ) );
               final File outputFile = new File( outputURI );
               if( !outputFile.exists() ) {
                  outputFile.mkdirs();
               }
               final File parentFolder = new File( file.getParent() );
               if( !parentFolder.exists() || !parentFolder.isDirectory() ) {
                  throw new RuntimeException( String.format( "Failed to create directory: %s", parentFolder.getAbsolutePath() ) );
               }
            }
            catch( Exception exception ) {
               throw new RuntimeException( String.format( "Failed to create parent directories for file: %s", file.getAbsolutePath() ), exception );
            }
         }
      }

      File inflate();
   }

   public static class SingleFile implements Unzip {
      private final File dest;
      private final File file;
      private final Array<Filter> filters;

      public SingleFile( File file, File dest ) {
         this( file, dest, new Array<Filter>() );
      }

      public SingleFile( File file, File dest, Iterable<Filter> filters ) {
         this.file = file;
         this.dest = dest;
         this.filters = new Array<Filter>( filters );
      }

      @Override
      public File unzip() {
         try {
            dest.mkdirs();

            File result = null;
            final FileInputStream fis = new FileInputStream( file );
            final ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream( ArchiveStreamFactory.ZIP, fis );
            try {
               ArchiveEntry entry = input.getNextEntry();
               while( entry != null ) {
                  if( include( entry ) ) {
                     result = new Inflate.Entry( dest, input, entry ).inflate();
                     break;
                  }
                  entry = input.getNextEntry();
               }
            }
            finally {
               input.close();
            }
            fis.close();

            return result;
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to read zip file: %s", file.getAbsolutePath() ), exception );
         }
      }

      public SingleFile when( Filter filter ) {
         return new SingleFile( file, dest, filters.with( filter ) );
      }

      private boolean include( ArchiveEntry entry ) {
         boolean result = true;
         for( Filter filter : filters ) {
            result = result && filter.accept( entry.getName() );
         }
         return result;
      }

   }

   public static class Temp implements Unzip {
      private final File file;
      private final Array<Filter> filters;

      public Temp( File file ) {
         this( file, new Array<Filter>() );
      }

      public Temp( File file, Iterable<Filter> filters ) {
         this.file = file;
         this.filters = new Array<Filter>( filters );
      }

      @Override
      public File unzip() {
         final File temp = new TempFolder.Default().create();
         return new Default( file, temp, filters ).unzip();
      }

      public Temp when( Filter filter ) {
         return new Temp( file, filters.with( filter ) );
      }
   }

   public static class TempSingleFile implements Unzip {
      private final File file;
      private final Array<Filter> filters;

      public TempSingleFile( File file ) {
         this( file, new Array<Filter>() );
      }

      public TempSingleFile( File file, Iterable<Filter> filters ) {
         this.file = file;
         this.filters = new Array<Filter>( filters );
      }

      @Override
      public File unzip() {
         final File temp = new TempFolder.Default().create();
         return new SingleFile( file, temp, filters ).unzip();
      }

      public TempSingleFile when( Filter filter ) {
         return new TempSingleFile( file, filters.with( filter ) );
      }

   }

   File unzip();
}
