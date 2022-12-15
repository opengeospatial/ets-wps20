package org.opengis.cite.wps20.asynchronous;

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

public class AsyncTests extends CommonFixture {
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
	 * A.5.14. Verify that the server can handle GetStatus requests via POST/XML. 
	 * Flow of Test Description: Send a valid XML Execute request to the server under test, 
	 * setting the “mode” attribute to “async”. Verify that a valid wps:StatusInfo document 
	 * is returned. Extract the wps:JobID. Send a valid XML GetStatus request to the server 
	 * under test using the extracted JobID. Test passes if a valid wps:StatusInfo document 
	 * is returned.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.14. Verify that the server can handle GetStatus requests via POST/XML.")
	public void ValidGetStatusViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "async");
		executeElement.setAttribute("response", "document");

		String VAEXmlString 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument 	= TransformXMLStringToXMLDocument(VAEXmlString);

		Boolean VAE_Flag = (VAEDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;

		if (VAE_Flag) {				 			
			Element JobIDElement1 	= (Element) VAEDocument
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			
			URI URIGetStatusTemplate = BasicTests.class.getResource(GET_STATUS_TEMPLATE_PATH).toURI();
			Document GetStatusDocument 	= URIUtils.parseURI(URIGetStatusTemplate);
			Element JobIDElement2 	= (Element) GetStatusDocument
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			JobIDElement2.setTextContent(JobIDElement1.getTextContent());
			
			String VGSXmlString 	= GetContentFromPOSTXMLRequest(SERVICE_URL, GetStatusDocument);
			Document VGSDocument 	= TransformXMLStringToXMLDocument(VGSXmlString);
			
			Boolean VGS_Flag = (VGSDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").getLength() > 0) ? true : false;
			
			if (VGS_Flag) {
				String msg = "Valid GetStatus via POST/XML for WPS 2.0";
				Assert.assertTrue(VGS_Flag, msg);
			} else {
				String msg = "Invalid GetStatus via POST/XML for WPS 2.0";
				Assert.assertTrue(VGS_Flag, msg);
			}
		} else {
			String msg = "Invalid Execute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		}
	}
	
	/**
	 * A.5.15. VSend a valid XML Execute request to the server under test, setting the “mode” 
	 * attribute to “async”. Modulate the “response” parameter. Verify that a valid wps:StatusInfo
	 * document is returned. Extract the wps:JobID. Check the status of the job. If the job 
	 * succeeded, send a valid XML GetResult request to the server under test using the extracted 
	 * JobID. Depending on the value of the “response” parameter of the above Execute request.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.15. Verify that the server can handle GetResult requests via POST/XML.")
	public void ValidGetResultViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "async");
		executeElement.setAttribute("response", "raw");
		String VAEXmlString1 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument1	= TransformXMLStringToXMLDocument(VAEXmlString1);
		Boolean VAE_Flag1 		= (VAEDocument1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;
		
		executeElement.setAttribute("response", "document");		
		String VAEXmlString2 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument2	= TransformXMLStringToXMLDocument(VAEXmlString2);
		Boolean VAE_Flag2 		= (VAEDocument2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;
		
		Boolean VAE_Flag = VAE_Flag1 && VAE_Flag2;
		
		if (VAE_Flag) {			
			URI URIGetResultTemplate1 = BasicTests.class.getResource(GET_RESULT_TEMPLATE_PATH).toURI();			
			Element JobIDElement1 	= (Element) VAEDocument1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);	
			Document GetResultDocument1 	= URIUtils.parseURI(URIGetResultTemplate1);
			Element JobIDElement2 	= (Element) GetResultDocument1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			JobIDElement2.setTextContent(JobIDElement1.getTextContent());			
			String VGRXmlString1 	= GetContentFromPOSTXMLRequest(SERVICE_URL, GetResultDocument1);
			Document VGRDocument1 	= TransformXMLStringToXMLDocument(VGRXmlString1);			
			Boolean VGR_Flag1 = (VGRDocument1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "LiteralValue").getLength() > 0) ? true : false;
			
			URI URIGetResultTemplate2 = BasicTests.class.getResource(GET_RESULT_TEMPLATE_PATH).toURI();	
			Element JobIDElement3 	= (Element) VAEDocument2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			Document GetResultDocument2 	= URIUtils.parseURI(URIGetResultTemplate2);
			Element JobIDElement4 	= (Element) GetResultDocument2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			JobIDElement4.setTextContent(JobIDElement3.getTextContent());			
			String VGRXmlString2 	= GetContentFromPOSTXMLRequest(SERVICE_URL, GetResultDocument2);
			
			Boolean VGR_Flag2 	= VGRXmlString2.contains("LiteralValue") ? true : false;

//			Document VGRDocument2 	= TransformXMLStringToXMLDocument(VGRXmlString2);			
//			Boolean VGR_Flag2 = (VGRDocument2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Result").getLength() > 0) ? true : false;			
			
			Boolean VGR_Flag 	= VGR_Flag1 && VGR_Flag2;
			
			if (VGR_Flag) {
				String msg = "Valid GetResult via POST/XML for WPS 2.0";
				Assert.assertTrue(VGR_Flag, msg);
			} else {
				String msg = "Invalid GetResult via POST/XML for WPS 2.0";
				Assert.assertTrue(VGR_Flag, msg);
			}
		} else {
			String msg = "Invalid Execute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		}
	}

	
	
	/**
	 * A.5.18. Verify that the server can handle GetStatus requests via GET/KVP. 
	 * Send a valid XML Execute request to the server under test, setting the “mode” attribute 
	 * to “async”. Verify that a valid wps:StatusInfo document is returned. Extract the 
	 * wps:JobID. Send a valid KVP GetStatus request to the server under test, using the 
	 * extracted JobID and modulating upper and lower case of the parameter names. Test passes 
	 * if a valid document of the type wps:StatusInfo is returned. 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.18. Verify that the server can handle GetStatus requests via GET/KVP.")
	private void ValidGetStatusViaGETKVP() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "async");
		executeElement.setAttribute("response", "document");

		String VAEXmlString 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument 	= TransformXMLStringToXMLDocument(VAEXmlString);

		Boolean VAE_Flag = (VAEDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;
		
		if (VAE_Flag) {				 			
			Element JobIDElement1 	= (Element) VAEDocument
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			
			Map<String, Object> GSU_Parameters = new LinkedHashMap<>();
			GSU_Parameters.put("Service".toUpperCase(), "WPS");
			GSU_Parameters.put("Version".toUpperCase(), "2.0.0");
			GSU_Parameters.put("Request".toUpperCase(), "GetStatus");
			GSU_Parameters.put("JobID".toUpperCase(), JobIDElement1.getTextContent());
			String GSU_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, GSU_Parameters);
			Document GSU_Document = TransformXMLStringToXMLDocument(GSU_XmlString);

			Map<String, Object> GSL_Parameters = new LinkedHashMap<>();
			GSL_Parameters.put("Service".toLowerCase(), "WPS");
			GSL_Parameters.put("Version".toLowerCase(), "2.0.0");
			GSL_Parameters.put("Request".toLowerCase(), "GetStatus");
			GSL_Parameters.put("JobID".toLowerCase(), JobIDElement1.getTextContent());
			String GSL_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, GSL_Parameters);
			Document GSL_Document = TransformXMLStringToXMLDocument(GSL_XmlString);

			Boolean GS_KVP_Flag = (GSU_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").getLength() > 0
					&& GSL_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").getLength() > 0) ? true : false;			
			
			if (GS_KVP_Flag) {
				String msg = "Valid GetStatus via GET/KVP for WPS 2.0";
				Assert.assertTrue(GS_KVP_Flag, msg);
			} else {
				String msg = "Invalid GetStatus via GET/KVP for WPS 2.0";
				Assert.assertTrue(GS_KVP_Flag, msg);
			}
		} else {
			String msg = "Invalid Execute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		}
	}
	
	/**
	 * A.5.19. Verify that the server can handle GetResult requests via GET/KVP. 
	 * Send a valid XML Execute request to the server under test, setting the “mode” attribute 
	 * to “async”. Modulate the “response” parameter. Verify that a valid wps:StatusInfo 
	 * document is returned. Extract the wps:JobID. Check the status of the job. If the job 
	 * succeeded, send a valid KVP GetResult request to the server under test using the 
	 * extracted JobID and modulating upper and lower case of the parameter names. Depending 
	 * on the value of the “response” parameter of the above Execute request: 
	 * - Parameter value equal “document”. Verify that a valid Execute wps:Result document is returned. 
	 * - Parameter equal to “raw”. Verify that raw is returned. 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.19. Verify that the server can handle GetResult requests via GET/KVP.")
	private void ValidGetResultViaGETKVP() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "async");
		
		executeElement.setAttribute("response", "document");
		String VAEXmlString1 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument1	= TransformXMLStringToXMLDocument(VAEXmlString1);
		Boolean VAE_Flag1 		= (VAEDocument1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;
		
		executeElement.setAttribute("response", "raw");		
		String VAEXmlString2 	= GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument2	= TransformXMLStringToXMLDocument(VAEXmlString2);
		Boolean VAE_Flag2 		= (VAEDocument2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "StatusInfo").getLength() > 0) ? true : false;
		
		Boolean VAE_Flag = VAE_Flag1 && VAE_Flag2;
		
		if (VAE_Flag) {
			Element JobIDElement1 	= (Element) VAEDocument1
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			CheckGetStatus(SERVICE_URL, JobIDElement1.getTextContent());			
			Map<String, Object> GR_Parameters1 = new LinkedHashMap<>();
			GR_Parameters1.put("Service", "WPS");
			GR_Parameters1.put("Version", "2.0.0");
			GR_Parameters1.put("Request", "GetResult");
			GR_Parameters1.put("JobID", JobIDElement1.getTextContent());
			String GR_XmlString1 	= GetContentFromGETKVPRequest(SERVICE_URL, GR_Parameters1);
			Document GR_Document1 	= TransformXMLStringToXMLDocument(GR_XmlString1);
			
			Boolean VGR_Flag1 = (GR_Document1.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Result").getLength() > 0) ? true : false;
			
			Element JobIDElement2 	= (Element) VAEDocument2
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0);
			CheckGetStatus(SERVICE_URL, JobIDElement2.getTextContent());			
			Map<String, Object> GR_Parameters2 = new LinkedHashMap<>();
			GR_Parameters2.put("Service", "WPS");
			GR_Parameters2.put("Version", "2.0.0");
			GR_Parameters2.put("Request", "GetResult");
			GR_Parameters2.put("JobID", JobIDElement2.getTextContent());
			String GR_XmlString2 	= GetContentFromGETKVPRequest(SERVICE_URL, GR_Parameters2);
//			System.out.println(GR_XmlString2);
//			System.out.println(JobIDElement2.getTextContent());
			
//			Boolean VGR_Flag2 = GR_XmlString2.contains("wps:LiteralValue") ? true : false;

			Document GR_Document2 	= TransformXMLStringToXMLDocument(GR_XmlString2);			
			Boolean VGR_Flag2 = (GR_Document2.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "LiteralValue").getLength() > 0) ? true : false;
			
			Boolean VGR_Flag = VGR_Flag1 && VGR_Flag2;
			
			if (VGR_Flag) {
				String msg = "Valid GetResult via GET/KVP for WPS 2.0";
				Assert.assertTrue(VGR_Flag, msg);
			} else {
				String msg = "Invalid GetResult via GET/KVP for WPS 2.0";
				Assert.assertTrue(VGR_Flag, msg);
			}
		} else {
			String msg = "Invalid Execute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		}
	}
	
	public void CheckGetStatus(String SERVICE_URL, String jobID) {
		Map<String, Object> GR_Parameters1 = new LinkedHashMap<>();
		GR_Parameters1.put("Service", "WPS");
		GR_Parameters1.put("Version", "2.0.0");
		GR_Parameters1.put("Request", "GetStatus");
		GR_Parameters1.put("JobID", jobID);

		String GR_XmlString1 = GetContentFromGETKVPRequest(SERVICE_URL, GR_Parameters1);
		Document docGetStatus = TransformXMLStringToXMLDocument(GR_XmlString1);
		Boolean statusFlag = (docGetStatus.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").getLength() > 0) ? true : false;
		String msg = "Invalid GetStatus via GET/KVP for WPS 2.0";
		if (!statusFlag)
			Assert.assertTrue(false, msg);
		else {
			Element statusElement = (Element) docGetStatus.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").item(0);
			String status = statusElement.getTextContent();
			if (status.toLowerCase().equals("succeeded")) {
				return;
			}
			else {
				Assert.assertTrue(false, msg);
			}
		}
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
