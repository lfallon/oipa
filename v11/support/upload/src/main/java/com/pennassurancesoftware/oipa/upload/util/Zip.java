package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Zip {

   public static Default of( File file ) {
      return new Default( file );
   }

   public abstract File zip();

   public static class Temp extends Zip {
      private final File file;
      private final boolean includeRootFolder;

      public Temp( File file ) {
         this( file, true );
      }

      public Temp( File file, boolean includeRootFolder ) {
         this.file = file;
         this.includeRootFolder = includeRootFolder;
      }

      private Default dflt() {
         try {
            return new Default( file, File.createTempFile( "temp-", ".zip" ), includeRootFolder );
         }
         catch( Exception exception ) {
            throw new RuntimeException( "Failed to create temp file", exception );
         }
      }

      @Override
      public File zip() {
         return dflt().zip();
      }
   }

   public static class Default extends Zip {
      private static final Logger LOG = LoggerFactory.getLogger( Default.class );

      private final File file;
      private final Optional<File> targetFile;
      private final boolean includeRootFolder;

      public Temp toTemp() {
         return new Temp( file, includeRootFolder );
      }

      public Default( File file ) {
         this( file, Optional.<File> empty(), true );
      }

      public Default( File file, File targetFile, boolean includeRootFolder ) {
         this( file, Optional.ofNullable( targetFile ), includeRootFolder );
      }

      public Default( File file, File targetFile ) {
         this( file, Optional.ofNullable( targetFile ), true );
      }

      public Default( File file, Optional<File> targetFile, boolean includeRootFolder ) {
         this.file = file;
         this.targetFile = targetFile;
         this.includeRootFolder = includeRootFolder;
      }

      public Default excludeRootFolder() {
         return new Default( file, targetFile, false );
      }

      private File targetFile() {
         return targetFile.orElse( new File( file.getAbsolutePath() + ".zip" ) );
      }

      @Override
      public File zip() {
         try {
            if( !file.exists() ) {
               throw new RuntimeException( String.format( "Cannot find file/folder: %s to zip", file.getAbsolutePath() ) );
            }
            final OutputStream os = new FileOutputStream( targetFile() );
            final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream( ArchiveStreamFactory.ZIP, os );
            zip( aos );
            aos.finish();
            os.close();
            return targetFile();
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed to zip: %s to %s", file.getAbsolutePath(), targetFile().getAbsolutePath() ), exception );
         }
      }

      private void zip( ArchiveOutputStream aos ) throws Exception {
         if( includeRootFolder || !file.isDirectory() ) {
            dozip( aos, file, "" );
         }
         else {
            final File[] filesList = file.listFiles();
            if( filesList != null ) {
               for( File sub : filesList ) {
                  dozip( aos, sub, "" );
               }
            }
         }
      }

      private void dozip( ArchiveOutputStream aos, File file, String baseDir ) throws Exception {
         final String entryName = baseDir + new File( file.getCanonicalPath() ).getName();
         //DO NOT do a putArchiveEntry for folders as it is not needed  
         //if it's a directory then list and zip the contents. Uses recursion for nested directories  
         if( file.isDirectory() ) {
            final File[] filesList = file.listFiles();
            if( filesList != null ) {
               for( File sub : filesList ) {
                  dozip( aos, sub, entryName + File.separator );
               }
            }
         }
         else {
            LOG.info( String.format( "Deflate: %s", file.getAbsolutePath() ) );
            aos.putArchiveEntry( new ZipArchiveEntry( file, entryName ) );
            final FileInputStream in = new FileInputStream( file );
            IOUtils.copy( in, aos );
            in.close();
            aos.closeArchiveEntry();
         }
      }
   }
}
