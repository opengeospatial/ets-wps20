package org.opengis.cite.wps20.synchronous;

import static org.testng.Assert.assertTrue;

//import org.opengis.cite.wps20.Namespaces;
//import org.opengis.cite.wps20.SuiteAttribute;
import org.opengis.cite.wps20.CommonFixture;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * SyncTests class.
 * </p>
 *
 */
public class SyncTests extends CommonFixture {

	/**
	 * A.5.11. Verify that the server can handle the execution mode 'synchronous'
	 * requested via POST/XML Flow of Test Description: Send a valid XML Execute request
	 * to the server under test, setting the “mode” attribute to “sync”. Verify that a
	 * valid Execute wps:Result is returned.
	 * @throws java.lang.Exception
	 */
	@Test(enabled = true, groups = "A.5. Basic Tests",
			description = "A.5.11. Verify that the server can handle the execution mode 'synchronous' requested via POST/XML.")
	public void ValidSyncExcecuteViaPOSTXML() throws Exception {
		String SERVICE_URL = this.ServiceUrl.toString();
		Document literalDocument = GetDocumentTemplate(LITERAL_REQUEST_TEMPLATE_PATH, this.EchoProcessId,
				LITERAL_INPUT_ID, LITERAL_OUTPUT_ID);

		// Response document
		Element executeElement = (Element) literalDocument
			.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Execute")
			.item(0);
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
		 * int responseCode = conn.getResponseCode(); boolean respDocFlag = (responseCode
		 * == HttpURLConnection.HTTP_OK);
		 */

		String respDocResult = GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument);
		Document respDocResultDocument = TransformXMLStringToXMLDocument(respDocResult);
		boolean respDocFlag = respDocResultDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0", "Result")
			.getLength() > 0;
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
		}
		else {
			String msg1 = "Invalid SyncExecute via POST/XML for WPS 2.0";
			Assert.assertTrue(VSE_Flag, msg1);
		}

		/*
		 * // The Process Flow should as this one String VSEXmlString =
		 * GetContentFromPOSTXMLRequest(SERVICE_URL, literalDocument); Document
		 * VSEDocument = TransformXMLStringToXMLDocument(VSEXmlString); Boolean VSE_Flag =
		 * (VSEDocument.getElementsByTagNameNS("http://www.opengis.net/wps/2.0",
		 * "Result").getLength() > 0) ? true : false; if (VSE_Flag) { String msg =
		 * "Valid SyncExecute via POST/XML for WPS 2.0"; Assert.assertTrue(VSE_Flag, msg);
		 * } else { String msg = "Invalid SyncExecute via POST/XML for WPS 2.0";
		 * Assert.assertTrue(VSE_Flag, msg); }
		 */
	}

}
