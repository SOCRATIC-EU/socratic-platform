# Socratic-platform
The main objective of [SOCRATIC](https://www.socratic.eu/) is to facilitate a platform so citizens and/or organizations can collaboratively identify specific innovative solutions for achieving the desired Global Sustainability Goals, as defined by the United Nations. 
	
## Architecture
SOCRATIC platform is a Web application following an MVC design pattern. The model layer contains persistence framework. The persistence layer provides CRUD operations to the data store. The controller takes care of the business logic and contains essential services for the platform, The view layer provides the user interfaces of the platform in form of HTML web pages.

Web Framework: Apache Wicket >=6.20.0
JavaScript Framework: JQuery >=1.11.3
CSS Framework: Twitter Bootstrap >=2.3

The platform is based on [Maven](http://maven.apache.org) executed on top of [JBoss AS 7.1.1](http://www.jboss.org/jbossas). The current variant of the platform requires Java 7 SDK.

## Importing project to Eclipse

To use the project in Eclipse, the following plugin has to be installed:

* m2e - Maven Integration for Eclipse

To import the project to an Eclipse workspace: 

* "File --> Import... --> Existing Maven Projects"

The required files by Eclipse (.project, .classpath) are created during the import. 

After the import the following command has to be executed in Eclipse: 

* "Run As --> Maven generate-sources" 

The project can then be deployed on a JBoss-Server 

* As requirement the corresponding JBoss installation has to be configured as server in Eclipse
* The project has been tested with the version [JBoss AS 7.1.1.Final Certified Java EE 6 Full Profile](http://www.jboss.org/jbossas/downloads/)


## Database

* The platform requires a database. As of default an in-memory H2 database is used.	One can alternatively use a file based H2 database or a MySQL database. 
* The DB configuration shall be provided in the included file `default.properties`. 
