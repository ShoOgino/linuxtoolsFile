/*******************************************************************************
 * Copyright (c) 2013, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.binutils.link2source;

import org.eclipse.osgi.util.NLS;


/**
 * @since 4.1
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.binutils.link2source.messages"; //$NON-NLS-1$
    public static String STCSourceNotFoundEditor_cant_find_source_file;
    public static String STCSourceNotFoundEditor_edit_source_lookup_path;
    public static String STCSourceNotFoundEditor_failed_saving_settings_for_content_type;
    public static String STCSourceNotFoundEditor_locate_file;
    public static String STCSourceNotFoundEditor_missing_source_file;
    public static String STCSourceNotFoundEditor_no_source_available;
    public static String STCSourceNotFoundEditorInput_source_not_found;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
