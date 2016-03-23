# Time registration
##Installation
###Prerequisites
The Java EE backend has been developed using [WildFly 10.0.0 (final)](http://wildfly.org/downloads/). The backend uses Maven to download all dependencies and Android uses Gradle. It is highly recommended to use the [IntelliJ IDEA]
(https://www.jetbrains.com/idea/download/?gclid=CjwKEAjw_ci3BRDSvfjortr--DQSJADU8f2josid6FG5EH05496lZIS0uaHx6WveuUzptxxJOGYoZxoCvGzw_wcB&gclsrc=aw.ds&dclid=CKO9n4nH1ssCFWNhwgodjE8Hww#section=windows) as you will be provided with existing run configurations for testing. The backend uses MySQL 5 as the underlying database, so you should have MySQL installed on your computer.

### Install backend
To install the backend application, first clone the project by `git clone https://github.com/IWMC/geofencing-beacons`.
Next open IntelliJ IDEA and `File > Open` the folder located at `geofencing-beacons/Backend/TimeRegistration`. IntelliJ will notify you that it found a non-managed `pom.xml` file, allow it to add the project as a Maven project. It will now resolve all Maven dependencies and will generate the entire project from the existing sources.

Next you will either edit `persistence.xml` to reference to existing data sources on your application server or you add new MySQL data sources to the application server. The JNDI's of the two data sources it will look for are `java:jboss/datasources/LocalTimeRegistration` and `java:jboss/datasources/LocalTimeRegistrationTest`. The appropriate data sources on the application server should therefore be named `LocalTimeRegistration` and `LocalTimeRegistrationTest` and should reference to a MySQL 5 database.

If you have any trouble adding the MySQL connector to your WildFly application server you can find some information to solve your problem
[here](http://wildfly.org/news/2014/02/06/GlassFish-to-WildFly-migration/).

**If you are not using the WildFly or JBoss application server you will need to edit these JNDI's even if you have created the appropriate data sources on your application server.**

To run the application on an application server, you need to add the appropriate configuration to the project. The context path, defined in `jboss-web.xml`, is `/` and the artifact that should be deployed is either `TimeRegistration:war` or `TimeRegistration:war exploded`.
The SDK used during development is 1.8.0_73 and uses 1.8 features such as the Streams API and lambdas. Make sure to add the Module SDK from the TimeRegistration module under `Project Structure > Project Settings > Modules`.

Under `Project Structure > Project Settings > Facets` you need to assign the correct descriptors to the appropriate facets. If everything went correct you should see 2 facets: a JPA and a WEB facet.
First go to the web facet and click on `Add Application Server specific descriptor` with the following details (you can skip this step if you are not using WildFly/JBoss or do not want the web context path to be `/`):
- Application Server: JBoss Server
- Descriptor: JBoss Web Deployment Descriptor
- Version: 5.0

Next click on the plus button, click on web.xml and click on OK.

To add the `persistence.xml` to the JPA facet, simply open `persistence.xml`, it should automatically suggest to add the descriptor in the corresponding facet, press OK. You should now be able to run the backend, to test this, from the context root go to index.xhtml, so it might look something like this: `localhost/index.xhtml` or `localhost/TimeRegistrationBackend/index.xhtml`. To log in as an administrator, you can use username `admin` with password `Bla123` for your credentials.


### Test backend
MULTIRUN
