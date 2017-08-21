package com.pennassurancesoftware.oipa.upload;

import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

public abstract class Repos {
   public static RemoteRepository pasExtRepo() {
      return new RemoteRepository.Builder(
            "third-party-repository",
            "default",
            "http://repo.pennassurancesoftware.com/artifactory/ext-release-local" )
            .setAuthentication(
                  new AuthenticationBuilder()
                        .addUsername( "jbc" )
                        .addPassword( "london10" ).build() )
            .build();
   }
}
