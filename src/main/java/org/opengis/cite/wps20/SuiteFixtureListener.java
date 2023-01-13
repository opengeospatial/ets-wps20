package org.opengis.cite.wps20;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import org.opengis.cite.wps20.util.ClientUtils;
import org.opengis.cite.wps20.util.TestSuiteLogger;
import org.opengis.cite.wps20.util.URIUtils;
import org.opengis.cite.wps20.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;

/**
 * A listener that performs various tasks before and after a test suite is run,
 * usually concerned with maintaining a shared test suite fixture. Since this
 * listener is loaded using the ServiceLoader mechanism, its methods will be
 * called before those of other suite listeners listed in the test suite
 * definition and before any annotated configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test
 * group contexts (ITestContext). However, suite attributes are still accessible
 * from lower contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        processSuiteParameters(suite);
        registerClientComponent(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (null != System.getProperty("deleteSubjectOnFinish")) {
            deleteTempFiles(suite);
            System.getProperties().remove("deleteSubjectOnFinish");
        }
    }

    /**
     * Processes test suite arguments and sets suite attributes accordingly. The
     * entity referenced by the {@link TestRunArg#IUT iut} argument is retrieved
     * and written to a File that is set as the value of the suite attribute
     * {@link SuiteAttribute#TEST_SUBJ_FILE testSubjectFile}.
     * 
     * @param suite
     *            An ISuite object representing a TestNG test suite.
     */
    void processSuiteParameters(ISuite suite) {
        Map<String, String> params = suite.getXmlSuite().getParameters();
        TestSuiteLogger.log(Level.CONFIG, "Suite parameters\n" + params.toString());
        /*String iutParam = params.get(TestRunArg.IUT.toString());
        if ((null == iutParam) || iutParam.isEmpty()) {
            throw new IllegalArgumentException("Required test run parameter not found: " + TestRunArg.IUT.toString());
        }
        URI iutRef = URI.create(iutParam.trim());
        File entityFile = null;
        try {
            entityFile = URIUtils.dereferenceURI(iutRef);
        } catch (IOException iox) {
            throw new RuntimeException("Failed to dereference resource located at " + iutRef, iox);
        }
        TestSuiteLogger.log(Level.FINE, String.format("Wrote test subject to file: %s (%d bytes)",
                entityFile.getAbsolutePath(), entityFile.length()));
        suite.setAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName(), entityFile);
        Document iutDoc = null;
        try {
            iutDoc = URIUtils.parseURI(entityFile.toURI());
        } catch (Exception x) {
            throw new RuntimeException("Failed to parse resource retrieved from " + iutRef, x);
        }
        suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);*/
        
        /* Define SERVICE_URL parameter */
        String ServiceUrlParam = params.get(TestRunArg.IUT.toString());
		if ((null == ServiceUrlParam) || ServiceUrlParam.isEmpty()) {
			throw new IllegalArgumentException(
					"Required test run parameter not found: " + TestRunArg.IUT.toString());
		}
		//mapping TestRunArg.IUT to SuiteAttribute.SERVICE_URL
		suite.setAttribute(SuiteAttribute.SERVICE_URL.getName(), URI.create(ServiceUrlParam));
		
		/* Define SERVICE_URL parameter */
        String EchoProcessIdParam = params.get(TestRunArg.ECHO_PROCESS_ID.toString());
		if ((null == EchoProcessIdParam) || EchoProcessIdParam.isEmpty()) {
			throw new IllegalArgumentException(
					"Required test run parameter not found: " + TestRunArg.ECHO_PROCESS_ID.toString());
		}
		String EchoProcessIdRefString = params.get(TestRunArg.ECHO_PROCESS_ID.toString());
		suite.setAttribute(SuiteAttribute.ECHO_PROCESS_ID.getName(), EchoProcessIdRefString); 
		
		/*
		//Define GC_XML_URI parameter
		String GcXmlUriParam 	= params.get(TestRunArg.GC_XML_URI.toString());
        URI GcXmlUriRef 		= URI.create(GcXmlUriParam.trim());
        File GcXmlUriFile 		= null;
        try {
        	GcXmlUriFile = URIUtils.dereferenceURI(GcXmlUriRef);
        }catch (Exception ex) {
            throw new RuntimeException("Failed to dereference resource located at " + GcXmlUriRef, ex);        	
        }
        Document GcXmlUriDoc = null;
        try {
        	GcXmlUriDoc = URIUtils.parseURI(GcXmlUriFile.toURI());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse resource retrieved from " + GcXmlUriRef, ex);
        }
        suite.setAttribute(SuiteAttribute.GC_XML_URI.getName(), GcXmlUriDoc);
        
        //Define DP_XML_URI parameter
		String DpXmlUriParam 	= params.get(TestRunArg.DP_XML_URI.toString());
        URI DpXmlUriRef 		= URI.create(DpXmlUriParam.trim());
        File DpXmlUriFile 		= null;
        try {
        	DpXmlUriFile = URIUtils.dereferenceURI(DpXmlUriRef);
        }catch (Exception ex) {
            throw new RuntimeException("Failed to dereference resource located at " + DpXmlUriRef, ex);        	
        }
        Document DpXmlUriDoc = null;
        try {
        	DpXmlUriDoc = URIUtils.parseURI(DpXmlUriFile.toURI());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse resource retrieved from " + DpXmlUriRef, ex);
        }
        suite.setAttribute(SuiteAttribute.DP_XML_URI.getName(), DpXmlUriDoc);
        */
        
        
        if (TestSuiteLogger.isLoggable(Level.FINE)) {
            StringBuilder logMsg = new StringBuilder("Parsed resource retrieved from ");
            //logMsg.append(iutRef).append("\n");
            //logMsg.append(XMLUtils.writeNodeToString(iutDoc));
            logMsg.append(ServiceUrlParam).append("\n");
            TestSuiteLogger.log(Level.FINE, logMsg.toString());
        }
    }

    /**
     * A client component is added to the suite fixture as the value of the
     * {@link SuiteAttribute#CLIENT} attribute; it may be subsequently accessed
     * via the {@link org.testng.ITestContext#getSuite()} method.
     *
     * @param suite
     *            The test suite instance.
     */
    void registerClientComponent(ISuite suite) {
        Client client = ClientUtils.buildClient();
        if (null != client) {
            suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
        }
    }

    /**
     * Deletes temporary files created during the test run if TestSuiteLogger is
     * enabled at the INFO level or higher (they are left intact at the CONFIG
     * level or lower).
     *
     * @param suite
     *            The test suite.
     */
    void deleteTempFiles(ISuite suite) {
        if (TestSuiteLogger.isLoggable(Level.CONFIG)) {
            return;
        }
        File testSubjFile = (File) suite.getAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName());
        if (testSubjFile.exists()) {
            testSubjFile.delete();
        }
    }
}
