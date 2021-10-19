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
	
	/**
	 * A.5.0. Verify that the server can handle the execution mode 'synchronous' requested via POST/XML
	 * Flow of Test Description: Send a valid XML Execute request to the server under test, setting the “mode” attribute to “sync”. Verify that a valid Execute wps:Result is returned. 
	 * 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.0. Verify that the server can handle echo process")
	public void ValidEchoProcess() throws Exception { 
		String SERVICE_URL = this.ServiceUrl.toString();
		
		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document SEPDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		
		//Get the processid from user and replace the processid in the template xml request file
		String ECHO_PROCESS_ID = this.EchoProcessId;
		
		//Parse the input id and output id in DescribeProcess
		Map<String,Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", ECHO_PROCESS_ID);
		String responseDescribeProcess = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document responseDescribeProcessDocument = TransformXMLStringToXMLDocument(responseDescribeProcess);
		
		//get input id
		NodeList inputList = responseDescribeProcessDocument.getElementsByTagName("wps:Input");
		String literalInputId = "", literalOutputId = "", complexInputId = "", complexOutputId = "";
		for (int i = 0; i < inputList.getLength(); i++) {
			Element element = (Element) inputList.item(i);
			Element literalInputElement = (Element) element.getElementsByTagName("ns:LiteralData").item(0);
			Element complexInputElement = (Element) element.getElementsByTagName("ns:ComplexData").item(0);
			String Id = element.getElementsByTagName("ows:Identifier").item(0).getTextContent();
			if(literalInputElement != null) {
				literalInputId = Id;
			}
			else if(complexInputElement != null) {
				complexInputId = Id;
			}
		}
		
		//get output id
		NodeList outputList = responseDescribeProcessDocument.getElementsByTagName("wps:Output");
		for (int i = 0; i < outputList.getLength(); i++) {
			Element element = (Element) outputList.item(i);
			Element literalOutputElement = (Element) element.getElementsByTagName("ns:LiteralData").item(0);
			Element complexOutputElement = (Element) element.getElementsByTagName("ns:ComplexData").item(0);
			String Id = element.getElementsByTagName("ows:Identifier").item(0).getTextContent();
			if(literalOutputElement != null) {
				literalOutputId = Id;
			}
			else if(complexOutputElement != null) {
				complexOutputId = Id;
			}
		}
		
		//Test LiteralData
		Element requestInputElement = (Element) SEPDocument.getElementsByTagName("wps:Input").item(0);
		Element requestOutputElement = (Element) SEPDocument.getElementsByTagName("wps:Output").item(0);
		Element requestIdElement = (Element) SEPDocument.getElementsByTagName("ows:Identifier").item(0);
		//replace id
		requestIdElement.setTextContent(ECHO_PROCESS_ID);
		requestInputElement.setAttribute("id", literalInputId);
		requestOutputElement.setAttribute("id", literalOutputId);
		prettyPrint(SEPDocument);
		
		String resultLiteral = GetContentFromPOSTXMLRequest(SERVICE_URL, SEPDocument);
		String msgLiteral = "Echo Process LiteralData Test Failed";
		//Check the response string is equal to hello_literal(hello_literal is defined in file from LITERAL_REQUEST_TEMPLATE_PATH)
		Assert.assertTrue(resultLiteral.equals("hello_literal"), msgLiteral);
		
		
		//Test ComplexData
		URI uriComplexLiteralRequestTemplate = BasicTests.class.getResource(COMPLEX_REQUEST_TEMPLATE_PATH).toURI();
		SEPDocument = URIUtils.parseURI(uriComplexLiteralRequestTemplate);
		requestInputElement = (Element) SEPDocument.getElementsByTagName("wps:Input").item(0);
		requestOutputElement = (Element) SEPDocument.getElementsByTagName("wps:Output").item(0);
		requestIdElement = (Element) SEPDocument.getElementsByTagName("ows:Identifier").item(0);
		//replace id
		requestIdElement.setTextContent(ECHO_PROCESS_ID);
		requestInputElement.setAttribute("id", complexInputId);
		requestOutputElement.setAttribute("id", complexOutputId);
		prettyPrint(SEPDocument);
		
		String responseComplex = GetContentFromPOSTXMLRequest(SERVICE_URL, SEPDocument);
		Document complexOutputDocument = TransformXMLStringToXMLDocument(responseComplex);
		String resultComplex = complexOutputDocument.getElementsByTagName("testElement").item(0).getTextContent();
		String msgComplex = "Echo Process ComplexData Test Failed";
		//Check the string in testElement tag is equal to hello_complex(hello_complex is defined in file from COMPLEX_REQUEST_TEMPLATE_PATH)
		Assert.assertTrue(resultComplex.equals("hello_complex"), msgComplex);
	}
	
	/**
	 * A.5.1. Verify that the correctly handles the service name parameter 
	 * Flow of Test Description: Send a parameter value equal to what is required. Verify that request succeeds. Send a parameter value not equal to what is required. Verify that request
fails. Overall test passes if all individual tests pass.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException 
	 */
	@Test(enabled=true, dependsOnMethods={}, groups="A.5. Basic Tests", description="A.5.1. Verify that the correctly handles the service name parameter.")
	private void ValidServiceName() throws IOException,URISyntaxException, SAXException { 
		String SERVICE_URL 	= this.ServiceUrl.toString(); 
		
		Map<String,Object> GC_TParameters = new LinkedHashMap<>();
		GC_TParameters.put("Service", "WPS");
		GC_TParameters.put("Version", "2.0.0");
		GC_TParameters.put("Request", "GetCapabilities");
		
		Map<String,Object> GC_FParameters = new LinkedHashMap<>();
		GC_FParameters.put("Service", "WMS");
		GC_FParameters.put("Version", "2.0.0");
		GC_FParameters.put("Request", "GetCapabilities");
	    
		Boolean GC_Flag = null;
	    if(IsValidHTTP(SERVICE_URL, GC_TParameters) && !IsValidHTTP(SERVICE_URL, GC_FParameters)) {
	    	GC_Flag	 	= true;
	    	String msg 	= "Valid Service Name for WPS 2.0";
	    	Assert.assertTrue(GC_Flag, msg);
	    } else {
	    	GC_Flag	 	= false;
	    	String msg 	= "Invalid Service Name for WPS 2.0";
	    	Assert.assertTrue(GC_Flag, msg);
	    }
	 }
	
	/**
	 * A.5.2. Verify that the correctly handles the service version parameter 
	 * Flow of Test Description: Send a parameter value equal to what is required. Verify that request succeeds. Send a parameter value not equal to what is required. Verify that request
fails. Overall test passes if all individual tests pass.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.2. Verify that the correctly handles the service version parameter.")
	private void ValidServiceVersion() throws IOException,URISyntaxException, SAXException { 
		String SERVICE_URL 	= this.ServiceUrl.toString(); 
		
		Map<String,Object> GC_TParameters = new LinkedHashMap<>();
		GC_TParameters.put("Service", "WPS");
		GC_TParameters.put("Version", "2.0.0");
		GC_TParameters.put("Request", "GetCapabilities");
		
		Map<String,Object> GC_FParameters = new LinkedHashMap<>();
		GC_FParameters.put("Service", "WPS");
		GC_FParameters.put("Version", "3.0.0");
		GC_FParameters.put("Request", "GetCapabilities");
	    
		Boolean GC_Flag = null;
	    if(IsValidHTTP(SERVICE_URL, GC_TParameters) && !IsValidHTTP(SERVICE_URL, GC_FParameters)) {
	    	GC_Flag	 	= true;
	    	String msg 	= "Valid Service Version for WPS 2.0";
	    	Assert.assertTrue(GC_Flag, msg);
	    } else {
	    	GC_Flag	 	= false;
	    	String msg 	= "Invalid Service Version for WPS 2.0";
	    	Assert.assertTrue(GC_Flag, msg);
	    }
	 }
	
	/**
	 * A.5.7. Verify that each process the server offers has a unique identifier
	 * Flow of Test Description: Get all available processes from the server under test. Test passes if all processes have a unique identifier.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.7. Verify that each process the server offers has a unique identifier.")
	private void ValidUniqueIdentifier() { 
		String SERVICE_URL 	= this.ServiceUrl.toString(); 
		
		Map<String,Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", "ALL");
		String DPXmlString 	= GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document DPDocument = TransformXMLStringToXMLDocument(DPXmlString);
		NodeList DPList 	= DPDocument.getElementsByTagName("wps:Process");
		
		Boolean UI_Flag 		= true;
		Set<String> PNameList 	= new HashSet<>();
        for (int i = 0; i < DPList.getLength(); i++) {
        	Element PDocument 	= (Element) DPList.item(i);
			String PName 		= PDocument.getElementsByTagName("ows:Identifier").item(0).getTextContent();
            if (PNameList.add(PName) == false) {
            	UI_Flag = false;
            	break;
            }
        }
        
		if (UI_Flag) {
			String msg = "Valid Unique Identifier for WPS 2.0";
			Assert.assertTrue(UI_Flag, msg);
		} 
		else {
			String msg = "Invalid Unique Identifier for WPS 2.0"; 
			Assert.assertTrue(UI_Flag, msg); 
		}
		 
	 }
	
	/**
	 * A.5.9. Verify that the server can handle GetCapabilities requests via POST/XML
	 * Flow of Test Description: Send a valid GetCapabilities request to the server under test. Test passes if a valid document of the type wps:Capabilities is returned. 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.9. Verify that the server can handle GetCapabilities requests via POST/XML.")
	public void ValidGetCapabilitiesViaPOSTXML() throws IOException,URISyntaxException, SAXException { 
		String SERVICE_URL = this.ServiceUrl.toString();
		URI uriGetCapabilitiesRequestTemplate = BasicTests.class.getResource(GET_CAPABILITIES_REQUEST_TEMPLATE_PATH).toURI();
		Document GCDocument = URIUtils.parseURI(uriGetCapabilitiesRequestTemplate);
		String GCRXmlString = GetContentFromPOSTXMLRequest(SERVICE_URL, GCDocument);
		Document GCRDocument = TransformXMLStringToXMLDocument(GCRXmlString);
		
		Boolean GCP_Flag = (GCRDocument.getElementsByTagName("wps:Capabilities").getLength() > 0) ? true : false;
		if (GCP_Flag) {
			String msg = "Valid GetCapabilities via POST/XML for WPS 2.0";
			Assert.assertTrue(GCP_Flag, msg);
		} 
		else {
			String msg = "Invalid GetCapabilities via POST/XML for WPS 2.0"; 
			Assert.assertTrue(GCP_Flag, msg); 
		}
	}
	
	/**
	 * A.5.10. Verify that the server can handle DescribeProcess requests via POST/XML 
	 * Flow of Test Description: Send a valid DescribeProcess request to the server under test. Test passes if a valid document of the type wps:ProcessOfferings is returned.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws SAXException 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.10. Verify that the server can handle DescribeProcess requests via POST/XML.")
	public void ValidDescribeProcessViaPOSTXML() throws IOException,URISyntaxException, SAXException { 
		String SERVICE_URL = this.ServiceUrl.toString();
		URI uriDescribeProcessRequestTemplate = BasicTests.class.getResource(DESCRIBE_PROCESS_REQUEST_TEMPLATE_PATH).toURI();
		Document DPDocument = URIUtils.parseURI(uriDescribeProcessRequestTemplate);
		DPDocument.getElementsByTagName("ows:Identifier").item(0).setTextContent(this.EchoProcessId);;
		
		String DPRXmlString = GetContentFromPOSTXMLRequest(SERVICE_URL, DPDocument);
		Document DPRDocument = TransformXMLStringToXMLDocument(DPRXmlString);
		
		Boolean DPP_Flag = (DPRDocument.getElementsByTagName("wps:ProcessOfferings").getLength() > 0) ? true : false;
		if (DPP_Flag) {
			String msg = "Valid DescribeProcess via POST/XML for WPS 2.0";
			Assert.assertTrue(DPP_Flag, msg);
		} 
		else {
			String msg = "Invalid DescribeProcess via POST/XML for WPS 2.0"; 
			Assert.assertTrue(DPP_Flag, msg); 
		}
	}
	
	/**
	 * A.5.11. Verify that the server can handle the execution mode 'synchronous' requested via POST/XML
	 * Flow of Test Description: Send a valid XML Execute request to the server under test, setting the “mode” attribute to “sync”. Verify that a valid Execute wps:Result is returned. 
	 * @throws Exception 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.11. Verify that the server can handle the execution mode 'synchronous' requested via POST/XML.")
	public void ValidSyncExcecuteViaPOSTXML() throws Exception { 
		String SERVICE_URL = this.ServiceUrl.toString();
		
		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		
		//Process Literal Request
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		
		//Response document
		Element executeElement = (Element) literalDocument.getElementsByTagName("wps:Execute").item(0);
		executeElement.setAttribute("mode", "sync");
		executeElement.setAttribute("response", "document");
		try {
			prettyPrint(literalDocument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//status code is 200 response validation
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
		//String respDocResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		//Document respDocResultDocument = TransformXMLStringToXMLDocument(respDocResult);
		//boolean respDocFlag = respDocResultDocument.getElementsByTagName("wps:Result").getLength() > 0;
		String msg = "Invalid SyncExecute via POST/XML for WPS 2.0";
		assertTrue(respDocFlag, msg);
		
		//Raw data output
		executeElement.setAttribute("response", "raw");
		try {
			prettyPrint(literalDocument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String respRawResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		boolean respRawFlag = respRawResult.equals("hello_literal");
		assertTrue(respRawFlag, msg);
	}
	
	public void ProcessEchoProcessLiteralDataRequest(String SERVICE_URL, Document SEPDocument) {
		//Get the processid from user and replace the processid in the template xml request file
		String ECHO_PROCESS_ID = this.EchoProcessId;

		//Parse the input id and output id in DescribeProcess
		Map<String,Object> DP_Parameters = new LinkedHashMap<>();
		DP_Parameters.put("Service", "WPS");
		DP_Parameters.put("Version", "2.0.0");
		DP_Parameters.put("Request", "DescribeProcess");
		DP_Parameters.put("Identifier", ECHO_PROCESS_ID);
		String responseDescribeProcess = GetContentFromGETKVPRequest(SERVICE_URL, DP_Parameters);
		Document responseDescribeProcessDocument = TransformXMLStringToXMLDocument(responseDescribeProcess);

		//get input id
		NodeList inputList = responseDescribeProcessDocument.getElementsByTagName("wps:Input");
		String literalInputId = "", literalOutputId = "", complexInputId = "", complexOutputId = "";
		for (int i = 0; i < inputList.getLength(); i++) {
			Element element = (Element) inputList.item(i);
			Element literalInputElement = (Element) element.getElementsByTagName("ns:LiteralData").item(0);
			Element complexInputElement = (Element) element.getElementsByTagName("ns:ComplexData").item(0);
			String Id = element.getElementsByTagName("ows:Identifier").item(0).getTextContent();
			if(literalInputElement != null) {
				literalInputId = Id;
			}
			else if(complexInputElement != null) {
				complexInputId = Id;
			}
		}

		//get output id
		NodeList outputList = responseDescribeProcessDocument.getElementsByTagName("wps:Output");
		for (int i = 0; i < outputList.getLength(); i++) {
			Element element = (Element) outputList.item(i);
			Element literalOutputElement = (Element) element.getElementsByTagName("ns:LiteralData").item(0);
			Element complexOutputElement = (Element) element.getElementsByTagName("ns:ComplexData").item(0);
			String Id = element.getElementsByTagName("ows:Identifier").item(0).getTextContent();
			if(literalOutputElement != null) {
				literalOutputId = Id;
			}
			else if(complexOutputElement != null) {
				complexOutputId = Id;
			}
		}

		//Test LiteralData
		Element requestInputElement = (Element) SEPDocument.getElementsByTagName("wps:Input").item(0);
		Element requestOutputElement = (Element) SEPDocument.getElementsByTagName("wps:Output").item(0);
		Element requestIdElement = (Element) SEPDocument.getElementsByTagName("ows:Identifier").item(0);
		//replace id
		requestIdElement.setTextContent(ECHO_PROCESS_ID);
		requestInputElement.setAttribute("id", literalInputId);
		requestOutputElement.setAttribute("id", literalOutputId);
		
	}
	
	/**
	 * A.5.12. Verify that the server can handle the execution mode 'asynchronous' requested via POST/XML
	 * Flow of Test Description: Flow of Test Description: Send a valid XML Execute request to the server under test, setting the “mode” attribute to “async”. Verify that a valid Execute wps:Result is returned.
	 * @throws Exception 
	 */
	@Test(enabled=true, groups="A.5. Basic Tests", description="A.5.12. Verify that the server can handle the execution mode 'asynchronous' requested via POST/XML.")
	public void ValidAsyncExcecuteViaPOSTXML() throws Exception { 
		String SERVICE_URL = this.ServiceUrl.toString();
		
		URI uriLiteralRequestTemplate = BasicTests.class.getResource(LITERAL_REQUEST_TEMPLATE_PATH).toURI();
		Document literalDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		
		//Process Literal Request
		ProcessEchoProcessLiteralDataRequest(SERVICE_URL, literalDocument);
		
		//Response document
		Element executeElement = (Element) literalDocument.getElementsByTagName("wps:Execute").item(0);
		executeElement.setAttribute("mode", "async");
		executeElement.setAttribute("response", "document");
		try {
			prettyPrint(literalDocument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//status code is 200 response validation
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
		StringBuilder builder = new StringBuilder();
		// read response
		BufferedReader in;
		if(responseCode > 299)
		    in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		else
		    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String str;
		while ((str = in.readLine()) != null) {
			builder.append(str);
		}
		in.close();
		System.out.println(builder.toString());
		
		boolean respDocFlag = (responseCode == HttpURLConnection.HTTP_OK);
		String msg = "Invalid AsyncExecute via POST/XML for WPS 2.0";
		assertTrue(respDocFlag, msg);
	}
	
	/**
	 * Description: Identify that a XML document is valid with XSD Template or not
	 * @param xmlString
	 * @param xsdPath
	 * @return
	 */
	private static boolean isXMLSchemaValid(String xmlString, String xsdPath){        
        try {
            Schema schema = ValidationUtils.createSchema(xsdPath);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlString)));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * @param xmlString
     * @return
     */
    private static Document TransformXMLStringToXMLDocument(String xmlString) 
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param URI
     * @return
     */
    private static Document TransformXMLFileToXMLDocument(String URI) 
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(URI));
            return doc;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
    /**
	 * @param xmlDoc
	 * @throws Exception
	 */
	private static String TransformXMLDocumentToXMLString(Document xmlDoc) throws Exception{
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
	 * @param any_url
	 * @return
	 * @throws IOException
	 */
	private static Boolean IsValidHTTP(String any_url, Map<String,Object> params) throws IOException {
		StringBuilder Data = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (Data.length() != 0) Data.append('&');
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
	 * @param any_url
	 * @param params
	 * @return
	 */
	private static String GetContentFromGETKVPRequest(String any_url, Map<String,Object> params) {
		StringBuilder sb 			= new StringBuilder();
		HttpURLConnection urlConn 	= null;
		InputStreamReader in 		= null;
		try {
			StringBuilder Data = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (Data.length() != 0) Data.append('&');
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
	 * @param any_url
	 * @param params
	 * @return
	 */
	private static String GetContentFromPOSTXMLRequest(String any_url, Document xml_doc) {
		StringBuilder sb 			= new StringBuilder();
		HttpURLConnection urlConn 	= null;
		InputStreamReader in 		= null;
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
