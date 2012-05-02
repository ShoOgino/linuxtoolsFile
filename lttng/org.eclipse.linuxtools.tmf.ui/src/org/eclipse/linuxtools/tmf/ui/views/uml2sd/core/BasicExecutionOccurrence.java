/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;

/**
 * BasicExecutionOccurrence is the UML2 execution occurrence graphical representation. It is attached to one Lifeline,
 * the event occurrence "duration" along the lifeline is defined by two event occurrences
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @version 1.0 
 * @author sveyrier
 * 
 */
public class BasicExecutionOccurrence extends GraphNode {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The grahNode ID constant
     */
    public static final String EXEC_OCC_TAG = "Execution_Occ"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The corresponding lifeline. 
     */
    protected Lifeline lifeline = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructore
     */
    public BasicExecutionOccurrence() {
        prefId = ISDPreferences.PREF_EXEC;
    }

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getX() {
        if (lifeline == null) {
            return 0;
        }
        return lifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getY()
     */
    @Override
    public int getY() {
        if (lifeline == null) {
            return 0;
        }
        return lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * startEventOccurrence;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getWidth()
     */
    @Override
    public int getWidth() {
        if (lifeline == null) {
            return 0;
        }
        return Metrics.EXECUTION_OCCURRENCE_WIDTH;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getHeight()
     */
    @Override
    public int getHeight() {
        if (lifeline == null) {
            return 0;
        }
        return ((Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing())) * (endEventOccurrence - startEventOccurrence);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#contains(int, int)
     */
    @Override
    public boolean contains(int _x, int _y) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        if (Frame.contains(x, y, width, height, _x, _y)) {
            return true;
        }

        if (getNodeAt(_x, _y) != null) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getName()
     */
    @Override
    public String getName() {
        if (super.getName() == null || super.getName().equals("")) { //$NON-NLS-1$
            return lifeline.getToolTipText();
        } else {
            return super.getName();
        }
    }

    /**
     * Set the lifeline on which the execution occurrence appears.
     * 
     * @param theLifeline - the parent lifeline
     */
    public void setLifeline(Lifeline theLifeline) {
        lifeline = theLifeline;
    }

    /**
     * Get the lifeline on which the execution occurrence appears.
     * 
     * @return - the parent lifeline
     */
    public Lifeline getLifeline() {
        return lifeline;
    }

    /**
     * Get the execution start event occurrence
     * 
     * @return the start event occurrence to set
     */
    @Override
    public int getStartOccurrence() {
        return startEventOccurrence;
    }

    /**
     * Set the execution end event occurrence
     * 
     * @return the end event occurrence to set
     */
    @Override
    public int getEndOccurrence() {
        return endEventOccurrence;
    }

    /**
     * Set the execution start event occurrence
     * 
     * @param occurrence the start event occurrence to set
     */
    public void setStartOccurrence(int occurrence) {
        startEventOccurrence = occurrence;
    }

    /**
     * Set the execution end event occurrence
     * 
     * @param occurrence the end event occurrence to set
     */
    public void setEndOccurrence(int occurrence) {
        endEventOccurrence = occurrence;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    public void draw(IGC context) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        IColor tempFillColor = null, tempStrokeColor = null;

        // The execution occurrence is selected
        // if the owning lifeline is selected
        if (lifeline.isSelected() || isSelected()) {
            context.setBackground(Frame.getUserPref().getBackGroundColorSelection());
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
        } else {
            tempFillColor = setUnselectedFillColor(context);
        }
        if (Frame.getUserPref().useGradienColor()) {
            context.fillGradientRectangle(x, y, width, height, false);
        } else {
            context.fillRectangle(x, y, width, height);
        }
        tempStrokeColor = setUnselectedStrokeColor(context);
        context.drawRectangle(x, y, width, height);
        if (tempFillColor != null) {
            tempFillColor.dispose();
            tempFillColor = null;
        }
        if (tempStrokeColor != null) {
            tempStrokeColor.dispose();
            tempStrokeColor = null;
        }
        if (hasFocus()) {
            drawFocus(context);
        }
        super.drawChildenNodes(context);
    }

    /**
     * Rewrite this method in your extension in order to support customized fill colors
     * 
     * @param context
     * @return IColor
     */
    protected IColor setUnselectedFillColor(IGC context) {
        if (Frame.getUserPref().useGradienColor()) {
            context.setGradientColor(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_EXEC));
            context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
        } else {
            context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_EXEC));
        }
        return null;
    }

    /**
     * Rewrite this method in your extension in order to support customized stroke colors
     * 
     * @param context
     * @return IColor
     */
    protected IColor setUnselectedStrokeColor(IGC context) {
        context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_EXEC));
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return EXEC_OCC_TAG;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#positiveDistanceToPoint(int, int)
     */
    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        if (getY() + getHeight() > y) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#isVisible(int, int, int, int)
     */
    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        if ((getLifeline() != null) && (getLifeline().isVisible(x, y, width, height))) {
            int ly = getY();
            int lh = getHeight();
            if (ly >= y && ly < y + height) {
                return true;
            }
            if (ly + lh > y && ly + lh <= y + height) {
                return true;
            }
            if ((ly < y) && (ly + lh > y + height)) {
                return true;
            }
        }
        return false;
    }
}
