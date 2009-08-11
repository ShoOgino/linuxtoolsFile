/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class AutotoolsPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static AutotoolsPlugin plugin;
	private ResourceBundle resourceBundle;
	
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.cdt.autotools"; //$NON-NLS-1$

	private AutotoolsMakeTargetManager fTargetManager;
	private IConfigurationElement generatorElement;

	public static final String EXTENSION_POINT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".buildDefinitions";		//$NON-NLS-1$
	public static final String BUILDER = "builder";   //$NON-NLS-1$
	public static final String ID_ELEMENT_NAME = "id"; // $NON-NLS-1$
	public static final String AUTOTOOLS_BUILDER_ID = PLUGIN_ID + ".builder"; // $NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public AutotoolsPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.linuxtools.cdt.autotools.Resources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}

	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}
	
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public IMakeTargetManager getTargetManager() {
		if ( fTargetManager == null) {
			fTargetManager = new AutotoolsMakeTargetManager();
			fTargetManager.startup();
		}
		return fTargetManager;
	}

	@SuppressWarnings("deprecation")
	public static void verifyScannerInfoProvider(IProject project) throws CoreException {
		boolean found = false;
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
        ICExtensionReference[] refs = desc.get(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
        for (int i = 0; i < refs.length; ++i) {
        	if (refs[i].getID().equals(AutotoolsScannerInfoProvider.INTERFACE_IDENTITY))
        		found = true;
        }
        if (!found) {
        	setScannerInfoProvider(project);
        }
	}
	
	@SuppressWarnings("deprecation")
	public static void setScannerInfoProvider(IProject project) throws CoreException {
		ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
		desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
		desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, AutotoolsScannerInfoProvider.INTERFACE_IDENTITY);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static AutotoolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns active shell.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * 
	 * @param key the message key
	 * @return the resource bundle message
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = AutotoolsPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * 
	 * @param key the message key
	 * @param args an array of substituition strings
	 * @return the resource bundle message
	 */
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[])args);
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Get the configuration element for the Autotools Makefile generator
	 * @return the generator element
	 */
	public IConfigurationElement getGeneratorElement() {
		if (generatorElement == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
			if( extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				if (extensions != null) {
					for (int i = 0; i < extensions.length; ++i) {
						IExtension extension = extensions[i];
				
						IConfigurationElement[] elements = extension.getConfigurationElements();
						
						// Get the managedBuildRevsion of the extension.
						for (int j = 0; j < elements.length; j++) {
							IConfigurationElement element = elements[j];
							if (element.getName().equals(BUILDER)) {
								String id = element.getAttribute(ID_ELEMENT_NAME);
								if (id.equals(AUTOTOOLS_BUILDER_ID)) {
									generatorElement = element;
									break;
								}
							}
						}
					}
				}
			}
		}
		return generatorElement;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.linuxtools.cdt.autotools", path);
	}
	
	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	public static void logException(Throwable e, final String title, String message) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else {
			if (message == null)
				message = e.getMessage();
			if (message == null)
				message = e.toString();
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, message, e);
		}
		ResourcesPlugin.getPlugin().getLog().log(status);
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		final IStatus fstatus = status;
		display.asyncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(null, title, null, fstatus);
			}
		});
	}

	public static void logException(Throwable e) {
		logException(e, null, null);
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		log(s);
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message = null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message = null;
			}
		} else {
			status = new Status(IStatus.ERROR, AutotoolsPlugin.getUniqueIdentifier(), -1, "Internal Error: ", t); //$NON-NLS-1$	
		}
		ErrorDialog.openError(shell, title, message, status);
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the active workbench window or <code>null</code> if none
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}
}
