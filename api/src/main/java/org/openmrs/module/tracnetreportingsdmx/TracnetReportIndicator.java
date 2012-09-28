/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.tracnetreportingsdmx;

import java.util.Date;

import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.indicator.BaseIndicator;

/**
 * Indicator implementation that delegates to a method within the TracnetIndicatorService
 * in the TracnetReporting module to calculate it's indicator result value
 */
public class TracnetReportIndicator extends BaseIndicator {
    
	//***** PROPERTIES *****

	@ConfigurationProperty(required=true)
	private String methodName;
	
	@ConfigurationProperty(required=false)
	private Date startDate;
	
	@ConfigurationProperty(required=false)
	private Date endDate;

	//***** CONSTRUCTORS *****

	/**
	 * Default constructor
	 */
	public TracnetReportIndicator() {}
	
	/**
	 * Full constructor
	 */
	public TracnetReportIndicator(String methodName) {
		super();
		setName("TracnetReport: " + methodName);
		this.methodName = methodName;
	}
	
	//***** INSTANCE METHODS *****
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
	//***** PROPERTY ACCESS *****

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
