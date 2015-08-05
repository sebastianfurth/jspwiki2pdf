## About JSPWiki PDF Plugin ##
Servlet to convert a [JSPWiki](http://www.jspwiki.org/) HTML page into a PDF file.

This was created to add PDF export functionality to JSPWiki but could likely be modified to work elsewhere without too much trouble.

**Parameters:**
| page | The name of the page you wish to create a PDF file of. (Required)  |
|:-----|:-------------------------------------------------------------------|
| xsl  | The name of an XSL-file that should be used to convert the XML FO file into a PDF. Default value: default.xsl. (Optional)  |
| ext  | If you experience problems with Internet Explorer, it might help to add an URL parameter last in the request that ends with .pdf. (Optional)  |
|encoding|ID of the encoding to be used like `UTF-8` or `ISO-8859-1`. (Optional)|

To install, simply copy jspwiki2pdf-jar-with-dependencies.jar to /WEB-INF/lib of your JSPWiki installation and enter the following in the web.xml file:

```
 <servlet>
     <servlet-name>Wiki2PDFServlet</servlet-name>
     <servlet-class>com.palbrattberg.jspwiki.Wiki2PDFServlet</servlet-class>
 </servlet>
 
 <servlet-mapping>
     <servlet-name>Wiki2PDFServlet</servlet-name>
     <url-pattern>/wiki.pdf</url-pattern>
 </servlet-mapping>
```

You should also add the call to the servlet somewhere on your page, templates/default/ViewTemplate.jsp is a good candidate:

```
 <a href="wiki.pdf?page=<wiki:Variable var="pagename" />&encoding=UTF8&ext=.pdf">View PDF</a>
```

In case you do not want the massive jspwiki2pdf-jar-with-dependencies.jar file that includes all dependancies needed, you must download them yourself and add them to tt>WEB-INF/lib. If you do that, you may use the smuch smaller jspwiki2pdf.jar file that just includes the actual code for Wiki2PDFServlet.
Apache FOP is used to transform XML to PDF.
Download from here: http://www.apache.org/dyn/closer.cgi/xmlgraphics/fop Unpack it, and copy the following libraries to WEB-INF/lib:

avalon-framework.jar (or whatever version, avalon-framework-cvs-20020806.jar for example)
batik.jar
fop.jar
jTidy is used to transform HTML to XML.
Download the binary distribution from here: http://jtidy.sourceforge.net/download.html Unpack it, and copy Tidy.jar to WEB-INF/lib.

Next, place the wiki2pdf.jar in /WEB-INF/lib and restart tomcat for the changes to take effect.

If things do not work out, visit the jspwiki2pdf page for assistance.

Thanks to Ruben Inoto for his prior work and to Henrik Zätterman for adding new features!


