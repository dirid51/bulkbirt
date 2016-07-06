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
	private static String ENGINE_HOME = null;
	private static String REPORT_PATH = null;
	private static String REPORT_PARAMETER_1 = null;
	private static String REPORT_PARAMETER_1_VALUE = null;
	private static String REPORT_PARAMETER_2 = null;
	private static String REPORT_PARAMETER_2_VALUE = null;
	private static String OUTPUT_DIR_PATH = null;
	private static String OUTPUT_FILE_PREFIX = null;
	private static String OUTPUT_FILE_EXTENSION = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void executeReport() throws EngineException {

		IReportEngine engine = null;
		EngineConfig config = null;

		try {

			// Configure the Engine and start the Platform
			config = new EngineConfig();
			config.setEngineHome(ENGINE_HOME);
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
		IReportRunnable design = engine.openReportDesign(REPORT_PATH);

		// Create task to run and render the report,
		IRunAndRenderTask task = engine.createRunAndRenderTask(design);

		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, RunAndRenderTask.class.getClassLoader());

		IGetParameterDefinitionTask paramDefnTask = engine.createGetParameterDefinitionTask(design);

		Collection<IParameterDefnBase> params = paramDefnTask.getParameterDefns(true);

		Iterator<IParameterDefnBase> iter = params.iterator();
		// Iterate over all parameters
		while (iter.hasNext()) {
			IParameterDefnBase param = iter.next();
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
				if (param.getName().equalsIgnoreCase(REPORT_PARAMETER_1)) {
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
								String label = (selectionItem == null || selectionItem.getLabel() == null) ? value
										: selectionItem.getLabel();
								// String label = selectionItem.getLabel();
								// System.out.println(label + "--" + value);
								// Set parameter values and validate
								task.setParameterValue(REPORT_PARAMETER_2, REPORT_PARAMETER_2_VALUE);
								task.setParameterValue(REPORT_PARAMETER_1,
										REPORT_PARAMETER_1_VALUE.equalsIgnoreCase("[value]") ? value
												: REPORT_PARAMETER_1_VALUE);
								task.validateParameters();
								PDFRenderOption options = new PDFRenderOption();
								options.setOutputFileName(OUTPUT_DIR_PATH + OUTPUT_FILE_PREFIX + "-"
										+ cleanStringForFilename(label) + "." + OUTPUT_FILE_EXTENSION);
								options.setOutputFormat(OUTPUT_FILE_EXTENSION);
								task.setRenderOption(options);
								// run and render report
								task.run();
							}
						}
					}
				}
			}
		}

		task.close();

		engine.destroy();
		Platform.shutdown();
	}

	private static String cleanStringForFilename(String s) {
		return s.replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("[_]+", "_");
	}

	/**
	 * @param ARGUMENTS
	 */
	public static void main(String[] args) {
		try {

			ENGINE_HOME = System.getProperty("engine_home");
			REPORT_PATH = System.getProperty("report_path");
			REPORT_PARAMETER_1 = System.getProperty("report_parameter_1");
			REPORT_PARAMETER_1_VALUE = System.getProperty("report_parameter_1_value");
			REPORT_PARAMETER_2 = System.getProperty("report_parameter_2");
			REPORT_PARAMETER_2_VALUE = System.getProperty("report_parameter_2_value");
			OUTPUT_DIR_PATH = System.getProperty("output_dir_path");
			OUTPUT_FILE_PREFIX = System.getProperty("output_file_prefix");
			OUTPUT_FILE_EXTENSION = System.getProperty("output_file_extension");

			executeReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}