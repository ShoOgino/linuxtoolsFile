/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>HistogramCurrentTimeControl</u></b>
 * <p>
 * This control provides a group containing a text control.
 */
public class HistogramCurrentTimeControl extends HistogramTextControl {

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle) {
        this(parentView, parent, textStyle, groupStyle, "", HistogramUtils.nanosecondsToString(0L)); //$NON-NLS-1$
    }

    public HistogramCurrentTimeControl(HistogramView parentView, Composite parent, int textStyle, int groupStyle, String groupValue, String textValue) {
        super(parentView, parent, textStyle, groupStyle, groupValue, textValue);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void updateValue() {
        String stringValue = fTextValue.getText();
        long value = HistogramUtils.stringToNanoseconds(stringValue);

        if (getValue() != value) {
            setValue(value);
            fParentView.updateCurrentEventTime(value);
        }
    }

}
