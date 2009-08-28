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


import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
import org.eclipse.linuxtools.systemtap.localgui.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.ui.IEditorPart;


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
	
	public void Init() {
		name = ""; //$NON-NLS-1$
		dirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		binaryPath = LaunchConfigurationConstants.DEFAULT_BINARY_PATH;
		arguments = LaunchConfigurationConstants.DEFAULT_ARGUMENTS;
		outputPath = LaunchConfigurationConstants.DEFAULT_OUTPUT_PATH + System.currentTimeMillis();
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
				//TODO: Find out if we are guaranteed the order in which configs are added.
				if (configs[i].exists() && !config.equals(configs[i])) {
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
			
		//System.out.println("SystemTapLaunchShortcut: finishLaunch"); //$NON-NLS-1$
		
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
			//TODO: Uncomment me when done JUnit testing
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
		//TODO: Just make getLaunchType() public :)
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

}
