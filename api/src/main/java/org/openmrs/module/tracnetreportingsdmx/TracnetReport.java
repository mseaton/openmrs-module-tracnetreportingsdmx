package org.openmrs.module.tracnetreportingsdmx;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jembi.sdmxhd.SDMXHDMessage;
import org.jembi.sdmxhd.dsd.DSD;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.SimpleIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration;
import org.openmrs.module.reporting.report.ReportProcessorConfiguration.ProcessorMode;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.processor.EmailReportProcessor;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sdmxhdintegration.Utils;
import org.openmrs.module.sdmxhdintegration.reporting.extension.SdmxReportRenderer;
import org.openmrs.module.sdmxhdintegration.reporting.extension.SdmxReportRendererConfig;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Defines the TracNet Report
*/
public class TracnetReport  {
	
	public static final String REPORT_NAME = "TRACNet Report";
	public static final String REPORT_UUID = "313b0f74-efe2-4fc8-abdf-674cb72d2b88";
	public static final String REPORT_DESIGN_UUID = "3d959286-bd43-4724-b21b-77038f6a933f";
	public static final String REPORT_PROCESSOR_UUID = "26026cad-fb94-4bad-bad1-124d820060cc";
	
	/**
	 * Constructs, saves, and returns a new version of the TracNet Report Definition
	 * This will override any changes to the ReportDefinition, it's Designs, and Processors that may have been made directly in the UI
	 */
	public static ReportDefinition getTracnetReportDefinition(boolean forceUpdate) {
		
		ReportDefinition reportDefinition = getReportDefinitionService().getDefinitionByUuid(REPORT_UUID);
		if (reportDefinition == null) {
			reportDefinition = new ReportDefinition();
			forceUpdate = true;
		}
		
		if (forceUpdate) {
			reportDefinition.setName(REPORT_NAME);
			reportDefinition.setUuid(REPORT_UUID);
			reportDefinition.getParameters().clear();
			reportDefinition.getDataSetDefinitions().clear();
			
			// Add Parameters
			reportDefinition.addParameter(ReportingConstants.START_DATE_PARAMETER);
			reportDefinition.addParameter(ReportingConstants.END_DATE_PARAMETER);
			
			// Add in a DataSet Definition
			SimpleIndicatorDataSetDefinition indicatorDsd = new SimpleIndicatorDataSetDefinition();
			indicatorDsd.addParameter(ReportingConstants.START_DATE_PARAMETER);
			indicatorDsd.addParameter(ReportingConstants.END_DATE_PARAMETER);
			Map<String, Object> mappings = ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}");
			reportDefinition.addDataSetDefinition("indicators", new Mapped<SimpleIndicatorDataSetDefinition>(indicatorDsd, mappings));
	
			// Load in the IndicatorSpecifications.  This serves a dual purpose.
			// The format of this file is "columnName=methodNameInIndicatorService|indicatorAndDimensionOptionsForSdmx
			// This allows us to consolidate 2 functions:
			//  1. columnName -> methodNameInIndicatorService will tell us how to compute each indicator value from OpenMRS
			//  2. columnName -> indicatorAndDimensionOptionsForSdmx will tells us how this should be Mapped to the sdmx DSD
			Properties indicatorSpecification = ObjectUtil.loadPropertiesFromClasspath("org/openmrs/module/tracnetreportingsdmx/IndicatorSpecifications-7.properties");
	
			// Load in the SDMX Message.  This also serves a dual purpose:
			// 1. We can use the information in here to get descriptions for each of the indicators for displaying
			// 2. The renderer needs this data as one of it's resources for rendering
			
			ReportDesign reportDesign = getReportService().getReportDesignByUuid(REPORT_DESIGN_UUID);
			if (reportDesign == null) {
				reportDesign = new ReportDesign();
			}
			reportDesign.setUuid(REPORT_DESIGN_UUID);
			reportDesign.setName("TracNet SDMX Export");
			reportDesign.setReportDefinition(reportDefinition);
			reportDesign.setRendererType(SdmxReportRenderer.class);
			reportDesign.getResources().clear();
			reportDesign.getProperties().clear();
	
			ReportDesignResource sdmxDsdResource = new ReportDesignResource();
			try {
				sdmxDsdResource.setName("template");
				sdmxDsdResource.setExtension("zip");
				sdmxDsdResource.setReportDesign(reportDesign);
				InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/tracnetreportingsdmx/TracnetReportSpecification.zip");
				sdmxDsdResource.setContents(IOUtils.toByteArray(is));
				IOUtils.closeQuietly(is);
				reportDesign.addResource(sdmxDsdResource);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to load SDMX DSD resource", e);
			}

			SDMXHDMessage sdmxMessage = SdmxReportRenderer.getSdmxMessage(sdmxDsdResource);
			DSD sdmxDsd = sdmxMessage.getDsd();
			
			Properties indicatorMappingProperties = new Properties();
			for (Map.Entry<Object, Object> spec : indicatorSpecification.entrySet()) {
				String columnName = (String)spec.getKey();
				String[] methodAndSdmxSpec = spec.getValue().toString().split("\\|");
				String methodName = methodAndSdmxSpec[0];
				String sdmxSpec = methodAndSdmxSpec[1];
				indicatorMappingProperties.put(columnName, sdmxSpec);
				TracnetReportIndicator reportIndicator = new TracnetReportIndicator(methodName);
				reportIndicator.addParameter(ReportingConstants.START_DATE_PARAMETER);
				reportIndicator.addParameter(ReportingConstants.END_DATE_PARAMETER);
				String displayName = Utils.getDisplayNameForIndicatorAndDimensions(sdmxDsd, sdmxSpec);
				indicatorDsd.addColumn(columnName, displayName, new Mapped<TracnetReportIndicator>(reportIndicator, mappings));
			}
	
			try {
				ReportDesignResource configResource = new ReportDesignResource();
				configResource.setName("config");
				configResource.setExtension("xml");
				configResource.setReportDesign(reportDesign);
				
				SdmxReportRendererConfig config = new SdmxReportRendererConfig();
				config.setReportfrequency("M");
				config.setOutputWithinOriginalDsd(false);
				config.setCompressOutput(false);
				config.addDataSetAttribute("dataProviderId", "gp:tracnetreportingsdmx.locationDataProviderId");
				config.addDataSetAttribute("confirmationEmail", "gp:tracnetreportingsdmx.confirmation_email_address");
				config.setColumnMappings(indicatorMappingProperties);
				
				configResource.setContents(SdmxReportRendererConfig.serializeToXml(config).getBytes("UTF-8"));
				reportDesign.addResource(configResource);
			}
			catch (Exception e) {
				throw new ReportingException("Unable to construct configuration resource from properties", e);
			}
			
			reportDefinition = getReportDefinitionService().saveDefinition(reportDefinition);
			reportDesign = getReportService().saveReportDesign(reportDesign);
			
			ReportProcessorConfiguration emailConfiguration = getReportService().getReportProcessorConfigurationByUuid(REPORT_PROCESSOR_UUID);
			if (emailConfiguration == null) {
				emailConfiguration = new ReportProcessorConfiguration();
			}
			
			emailConfiguration.setName("TracNet Email Submission");
			emailConfiguration.setProcessorType(EmailReportProcessor.class.getName());
			emailConfiguration.setProcessorMode(ProcessorMode.ON_DEMAND);
			emailConfiguration.setReportDesign(reportDesign);
	
			Properties emailProps = new Properties();
			emailProps.put("from", Context.getAdministrationService().getGlobalProperty("tracnetreportingsdmx.email_from"));
			emailProps.put("to", Context.getAdministrationService().getGlobalProperty("tracnetreportingsdmx.email_to"));
			emailProps.put("subject", "TracNet Submission Test");
			emailProps.put("addOutputToContent", "false");
			emailProps.put("addOutputAsAttachment", "true");
			emailProps.put("attachmentName", "sdmx.zip");
			emailConfiguration.setConfiguration(emailProps);

			emailConfiguration = getReportService().saveReportProcessorConfiguration(emailConfiguration);
		}

		return reportDefinition;
	}
	
	/**
	 * Delete the existing TracNetReportDefinition
	 */
	public static void deleteTracnetReportDefinition() {
		// TODO: Delete what we added above, by uuid constants
	}
	
	/**
	 * @return ReportDefinitionService
	 */
	private static ReportDefinitionService getReportDefinitionService() {
		return Context.getService(ReportDefinitionService.class);
	}
	
	/**
	 * @return ReportService
	 */
	private static ReportService getReportService() {
		return Context.getService(ReportService.class);
	}
}