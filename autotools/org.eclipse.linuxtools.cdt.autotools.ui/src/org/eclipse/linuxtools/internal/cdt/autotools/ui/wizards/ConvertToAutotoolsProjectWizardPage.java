/*******************************************************************************
 * Copyright (c) 2000, 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.wizards;

 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsProjectNature;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.linuxtools.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.linuxtools.internal.cdt.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.linuxtools.internal.cdt.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;


/**
 *
 * ConvertToAutotoolsProjectWizardPage
 * Standard main page for a wizard that adds a Managed Make C project Nature to a project with no nature associated with it.
 * This conversion is one way in that the project cannot be converted back (i.e have the nature removed).
 *
 * @author Jeff Johnston
 * @since Feb 8, 2006
 *<p>
 * Example useage:
 * <pre>
 * mainPage = new ConvertToAutotoolsProjectWizardPage("ConvertProjectPage");
 * mainPage.setTitle("Project Conversion");
 * mainPage.setDescription("Add C or C++ Managed Make Nature to a project.");
 * </pre>
 * </p>
 */
public class ConvertToAutotoolsProjectWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "WizardAutotoolsProjectConversion.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "WizardAutotoolsProjectConversion.description"; //$NON-NLS-1$
    private static final String PREFIX = "WizardAutotoolsProjectConversion";
	protected static final String MSG_ADD_NATURE = PREFIX + ".message.add_nature";	//$NON-NLS-1$
	protected static final String MSG_ADD_BUILDER = PREFIX + ".message.add_builder";	//$NON-NLS-1$
	protected static final String MSG_SAVE = PREFIX + ".message.save";	//$NON-NLS-1$
    
	/**
	 * Constructor for ConvertToStdMakeProjectWizardPage.
	 * @param pageName
	 */
	public ConvertToAutotoolsProjectWizardPage(String pageName, ConvertToAutotoolsProjectWizard wizard) {
		super(pageName);
		setWizard(wizard);
	}
    
    /**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return AutotoolsUIPlugin.getResourceString(WZ_TITLE);
    }
    
    /**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return AutotoolsUIPlugin.getResourceString(WZ_DESC);
    }
       
    /**
     * Method isCandidate returns true for all projects.
     * 
     * @param project
     * @return boolean
     */
    public boolean isCandidate(IProject project) { 
		return true; // all 
    }    
    
    protected IProjectType getProjectType() {
    	return ((ConvertToAutotoolsProjectWizard)getWizard()).getProjectType();
    }
    
    protected IConfiguration[] getSelectedConfigurations() {
    	return ((ConvertToAutotoolsProjectWizard)getWizard()).getSelectedConfigurations();
    }
    
    protected void applyOptions(IProject project, IProgressMonitor monitor) {
    	((ConvertToAutotoolsProjectWizard)getWizard()).applyOptions(project, monitor);
    }
    
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(AutotoolsUIPlugin.getResourceString("WizardMakeProjectConversion.monitor.convertingToMakeProject"), 7); //$NON-NLS-1$
		try {
			super.convertProject(project, new SubProgressMonitor(monitor, 1), projectID);
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_ADD_NATURE));
			ManagedCProjectNature.addManagedNature(project, new SubProgressMonitor(monitor, 1));
			AutotoolsNewProjectNature.addAutotoolsNature(project, new SubProgressMonitor(monitor, 1));
			AutotoolsProjectNature.removeAutotoolsNature(project, new SubProgressMonitor(monitor, 1));
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_ADD_BUILDER));
//			ManagedCProjectNature.addManagedBuilder(project, new SubProgressMonitor(monitor, 1));
			AutotoolsNewProjectNature.addAutotoolsBuilder(project, new SubProgressMonitor(monitor,1));
			// FIXME: Default scanner property: make -w - eventually we want to use Make core's build scanner
			project.setPersistentProperty(AutotoolsPropertyConstants.SCANNER_USE_MAKE_W, AutotoolsPropertyConstants.TRUE);
			CCorePlugin.getDefault().mapCProjectOwner(project, projectID, true);
			// Add the ManagedProject to the project
			IManagedProject newManagedProject = null;
			IManagedBuildInfo info = null;
			try {
				info = ManagedBuildManager.createBuildInfo(project);
				IProjectType parent = getProjectType();
				newManagedProject = ManagedBuildManager.createManagedProject(project, parent);
				if (newManagedProject != null) {
					IConfiguration [] selectedConfigs = getSelectedConfigurations();
					for (int i = 0; i < selectedConfigs.length; i++) {
						IConfiguration config = selectedConfigs[i];
						int id = ManagedBuildManager.getRandomNumber();
						IConfiguration newConfig = newManagedProject.createConfiguration(config, config.getId() + "." + id); //$NON-NLS-1$
						newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
					}
					// Now add the first supported config in the list as the default
					IConfiguration defaultCfg = null;
					IConfiguration[] newConfigs = newManagedProject.getConfigurations();
					for(int i = 0; i < newConfigs.length; i++) {
						if(newConfigs[i].isSupported()){
							defaultCfg = newConfigs[i];
							break;
						}
					}
					
					if(defaultCfg == null && newConfigs.length > 0)
						defaultCfg = newConfigs[0];
					
					// Create a default Autotools configuration and save it.
					AutotoolsConfigurationManager.getInstance().getConfiguration(project, defaultCfg.getName(), true);
					AutotoolsConfigurationManager.getInstance().saveConfigs(project.getName());
					
					if(defaultCfg != null) {
						ManagedBuildManager.setDefaultConfiguration(project, defaultCfg);
						ManagedBuildManager.setSelectedConfiguration(project, defaultCfg);
					}
					ManagedBuildManager.setNewProjectVersion(project);
				}
			} catch (BuildException e) {
				AutotoolsUIPlugin.log(e);
			}

			// Following is a bit of a hack because changing the project options
			// causes a change event to be fired which will try to reindex the project.  
			// We are in the middle of setting the project indexer which may end up 
			// being the null indexer.  In that case, we don't want the default indexer 
			// (Fast Indexer) to be invoked.
			//IIndexManager manager = CCorePlugin.getIndexManager();
			//ICProject cproject = CoreModel.getDefault().create(project);
			//manager.setIndexerId(cproject, ConvertToAutotoolsProjectWizard.NULL_INDEXER_ID);
		
			// Modify the project settings
			if (project != null) {
				applyOptions(project, new SubProgressMonitor(monitor, 2));
			}

//			 Set the ScannerInfoProvider.  We must do this after
//			 applying the options because changing the ScannerInfoProvider
//			 is considered a change to the project and a reindex will
//			 occur.  One of the options being applied above is the indexer
//			 selected by the user.  Thus, we wait until now.
//			try {
//				AutotoolsUIPlugin.setScannerInfoProvider(project);
//			} catch (CoreException e) {
//				ManagedBuilderUIPlugin.log(e);
//			}

			// Save the build options
			monitor.subTask(AutotoolsUIPlugin.getResourceString(MSG_SAVE));
			if (info != null) {
				info.setValid(true);
				ManagedBuildManager.saveBuildInfo(project, true);
			}
		} finally {
			IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
			if (initResult.getCode() != IStatus.OK) {
				// At this point, I can live with a failure
				AutotoolsUIPlugin.log(initResult);
			}
			monitor.done();
		}
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		IStructuredSelection sel = ((BasicNewResourceWizard)getWizard()).getSelection();
		if ( sel != null) {
			tableViewer.setCheckedElements(sel.toArray());
			setPageComplete(validatePage());
		}
	}

}
