package com.realdolmen;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.mockito.InjectMocks;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * Util for generating a WebArchive compatible with Arquillian.
 */
public class WarFactory {

    private static WebArchive deployment;

    public static WebArchive createDeployment() {
        if (deployment == null) {
            WebArchive archive = ShrinkWrap.create(WebArchive.class);
            Arrays.stream(Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies()
                    .resolve().withoutTransitivity().asFile()).forEach(archive::addAsLibrary);

            deployment = archive.addAsResource("META-INF/persistence.xml")
                    .addPackages(true, "com.realdolmen")
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        }

        return deployment;
    }
}
