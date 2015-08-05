# How to release a new version of jspwiki2pdf #

For creating a new release, see: http://maven.apache.org/plugins/maven-release-plugin/

Also, make sure to set command line language to english first (`set LANGUAGE=en`) or `svn` might give problems.

Recipe (for Windows):
```
  set LANGUAGE=en
  mvn release:prepare
  mvn 
```

Then upload the generated files to this site and deprecate the older versions.