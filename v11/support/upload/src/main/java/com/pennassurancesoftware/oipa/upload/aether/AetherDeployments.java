package com.pennassurancesoftware.oipa.upload.aether;

import java.io.File;
import java.io.FileWriter;
import java.util.Optional;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.artifact.SubArtifact;

import com.pennassurancesoftware.oipa.upload.util.TempFolder;

public interface AetherDeployments {
   void deploy( Artifact artifact );

   public static class Default implements AetherDeployments {
      private final transient RepositorySystem system;
      private final transient Optional<LocalRepository> localRepository;
      private final transient RemoteRepository distributionRepository;

      public Default( RemoteRepository distributionRepository ) {
         this( RepositorySystems.defaultSystem(), Optional.<LocalRepository> empty(), distributionRepository );
      }

      public Default( RepositorySystem system, Optional<LocalRepository> localRepository, RemoteRepository distributionRepository ) {
         this.system = system;
         this.localRepository = localRepository;
         this.distributionRepository = distributionRepository;
      }

      public Default with( RepositorySystem system ) {
         return new Default( system, localRepository, distributionRepository );
      }

      public Default withLocalRepo( Optional<LocalRepository> localRepository ) {
         return new Default( system, localRepository, distributionRepository );
      }

      private DeployRequest request( Artifact artifact ) {
         return new DeployRequest()
               .addArtifact( artifact )
               .addArtifact( new GeneratePom.FromArtifact( artifact ).generate() )
               .setRepository( distributionRepository );
      }

      @Override
      public void deploy( Artifact artifact ) {
         try {
            system.deploy( session(), request( artifact ) );
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Failed deploying artifact: %s", artifact ), exception );
         }
      }

      private LocalRepository defaultLocalRepository() {
         return new LocalRepository( new TempFolder.Default().create() );
      }

      private RepositorySystemSession session() {
         DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
               .setUpdatePolicy( RepositoryPolicy.UPDATE_POLICY_ALWAYS );
         session = session.setLocalRepositoryManager( system.newLocalRepositoryManager(
               session,
               localRepository.orElse( defaultLocalRepository() ) ) );
         return session;
      }
   }

   public static interface GeneratePom {
      Artifact generate();

      public static class FromArtifact implements GeneratePom {
         private final transient Artifact artifact;

         public FromArtifact( Artifact artifat ) {
            this.artifact = artifat;
         }

         @Override
         public Artifact generate() {
            try {
               final File pomFile = File.createTempFile( String.format( "pom-%s", artifact.getArtifactId() ), ".xml" );
               final FileWriter writer = new FileWriter( pomFile );
               final Model model = new Model();
               model.setGroupId( artifact.getGroupId() );
               model.setArtifactId( artifact.getArtifactId() );
               model.setVersion( artifact.getVersion() );
               model.setPackaging( artifact.getExtension() );
               new MavenXpp3Writer().write( writer, model );
               writer.close();
               return new SubArtifact( artifact, "", "pom" ).setFile( pomFile );
            }
            catch( Exception exception ) {
               throw new RuntimeException( String.format( "Failed generating POM for artifact: %s", artifact ), exception );
            }
         }

      }
   }
}
