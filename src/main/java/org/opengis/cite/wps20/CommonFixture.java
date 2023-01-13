package org.opengis.cite.wps20;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;

import org.opengis.cite.wps20.util.ClientUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
    protected ClientResponse response;
    
    protected URI ServiceUrl;
    
    /* Define Arguments */
    protected String EchoProcessId;
    
    //protected Document GcXmlUri;
    
    //protected Document DpXmlUri;

    /**
     * Initializes the common test fixture with a client component for 
     * interacting with HTTP endpoints.
     *
     * @param testContext The test context that contains all the information for
     * a test run, including suite attributes.
     */
    @BeforeClass
    public void initCommonFixture(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
        if (null != obj) {
            this.client = Client.class.cast(obj);
        }
        obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
        if (null == obj) {
            throw new SkipException("Test subject not found in ITestContext.");
        }
         
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
        
        /*
        //Define GC_XML_URI parameter
        Object GcXmlUriObj = testContext.getSuite().getAttribute(SuiteAttribute.GC_XML_URI.getName());
        if((null != GcXmlUriObj) && Document.class.isAssignableFrom(GcXmlUriObj.getClass())) {
        	this.GcXmlUri = Document.class.cast(GcXmlUriObj);
        	System.out.println("WPS 2.0 GET CAPABILITIES POST/XML URL LOADED");  
        }
        
        //Define DP_XML_URI parameter
        Object DpXmlUriObj = testContext.getSuite().getAttribute(SuiteAttribute.DP_XML_URI.getName());
        if((null != DpXmlUriObj) && Document.class.isAssignableFrom(DpXmlUriObj.getClass())) {
        	this.DpXmlUri = Document.class.cast(DpXmlUriObj);
        	System.out.println("WPS 2.0 DESCRIBE PROCESS POST/XML URL LOADED");  
        }
        */
    }

    @BeforeMethod
    public void clearMessages() {
        this.request = null;
        this.response = null;
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
    public Document getResponseEntityAsDocument(ClientResponse response,
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
     * @return A ClientRequest object.
     *
     * @see ClientUtils#buildGetRequest
     */
    public ClientRequest buildGetRequest(URI endpoint,
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
}
