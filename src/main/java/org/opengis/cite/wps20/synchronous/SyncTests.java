package org.opengis.cite.wps20.synchronous;

import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
//import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
//import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
//import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

//import org.opengis.cite.wps20.Namespaces;
//import org.opengis.cite.wps20.SuiteAttribute;
import org.opengis.cite.wps20.CommonFixture;
import org.opengis.cite.wps20.basictests.BasicTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
//import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;*/

import org.opengis.cite.wps20.util.*;

public class SyncTests extends CommonFixture {
	
	/**
	 * A.5.11. Verify that the server can handle the execution mode 'synchronous'
	 * requested via POST/XML Flow of Test Description: Send a valid XML Execute
	 * request to the server under test, setting the “mode” attribute to “sync”.
	 * Verify that a valid Execute wps:Result is returned.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.11. Verify that the server can handle the execution mode 'synchronous' requested via POST/XML.")
	public void ValidSyncExcecuteViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();
		Document literalDocument = GetDocumentTemplate(LITERAL_REQUEST_TEMPLATE_PATH, this.EchoProcessId, LITERAL_INPUT_ID, LITERAL_OUTPUT_ID);

		// Response document
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "sync");
		executeElement.setAttribute("response", "document");

		/*
		 * try { prettyPrint(literalDocument); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		
		/*
		 * // Code by Aries //status code is 200 response validation HttpURLConnection
		 * conn = GetConnection(SERVICE_URL); conn.setRequestMethod("POST");
		 * conn.setRequestProperty("Content-Type", "application/xml");
		 * conn.setDoOutput(true);
		 * 
		 * DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
		 * String xml = TransformXMLDocumentToXMLString(literalDocument);
		 * outputStream.writeBytes(xml); outputStream.flush(); outputStream.close();
		 * 
		 * int responseCode = conn.getResponseCode(); boolean respDocFlag =
		 * (responseCode == HttpURLConnection.HTTP_OK);
		 */
		  
		String respDocResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument); 
		Document respDocResultDocument = TransformXMLStringToXMLDocument(respDocResult);
		boolean respDocFlag = respDocResultDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Result").getLength() > 0;
		String msg = "Invalid SyncExecute via POST/XML for WPS 2.0";
		assertTrue(respDocFlag, msg);		 

		// Raw data output
		executeElement.setAttribute("response", "raw");

		/*
		 * try { prettyPrint(literalDocument); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		String respRawResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		boolean VSE_Flag = respRawResult.contains("hello_literal");

		if (VSE_Flag) {
			String msg1 = "Valid SyncExecute via POST/XML for WPS 2.0";
			Assert.assertTrue(VSE_Flag, msg1);
		} else {
			String msg1 = "Invalid SyncExecute via POST/XML for WPS 2.0";
			Assert.assertTrue(VSE_Flag, msg1);
		}

		/*
		 * // The Process Flow should as this one String VSEXmlString =
		 * GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument); Document
		 * VSEDocument = TransformXMLStringToXMLDocument(VSEXmlString); Boolean VSE_Flag
		 * = (VSEDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
		 * "Result").getLength() > 0) ? true : false; if (VSE_Flag) { String msg =
		 * "Valid SyncExecute via POST/XML for WPS 2.0"; Assert.assertTrue(VSE_Flag,
		 * msg); } else { String msg = "Invalid SyncExecute via POST/XML for WPS 2.0";
		 * Assert.assertTrue(VSE_Flag, msg); }
		 */
	}
	
}
