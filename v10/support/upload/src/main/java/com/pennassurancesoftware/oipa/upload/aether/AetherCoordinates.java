package com.pennassurancesoftware.oipa.upload.aether;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.Artifact;

import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.upload.util.Unzip;
import com.pennassurancesoftware.oipa.upload.util.Unzip.Filter;

public abstract class AetherCoordinates {
   public static Default empty() {
      return new Default();
   };

   public static FromArtifact from( Artifact artifact ) {
      return new FromArtifact( artifact );
   }

   public static class FromArtifact extends AetherCoordinates {
      private final Artifact artifact;

      public FromArtifact( Artifact artifact ) {
         this.artifact = artifact;
      }

      @Override
      public String artifactId() {
         return artifact.getArtifactId();
      }

      @Override
      public String classifier() {
         return artifact.getClassifier();
      }

      @Override
      public String groupId() {
         return artifact.getGroupId();
      }

      @Override
      public String packaging() {
         return artifact.getExtension();
      }

      @Override
      public String version() {
         return artifact.getVersion();
      }
   }

   public static class Default extends AetherCoordinates {
      private final Optional<String> artifactId;
      private final Optional<String> classifier;
      private final Optional<String> groupId;
      private final Optional<String> packaging;
      private final Optional<String> version;

      public Default() {
         this(
               Optional.<String> empty(),
               Optional.<String> empty(),
               Optional.<String> empty(),
               Optional.<String> empty(),
               Optional.ofNullable( "jar" ) );
      }

      public Default(
            Optional<String> artifactId,
            Optional<String> groupId,
            Optional<String> version,
            Optional<String> classifier,
            Optional<String> packaging ) {
         this.artifactId = artifactId;
         this.groupId = groupId;
         this.version = version;
         this.classifier = classifier;
         this.packaging = packaging;
      }

      public Default withArtifactId( String artifactId ) {
         return new Default( Optional.ofNullable( artifactId ), groupId, version, classifier, packaging );
      }

      public Default withGroupId( String groupId ) {
         return new Default( artifactId, Optional.ofNullable( groupId ), version, classifier, packaging );
      }

      public Default withVersion( String version ) {
         return new Default( artifactId, groupId, Optional.ofNullable( version ), classifier, packaging );
      }

      public Default withClassifier( String classifier ) {
         return new Default( artifactId, groupId, version, Optional.ofNullable( classifier ), packaging );
      }

      public Default withPackaging( String packaging ) {
         return new Default( artifactId, groupId, version, classifier, Optional.ofNullable( packaging ) );
      }

      @Override
      public String artifactId() {
         return artifactId.orElse( null );
      }

      @Override
      public String classifier() {
         return classifier.orElse( null );
      }

      @Override
      public String groupId() {
         return groupId.orElse( null );
      }

      @Override
      public String packaging() {
         return packaging.orElse( null );
      }

      @Override
      public String version() {
         return version.orElse( null );
      }
   }

   public static class FromArtifactFile extends AetherCoordinates {
      private final transient File file;
      private final transient PomProperties props;

      public FromArtifactFile( File file ) {
         this.file = file;
         this.props = new PomProperties.Cached( new PomProperties.Archive( file ) );
      }

      @Override
      public String artifactId() {
         return fromProperties().artifactId();
      }

      @Override
      public String classifier() {
         return fromProperties().classifier();
      }

      @Override
      public String groupId() {
         return fromProperties().groupId();
      }

      @Override
      public String packaging() {
         return FilenameUtils.getExtension( file.getName() );
      }

      @Override
      public String version() {
         return fromProperties().version();
      }

      private AetherCoordinates fromProperties() {
         return new FromProperties( properties().properties() );
      }

      private PomProperties properties() {
         return props;
      }
   }

   public static class FromProperties extends AetherCoordinates {
      private final transient Properties properties;

      public FromProperties( Properties properties ) {
         this.properties = properties;
      }

      @Override
      public String artifactId() {
         return properties.getProperty( "artifactId" );
      }

      @Override
      public String classifier() {
         return properties.getProperty( "classifier" );
      }

      @Override
      public String groupId() {
         return properties.getProperty( "groupId" );
      }

      @Override
      public String packaging() {
         throw new UnsupportedOperationException( "packing not known by the properties file" );
      }

      @Override
      public String version() {
         return properties.getProperty( "version" );
      }
   }

   public static class Lazy extends AetherCoordinates {
      private final ConcurrentHashMap<Long, AetherCoordinates> cache = new ConcurrentHashMap<Long, AetherCoordinates>();
      private final Supplier<AetherCoordinates> supplier;

      public Lazy( Supplier<AetherCoordinates> supplier ) {
         this.supplier = supplier;
      }

      @Override
      public String artifactId() {
         return coords().artifactId();
      }

      @Override
      public String classifier() {
         return coords().classifier();
      }

      @Override
      public String groupId() {
         return coords().groupId();
      }

      @Override
      public String packaging() {
         return coords().packaging();
      }

      @Override
      public String version() {
         return coords().version();
      }

      private AetherCoordinates coords() {
         return cache.computeIfAbsent( 1L, ( key ) -> supplier.get() );
      }
   }

   public static interface PomProperties {
      public static class Archive implements PomProperties {
         private final File file;

         public Archive( File file ) {
            this.file = file;
         }

         @Override
         public Properties properties() {
            try {
               final Optional<File> propFile = Optional.ofNullable(
                     new Unzip.TempSingleFile( file )
                           .when( new Filter.EndsWith( "pom.properties" ) )
                           .when( artifactFromPathMatches() )
                           .unzip()
                     );
               Properties result = new Properties();
               if( propFile.isPresent() ) {
                  result.load( new FileInputStream( propFile.get() ) );
               }
               return result;
            }
            catch( Exception exception ) {
               throw new RuntimeException( String.format( "Failed to load pom properties from archive: %s", file.getAbsolutePath() ), exception );
            }
         }

         private Filter artifactFromPathMatches() {
            return new Filter() {
               @Override
               public boolean accept( String entryName ) {
                  return file.getName().startsWith( artifactId( entryName ) );
               }

               private String artifactId( String entryName ) {
                  String result = "";
                  Array<String> pathArray = new Array<String>( entryName.split( "/" ) );
                  if( pathArray.size() > 1 ) {
                     result = pathArray.get( pathArray.size() - 2 );
                  }
                  return result;
               }
            };
         }
      }

      public static class Cached implements PomProperties {
         private final ConcurrentHashMap<Long, Properties> cache = new ConcurrentHashMap<Long, Properties>();
         private final transient PomProperties properties;

         public Cached( PomProperties props ) {
            this.properties = props;
         }

         @Override
         public Properties properties() {
            return cache.computeIfAbsent( 1L, ( key ) -> properties.properties() );
         }
      }

      Properties properties();
   }

   public abstract String artifactId();

   public abstract String classifier();

   public abstract String groupId();

   public abstract String packaging();

   public abstract String version();
   
   @Override
   public String toString() {
      final StringBuffer buffer = new StringBuffer();
      buffer.append( String.format( "%s:%s:%s(%s)", groupId(), artifactId(), version(), packaging() ) );
      return buffer.toString();
   }
}
