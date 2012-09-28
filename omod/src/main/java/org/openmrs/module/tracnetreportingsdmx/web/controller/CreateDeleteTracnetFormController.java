package org.openmrs.module.tracnetreportingsdmx.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.tracnetreportingsdmx.TracnetReport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CreateDeleteTracnetFormController {

	@RequestMapping("/module/tracnetreportingsdmx/createTracnetReport")
	public String createTracnetReport(HttpServletRequest request) throws Exception {
		TracnetReport.getTracnetReportDefinition(true);
		return "redirect:/module/tracnetreportingsdmx/tracnetReport.form";
	}
}
