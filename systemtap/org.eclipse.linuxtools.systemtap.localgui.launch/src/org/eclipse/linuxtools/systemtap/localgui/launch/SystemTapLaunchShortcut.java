/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.localgui.launch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.profiling.launch.Messages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.systemtap.localgui.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;


public class SystemTapLaunchShortcut extends ProfileLaunchShortcut{
	protected IEditorPart editor;
	protected ILaunchConfiguration config;

	protected String name;
	protected String binaryPath;
	protected String scriptPath; //$NON-NLS-1$
	protected String arguments;
	protected String outputPath;
	protected String binName;
	protected String dirPath;
	protected String generatedScript;
	protected boolean needToGenerate;
	protected boolean overwrite;
	protected boolean useColours;
	protected String resourceToSearchFor;
	protected boolean searchForResource;
	
	/**
	 * Provides access to the Profiling Frameworks' launch method
	 * 
	 * @param editor
	 * @param mode
	 */
	public void reLaunch(IEditorPart editor, String mode) {
		launch(editor, mode);
	}
	
	public void Init() {
		name = ""; //$NON-NLS-1$
		dirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		binaryPath = LaunchConfigurationConstants.DEFAULT_BINARY_PATH;
		arguments = LaunchConfigurationConstants.DEFAULT_ARGUMENTS;
		outputPath = PluginConstants.DEFAULT_OUTPUT + System.currentTimeMillis();
		overwrite = true;
		scriptPath = null; 	//Every shortcut MUST declare its own script path.
		generatedScript = LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT;
		needToGenerate = false;
		useColours = false;
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		//System.out.println("SystemTapLaunchShortcut: getLaunchConfigType"); //$NON-NLS-1$
		return getLaunchManager().getLaunchConfigurationType(PluginConstants.CONFIGURATION_TYPE_ID);
	}


	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
		SystemTapOptionsTab tab = new SystemTapOptionsTab();
		tab.setDefaults(wc);
		//System.out.println("SystemTapLaunchShortcut: setDefaultProfileAttributes"); //$NON-NLS-1$
	}	


	protected ILaunchConfiguration checkForExistingConfiguration() {
		ILaunchConfigurationType configType = getLaunchConfigType();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			
			for (int i = 0; i < configs.length; i++) {
				if (configs[i].exists() && configs[i]!=null && !config.equals(configs[i])) {
					if(checkIfAttributesAreEqual(config, configs[i])) {
						config.delete();
						config = configs[i];
						}
				}
			}
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return config;
		
	}
	
	
	/**
	 * Returns true if two configurations are exactly identical (i.e. all attributes are equal)
	 * 
	 * @param first
	 * @param second
	 * @return True if two configurations are exactly identical (i.e. all attributes are equal)
	 */
	private boolean checkIfAttributesAreEqual(ILaunchConfiguration first,ILaunchConfiguration second) {
		boolean isEqual = false;
		
		try {
			if (first.getAttributes().equals(second.getAttributes()))
				isEqual = true;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return isEqual;
	}
	
/**
 * Helper function to complete launches. Uses protected parameters
 * (Strings) scriptPath, binaryPath, arguments, outputPath and (boolean)
 * overwrite. These must be set by the calling function (or else
 * nonsensical results will occur).
 * 
 * ScriptPath MUST be set.
 * 
 * @param name: Used to generate the name of the new configuration
 * @param bin:	Affiliated executable
 * @param mode:	Mode setting
 */
	protected void finishLaunch(String name, String mode) {
		
		if (scriptPath.length() < 1) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchShortcut.ErrorMessageName"),  //$NON-NLS-1$
					Messages.getString("SystemTapLaunchShortcut.ErrorMessageTitle"), Messages.getString("SystemTapLaunchShortcut.ErrorMessage") + name); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
			return;
		}  
			
		
		ILaunchConfigurationWorkingCopy wc = null;
		if (config != null) {
			try {
				wc = config.getWorkingCopy(); //$NON-NLS-1$
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			
			wc.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH, scriptPath);
			wc.setAttribute(LaunchConfigurationConstants.BINARY_PATH, binaryPath);
			wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH, outputPath);
			wc.setAttribute(LaunchConfigurationConstants.ARGUMENTS, arguments);
			wc.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, generatedScript);
			wc.setAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE, needToGenerate);
			wc.setAttribute(LaunchConfigurationConstants.OVERWRITE, overwrite);
			wc.setAttribute(LaunchConfigurationConstants.USE_COLOUR, useColours);

			try {
				config = wc.doSave();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
			checkForExistingConfiguration();
			
			DebugUITools.launch(config, mode);
		} 
		
	}
	
	
	//TODO: Should merge finishWith and Without binary - we only use
	//the IBinary to find the name, in any case.
	/**
	 * This function is identical to the function above, except it does not
	 * require a binary.
	 * 
	 * Helper function to complete launches. Uses protected parameters
	 * (Strings) scriptPath, arguments, outputPath and (boolean)
	 * overwrite. These must be set by the calling function (or else
	 * nonsensical results will occur).
	 * 
	 * ScriptPath MUST be set.
	 * 
	 * @param name: Used to generate the name of the new configuration
	 * @param bin:	Affiliated executable
	 * @param mode:	Mode setting
	 */
protected void finishLaunchWithoutBinary(String name, String mode) {
		
	
		if (scriptPath.length() < 1) {
			SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchShortcut.ErrorMessagename"),  //$NON-NLS-1$
					Messages.getString("SystemTapLaunchShortcut.ErrorMessageTitle"), Messages.getString("SystemTapLaunchShortcut.ErrorMessage") + name); //$NON-NLS-1$ //$NON-NLS-2$
			mess.schedule();
			return;
		}
	
		ILaunchConfigurationWorkingCopy wc = null;
		if (config != null) {
			try {
				wc = config.getWorkingCopy(); 
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			
			wc.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH, scriptPath);
			wc.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH, outputPath);
			wc.setAttribute(LaunchConfigurationConstants.ARGUMENTS, arguments);
			wc.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, generatedScript);
			wc.setAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE, needToGenerate);
			wc.setAttribute(LaunchConfigurationConstants.OVERWRITE, overwrite);
			wc.setAttribute(LaunchConfigurationConstants.USE_COLOUR, useColours);

			
			try {
				config = wc.doSave();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			checkForExistingConfiguration();

			DebugUITools.launch(config, mode);
		}
	}


/**
 * Returns bin.getPath().toString()
 * 
 * @param bin
 * @return
 */
	public String getName(IBinary bin) {
		if (bin != null) {
			binName = bin.getPath().toString();
		} else {
			binName = ""; //$NON-NLS-1$
//			SystemTapUIErrorMessages error = new SystemTapUIErrorMessages(
//					"Null_Binary",
//					"Invalid executable",
//					"An error has occured: a binary/executable file was not given to the launch shortcut.");
//			error.schedule();
		}
		return binName;
	}
	
	/**
	 * Creates a configuration for the given IBinary
	 * 
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(IBinary bin){
		if (bin != null){
			return super.createConfiguration(bin);
		}else{			
			try {
				return getLaunchConfigType().newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(Messages.getString("SystemTapLaunchShortcut.0"))); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	

	/**
	 * Creates a configuration with the given name - does not use a binary
	 * 
	 * @param name
	 * @return
	 */
	protected ILaunchConfiguration createConfiguration(String name) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name));
			
			setDefaultProfileAttributes(wc);
	
			config = wc.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	/**
	 * Allows null configurations to be launched. Any launch that uses a binary should
	 * never call this configuration with a null parameter, and any launch that does not
	 * use a binary should never call this function. The null handling is included for 
	 * ease of testing.
	 * 
	 * @param bin
	 * @param name - Customize the name based on the shortcut being launched
	 * @return A launch configuration, or null
	 */
	protected ILaunchConfiguration createConfiguration(IBinary bin, String name) {
		if (bin != null) {
			config = null;
			try {
				String projectName = bin.getResource().getProjectRelativePath().toString();
				ILaunchConfigurationType configType = getLaunchConfigType();
				ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, 
						getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name + " - " + bin.getElementName())); //$NON-NLS-1$
		
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
				wc.setMappedResources(new IResource[] {bin.getResource(), bin.getResource().getProject()});
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
		
				setDefaultProfileAttributes(wc);
		
				config = wc.doSave();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		else
			try {
				ILaunchConfigurationWorkingCopy wc = getLaunchConfigType().newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name)); //$NON-NLS-1$
				setDefaultProfileAttributes(wc);
				config = wc.doSave();
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
			//An error has occured!
		return config;

//			SystemTapUIErrorMessages error = new SystemTapUIErrorMessages(
//					"Could not create configuration",
//					"Could not create configuration",
//					"An error has occured: the code has reached an impossible location.");
//			error.schedule();
	}
	
	/**
	 * Creates an error message stating that the launch failed for the specified reason.
	 * 
	 * @param reason
	 */
	protected void failedToLaunch(String reason) {
		SystemTapUIErrorMessages mess = new SystemTapUIErrorMessages(Messages.getString("SystemTapLaunchShortcut.StapLaunchFailed"), //$NON-NLS-1$
				Messages.getString("SystemTapLaunchShortcut.StapLaunchFailedTitle"), Messages.getString("SystemTapLaunchShortcut.StapLaunchFailedMessage") + reason); //$NON-NLS-1$ //$NON-NLS-2$
		mess.schedule();
	}
	
	
	public void errorHandler() {
	};

	
	/**
	 * The following are convenience methods for test programs, etc. to check
	 * the value of certain protected parameters.
	 * 
	 */
	public ILaunchConfigurationType outsideGetLaunchConfigType() {
		return getLaunchConfigType();
	}

	public ILaunchConfiguration getConfig() {
		return config; 
	}

	public String getScriptPath() {
		return scriptPath;
	}
	
	public String getDirPath() {
		return dirPath;
	}
	
	public String getArguments() {
		return arguments;
	}
	
	public String getBinaryPath() {
		return binaryPath;
	}

	/**
	 * Retrieves the names of all functions referenced by the binary. If searchForResource
	 * is true, this function will return all function names belonging to an element with name
	 * matching the String held by resourceToSearchFor. Otherwise it will create a dialog
	 * prompting the user to select from a list of files to profile, or select the only 
	 * available file if only one file is available.
	 * 
	 * 
	 * @param bin
	 * @return
	 */
	protected String getFunctionsFromBinary(IBinary bin, String targetResource) {
		String funcs = "";
		try {			
			ArrayList<ICContainer> list = new ArrayList<ICContainer>();
			TranslationUnitVisitor v = new TranslationUnitVisitor();
//			ASTTranslationUnitVisitor v  = new ASTTranslationUnitVisitor();
			
			for (ICElement b : bin.getCProject().getChildrenOfType(ICElement.C_CCONTAINER)) {
				ICContainer c = (ICContainer) b;
				
				for (ITranslationUnit tu : c .getTranslationUnits()) {
					if (searchForResource && tu.getElementName().contains(targetResource)) {
						tu.accept(v);
						funcs+=v.getFunctions();
						return funcs;
					} else {
						if (!list.contains(c))
							list.add(c);
//						if (!tu.getElementName().contains(".h")) {
//							list.add(tu);
//						}
					}
				}
				
				//Iterate down to all children, checking for more C_Containers
				while (c.getChildrenOfType(ICElement.C_CCONTAINER).size() > 0) {
					ICContainer e = null;
					for (ICElement d : c.getChildrenOfType(ICElement.C_CCONTAINER)) {
						e = (ICContainer) d;
						for (ITranslationUnit tu : e.getTranslationUnits()) {
							if (searchForResource && tu.getElementName().contains(targetResource)) {
								tu.accept(v);
								funcs+=(v.getFunctions());
								return funcs;
							} else {
								if (!list.contains(c))
									list.add(c);
//								if (!tu.getElementName().contains(".h")) {
//									list.add(tu);
//								}
							}
						}
					}
					c = e;
				}
			}
			
			int numberOfFiles = numberOfValidFiles(list.toArray());
			if (numberOfFiles == 1) {
				for (ICContainer c : list) {
					for (ITranslationUnit e : c.getTranslationUnits()) {
						if (e.getElementName().endsWith(".c") || 
								e.getElementName().endsWith(".cpp")) {
							e.accept(v);
							funcs+=v.getFunctions();
						}
					}
				}
			} else {
//				funcs.clear();
//				funcs.add("*");
				Object[] unitList = chooseUnit(list, numberOfFiles); 
//				if (unitList.length >= list.size()) {
//					//User selected all items, just add * instead of searching
//					funcs.clear();
//					funcs.add("*");
//				} else {
				if (unitList == null || unitList.length == 0) {
					return null;
				}
				for (Object obj : unitList) {
					if (obj instanceof ITranslationUnit) {
						ITranslationUnit tu = (ITranslationUnit) obj;
						tu.accept(v);
						funcs+=(v.getFunctions());
					} else {
						//Non-Translation unit object added to list, just probe *
						funcs = "*";
						return funcs;
					}
				}
					
			}
			
			return funcs;
			
		} catch (CModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Creates a dialog that prompts the user to select from the given list
	 * of translation units.
	 * 
	 * @param list
	 * @return
	 */
	protected Object[] chooseUnit(List<ICContainer> list, int numberOfValidFiles) {
//		ILabelProvider programLabelProvider = new LabelProvider() {
//			public String getText(Object element) {
//				if (element instanceof ITranslationUnit) {
//					String name = ((ITranslationUnit) element).getElementName();
//					return name;
//				}
//				return super.getText(element);
//			}
//		};
//	
//		//Currently we end up visiting the translation unit twice, could be
//		//expensive for larger programs
//		ILabelProvider qualifierLabelProvider = new LabelProvider() {
//			public String getText(Object element) {
//				try {
//					if (element instanceof ITranslationUnit) {
//						//Only select names that are not .h files
//						TranslationUnitVisitor v = new TranslationUnitVisitor();
//						((ITranslationUnit) element).accept(v);
//						return "Functions: " + v.getNumberOfFunctions();
//					}
//				} catch (CoreException e) {
//					e.printStackTrace();
//				}
//				return super.getText(element);
//			}
//		};
//	
//		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
//				getActiveWorkbenchShell(), programLabelProvider);
//		TwoPaneElementSelector dialog = new TwoPaneElementSelector(
//				getActiveWorkbenchShell(), programLabelProvider, qualifierLabelProvider);
//		dialog.setElements(list.toArray());
//		dialog.setTitle("Choose files"); //$NON-NLS-1$
//		dialog.setMessage("Probe functions declared in selected files."); //$NON-NLS-1$
////		dialog.setUpperListLabel("Detected files"); //$NON-NLS-1$
////		dialog.setLowerListLabel("Number of functions"); //$NON-NLS-1$
//		dialog.setMultipleSelection(true);
//		dialog.setInitialElementSelections(list);
//		
		
		ListTreeContentProvider prov = new ListTreeContentProvider();
		
	    CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(getActiveWorkbenchShell(), 
	    		new WorkbenchLabelProvider(), prov);
	    
	    dialog.setTitle("Tree Selection");
	    dialog.setMessage("Select .c/.cpp files to probe. Leave selection blank to select all.");
	    dialog.setInput(list);
		
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result == null)
				return null;
			
			ArrayList<Object> output = new ArrayList<Object>();
			try {
				for (Object obj : result) {
					if (obj instanceof ICContainer){
						List<ITranslationUnit> units =
							Arrays.asList(((ICContainer) obj).getTranslationUnits()); 
						for (ITranslationUnit c : units) {
							if (!(c.getElementName().endsWith(".c") ||
									c.getElementName().endsWith(".cpp")))
								continue;
							if (!output.contains(c))
								output.add(c);
						}
					}
					else if (obj instanceof ICElement) {
						ICElement parent = ((ICElement) obj).getParent();
						if (parent instanceof ICContainer){
							String name =((ICElement) obj).getElementName(); 
							if (name.endsWith(".c") || name.endsWith(".cpp")) {
							ITranslationUnit b = ((ICContainer) parent).getTranslationUnit(name);
							if (!output.contains(b)) {
								output.add(b);
							}
							}
						}
					}
				}
			
				int totalValidFiles = numberOfValidFiles;
				if ( output.size() >= totalValidFiles) {
					output.clear();
					output.add(new Object());
				} else if (output.size() > 10 && totalValidFiles > 300) {
					if (confirmProbeAll(output.size())) {
						output.clear();
						output.add(new Object());
					}
				}
			} catch (CModelException e) {
				e.printStackTrace();
			}
	
//			
//			if ( output.size() == 0) {
//				output.clear();
//				output.add(new Object());
//			} else if (output.size() > 1) {
//				if (confirmProbeAll(output.size())) {
//					output.clear();
//					output.add(new Object());
//				}
//			}
			
			return output.toArray();
		}
		return null;
	}

	
	private boolean confirmProbeAll(int num) {
		
		return !MessageDialog.openConfirm(new Shell(), "Fetch list of functions?", 
				"Currently attempting to fetch functions from " + num + " files. This could " +
				"Currently attempting to fetch functions from " + num + "files. This could " +
				"take up to " + 2*num + " seconds to complete. Press OK to continue fetching or" +
				" press Cancel to probe all functions without fetching (much faster). In the future," +
				" you can simply leave the file selection dialog blank to profile all functions.");		
	}
	
	
	private int numberOfValidFiles(Object[] list) throws CModelException {
		int output = 0;
		for (Object parent : list) {
			if (parent instanceof ICContainer) {
				ICContainer cont = (ICContainer) parent;
					for (ICElement ele : cont.getChildren()) {
					if (ele instanceof ICContainer) {
						output += numberOfValidFiles(((ICContainer) ele).getChildren());
					}
					if (ele instanceof ICElement) {
						if (ele.getElementName().endsWith(".c") ||
							ele.getElementName().endsWith(".cpp"))
							output++;
					}
				}
			} else if (parent instanceof ICElement) {
				if (((ICElement) parent).getElementName().endsWith(".c") ||
						((ICElement) parent).getElementName().endsWith(".cpp"))
						output++;
			}
		}
		return output;
	}
}
