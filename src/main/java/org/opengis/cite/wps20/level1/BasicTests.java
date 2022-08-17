package org.opengis.cite.wps20.level1;

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

public class BasicTests extends CommonFixture {

	String GET_CAPABILITIES_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/GetCapabilities.xml";
	String DESCRIBE_PROCESS_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/DescribeProcess.xml";
	String LITERAL_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Literal.xml";
	String COMPLEX_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Complex.xml";

	
	String INPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputValue.xml";
	String INPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputReference.xml";
	String OUTPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputValue.xml";
	String OUTPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputReference.xml";
	String UNIQUE_JOB_IDS_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidUniqueJobIds.xml";

	/**
	 * A.4.1. Verify that a given process description is in compliance with the
Process XML encoding. Verify that the tested document fulfils all requirements listed in
req/native-process/xml-encoding/process. 
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, dependsOnMethods = {}, groups = "A.4. WPS Process Model Encoding", description = "A.4.1. Verify that a given process description is in compliance with the Process XML encoding")
	private void VerifyProcessXMLEncoding() throws IOException, URISyntaxException, SAXException 	{
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document SEPDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		String ECHO_PROCESS_ID = this.EchoProcessId;
		
		Map<String, Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", ECHO_PROCESS_ID);
		String VPE_String = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);

		Boolean VPE_Flag = null;
		if (isXMLSchemaValid(VPE_String, "xsd/opengis/wps/2.0/wps.xsd")) {
			VPE_Flag = true;
			String msg = "Valid Process XML Encoding for WPS 2.0";
			Assert.assertTrue(VPE_Flag, msg);
		} else {
			VPE_Flag = false;
			String msg = "Invalid Process XML Encoding for WPS 2.0";
			Assert.assertTrue(VPE_Flag, msg);
		}
	}
	
	/**
	 * A.4.3. Verify that a given process description is in compliance with the
Process XML encoding. Verify that the tested document fulfills all requirements listed in
req/native-process/xml-encoding/process. 
	 * @throws Exception 
	 */
	@Test(enabled = true, dependsOnMethods = {}, groups = "A.4. WPS Process Model Encoding", description = "A.4.3. Verify that any XML data type description and values that are used in conjunction with the native process model are encoded in compliance with the process model XML encoding.")
	private void VerifyProcessDataTypeXMLEncoding() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document SEPDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		String ECHO_PROCESS_ID = this.EchoProcessId;
		
		Map<String, Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", ECHO_PROCESS_ID);
		String VPE_String = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document VPE_Document = TransformXMLStringToXMLDocument(VPE_String);
		
		Boolean HLCB_Flag = true;
		
		if(VPE_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "LiteralData").getLength() == 0) {
			HLCB_Flag = false;
		}
		
		if(VPE_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ComplexData").getLength() == 0) {
			HLCB_Flag = false;
		}
		
		if(VPE_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "BoundingBoxData").getLength() == 0) {
			HLCB_Flag = false;
		}
				
		if(HLCB_Flag) { 	
			Boolean VPE_Flag = null;
			String msg = null;
			
			if (isXMLSchemaValid(VPE_String, "xsd/opengis/wps/2.0/wps.xsd")) {
				VPE_Flag = true;
				msg = "Valid Process DataTypes XML Encoding for WPS 2.0";
			} else {
				VPE_Flag = false;
				msg = "Invalid Process DataTypes XML Encoding for WPS 2.0";
			}
			
			Assert.assertTrue(VPE_Flag, msg);
		} else {
			String msg = "The process should include ComplexData, LiteralData and BoundingBoxData";
			Assert.assertTrue(HLCB_Flag, msg);
		}
	}
	
	/**
	 * Precondition. Verify that the server can handle echo process
	 * Flow of Test Description - Step 1: Send a valid DescribeProcess request to the server under test, setting the identifier to the echo process id. Verify that the server offers an echo process.
	 * Flow of Test Description - Step 2: Send a valid Execute request to the server under test, setting the identifier to the echo process id. Verify that the server can handle echo process.
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "Precondition: Verify that the server can handle echo process")
	public void ValidEchoProcess() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document SEPDocument = URIUtils.parseURI(uriLiteralRequestTemplate);

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
//		prettyPrint(SEPDocument);

		String resultLiteral = GetContentFromPOSTXMLRequest(SERVICE_URL, SEPDocument);
		String msgLiteral = "Echo Process LiteralData Test Failed";
		// Check the response string is equal to hello_literal(hello_literal is defined
		// in file from LITERAL_REQUEST_TEMPLATE_PATH)
		Assert.assertTrue(resultLiteral.contains("hello_literal"), msgLiteral);

		// Test ComplexData
		URI uriComplexLiteralRequestTemplate = BasicTests.class.getResource(COMPLEX_REQUEST_TEMPLATE_PATH).toURI();
		SEPDocument = URIUtils.parseURI(uriComplexLiteralRequestTemplate);
		requestInputElement = (Element) SEPDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Input")
				.item(0);
		requestOutputElement = (Element) SEPDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Output")
				.item(0);
		requestIdElement = (Element) SEPDocument.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier")
				.item(0);
		// replace id
		requestIdElement.setTextContent(ECHO_PROCESS_ID);
		requestInputElement.setAttribute("id", complexInputId);
		requestOutputElement.setAttribute("id", complexOutputId);
		prettyPrint(SEPDocument);

		String responseComplex = GetContentFromPOSTXMLRequest(SERVICE_URL, SEPDocument);
		Document complexOutputDocument = TransformXMLStringToXMLDocument(responseComplex);
		String resultComplex = complexOutputDocument.getElementsByTagName("testElement").item(0).getTextContent();
		String msgComplex = "Echo Process ComplexData Test Failed";
		// Check the string in testElement tag is equal to hello_complex(hello_complex
		// is defined in file from COMPLEX_REQUEST_TEMPLATE_PATH)
		Assert.assertTrue(resultComplex.equals("hello_complex"), msgComplex);
	}

	/**
	 * A.5.1. Verify that the correctly handles the service name parameter Flow of
	 * Test Description: Send a parameter value equal to what is required. Verify
	 * that request succeeds. Send a parameter value not equal to what is required.
	 * Verify that request fails. Overall test passes if all individual tests pass.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, dependsOnMethods = {}, groups = "A.5. Basic Tests", description = "A.5.1. Verify that the correctly handles the service name parameter.")
	private void ValidServiceName() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		Map<String, Object> GC_TParameters = new LinkedHashMap<>();
		GC_TParameters.put("Service", "WPS");
		GC_TParameters.put("Version", "2.0.0");
		GC_TParameters.put("Request", "GetCapabilities");

		Map<String, Object> GC_FParameters = new LinkedHashMap<>();
		GC_FParameters.put("Service", "WMS");
		GC_FParameters.put("Version", "2.0.0");
		GC_FParameters.put("Request", "GetCapabilities");

		Boolean GC_Flag = null;
		if (IsValidHTTP(SERVICE_URL, GC_TParameters) && !IsValidHTTP(SERVICE_URL, GC_FParameters)) {
			GC_Flag = true;
			String msg = "Valid Service Name for WPS 2.0";
			Assert.assertTrue(GC_Flag, msg);
		} else {
			GC_Flag = false;
			String msg = "Invalid Service Name for WPS 2.0";
			Assert.assertTrue(GC_Flag, msg);
		}
	}

	/**
	 * A.5.2. Verify that the correctly handles the service version parameter Flow
	 * of Test Description: Send a parameter value equal to what is required. Verify
	 * that request succeeds. Send a parameter value not equal to what is required.
	 * Verify that request fails. Overall test passes if all individual tests pass.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.2. Verify that the correctly handles the service version parameter.")
	private void ValidServiceVersion() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		Map<String, Object> GC_TParameters = new LinkedHashMap<>();
		GC_TParameters.put("Service", "WPS");
		GC_TParameters.put("Version", "2.0.0");
		GC_TParameters.put("Request", "GetCapabilities");

		Map<String, Object> GC_FParameters = new LinkedHashMap<>();
		GC_FParameters.put("Service", "WPS");
		GC_FParameters.put("Version", "3.0.0");
		GC_FParameters.put("Request", "GetCapabilities");

		Boolean GC_Flag = null;
		if (IsValidHTTP(SERVICE_URL, GC_TParameters) && !IsValidHTTP(SERVICE_URL, GC_FParameters)) {
			GC_Flag = true;
			String msg = "Valid Service Version for WPS 2.0";
			Assert.assertTrue(GC_Flag, msg);
		} else {
			GC_Flag = false;
			String msg = "Invalid Service Version for WPS 2.0";
			Assert.assertTrue(GC_Flag, msg);
		}
	}

	/**
	 * A.5.3. Verify that the server correctly handles input data transmission by
	 * value. Flow of Test Description: Send Execute requests to the server under
	 * test with valid inputs passed by value. Test passed if the execution finishes
	 * successfully.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.3. Verify that the server correctly handles input data transmission by value.")
	private void ValidInputDataTranmissionByValue() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI URIInputValueTemplate = BasicTests.class.getResource(INPUT_VALUE_TRANSMISSION_TEMPLATE_PATH).toURI();
		Document InputValueDocument = URIUtils.parseURI(URIInputValueTemplate);

		String InputValueResponse = GetContentFromPOSTXMLRequest(SERVICE_URL, InputValueDocument);
		Document InputValueResponseDocument = TransformXMLStringToXMLDocument(InputValueResponse);
		NodeList IVRD_List = InputValueResponseDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Data");
		boolean IVRD_Flag = IVRD_List.getLength() > 0;

		if (IVRD_Flag) {
			String msg = "Valid Input Data Transmission by Value for WPS 2.0";
			Assert.assertTrue(IVRD_Flag, msg);
		} else {
			String msg = "Invalid Input Data Transmission by Value for WPS 2.0";
			Assert.assertTrue(IVRD_Flag, msg);
		}
	}

	/**
	 * A.5.4. Verify that the server correctly handles input data transmission by
	 * reference. Flow of Test Description: Send Execute requests to the server
	 * under test with valid inputs passed by reference. Test passed if the
	 * execution finishes successfully.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.4. Verify that the server correctly handles input data transmission by reference.")
	private void ValidInputDataTranmissionByReference() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI URIInputReferenceTemplate = BasicTests.class.getResource(INPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH)
				.toURI();
		Document InputReferenceDocument = URIUtils.parseURI(URIInputReferenceTemplate);

		String InputReferenceResponse = GetContentFromPOSTXMLRequest(SERVICE_URL, InputReferenceDocument);
		Document InputReferenceResponseDocument = TransformXMLStringToXMLDocument(InputReferenceResponse);
		NodeList IRRD_List = InputReferenceResponseDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Result");
		boolean IRRD_Flag = IRRD_List.getLength() > 0;

		if (IRRD_Flag) {
			String msg = "Valid Input Data Transmission by Reference for WPS 2.0";
			Assert.assertTrue(IRRD_Flag, msg);
		} else {
			String msg = "Invalid Input Data Transmission by Reference for WPS 2.0";
			Assert.assertTrue(IRRD_Flag, msg);
		}
	}

	/**
	 * A.5.5. Verify that the server correctly handles output data transmission by
	 * value. Flow of Test Description: Check the available process offerings for
	 * outputs that can be retrieved by value. If there is an output that can be
	 * retrieved by value, send an Execute request to the server requesting the
	 * output by value. Test passes if a valid Execute response is returned
	 * containing the requested output. Skip this test if no output can be retrieved
	 * by value.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.5. Verify that the server correctly handles output data transmission by value.")
	private void ValidOutDataTranmissionByValue() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI URIOutputValueTemplate = BasicTests.class.getResource(OUTPUT_VALUE_TRANSMISSION_TEMPLATE_PATH).toURI();
		Document OutputValueDocument = URIUtils.parseURI(URIOutputValueTemplate);

		String OutputValueResponse = GetContentFromPOSTXMLRequest(SERVICE_URL, OutputValueDocument);
		Document OutputValueResponseDocument = TransformXMLStringToXMLDocument(OutputValueResponse);
		NodeList OVRD_List = OutputValueResponseDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Data");
		boolean OVRD_Flag = OVRD_List.getLength() > 0;

		if (OVRD_Flag) {
			String msg = "Valid Output Data Transmission by Value for WPS 2.0";
			Assert.assertTrue(OVRD_Flag, msg);
		} else {
			String msg = "Invalid Output Data Transmission by Value for WPS 2.0";
			Assert.assertTrue(OVRD_Flag, msg);
		}
	}

	/**
	 * A.5.6. Verify that the server correctly handles output data transmission by
	 * reference. Flow of Test Description: Check the available process offerings
	 * for outputs that can be retrieved by value. If there is an output that can be
	 * retrieved by value, send an Execute request to the server requesting the
	 * output by reference. Test passes if a valid Execute response is returned
	 * containing the requested output. Skip this test if no output can be retrieved
	 * by reference.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.6. Verify that the server correctly handles output data transmission by reference.")
	private void ValidOutDataTranmissionByReference() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI URIOutputReferenceTemplate = BasicTests.class.getResource(OUTPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH)
				.toURI();
		Document OutputReferenceDocument = URIUtils.parseURI(URIOutputReferenceTemplate);

		String OutputReferenceResponse = GetContentFromPOSTXMLRequest(SERVICE_URL, OutputReferenceDocument);
		Document OutputReferenceResponseDocument = TransformXMLStringToXMLDocument(OutputReferenceResponse);
		NodeList ORRD_List = OutputReferenceResponseDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
				"Reference");
		boolean ORRD_Flag = ORRD_List.getLength() > 0;

		if (ORRD_Flag) {
			String msg = "Valid Output Data Transmission by Reference for WPS 2.0";
			Assert.assertTrue(ORRD_Flag, msg);
		} else {
			String msg = "Invalid Output Data Transmission by Reference for WPS 2.0";
			Assert.assertTrue(ORRD_Flag, msg);
		}
	}

	/**
	 * A.5.7. Verify that each process the server offers has a unique identifier
	 * Flow of Test Description: Get all available processes from the server under
	 * test. Test passes if all processes have a unique identifier.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.7. Verify that each process the server offers has a unique identifier.")
	private void ValidUniqueIdentifier() {
		String SERVICE_URL = this.ServiceUrl.toString();

		Map<String, Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", "ALL");
		String DPXmlString = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document DPDocument = TransformXMLStringToXMLDocument(DPXmlString);
		NodeList DPList = DPDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Process");

		Boolean UI_Flag = true;
		Set<String> PNameList = new HashSet<>();
		for (int i = 0; i < DPList.getLength(); i++) {
			Element PDocument = (Element) DPList.item(i);
			String PName = PDocument.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
					.getTextContent();
			if (PNameList.add(PName) == false) {
				UI_Flag = false;
				break;
			}
		}

		if (UI_Flag) {
			String msg = "Valid Unique Identifier for WPS 2.0";
			Assert.assertTrue(UI_Flag, msg);
		} else {
			String msg = "Invalid Unique Identifier for WPS 2.0";
			Assert.assertTrue(UI_Flag, msg);
		}
	}

	/**
	 * A.5.8. Verify that the server creates a unique jobID for each job Flow of
	 * Test Description: Send more than one asynchronous Execute requests to the
	 * server under test. Test passes if the retrieved JobIDs differ from each
	 * other.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.8. Verify that the server creates a unique jobID for each job.")
	private void ValidUniqueJobIdentifier() throws URISyntaxException, SAXException, IOException {
		String SERVICE_URL = this.ServiceUrl.toString();

		Boolean UJRD_Flag = true;
		Set<String> JNameList = new HashSet<>();

		Random rand = new Random();
		int value = rand.nextInt(10);

		for (int i = 0; i < value; i++) {
			URI URIUniqueJobIdsTemplate = BasicTests.class.getResource(UNIQUE_JOB_IDS_TEMPLATE_PATH).toURI();
			Document UniqueJobIdsDocument = URIUtils.parseURI(URIUniqueJobIdsTemplate);
			String UniqueJobIdsResponse = GetContentFromPOSTXMLRequest(SERVICE_URL, UniqueJobIdsDocument);
			Document UniqueJobIdsResponseDocument = TransformXMLStringToXMLDocument(UniqueJobIdsResponse);
			String JName = UniqueJobIdsResponseDocument
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "JobID").item(0).getTextContent();
			if (JNameList.add(JName) == false) {
				UJRD_Flag = false;
				break;
			}
		}

		if (UJRD_Flag) {
			String msg = "Valid Unique Job Ids for WPS 2.0";
			Assert.assertTrue(UJRD_Flag, msg);
		} else {
			String msg = "Invalid Unique Job Ids for WPS 2.0";
			Assert.assertTrue(UJRD_Flag, msg);
		}
	}

	/**
	 * A.5.9. Verify that the server can handle GetCapabilities requests via
	 * POST/XML Flow of Test Description: Send a valid GetCapabilities request to
	 * the server under test. Test passes if a valid document of the type
	 * wps:Capabilities is returned.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.9. Verify that the server can handle GetCapabilities requests via POST/XML.")
	public void ValidGetCapabilitiesViaPOSTXML() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();
		URI uriGetCapabilitiesRequestTemplate = BasicTests.class.getResource(GET_CAPABILITIES_REQUEST_TEMPLATE_PATH)
				.toURI();
		Document GCDocument = URIUtils.parseURI(uriGetCapabilitiesRequestTemplate);
		String GCRXmlString = GetContentFromPOSTXMLRequest(SERVICE_URL, GCDocument);
		Document GCRDocument = TransformXMLStringToXMLDocument(GCRXmlString);

		Boolean GCP_Flag = (GCRDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Capabilities")
				.getLength() > 0) ? true : false;
		if (GCP_Flag) {
			String msg = "Valid GetCapabilities via POST/XML for WPS 2.0";
			Assert.assertTrue(GCP_Flag, msg);
		} else {
			String msg = "Invalid GetCapabilities via POST/XML for WPS 2.0";
			Assert.assertTrue(GCP_Flag, msg);
		}
	}

	/**
	 * A.5.10. Verify that the server can handle DescribeProcess requests via
	 * POST/XML Flow of Test Description: Send a valid DescribeProcess request to
	 * the server under test. Test passes if a valid document of the type
	 * wps:ProcessOfferings is returned.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.10. Verify that the server can handle DescribeProcess requests via POST/XML.")
	public void ValidDescribeProcessViaPOSTXML() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();
		URI uriDescribeProcessRequestTemplate = BasicTests.class.getResource(DESCRIBE_PROCESS_REQUEST_TEMPLATE_PATH)
				.toURI();
		Document DPDocument = URIUtils.parseURI(uriDescribeProcessRequestTemplate);
		DPDocument.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
				.setTextContent(this.EchoProcessId);

		String DPRXmlString = GetContentFromPOSTXMLRequest(SERVICE_URL, DPDocument);
		Document DPRDocument = TransformXMLStringToXMLDocument(DPRXmlString);

		Boolean DPP_Flag = (DPRDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ProcessOfferings")
				.getLength() > 0) ? true : false;
		if (DPP_Flag) {
			String msg = "Valid DescribeProcess via POST/XML for WPS 2.0";
			Assert.assertTrue(DPP_Flag, msg);
		} else {
			String msg = "Invalid DescribeProcess via POST/XML for WPS 2.0";
			Assert.assertTrue(DPP_Flag, msg);
		}
	}

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

	/**
	 * A.5.12. Verify that the server can handle the execution mode 'asynchronous'
	 * requested via POST/XML Flow of Test Description: Flow of Test Description:
	 * Send a valid XML Execute request to the server under test, setting the “mode”
	 * attribute to “async”. Verify that a valid Execute wps:Result is returned.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.12. Verify that the server can handle the execution mode 'asynchronous' requested via POST/XML.")
	public void ValidAsyncExcecuteViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);

		// Process Literal Request
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);

		// Response document
		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);
		executeElement.setAttribute("mode", "async");
		executeElement.setAttribute("response", "document");

		/*
		 * try { prettyPrint(literalDocument); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		String VAEXmlString = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document VAEDocument = TransformXMLStringToXMLDocument(VAEXmlString);

		Boolean VAE_Flag = (VAEDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Status").getLength() > 0) ? true : false;

		if (VAE_Flag) {
			String msg = "Valid AsyncExecute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		} else {
			String msg = "Invalid AsyncExecute via POST/XML for WPS 2.0";
			Assert.assertTrue(VAE_Flag, msg);
		}

		/*
		 * Code by Aries //status code is 200 response validation HttpURLConnection conn
		 * = GetConnection(SERVICE_URL); conn.setRequestMethod("POST");
		 * conn.setRequestProperty("Content-Type", "application/xml");
		 * conn.setDoOutput(true);
		 * 
		 * DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
		 * String xml = TransformXMLDocumentToXMLString(literalDocument);
		 * outputStream.writeBytes(xml); outputStream.flush(); outputStream.close();
		 * 
		 * int responseCode = conn.getResponseCode(); StringBuilder builder = new
		 * StringBuilder(); // read response BufferedReader in; if(responseCode > 299)
		 * in = new BufferedReader(new InputStreamReader(conn.getErrorStream())); else
		 * in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		 * 
		 * String str; while ((str = in.readLine()) != null) { builder.append(str); }
		 * in.close(); System.out.println(builder.toString());
		 * 
		 * boolean respDocFlag = (responseCode == HttpURLConnection.HTTP_OK);
		 * 
		 * if (respDocFlag) { String msg =
		 * "Valid AsyncExecute via POST/XML for WPS 2.0"; Assert.assertTrue(respDocFlag,
		 * msg); } else { String msg = "Invalid AsyncExecute via POST/XML for WPS 2.0";
		 * Assert.assertTrue(respDocFlag, msg); }
		 */
	}

	/**
	 * A.5.13. Verify that the server can handle the execution mode 'auto' requested
	 * via POST/XML Flow of Test Description: Flow of Test Description: Send a valid
	 * XML Execute request to the server under test, setting the “mode” attribute to
	 * “auto”. Verify that a valid Execute wps:Result is returned.
	 * 
	 * @throws Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.13. Verify that the server can handle the execution mode 'auto' requested via POST/XML.")
	public void ValidAutoExcecuteViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();

		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		// Process Literal Request
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);

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

		Element ProcessOfferingElement = (Element) responseDescribeProcessDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ProcessOffering").item(0);
		// Element ProcessOfferingElement = (Element)
		// responseDescribeProcessDocument.getElementsByTagName("wps:ProcessOffering").item(0);
		// System.out.println(ProcessOfferingElement.getAttribute("outputTransmission"));
		String outputTransmission = ProcessOfferingElement.getAttribute("outputTransmission");
		String jobControlOptions = ProcessOfferingElement.getAttribute("jobControlOptions");

		Element executeElement = (Element) literalDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute").item(0);

		// case 1
		if (outputTransmission.contains("reference")) {
			// Response document
			executeElement.setAttribute("response", "document");

			// case 1c runs both case 1a and 1b
			// case 1a
			if (jobControlOptions.contains("sync-execute")) {
				executeElement.setAttribute("mode", "sync");
				TestPostWithDocumentAndAssertMessage(SERVICE_URL, literalDocument,
						"Invalid SyncExecute via POST/XML for WPS 2.0");
			}

			// case 1b
			if (jobControlOptions.contains("async-execute")) {
				executeElement.setAttribute("mode", "async");
				TestPostWithDocumentAndAssertMessage(SERVICE_URL, literalDocument,
						"Invalid AsyncExecute via POST/XML for WPS 2.0");
			}

		}

		// case 2
		if (outputTransmission.contains("value")) {
			// Raw
			executeElement.setAttribute("response", "raw");

			// case 2c runs both case 2a and 2b
			// case 2a
			if (jobControlOptions.contains("sync-execute")) {
				executeElement.setAttribute("mode", "sync");
				String respRawResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
				boolean respRawFlag = respRawResult.contains("hello_literal");
				assertTrue(respRawFlag, "Invalid SyncExecute via POST/XML for WPS 2.0");
			}

			// case 2b
			if (jobControlOptions.contains("async-execute")) {
				executeElement.setAttribute("mode", "async");
				TestPostWithDocumentAndAssertMessage(SERVICE_URL, literalDocument,
						"Invalid AsyncExecute via POST/XML for WPS 2.0");
			}
		}
	}

	/**
	 * A.5.16. Verify that the server can handle GetCapabilities requests via
	 * GET/KVP
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.16. Verify that the server can handle GetCapabilities requests via GET/KVP.")
	private void ValidGetCapabilitiesViaGETKVP() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		Map<String, Object> GCU_Parameters = new LinkedHashMap<>();
		GCU_Parameters.put("Service".toUpperCase(), "WPS");
		GCU_Parameters.put("Version".toUpperCase(), "2.0.0");
		GCU_Parameters.put("Request".toUpperCase(), "GetCapabilities");
		String GCU_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, GCU_Parameters);
		Document GCU_Document = TransformXMLStringToXMLDocument(GCU_XmlString);

		Map<String, Object> GCL_Parameters = new LinkedHashMap<>();
		GCL_Parameters.put("Service".toLowerCase(), "WPS");
		GCL_Parameters.put("Version".toLowerCase(), "2.0.0");
		GCL_Parameters.put("Request".toLowerCase(), "GetCapabilities");
		String GCL_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, GCL_Parameters);
		Document GCL_Document = TransformXMLStringToXMLDocument(GCL_XmlString);

		Boolean GC_KVP_Flag = (GCU_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Capabilities")
				.getLength() > 0
				&& GCL_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Capabilities")
						.getLength() > 0) ? true : false;
		if (GC_KVP_Flag) {
			String msg = "Valid GetCapabilities via KVP for WPS 2.0";
			Assert.assertTrue(GC_KVP_Flag, msg);
		} else {
			String msg = "Invalid GetCapabilities via KVP for WPS 2.0";
			Assert.assertTrue(GC_KVP_Flag, msg);
		}
	}

	/**
	 * A.5.17. Verify that the server can handle DescribeProcess requests via
	 * GET/KVP
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests", description = "A.5.17. Verify that the server can handle DescribeProcess requests via GET/KVP.")
	private void ValidDescribeProcessViaGETKVP() throws IOException, URISyntaxException, SAXException {
		String SERVICE_URL = this.ServiceUrl.toString();

		Map<String, Object> GC_Parameters = new LinkedHashMap<>();
		GC_Parameters.put("Service", "WPS");
		GC_Parameters.put("Version", "2.0.0");
		GC_Parameters.put("Request", "GetCapabilities");
		String GC_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, GC_Parameters);
		Document GC_Document = TransformXMLStringToXMLDocument(GC_XmlString);
		String IdentifierName = GC_Document.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier")
				.item(0).getTextContent();

		Map<String, Object> DPU_Parameters = new LinkedHashMap<>();
		DPU_Parameters.put("Service".toUpperCase(), "WPS");
		DPU_Parameters.put("Version".toUpperCase(), "2.0.0");
		DPU_Parameters.put("Request".toUpperCase(), "DescribeProcess");
		DPU_Parameters.put("Identifier".toUpperCase(), IdentifierName);
		String DPU_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, DPU_Parameters);
		Document DPU_Document = TransformXMLStringToXMLDocument(DPU_XmlString);

		Map<String, Object> DPL_Parameters = new LinkedHashMap<>();
		DPL_Parameters.put("Service".toLowerCase(), "WPS");
		DPL_Parameters.put("Version".toLowerCase(), "2.0.0");
		DPL_Parameters.put("Request".toLowerCase(), "DescribeProcess");
		DPL_Parameters.put("Identifier".toLowerCase(), IdentifierName);
		String DPL_XmlString = GetContentFromGETKVPRequest(SERVICE_URL, DPL_Parameters);
		Document DPL_Document = TransformXMLStringToXMLDocument(DPL_XmlString);

		Boolean DP_KVP_Flag = (DPU_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ProcessOfferings")
				.getLength() > 0
				&& DPL_Document.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ProcessOfferings")
						.getLength() > 0) ? true : false;
		if (DP_KVP_Flag) {
			String msg = "Valid DescribeProcess via KVP for WPS 2.0";
			Assert.assertTrue(DP_KVP_Flag, msg);
		} else {
			String msg = "Invalid DescribeProcess via KVP for WPS 2.0";
			Assert.assertTrue(DP_KVP_Flag, msg);
		}
	}

	public void TestPostWithDocumentAndAssertMessage(String SERVICE_URL, Document literalDocument, String message)
			throws Exception {
		// status code is 200 response validation
		HttpURLConnection conn = GetConnection(SERVICE_URL);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setDoOutput(true);

		DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
		String xml = TransformXMLDocumentToXMLString(literalDocument);
		outputStream.writeBytes(xml);
		outputStream.flush();
		outputStream.close();

		int responseCode = conn.getResponseCode();
		boolean respDocFlag = (responseCode == HttpURLConnection.HTTP_OK);
		assertTrue(respDocFlag, message);
	}

	/**
	 * Description: Identify that a XML document is valid with XSD Template or not
	 * 
	 * @param xmlString
	 * @param xsdPath
	 * @return
	 */
	private static boolean isXMLSchemaValid(String xmlString, String xsdPath) {
		try {
			Schema schema = ValidationUtils.createSchema(xsdPath);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(xmlString)));
		} catch (IOException | SAXException e) {
			System.out.println("Exception: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @param xmlString
	 * @return
	 */
	private static Document TransformXMLStringToXMLDocument(String xmlString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param URI
	 * @return
	 */
	private static Document TransformXMLFileToXMLDocument(String URI) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(URI));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param xmlDoc
	 * @throws Exception
	 */
	private static String TransformXMLDocumentToXMLString(Document xmlDoc) throws Exception {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xmlDoc), new StreamResult(out));
		return out.toString();
	}

	/**
	 * @param xmlDoc
	 * @throws Exception
	 */
	private static void prettyPrint(Document xmlDoc) throws Exception {
		String str = TransformXMLDocumentToXMLString(xmlDoc);
		System.out.println(str);
	}

	/**
	 * Description: Identify that URL could get the response or not
	 * 
	 * @param any_url
	 * @return
	 * @throws IOException
	 */
	private static Boolean IsValidHTTP(String any_url, Map<String, Object> params) throws IOException {
		StringBuilder Data = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (Data.length() != 0)
				Data.append('&');
			Data.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			Data.append('=');
			Data.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		URL url = new URL(any_url + "?" + Data.toString());
		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		huc.setRequestMethod("GET");
		int responseCode = huc.getResponseCode();
		return (responseCode != HttpURLConnection.HTTP_OK) ? false : true;
	}

	/**
	 * Description: Send GET request with parameters and return Response as String
	 * 
	 * @param any_url
	 * @param params
	 * @return
	 */
	private static String GetContentFromGETKVPRequest(String any_url, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder();
		HttpURLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			StringBuilder Data = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (Data.length() != 0)
					Data.append('&');
				Data.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				Data.append('=');
				Data.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			URL url = new URL(any_url + "?" + Data.toString());
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("GET");
			urlConn.setDoOutput(true);
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:" + any_url, e);
		}
		return sb.toString();
	}

	/**
	 * Description: Send POST request with parameters and return Response as String
	 * 
	 * @param any_url
	 * @param params
	 * @return
	 */
	private static String GetContentFromPOSTXMLRequest(String any_url, Document xml_doc) {
		StringBuilder sb = new StringBuilder();
		HttpURLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(xml_doc), new StreamResult(out));
			byte[] postDataBytes = out.toString().getBytes("UTF-8");

			URL url = new URL(any_url);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("POST");
			urlConn.setRequestProperty("Content-Type", "application/xml");
			urlConn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			urlConn.setDoOutput(true);
			urlConn.getOutputStream().write(postDataBytes);
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:" + any_url, e);
		}
		return sb.toString();
	}

	private HttpURLConnection GetConnection(String serviceURL) throws IOException {
		URL urlObj = new URL(serviceURL);
		return (HttpURLConnection) urlObj.openConnection();
	}
}
