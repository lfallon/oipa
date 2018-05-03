package com.pennassurancesoftware.oipa.upload.util;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

public class TestTransformArchive {

   @Test(groups = { "unit" }, enabled = true)
   public void test() throws Exception {
      final File file = File.createTempFile( "test", ".zip" );
      IOUtils.copy( getClass().getResourceAsStream( "/test.zip" ), new FileOutputStream( file ) );
      final Archives.Default archive = Archives.of( file );

      final Archives.Default transformed = archive.transformer().filterBy( e -> !FilenameUtils.getBaseName( e ).startsWith( "debugger" ) ).transform();

      System.out.println( archive.file().getAbsolutePath() );
      System.out.println( archive.ls() );
      System.out.println( String.format( "Size: %s", archive.file().length() ) );
      System.out.println( transformed.file().getAbsolutePath() );
      System.out.println( transformed.ls() );
      System.out.println( String.format( "Size: %s", transformed.file().length() ) );
   }

   @Test(groups = { "integration" }, enabled = true)
   public void test2() throws Exception {
      final File file = new File( "build/oipa/PASJava-10.2.0.30.war" );
      final Archives.Default archive = Archives.of( file );

      final Archives.Default transformed = archive.transformer().filterBy( e -> !FilenameUtils.getBaseName( e ).startsWith( "debugger" ) ).transform();

      System.out.println( archive.file().getAbsolutePath() );
      System.out.println( archive.ls().filterBy( e -> e.startsWith( "WEB-INF/lib" ) ) );
      System.out.println( String.format( "Size: %s", archive.file().length() ) );
      System.out.println( transformed.file().getAbsolutePath() );
      System.out.println( transformed.ls().filterBy( e -> e.startsWith( "WEB-INF/lib" ) ) );
      System.out.println( String.format( "Size: %s", transformed.file().length() ) );
   }
}
