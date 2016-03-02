package com.realdolmen;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Convenience interface used to avoid boilerplate code for commonly WAR archives by Arquillian.
 */
public interface ArquillianTest {

    @Deployment
    static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }
}
