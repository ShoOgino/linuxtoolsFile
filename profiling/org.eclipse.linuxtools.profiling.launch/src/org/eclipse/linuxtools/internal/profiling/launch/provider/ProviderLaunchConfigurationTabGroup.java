/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

public class ProviderLaunchConfigurationTabGroup extends
        ProfileLaunchConfigurationTabGroup implements IExecutableExtension {

    // Profiling type.
    private String type;

    // Profiling type name to be displayed.
    private String name;

    @Override
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) {
        @SuppressWarnings("unchecked")
        Map<String, String> parameters = (Map<String, String>) data;
        String profilingType = parameters
                .get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);
        String profilingTypeName = parameters
                .get(ProviderProfileConstants.INIT_DATA_NAME_KEY);

        if (profilingType == null) {
            profilingType = ""; //$NON-NLS-1$
        }
        if (profilingTypeName == null) {
            profilingTypeName = ""; //$NON-NLS-1$
        }

        setProfilingType(profilingType);
        setProfilingTypeName(profilingTypeName);
    }

    @Override
    public AbstractLaunchConfigurationTab[] getProfileTabs() {
        ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<>();
        tabs.add(new ProviderOptionsTab(type, name));

        return tabs.toArray(new AbstractLaunchConfigurationTab [] {});
    }

    /**
     * Set profiling type.
     *
     * @param profilingType New profiling type.
     */
    private void setProfilingType(String profilingType) {
        type = profilingType;
    }

    /**
     * Set profiling type name to be displayed.
     *
     * @param profilingTypeName New profiling type name.
     */
    private void setProfilingTypeName(String profilingTypeName) {
        name = profilingTypeName;
    }
}
