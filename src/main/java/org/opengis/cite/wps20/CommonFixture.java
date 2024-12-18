package org.opengis.cite.wps20;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.opengis.cite.wps20.basictests.BasicTests;
import org.opengis.cite.wps20.util.ClientUtils;
import org.opengis.cite.wps20.util.URIUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A supporting base class that sets up a common test fixture. These
 * configuration methods are invoked before those defined in a subclass.
 */
public class CommonFixture {

    /**
     * Root test suite package (absolute path).
     */
    public static final String ROOT_PKG_PATH = "/org/opengis/cite/wps20/";
    /**
     * HTTP client component (JAX-RS Client API).
     */
    protected Client client;
    /**
     * An HTTP request message.
     */
    protected ClientRequest request;
    /**
     * An HTTP response message.
     */
    protected Response response;
    
    protected URI ServiceUrl;
    
    /* Define Arguments */
    protected String EchoProcessId;
    
    protected String LITERAL_INPUT_ID;
    protected String LITERAL_OUTPUT_ID;
    protected String COMPLEX_INPUT_ID;
    protected String COMPLEX_OUTPUT_ID;
    
    protected String GET_CAPABILITIES_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/GetCapabilities.xml";
    protected String DESCRIBE_PROCESS_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/DescribeProcess.xml";
    protected String LITERAL_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Literal.xml";
    protected String COMPLEX_REQUEST_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/Echo_Process_Complex.xml";

	
    protected String INPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputValue.xml";
    protected String INPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidInputReference.xml";
	protected String OUTPUT_VALUE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputValue.xml";
	protected String OUTPUT_REFERENCE_TRANSMISSION_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidOutputReference.xml";
	protected String UNIQUE_JOB_IDS_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidUniqueJobIds.xml";
	protected String GET_STATUS_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidGetStatus.xml";
	protected String GET_RESULT_TEMPLATE_PATH = "/org/opengis/cite/wps20/examples/ValidGetResult.xml";

    /**
     * Initializes the common test fixture with a client component for 
     * interacting with HTTP endpoints.
     *
     * @param testContext The test context that contains all the information for
     * a test run, including suite attributes.
     */
    @BeforeClass
    public void initCommonFixture(ITestContext testContext) throws Exception {
        Object obj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
        if (null != obj) {
            this.client = Client.class.cast(obj);
        }
        /*obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
        if (null == obj) {
            throw new SkipException("Test subject not found in ITestContext.");
        }*/
         
        /* Define SERVICE_URL parameter */
        Object ServiceUrlObj = testContext.getSuite().getAttribute(SuiteAttribute.SERVICE_URL.getName());
        if ((null != ServiceUrlObj)){        	
            this.ServiceUrl = URI.class.cast(ServiceUrlObj);
            System.out.println("WPS 2.0 SERVICE URL: " + this.ServiceUrl.toString());        	
        }
        
        /* Define ECHO_PROCESS_ID parameter */
        Object EchoProcessIdObj = testContext.getSuite().getAttribute(SuiteAttribute.ECHO_PROCESS_ID.getName());
        if ((null != EchoProcessIdObj)){        	
            this.EchoProcessId = String.class.cast(EchoProcessIdObj);
            System.out.println("WPS 2.0 ECHO PROCESS ID: " + this.EchoProcessId.toString());        	
        }
        
        GetEchoProcessInputIdAndOutputId();
    }

    @BeforeMethod
    public void clearMessages() {
        this.request = null;
        this.response = null;
    }

    public void GetEchoProcessInputIdAndOutputId() throws URISyntaxException, SAXException, IOException {
    	String SERVICE_URL = this.ServiceUrl.toString();
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
			Element dataTypeInputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "DataType").item(0);
			Element complexInputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ComplexData").item(0);
			String Id = element.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
					.getTextContent();
			if (literalInputElement != null && dataTypeInputElement != null) {
				//check if DataType accepts string
				if (dataTypeInputElement.getTextContent().toLowerCase().equals("string"))
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
			Element dataTypeOutputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "DataType").item(0);
			Element complexOutputElement = (Element) element
					.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "ComplexData").item(0);
			String Id = element.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0)
					.getTextContent();
			if (literalOutputElement != null && dataTypeOutputElement != null) {
				//check if DataType accepts string
				if (dataTypeOutputElement.getTextContent().toLowerCase().equals("string"))
					literalOutputId = Id;
			} else if (complexOutputElement != null) {
				complexOutputId = Id;
			}
		}
		
		LITERAL_INPUT_ID = literalInputId;
		LITERAL_OUTPUT_ID = literalOutputId;
		COMPLEX_INPUT_ID = complexInputId;
		COMPLEX_OUTPUT_ID = complexOutputId;
    }
    
    /**
     * Obtains the (XML) response entity as a DOM Document. This convenience
     * method wraps a static method call to facilitate unit testing (Mockito
     * workaround).
     *
     * @param response A representation of an HTTP response message.
     * @param targetURI The target URI from which the entity was retrieved (may
     * be null).
     * @return A Document representing the entity.
     *
     * @see ClientUtils#getResponseEntityAsDocument
     */
    public Document getResponseEntityAsDocument(Response response,
            String targetURI) {
        return ClientUtils.getResponseEntityAsDocument(response, targetURI);
    }

    /**
     * Builds an HTTP request message that uses the GET method. This convenience
     * method wraps a static method call to facilitate unit testing (Mockito
     * workaround).
     *
     * @param endpoint A URI indicating the target resource.
     * @param qryParams A Map containing query parameters (may be null);
     * @param mediaTypes A list of acceptable media types; if not specified,
     * generic XML ("application/xml") is preferred.
     * @return A Response object.
     *
     * @see ClientUtils#buildGetRequest
     */
    public Response buildGetRequest(URI endpoint,
            Map<String, String> qryParams, MediaType... mediaTypes) {
        return ClientUtils.buildGetRequest(endpoint, qryParams, mediaTypes);
    }
	/**
	 * Description: Send POST request with parameters and return Response as String
	 * 
	 * @param any_url
	 * @param xml_doc
	 * @return
	 */
    public String GetContentFromPOSTXMLRequest(String any_url, Document xml_doc) {
		StringBuilder sb = new StringBuilder();
		HttpURLConnection urlConn = null;
		InputStreamReader in = null;
		String xml = "";
		try {
			xml = TransformXMLDocumentToXMLString(xml_doc);
			System.out.println(xml);
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
			//System.out.println(xml);
			
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			throw new RuntimeException("Exception while calling URL:" + any_url, e);
		}
		return sb.toString();
	}
	/**
	 * @param xmlString
	 * @return
	 */
	public Document TransformXMLStringToXMLDocument(String xmlString) {
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
	 * Description: Send GET request with parameters and return Response as String
	 * 
	 * @param any_url
	 * @param params
	 * @return
	 */
	public String GetContentFromGETKVPRequest(String any_url, Map<String, Object> params) {
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
	 * @param URI
	 * @return
	 */
	public Document TransformXMLFileToXMLDocument(String URI) {
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
	public String TransformXMLDocumentToXMLString(Document xmlDoc) throws Exception {
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
	public void prettyPrint(Document xmlDoc) throws Exception {
		String str = TransformXMLDocumentToXMLString(xmlDoc);
		System.out.println(str);
	}	
	public HttpURLConnection GetConnection(String serviceURL) throws IOException {
		URL urlObj = new URL(serviceURL);
		return (HttpURLConnection) urlObj.openConnection();
	}
	
	public Document GetDocumentTemplate(String templatePath, String processId, String inputId, String outputId) throws URISyntaxException, SAXException, IOException {
		URI uriLiteralRequestTemplate = BasicTests.class.getResource(templatePath).toURI();
		Document SEPDocument = URIUtils.parseURI(uriLiteralRequestTemplate);
		Element requestInputElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Input").item(0);
		Element requestOutputElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Output").item(0);
		Element requestIdElement = (Element) SEPDocument
				.getElementsByTagNameNS("http://www.opengis.net/ows/2.0", "Identifier").item(0);
		// replace id
		requestIdElement.setTextContent(processId);
		requestInputElement.setAttribute("id", inputId);
		requestOutputElement.setAttribute("id", outputId);
		return SEPDocument;
	}
}
