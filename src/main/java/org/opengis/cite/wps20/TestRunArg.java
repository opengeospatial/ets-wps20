package org.opengis.cite.wps20;

/**
 * An enumerated type defining all recognized test run arguments.
 */
public enum TestRunArg {

    /**
     * An absolute URI that refers to a representation of the test subject or
     * metadata about it.
     */
    IUT,
	ECHO_PROCESS_ID
	/*SERVICE_URL,
	GC_XML_URI,
	DP_XML_URI,
	EX_SNC_XML_URI,
	EX_ANC_XML_URI,
	EX_ATO_XML_URI*/
	;

    @Override
    public String toString() {
        return name().toUpperCase();
    }
}
