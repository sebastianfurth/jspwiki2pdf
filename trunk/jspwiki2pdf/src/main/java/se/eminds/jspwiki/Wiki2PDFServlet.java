/* 
    Wiki2PDFServlet - Servlet to convert a JSPWiki page into a PDF file.

    Copyright (C) 2005 Pal Brattberg (pal@eminds.se)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package se.eminds.jspwiki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.sun.xml.internal.ws.wsdl.parser.MIMEConstants;

/**
 * Servlet to convert an HTML page into a PDF file.
 * 
 * <p/>This was created to add PDF export functionality to <a
 * href="http://www.jspwiki.org">JSPWiki</a> but could probably be modified to
 * work elsewhere without too much trouble.
 * 
 * <p/>Parameters:
 * <ul>
 * <li><i>page</i> - The name of the page you wish to create a PDF file of.
 * (Required)</li>
 * <li><i>xsl</i> - The name of an XSL-file that should be used to convert the
 * XML FO file into a PDF. Default value: <tt>xhtml2fo.xsl</tt>. (Optional)</li>
 * <li><i>ext</i> - If you experience problems with Internet Explorer, it
 * might help to add an URL parameter last in the request that ends with
 * <tt>.pdf</tt>. (Optional)</li>
 * </ul>
 * 
 * <p/>To install, enter the following in the <tt>web.xml</tt> of your JSPWiki
 * installation:
 * 
 * <pre>
 * &lt;servlet&gt;
 *     &lt;servlet-name&gt;Wiki2PDFServlet&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;se.eminds.jspwiki.Wiki2PDFServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * 
 * &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;Wiki2PDFServlet&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;/wiki.pdf&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * <p/>Add the call to the servlet in any page,
 * <tt>templates/default/ViewTemplate.jsp</tt> is a good candidate:
 * 
 * <pre>
 * &lt;a href=&quot;wiki.pdf?page=&lt;wiki:PageName /&gt;&amp;ext=.pdf&quot;&gt;View PDF&lt;/a&gt;
 * </pre>
 * 
 * <p/>You need to download some external JAR's that are need for this servlet
 * to work.
 * 
 * <p/><b>Apache FOP</b> is used to transform XML to PDF.<br/> Download the
 * binary distribution from here: <a
 * href="http://xml.apache.org/fop/download.html#binary">http://xml.apache.org/fop/download.html#binary</a>
 * Unpack it, and copy the following libraries to <tt>WEB-INF/lib</tt>:
 * <ul>
 * <li>avalon-framework.jar (or whatever version,
 * avalon-framework-cvs-20020806.jar for example)
 * <li>batik.jar</li>
 * <li>fop.jar</li>
 * </ul>
 * 
 * <p/><b>jTidy</b> is used to transform HTML to XML.<br/> Download the
 * binary distribution from here: <a
 * href="http://jtidy.sourceforge.net/download.html">http://jtidy.sourceforge.net/download.html</a>
 * Unpack it, and copy <tt>Tidy.jar</tt> to <tt>WEB-INF/lib</tt>.
 * 
 * <p/><b>JIMI</b> is used to allow image inclusion in PDFs.<br/> Download
 * from here: <a
 * href="http://java.sun.com/products/jimi/">http://java.sun.com/products/jimi/</a>
 * Unpack it, and copy <tt>JimiProClasses.zip</tt> to
 * <tt>WEB-INF/lib/JimiProClasses.<b>jar</b></tt>.
 * <em>Note the change of the file suffix.</em>
 * 
 * <p/>Next, place the <tt>wiki2pdf.jar</tt> in <tt>/WEB-INF/lib</tt> and
 * restart tomcat for the changes to take effect.
 * 
 * <p/>If things do not work out, visit the Wiki page for assistance.
 * 
 * @author <a href="http://palbrattberg.com">P&aring;l Brattberg</a>
 * @author Ruben Inoto
 * @version 2005-03-11
 * @see <a href="http://www.jspwiki.org/Wiki.jsp?page=PDFPlugin">PDF plugin page</a>
 */
public class Wiki2PDFServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected Logger log = Logger.getLogger(this.getClass().getName());
    protected TransformerFactory m_transformerFactory;
    protected FopFactory m_fopFactory;
    protected WikiEngine m_wikiEngine;
    protected static final String DEFAULT_XSL_FILE = "xhtml2fo.xsl";

    /*
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        m_wikiEngine = WikiEngine.getInstance(config);
        m_transformerFactory = TransformerFactory.newInstance();
        m_fopFactory = FopFactory.newInstance();
    }

    /*
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doGet(final HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {
        res.setHeader("Cache-control", "private");
        res.setHeader("Pragma", "private");

        String page = req.getParameter("page");
        if ((page == null) || ("".equals(page))) {
            throw new IllegalArgumentException("You must supply a valid value for parameter \"page\".");
        }

        WikiContext context = new WikiContext(m_wikiEngine, req, new WikiPage(m_wikiEngine, page));
        if (!context.hasAccess(res, true)) {
            return;
        }

        String xsl = req.getParameter("xsl");
        if ((xsl == null) || ("".equals(xsl))) {
            xsl = DEFAULT_XSL_FILE;
        }

        log.debug("page = \"" + page + "\", xsl = \"" + xsl + "\".");

        String htmlPage = m_wikiEngine.getHTML(page);
        log.debug("Successfully got the HTML for requested page.");

        try {
            createPDF(page, htmlPage, xsl, res);
        } catch (Throwable t) {
            log.error("Throwable caught while trying to generate PDF.", t);
            throw new ServletException(t);
        }

        log.debug("Finished successfully.");
    }

    /**
     * Convert HTML into XML. Transform that XML into a PDF byte stream. Write
     * the byte stream to the response to send it to the browser.
     * 
     * @param nameOfPage
     *            The name (title) of the page we are printing as PDF.
     * @param htmlOfPage
     *            The complete HTML document we convert.
     * @param response
     *            The <tt>HttpServletResponse</tt> we send the result to.
     * @param xslFileName
     *            Filename of the XSL file to use
     * @throws Exception
     *             If something goes wrong during the creation of the PDF.
     */
    private void createPDF(final String nameOfPage, final String htmlOfPage, final String xslFileName,
            final HttpServletResponse response) throws Exception {
        // Convert HTML into XML
        InputStream in = new ByteArrayInputStream(("<title>" + nameOfPage + "</title>" + htmlOfPage)
            .getBytes(response.getCharacterEncoding()));

        Tidy tidy = new Tidy();
        tidy.setXmlOut(true);
        Document xmlDocument = tidy.parseDOM(in, null);
        log.debug("Page \"" + nameOfPage + "\" converted into XML ok.");

        // Setup a buffer to obtain the content length
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Setup FOP
        Fop fop = m_fopFactory.newFop(MimeConstants.MIME_PDF, out);

        // Setup Transformer
        ClassLoader cl = Wiki2PDFServlet.class.getClassLoader();
        InputStream is = cl.getResourceAsStream(xslFileName);
        Source xsltSrc = new StreamSource(is);
        Transformer transformer = this.m_transformerFactory.newTransformer(xsltSrc);

        // Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        Source src = new DOMSource(xmlDocument);

        // Start the transformation and rendering process
        transformer.transform(src, res);

        // Start the transformation and rendering process
        transformer.transform(src, res);
        log.debug("Page \"" + nameOfPage + "\" converted into PDF ok. Size = " + out.size());

        // Prepare response
        response.setContentType("application/pdf");
        response.setContentLength(out.size());

        // Send content to Browser
        response.getOutputStream().write(out.toByteArray());
        response.getOutputStream().flush();
    }
}
