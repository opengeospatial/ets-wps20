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

	String GET_CAPABILITIES_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/GetCapabilities.xml";
	String DESCRIBE_PROCESS_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/DescribeProcess.xml";
	String LITERAL_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Literal.xml";
	String COMPLEX_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Complex.xml";

	
	String INPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputValue.xml";
	String INPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputReference.xml";
	String OUTPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputValue.xml";
	String OUTPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputReference.xml";
	String UNIQUE_JOB_IDS_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidUniqueJobIds.xml";
	String GET_STATUS_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidGetStatus.xml";
	String GET_RESULT_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidGetResult.xml";

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

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);

		// Process Literal Request
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);

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
	
	public void ProcessEchoProcessLiteralDataRequest(String SERVICE_URL, Document SEPDocument) {
		// Get the processid from user and replace the processid in the template xml
		// request file
		String ECHO_PROCESS_ID = this.EchoProcessId;

		// Parse the input id and output id in DescribeProcess
		Map<String, Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", ECHO_PROCESS_ID);
		String responseDescribeProcess = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document responseDescribeProcessDocument = TransformXMLStringToXMLDocument(responseDescribeProcess);

		// get input id
		NodeList inputList = responseDescribeProcessDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Input");
		String literalInputId = "", literalOutputId = "", complexInputId = "", complexOutputId = "";
		for (int i = 0; i < inputList.getLength(); i++) {
			Element element = (Element) inputList.item(i);
			Element literalInputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "LiteralData").item(0);
			Element complexInputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ComplexData").item(0);
			String Id = element.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
					.getTextContent();
			if (literalInputElement != null) {
				literalInputId = Id;
			} else if (complexInputElement != null) {
				complexInputId = Id;
			}
		}

		// get output id
		NodeList outputList = responseDescribeProcessDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Output");
		for (int i = 0; i < outputList.getLength(); i++) {
			Element element = (Element) outputList.item(i);
			Element literalOutputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "LiteralData").item(0);
			Element complexOutputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ComplexData").item(0);
			String Id = element.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
					.getTextContent();
			if (literalOutputElement != null) {
				literalOutputId = Id;
			} else if (complexOutputElement != null) {
				complexOutputId = Id;
			}
		}

		// Test LiteralData
		Element requestInputElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Input").item(0);
		Element requestOutputElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Output").item(0);
		Element requestIdElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0);
		// replace id
		requestIdElement.setTextContent(ECHO_PROCESS_ID);
		requestInputElement.setAttribute("id", literalInputId);
		requestOutputElement.setAttribute("id", literalOutputId);
	}	

}
