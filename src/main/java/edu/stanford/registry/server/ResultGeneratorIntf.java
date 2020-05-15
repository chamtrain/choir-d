package edu.stanford.registry.server;

import edu.stanford.registry.shared.PatientResultType;

public interface ResultGeneratorIntf {
	
	/*
	 *  Name used in the control id to identify the type of result 
	 */
	String getResultName();
	
	/*
	 * Get this results type object
	 */
	PatientResultType getResultType();
	
	/*
	 * The current version
	 */
	Long getResultVersion();
	
	/*
	 * Title 
	 */
	String getResultTitle();

	/*
	 * Document id
	 */
	String getDocumentControlId();
}
