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

import java.lang.reflect.Method;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.SimpleIndicatorResult;
import org.openmrs.module.reporting.indicator.evaluator.IndicatorEvaluator;
import org.openmrs.module.tracnetreporting.service.TracNetIndicatorService;

/**
 * Provides the functionality to evaluate a TracnetReportIndicator
 */
@Handler(supports={TracnetReportIndicator.class})
public class TracnetReportIndicatorEvaluator implements IndicatorEvaluator {
	
	public static String DATE_FORMAT = "yyyy/MM/dd";
	
	/**
	 * Default Constructor
	 */
	public TracnetReportIndicatorEvaluator() {}

	/**
	 * @see IndicatorEvaluator#evaluate(Indicator, EvaluationContext)
	 */
	public IndicatorResult evaluate(Indicator indicator, EvaluationContext context) throws EvaluationException {
		TracnetReportIndicator i = (TracnetReportIndicator)indicator;
		Integer num = null;
		try {
			TracNetIndicatorService svc = Context.getService(TracNetIndicatorService.class);
			Method m = TracNetIndicatorService.class.getMethod(i.getMethodName(), String.class, String.class);
			String startDate = DateUtil.formatDate(i.getStartDate(), DATE_FORMAT);
			String endDate = DateUtil.formatDate(i.getEndDate(), DATE_FORMAT);
			Object result = m.invoke(svc, startDate, endDate);
			num = (Integer)result;
		}
		catch (Exception e) {
			throw new EvaluationException("Error evaluating method " + i.getMethodName(), e);
		}
		SimpleIndicatorResult r = new SimpleIndicatorResult();
		r.setIndicator(indicator);
		r.setContext(context);
		r.setNumeratorResult(num);
		return r;
	}
}