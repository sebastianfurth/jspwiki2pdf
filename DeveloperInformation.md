# Contributing code to the jspwiki2pdf project #

The easiest way to contribute to this project is to mimic the way I do development on the project.

I use [Eclipse 3.4+](http://www.eclipse.org/downloads/) and import the project into my workspace. I use [Maven 2.2+](http://maven.apache.org/download.html) for building and maintaining the dependancies.

In order to get dependant JARs you must run the `mvn` command at least once for Maven to be able to download all dependancies.

Since the JSPWiki source wasn't available in the [Maven repository](http://www.mvnrepository.com/search.html?query=jspwiki) last I checked, you need to do that yourself. Easiest it probably to [import JSPWiki as a project into Eclipse](http://jspwiki.org/wiki/AnonymousCVSAccess), create a JAR-file, [add the JAR-file to your local repository](http://maven.apache.org/guides/mini/guide-installing-3rd-party-jars.html) and run `mvn` again.

My command for importing JSPWiki:
```
mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.ecyrd -DartifactId=jspwiki -Dversion=cvs -Dpackaging=jar
```

All set! Just do your work and run `mvn package` to create a new JAR-file for testing your modifications.

To create a single jar-file with all dependancies just run `mvn assembly:assembly`.

If you need commit acces, [send me an email](mailto:brattberg@gmail.com).