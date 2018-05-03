package com.pennassurancesoftware.oipa.upload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.upload.Upload.Support.Filter.FileEndsWith;
import com.pennassurancesoftware.oipa.upload.aether.AetherCoordinates;
import com.pennassurancesoftware.oipa.upload.aether.AetherDeployments;
import com.pennassurancesoftware.oipa.upload.util.Archives;
import com.pennassurancesoftware.oipa.upload.util.DeleteFile;
import com.pennassurancesoftware.oipa.upload.util.Unzip;
import com.pennassurancesoftware.oipa.upload.util.Zip;

public abstract class Upload {
   public static class _File extends Upload {
      private final static Logger LOG = LoggerFactory.getLogger( _File.class );

      private final AetherCoordinates coords;
      private final AetherDeployments deployments;
      private final File file;

      public _File( File file, AetherDeployments deployments, AetherCoordinates coords ) {
         this.file = file;
         this.deployments = deployments;
         this.coords = coords;
      }

      public Artifact artifact() {
         return new DefaultArtifact(
               coords.groupId(),
               coords.artifactId(),
               coords.classifier(),
               coords.packaging(),
               coords.version() )
               .setFile( file );
      }

      @Override
      public void upload() {
         LOG.info( "Upload {} Using Coords: {}", file.getAbsolutePath(), coords );
         deployments.deploy( artifact() );
      }

   }

   public static abstract class ExplodedWar extends Upload {
      public static class Classes extends ExplodedWar {
         private final static Logger LOG = LoggerFactory.getLogger( Classes.class );

         private final AetherCoordinates coords;
         private final transient AetherDeployments deployments;
         private final File file;

         public Classes( File file, AetherDeployments deployments, AetherCoordinates coords ) {
            this.file = file;
            this.deployments = deployments;
            this.coords = coords;
         }

         public Artifact artifact() {
            return artifact( jar() );
         }

         private Artifact artifact( File jar ) {
            return new DefaultArtifact(
                  coords.groupId(),
                  coords.artifactId(),
                  coords.classifier(),
                  coords.packaging(),
                  coords.version() )
                  .setFile( jar );
         }

         private File classesFolder() {
            return new File(
                  file.getAbsolutePath() + File.separator +
                        "WEB-INF" + File.separator +
                        "classes" );
         }

         private File jar() {
            return Zip.of( classesFolder() )
                  .excludeRootFolder()
                  .toTemp()
                  .zip();
         }

         @Override
         public void upload() {
            LOG.info( "Upload {} Using Coords: {}", classesFolder().getAbsolutePath(), coords );
            deployments.deploy( artifact() );
         }
      }

      public static class Libs extends ExplodedWar {
         private final static Logger LOG = LoggerFactory.getLogger( Libs.class );

         private final transient AetherDeployments deployments;
         private final File file;
         private final Array<Support.Filter> filters;

         public Libs( File file, AetherDeployments deployments ) {
            this( file, deployments, new Array<Support.Filter>() );
         }

         public Libs( File file, AetherDeployments deployments, Iterable<Support.Filter> filters ) {
            this.file = file;
            this.deployments = deployments;
            this.filters = new Array<Support.Filter>( filters );
         }

         private Artifact artifact( File lib ) {
            final AetherCoordinates coords = coords( lib );
            return new DefaultArtifact(
                  coords.groupId(),
                  coords.artifactId(),
                  coords.classifier(),
                  coords.packaging(),
                  coords.version() )
                  .setFile( lib );
         }

         public List<Artifact> artifacts() {
            return StreamSupport.stream( libs().spliterator(), false )
                  .map( f -> artifact( f ) )
                  .collect( Collectors.toList() );
         }

         private AetherCoordinates coords( File lib ) {
            return new AetherCoordinates.FromArtifactFile( lib );
         }

         private boolean include( File lib ) {
            boolean result = true;
            for( Support.Filter filter : filters ) {
               result = result && filter.accept( lib );
            }
            return result;
         }

         private Iterable<File> libs() {
            Array<File> result = new Array<File>();
            final File libs = new File( file.getAbsolutePath() + File.separator + "WEB-INF" + File.separator + "lib" );
            if( libs.exists() ) {
               for( File lib : libs.listFiles() ) {
                  if( lib.getName().endsWith( ".jar" ) && include( lib ) ) {
                     result = result.with( lib );
                  }
               }
            }
            return result;
         }

         @Override
         public void upload() {
            artifacts().forEach( a -> {
               LOG.info( "Upload {} Using Coords: {}", a.getFile().getName(), AetherCoordinates.from( a ) );
            } );

            for( File lib : libs() ) {
               LOG.info( "Upload {} Using Coords: {}", lib.getName(), coords( lib ) );
               deployments.deploy( artifact( lib ) );
            }
         }

         public Libs when( Support.Filter filter ) {
            return new Libs( file, deployments, filters.with( filter ) );
         }
      }

      public static Classes classes( File file, AetherDeployments deployments, AetherCoordinates coords ) {
         return new Classes( file, deployments, coords );
      }

      public static Libs libs( File file, AetherDeployments deployments ) {
         return new Libs( file, deployments );
      }
   }

   public static abstract class Oipa extends Upload {
      public static class App extends Oipa {
         private final File file;

         public App( File file ) {
            this.file = file;
         }

         @Override
         public <T> T accept( Visitor<T> visitor ) {
            return visitor.visit( this );
         }

         @Override
         public Preview preview() {
            return new Preview() {
               @Override
               public List<AetherCoordinates> all() {
                  return Stream.concat( Arrays.asList( war().artifact(), classes().artifact() ).stream().map( a -> AetherCoordinates.from( a ) ),
                        libs().artifacts().stream().map( a -> AetherCoordinates.from( a ) ) )
                        .collect( Collectors.toList() );
               }

               @Override
               public War.Classes classes() {
                  return new War.Classes( file, deployments(),
                        AetherCoordinates.empty()
                              .withArtifactId( "pas.web" )
                              .withGroupId( "com.adminserver" )
                              .withPackaging( "jar" )
                              .withVersion( version().toString() ) );
               }

               private AetherDeployments deployments() {
                  return new AetherDeployments.Default( Repos.pasExtRepo() );
               }

               private Version.InsideProps inside() {
                  return new Version.InsideProps( file, "WEB-INF/classes/Configuration.properties", "application.buildversion" );
               }

               @Override
               public War.Libs libs() {
                  return new War.Libs( file, deployments() )
                        .where( new FileEndsWith( String.format( "%s.jar", version().toString() ) ) );
               }

               @Override
               public Boolean valid() {
                  return inside().exists();
               }

               @Override
               public Version version() {
                  return new Version() {
                     @Override
                     public String version() {
                        return inside().value().orElseThrow( ( ) -> new RuntimeException( String.format( "Failed to get OIPA version from archive: %s", file.getAbsolutePath() ) ) );
                     }
                  };
               }
               
               private Archives.Default archive() {
                  return Archives.of( file );
               }
               
               // Remove the debugger from the libs directory if it is present
               private Archives.Default transformed() {
                  return archive().transformer().filterBy( e -> !FilenameUtils.getBaseName( e ).startsWith( "debugger" ) ).transform();
               }

               @Override
               public Upload._File war() {
                  return Upload.file( transformed().file(), deployments(),
                        AetherCoordinates.empty()
                              .withArtifactId( "PASJava" )
                              .withGroupId( "com.adminserver" )
                              .withPackaging( "war" )
                              .withVersion( version().toString() ) );
               }
            };
         }

         @Override
         public String toString() {
            return String.format( "OIPA %s => %s", version(), file.getAbsolutePath() );
         }

         @Override
         public void upload() {
            preview().war().upload();
            preview().classes().upload();
            preview().libs().upload();
         }

         @Override
         public Version version() {
            return preview().version();
         }
      }

      public static abstract class From {
         public static class _File extends From {
            private final File file;

            public _File( File file ) {
               this.file = file;
            }

            private Stream<File> files() {
               final Supplier<List<File>> files = ( ) -> new Array<File>( file.listFiles() );
               final Predicate<File> warOnly = ( file ) -> file.isFile() && file.getName().endsWith( ".war" );
               final Supplier<Stream<File>> wars = ( ) -> files.get().stream().filter( warOnly );
               Validate.isTrue( file.exists(), "%s does not exist", file.getAbsolutePath() );
               return file.isFile() ? Stream.of( file ) : wars.get();
            }

            private Oipa upload( File file ) {
               return new App( file ).valid()
                     ? new App( file )
                     : new Palette( file ).valid()
                           ? new Palette( file )
                           : new Unknown( file );
            }

            @Override
            public Stream<Oipa> uploads() {
               return files().map( this::upload );
            }
         }

         public abstract Stream<Oipa> uploads();
      }

      public static class Palette extends Oipa {
         private final File file;

         public Palette( File file ) {
            this.file = file;
         }

         @Override
         public <T> T accept( Visitor<T> visitor ) {
            return visitor.visit( this );
         }

         @Override
         public Preview preview() {
            return new Preview() {
               @Override
               public List<AetherCoordinates> all() {
                  return Stream.concat( Arrays.asList( war().artifact(), classes().artifact() ).stream().map( a -> AetherCoordinates.from( a ) ),
                        libs().artifacts().stream().map( a -> AetherCoordinates.from( a ) ) )
                        .collect( Collectors.toList() );
               }

               @Override
               public War.Classes classes() {
                  return new War.Classes( file, deployments(),
                        AetherCoordinates.empty()
                              .withArtifactId( "palette.web" )
                              .withGroupId( "com.adminserver" )
                              .withPackaging( "jar" )
                              .withVersion( version().toString() ) );
               }

               private AetherDeployments deployments() {
                  return new AetherDeployments.Default( Repos.pasExtRepo() );
               }

               private Version.InsideProps inside() {
                  return new Version.InsideProps( file, "WEB-INF/classes/oracle/insurance/palette/messages/login/Messages.properties", "buildversion" );
               }

               @Override
               public War.Libs libs() {
                  return new War.Libs( file, deployments() )
                        .where( new FileEndsWith( String.format( "%s.jar", version().toString() ) ) );
               }

               @Override
               public Boolean valid() {
                  return inside().exists();
               }

               @Override
               public Version version() {
                  return new Version() {
                     @Override
                     public String version() {
                        return inside().value().orElseThrow( ( ) -> new RuntimeException( String.format( "Failed to get OIPA Palette version from archive: %s", file.getAbsolutePath() ) ) );
                     }
                  };
               }

               @Override
               public Upload._File war() {
                  return Upload.file( file, deployments(),
                        AetherCoordinates.empty()
                              .withArtifactId( "PaletteConfig" )
                              .withGroupId( "com.adminserver" )
                              .withPackaging( "war" )
                              .withVersion( version().toString() ) );
               }
            };
         }

         @Override
         public String toString() {
            return String.format( "Palette %s => %s", version(), file.getAbsolutePath() );
         }

         @Override
         public void upload() {
            preview().war().upload();
            preview().classes().upload();
            preview().libs().upload();
         }

         @Override
         public Version version() {
            return preview().version();
         }
      }

      public static abstract class Preview {

         public abstract List<AetherCoordinates> all();

         public abstract War.Classes classes();

         public abstract War.Libs libs();

         @Override
         public String toString() {
            return all().isEmpty()
                  ? "[No Artifacts]"
                  : all().stream().map( c -> c.toString() ).collect( Collectors.joining( "\n" ) );
         }

         public abstract Boolean valid();

         public abstract Version version();

         public abstract Upload._File war();
      }

      public static class Unknown extends Oipa {
         private final File file;

         public Unknown( File file ) {
            this.file = file;
         }

         @Override
         public <T> T accept( Visitor<T> visitor ) {
            return visitor.visit( this );
         }

         @Override
         public Preview preview() {
            return new Preview() {
               @Override
               public List<AetherCoordinates> all() {
                  return new ArrayList<AetherCoordinates>();
               }

               @Override
               public Upload.War.Classes classes() {
                  throw new RuntimeException( String.format( "Unknown OIPA file: %s", file.getAbsolutePath() ) );
               }

               @Override
               public Upload.War.Libs libs() {
                  throw new RuntimeException( String.format( "Unknown OIPA file: %s", file.getAbsolutePath() ) );
               }

               @Override
               public Boolean valid() {
                  return true;
               }

               @Override
               public Version version() {
                  return new Version() {
                     @Override
                     public String version() {
                        return "NA";
                     }
                  };
               }

               @Override
               public _File war() {
                  throw new RuntimeException( String.format( "Unknown OIPA file: %s", file.getAbsolutePath() ) );
               }
            };
         }

         @Override
         public void upload() {}

         @Override
         public Version version() {
            return preview().version();
         }
      }

      public static abstract class Version {
         public static class InsideProps {
            private final File file;
            private final String path;
            private final String prop;

            public InsideProps( File file, String path, String prop ) {
               this.file = file;
               this.path = path;
               this.prop = prop;
            }

            private Support.Archive.Extract.ByPath.Default entry() {
               return Support.Archive.of( file ).extract().path( path );
            }

            public Boolean exists() {
               return value().isPresent();
            }

            public Optional<Properties> props() {
               final Function<InputStream, Properties> toProps = ( is ) -> {
                  try {
                     final Properties result = new Properties();
                     result.load( is );
                     return result;
                  }
                  catch( IOException exception ) {
                     throw new RuntimeException( String.format( "Failed to load properties from archive: %s", file.getAbsolutePath() ), exception );
                  }
               };

               return entry().toStream().map( toProps );
            }

            public Optional<String> value() {
               return props().map( props -> props.getProperty( prop ) );
            }
         }

         @Override
         public String toString() {
            return version();
         }

         public abstract String version();
      }

      public static interface Visitor<T> {
         public abstract T visit( App oipa );

         public abstract T visit( Palette oipa );

         public abstract T visit( Unknown oipa );
      }

      public static From._File from( File file ) {
         return new From._File( file );
      }

      public abstract <T> T accept( Visitor<T> visitor );

      public abstract Preview preview();

      public Boolean valid() {
         return preview().valid();
      }

      public abstract Version version();
   }

   public static abstract class Support {
      public static abstract class Archive {
         public static class Default extends Archive {
            private final File file;

            public Default( File file ) {
               this.file = file;
            }

            public Extract.Default extract() {
               return new Extract.Default( file );
            }
         }

         public static abstract class Extract {
            public static abstract class ByPath {
               public static class Default extends ByPath {
                  private final File file;
                  private final String path;

                  public Default( File file, String path ) {
                     this.file = file;
                     this.path = path;
                  }

                  public Optional<InputStream> toStream() {
                     try {
                        Optional<InputStream> result = Optional.empty();
                        final FileInputStream fin = new FileInputStream( file );
                        final BufferedInputStream bin = new BufferedInputStream( fin );
                        final ZipInputStream zin = new ZipInputStream( bin );
                        ZipEntry ze = null;
                        while( ( ze = zin.getNextEntry() ) != null ) {
                           if( ze.getName().equals( path ) ) {
                              result = Optional.<InputStream> of( zin );
                              break;
                           }
                        }
                        return result;
                     }
                     catch( IOException exception ) {
                        throw new RuntimeException( String.format( "Failed to extract: %s from zip file: %s", path, file.getAbsolutePath() ), exception );
                     }
                  }

                  @Override
                  public String toString() {
                     final Function<InputStream, String> toStr = new Function<InputStream, String>() {
                        @Override
                        public String apply( InputStream is ) {
                           try {
                              return IOUtils.toString( is );
                           }
                           catch( IOException exception ) {
                              throw new RuntimeException( String.format( "Failed to extract %s from archive: %s", path, file.getAbsolutePath() ), exception );
                           }
                        }
                     };

                     return toStream().map( toStr ).orElse( String.format( "%s could not be found in archive: %s", path, file.getAbsolutePath() ) );
                  }
               }
            }

            public static class Default extends Extract {
               private final File file;

               public Default( File file ) {
                  this.file = file;
               }

               public ByPath.Default path( String path ) {
                  return new ByPath.Default( file, path );
               }
            }
         }

         public static Default of( File file ) {
            return new Default( file );
         }
      }

      public static interface Filter {
         public static class FileEndsWith implements Filter {
            private final String str;

            public FileEndsWith( String str ) {
               this.str = str;
            }

            @Override
            public boolean accept( File file ) {
               return file.getName().endsWith( str );
            }
         }

         boolean accept( File file );
      }
   }

   public static abstract class War extends Upload {
      public static class Classes extends War {
         private final AetherCoordinates coords;
         private final AetherDeployments deployments;
         private final File file;

         public Classes( File file, AetherDeployments deployments, AetherCoordinates coords ) {
            this.file = file;
            this.deployments = deployments;
            this.coords = coords;
         }

         public Artifact artifact() {
            return fromExploded( e -> e.artifact() );
         }

         private void exploded( Consumer<ExplodedWar.Classes> consumer ) {
            fromExploded( l -> {
               consumer.accept( l );
               return true;
            } );
         }

         private <T> T fromExploded( Function<ExplodedWar.Classes, T> func ) {
            final File extracted = new Unzip.Temp( file ).unzip();
            final T result = func.apply( new ExplodedWar.Classes( extracted, deployments, coords ) );
            new DeleteFile.Quietly( extracted ).delete();
            return result;
         }

         @Override
         public void upload() {
            exploded( e -> e.upload() );
         }
      }

      public static class Libs extends War {
         private final AetherDeployments deployments;
         private final File file;
         private final Array<Support.Filter> filters;

         public Libs( File file, AetherDeployments deployments ) {
            this( file, deployments, new Array<Support.Filter>() );
         }

         public Libs( File file, AetherDeployments deployments, Iterable<Support.Filter> filters ) {
            this.file = file;
            this.deployments = deployments;
            this.filters = new Array<Support.Filter>( filters );
         }

         public List<Artifact> artifacts() {
            return fromExploded( l -> l.artifacts() );
         }

         private void exploded( Consumer<ExplodedWar.Libs> consumer ) {
            fromExploded( l -> {
               consumer.accept( l );
               return true;
            } );
         }

         private <T> T fromExploded( Function<ExplodedWar.Libs, T> func ) {
            final File extracted = new Unzip.Temp( file ).unzip();
            // new ExplodedWar.Libs( extracted, deployments, filters ).upload();
            final T result = func.apply( new ExplodedWar.Libs( extracted, deployments, filters ) );
            new DeleteFile.Quietly( extracted ).delete();
            return result;
         }

         @Override
         public void upload() {
            exploded( l -> l.upload() );
         }

         public Libs where( Support.Filter filter ) {
            return new Libs( file, deployments, filters.with( filter ) );
         }
      }
   }

   public static _File file( File file, AetherDeployments deployments, AetherCoordinates coords ) {
      return new _File( file, deployments, coords );
   }

   public abstract void upload();
}
