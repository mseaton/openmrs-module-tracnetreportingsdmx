package org.openmrs.module.tracnetreportingsdmx.web.extension;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class ManageAdminListExt extends AdministrationSectionExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "Tracnet Report";
	}
	
	public String getRequiredPrivilege() {
		return "Manage Reports";
	}
	
	public Map<String, String> getLinks() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("module/tracnetreportingsdmx/tracnetReport.form", "Run Tracnet Report");
		map.put("module/tracnetreportingsdmx/createTracnetReport.form", "Re-create Tracnet Report");
		return map;
	}
	
}
