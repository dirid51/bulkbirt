package edu.ksu.iso.aim.bulkbirt;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.impl.RunAndRenderTask;

public class ExecuteReport {
	static void executeReport() throws EngineException {

		IReportEngine engine = null;
		EngineConfig config = null;

		try {

			// Configure the Engine and start the Platform
			config = new EngineConfig();
			config.setEngineHome("C:/birtruntime/birt-runtime-version/ReportEngine");
			// set log config using (null, Level) if you do not want a log file
			config.setLogConfig(null, Level.OFF);

			Platform.startup(config);
			final IReportEngineFactory FACTORY = (IReportEngineFactory) Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

			engine = FACTORY.createReportEngine(config);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Open the report design
		IReportRunnable design = engine.openReportDesign(
				"W:/ISO/ISO Documentation/Application Development/AiM/BIRT/KSU in Production/KSU089 Building Inventory by Organization 20151211 0825 .rptdesign");

		// Create task to run and render the report,
		IRunAndRenderTask task = engine.createRunAndRenderTask(design);

		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, RunAndRenderTask.class.getClassLoader());

		IGetParameterDefinitionTask paramDefnTask = engine.createGetParameterDefinitionTask(design);

		Collection params = paramDefnTask.getParameterDefns(true);

		Iterator iter = params.iterator();
		// Iterate over all parameters
		while (iter.hasNext()) {
			IParameterDefnBase param = (IParameterDefnBase) iter.next();
			// Group section found
			if (param instanceof IParameterGroupDefn) {
				// // Get Group Name
				// IParameterGroupDefn group = (IParameterGroupDefn) param;
				// System.out.println("Parameter Group: " + group.getName());
				// // Get the parameters within a group
				// Iterator i2 = group.getContents().iterator();
				// while (i2.hasNext()) {
				// IScalarParameterDefn scalar = (IScalarParameterDefn)
				// i2.next();
				// System.out.println("\t" + scalar.getName());
				// }
			} else {
				// Parameters are not in a group
				IScalarParameterDefn scalar = (IScalarParameterDefn) param;
				// System.out.println(param.getName());
				// Parameter is a List Box
				if (scalar.getControlType() == IScalarParameterDefn.LIST_BOX) {
					Collection selectionList = paramDefnTask.getSelectionList(param.getName());
					// Selection contains data
					if (selectionList != null) {
						for (Iterator sliter = selectionList.iterator(); sliter.hasNext();) {
							// Print out the selection choices
							IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) sliter.next();
							String value = (String) selectionItem.getValue();
							// String label = selectionItem.getLabel();
							// System.out.println(label + "--" + value);
							// Set parameter values and validate
							task.setParameterValue("Organization", value);
							task.validateParameters();
							PDFRenderOption options = new PDFRenderOption();
							options.setOutputFileName("C:/Users/randallbooth/Desktop/ksu89-" + value + ".pdf");
							options.setOutputFormat("pdf");
							task.setRenderOption(options);
							// run and render report
							task.run();
						}
					}
				}
			}
		}

		task.close();

		engine.shutdown();
		Platform.shutdown();
		System.out.println("Finished");
	}

	/**
	 * @param ARGUMENTS
	 */
	public static void main(String[] args) {
		try {
			executeReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}