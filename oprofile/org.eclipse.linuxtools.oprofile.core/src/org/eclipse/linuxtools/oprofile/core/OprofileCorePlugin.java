/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 

package org.eclipse.linuxtools.oprofile.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.linuxtools.oprofile.core.linux.LinuxOpcontrolProvider;
import org.eclipse.linuxtools.oprofile.core.linux.LinuxOpxmlProvider;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OprofileCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.core";
	private static final String OPXML_FRAGMENT_PLUGIN_ID = "org.eclipse.linuxtools.oprofile.core.linux";
	private static final String OPXML_PATH_STRING = "$os$/opxml";
	private static String _pathToOpxml = null;

	//The shared instance.
	private static OprofileCorePlugin plugin;
	
	/**
	 * The constructor.
	 */
	public OprofileCorePlugin() {
		plugin = this;
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
	public static OprofileCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the unique id of this plugin. Should match plugin.xml!
	 */
	public static String getId() {
		return PLUGIN_ID;
	}
	
	/**
	 * Returns the OpxmlProvider registered with the plugin or throws an exception
	 * @return the OpxmlProvider
	 * @throws OpxmlException
	 */
	public IOpxmlProvider getOpxmlProvider() throws OpxmlException {
		if (_pathToOpxml != null) {
			URL opxmlUrl = FileLocator.find(Platform.getBundle(OPXML_FRAGMENT_PLUGIN_ID + Platform.getOSArch()), new Path(OPXML_PATH_STRING), null); 
			
			if (opxmlUrl == null) {
				// If no provider found, throw a new exception
				String msg = OprofileProperties.getString("opxmlProvider.error.missing"); //$NON-NLS-1$
				Status status = new Status(IStatus.ERROR, getId(), IStatus.OK, msg, null);
				throw new OpxmlException(status);
			} else {
				try {
					_pathToOpxml = FileLocator.toFileURL(opxmlUrl).getPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return new LinuxOpxmlProvider(_pathToOpxml);
	}
	
	/**
	 * Returns the registered opcontrol provider or throws an exception
	 * @return the OpcontrolProvider registered with the plugin
	 * @throws OpcontrolException
	 */
	public IOpcontrolProvider getOpcontrolProvider() throws OpcontrolException {
//		Exception except = null;
//		
//		if (_opcontrol == null) {
//			IExtensionRegistry registry = Platform.getExtensionRegistry();
//			IExtensionPoint extension = registry.getExtensionPoint(PLUGIN_ID, "OpcontrolProvider"); //$NON-NLS-1$
//			if (extension != null) {
//				IExtension[] extensions = extension.getExtensions();
//				IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
//				if (configElements.length != 0) {
//					try {
//						_opcontrol = (IOpcontrolProvider) configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
//					} catch (CoreException ce) {
//						except = ce;
//					}
//				}
//			}
//		}
//		
//		// If there was a problem finding opcontrol, throw an exception
//		if (_opcontrol == null) {
//			String msg = getResourceString("opcontrolProvider.error.missing"); //$NON-NLS-1$
//			Status status = new Status(IStatus.ERROR, getId(), IStatus.OK, msg, except);
//			throw new OpcontrolException(status);
//		}
//
//		return _opcontrol;
		
		return new LinuxOpcontrolProvider();
	}
}
