/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SDViewPref.java,v 1.3 2008/01/24 02:28:51 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences;

import java.util.Hashtable;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.ColorImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.FontImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * This is the Sequence Diagram preference handler This class is responsible for accessing the current user preferences
 * selection This class also provider getters for each modifiable preferences
 * 
 * @author sveyrier
 */
public class SDViewPref implements ISDPreferences, IPropertyChangeListener {

    public static final String BACK_COLOR_POSTFIX = "_BACK_COLOR";//$NON-NLS-1$
    public static final String FORE_COLOR_POSTFIX = "_FORE_COLOR";//$NON-NLS-1$
    public static final String TEXT_COLOR_POSTFIX = "_TEXT_COLOR";//$NON-NLS-1$

    public static final String[] fontList = { PREF_LIFELINE, PREF_EXEC, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };

    public static final String[] fontList2 = { SDMessages._88, SDMessages._89, SDMessages._90, SDMessages._91, SDMessages._92, SDMessages._93, SDMessages._94, SDMessages._95, SDMessages._96 };

    public static final String[] prefBackColorList = { PREF_LIFELINE, PREF_EXEC, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };

    public static final String[] prefForeColorList = { PREF_LIFELINE, PREF_EXEC, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };

    public static final String[] prefTextColorList = { PREF_LIFELINE, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };

    protected Hashtable<String, IFont> fontPref;
    protected Hashtable<String, IColor> foreColorPref;
    protected Hashtable<String, IColor> backColorPref;
    protected Hashtable<String, IColor> textColorPref;

    protected static SDViewPref handle = null;
    protected IPreferenceStore prefStore = null;
    protected IColor timeCompressionSelectionColor = null;

    protected boolean noFocusSelection = false;

    protected static final String TEMP_TAG = "_TEMP";//$NON-NLS-1$

    /**
     * Builds the Sequence Diagram preference handler: - Define the preference default values. - Load the currently used
     * preferences setting
     * 
     */
    protected SDViewPref() {
        prefStore = TmfUiPlugin.getDefault().getPreferenceStore();

        prefStore.setDefault(PREF_LINK_FONT, true);
        prefStore.setDefault(PREF_EXCLUDE_EXTERNAL_TIME, true);
        prefStore.setDefault(PREF_LIFELINE_WIDTH, 200);
        prefStore.setDefault(PREF_USE_GRADIENT, true);
        prefStore.setDefault(PREF_TOOLTIP, true);

        fontPref = new Hashtable<String, IFont>();
        foreColorPref = new Hashtable<String, IColor>();
        backColorPref = new Hashtable<String, IColor>();
        textColorPref = new Hashtable<String, IColor>();

        for (int i = 0; i < fontList.length; i++) {
            if (fontList[i].equals(PREF_FRAME_NAME)) {
                FontData[] data = Display.getDefault().getSystemFont().getFontData();
                data[0].setStyle(SWT.BOLD);
                PreferenceConverter.setDefault(prefStore, fontList[i], data[0]);
                PreferenceConverter.setDefault(prefStore, fontList[i] + TEMP_TAG, data[0]);
            } else {
                PreferenceConverter.setDefault(prefStore, fontList[i], Display.getDefault().getSystemFont().getFontData());
                PreferenceConverter.setDefault(prefStore, fontList[i] + TEMP_TAG, Display.getDefault().getSystemFont().getFontData());
            }
        }

        for (int i = 0; i < prefBackColorList.length; i++) {
            IColor color;
            if ((prefBackColorList[i].equals(PREF_EXEC)) || prefBackColorList[i].equals(PREF_FRAME_NAME))
                color = new ColorImpl(Display.getDefault(), 201, 222, 233);
            else if (prefBackColorList[i].equals(PREF_LIFELINE))
                color = new ColorImpl(Display.getDefault(), 220, 220, 220);
            else if (prefBackColorList[i].equals(PREF_LIFELINE_HEADER))
                color = new ColorImpl(Display.getDefault(), 245, 244, 244);
            else
                color = new ColorImpl(Display.getDefault(), 255, 255, 255);
            PreferenceConverter.setDefault(prefStore, prefBackColorList[i] + BACK_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(prefStore, prefBackColorList[i] + BACK_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        for (int i = 0; i < prefForeColorList.length; i++) {
            IColor color;
            if (prefForeColorList[i].equals(PREF_LIFELINE))
                color = new ColorImpl(Display.getDefault(), 129, 129, 129);
            else if (prefForeColorList[i].equals(PREF_FRAME_NAME))
                color = new ColorImpl(Display.getDefault(), 81, 153, 200);
            else if (prefForeColorList[i].equals(PREF_LIFELINE_HEADER))
                color = new ColorImpl(Display.getDefault(), 129, 127, 137);
            else
                color = new ColorImpl(Display.getDefault(), 134, 176, 212);
            PreferenceConverter.setDefault(prefStore, prefForeColorList[i] + FORE_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(prefStore, prefForeColorList[i] + FORE_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        for (int i = 0; i < prefTextColorList.length; i++) {
            IColor color;
            if (prefTextColorList[i].equals(PREF_LIFELINE))
                color = new ColorImpl(Display.getDefault(), 129, 129, 129);
            else if (prefTextColorList[i].equals(PREF_FRAME_NAME))
                color = new ColorImpl(Display.getDefault(), 0, 0, 0);
            else if (prefTextColorList[i].equals(PREF_LIFELINE_HEADER))
                color = new ColorImpl(Display.getDefault(), 129, 127, 137);
            else
                color = new ColorImpl(Display.getDefault(), 134, 176, 212);
            PreferenceConverter.setDefault(prefStore, prefTextColorList[i] + TEXT_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(prefStore, prefTextColorList[i] + TEXT_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        IColor color = new ColorImpl(Display.getDefault(), 218, 232, 238);
        PreferenceConverter.setDefault(prefStore, PREF_TIME_COMP, ((Color) color.getColor()).getRGB());
        color.dispose();

        buildFontsAndColors();

        prefStore.addPropertyChangeListener(this);
    }

    /**
     * Returns the PreferenceStore
     * 
     * @return the PreferenceStore
     */
    public IPreferenceStore getPreferenceStore() {
        return prefStore;
    }

    /**
     * Apply the preferences in the preferences handler
     */
    public void apply() {
        buildFontsAndColors();
        prefStore.firePropertyChangeEvent("PREFOK", null, null); //$NON-NLS-1$
    }

    /**
     * Returns an unique instance of the Sequence Diagram preference handler
     * 
     * @return the preference handler instance
     */
    static public SDViewPref getInstance() {
        if (handle == null)
            handle = new SDViewPref();
        return handle;
    }

    /**
     * Returns the chosen foreground color
     * 
     * @return the foreground color
     */
    @Override
    public IColor getForeGroundColor(String prefName) {
        if ((foreColorPref.get(prefName + FORE_COLOR_POSTFIX) != null) && (foreColorPref.get(prefName + FORE_COLOR_POSTFIX) instanceof ColorImpl))
            return (IColor) foreColorPref.get(prefName + FORE_COLOR_POSTFIX);
        else
            return ColorImpl.getSystemColor(SWT.COLOR_BLACK);
    }

    /**
     * Returns the chosen background color
     * 
     * @return the background color
     */
    @Override
    public IColor getBackGroundColor(String prefName) {
        if ((backColorPref.get(prefName + BACK_COLOR_POSTFIX) != null) && (backColorPref.get(prefName + BACK_COLOR_POSTFIX) instanceof ColorImpl))
            return (IColor) backColorPref.get(prefName + BACK_COLOR_POSTFIX);
        else
            return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    /**
     * Returns the chosen font color
     * 
     * @return the font color
     */
    @Override
    public IColor getFontColor(String prefName) {
        if ((textColorPref.get(prefName + TEXT_COLOR_POSTFIX) != null) && (textColorPref.get(prefName + TEXT_COLOR_POSTFIX) instanceof ColorImpl))
            return (IColor) textColorPref.get(prefName + TEXT_COLOR_POSTFIX);
        else
            return ColorImpl.getSystemColor(SWT.COLOR_BLACK);
    }

    /**
     * Returns the foreground color to use for widget selection. This color is system dependent and not set using the
     * preference page
     * 
     * @return the foreground color
     */
    @Override
    public IColor getForeGroundColorSelection() {
        if (noFocusSelection)
            return ColorImpl.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
        return ColorImpl.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
    }

    /**
     * Returns the background color to use for widget selection. This color is system dependent and not set using the
     * preference page
     * 
     * @return the background color
     */
    @Override
    public IColor getBackGroundColorSelection() {
        if (noFocusSelection)
            return ColorImpl.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        return ColorImpl.getSystemColor(SWT.COLOR_LIST_SELECTION);
    }

    /**
     * Returns the chosen font
     * 
     * @return the font
     */
    @Override
    public IFont getFont(String prefName) {
        if ((fontPref.get(prefName) != null) && (fontPref.get(prefName) instanceof IFont))
            return (IFont) fontPref.get(prefName);
        else
            return FontImpl.getSystemFont();
    }

    /**
     * Returns the SwimLane width chosen
     * 
     * @return the SwimLane width
     */
    public int getLifelineWidth() {
        return prefStore.getInt(PREF_LIFELINE_WIDTH);
    }

    /**
     * Returns if font linkage with zoom has been chosen
     * 
     * @return true if checked false otherwise
     */
    public boolean fontLinked() {
        return prefStore.getBoolean(PREF_LINK_FONT);
    }

    /**
     * Returns the tooltip enablement
     * 
     * @return true if checked false otherwise
     */
    public boolean tooltipEnabled() {
        return prefStore.getBoolean(PREF_TOOLTIP);
    }

    /**
     * Return true if the user do not want to take external time (basically found and lost messages with time) into
     * account in the min max computation
     * 
     * @return true if checked false otherwise
     */
    public boolean excludeExternalTime() {
        return prefStore.getBoolean(PREF_EXCLUDE_EXTERNAL_TIME);
    }

    /**
     * Returns if the use gradient color has been chosen
     * 
     * @return true if checked false otherwise
     */
    @Override
    public boolean useGradienColor() {
        return prefStore.getBoolean(PREF_USE_GRADIENT);
    }

    /**
     * Returns the color used to connect the time compression bar to the diagram graph node
     * 
     * @return the selection color
     */
    @Override
    public IColor getTimeCompressionSelectionColor() {
        return timeCompressionSelectionColor;
    }

    /**
     * Builds the new colors and fonts according the current user selection when the OK or Apply button is clicked
     * 
     */
    protected void buildFontsAndColors() {

        Display display = Display.getDefault();

        for (int i = 0; i < fontList.length; i++) {
            FontData fontData = PreferenceConverter.getFontData(prefStore, fontList[i]);
            if ((fontPref.get(fontList[i]) != null) && (fontPref.get(fontList[i]) instanceof IFont))
                ((IFont) fontPref.get(fontList[i])).dispose();
            fontPref.put(fontList[i], new FontImpl(display, fontData));
        }

        for (int i = 0; i < prefBackColorList.length; i++) {
            RGB rgb = PreferenceConverter.getColor(prefStore, prefBackColorList[i] + BACK_COLOR_POSTFIX);
            if ((backColorPref.get(prefBackColorList[i] + BACK_COLOR_POSTFIX) != null) && (backColorPref.get(prefBackColorList[i] + BACK_COLOR_POSTFIX) instanceof IColor))
                ((IColor) backColorPref.get(prefBackColorList[i] + BACK_COLOR_POSTFIX)).dispose();
            backColorPref.put(prefBackColorList[i] + BACK_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        for (int i = 0; i < prefForeColorList.length; i++) {
            RGB rgb = PreferenceConverter.getColor(prefStore, prefForeColorList[i] + FORE_COLOR_POSTFIX);
            if ((foreColorPref.get(prefForeColorList[i] + FORE_COLOR_POSTFIX) != null) && (foreColorPref.get(prefForeColorList[i] + FORE_COLOR_POSTFIX) instanceof IColor))
                ((IColor) foreColorPref.get(prefForeColorList[i] + FORE_COLOR_POSTFIX)).dispose();
            foreColorPref.put(prefForeColorList[i] + FORE_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        for (int i = 0; i < prefTextColorList.length; i++) {
            RGB rgb = PreferenceConverter.getColor(prefStore, prefTextColorList[i] + TEXT_COLOR_POSTFIX);
            if ((textColorPref.get(prefTextColorList[i] + TEXT_COLOR_POSTFIX) != null) && (textColorPref.get(prefTextColorList[i] + TEXT_COLOR_POSTFIX) instanceof IColor))
                ((IColor) textColorPref.get(prefTextColorList[i] + TEXT_COLOR_POSTFIX)).dispose();
            textColorPref.put(prefTextColorList[i] + TEXT_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        RGB rgb = PreferenceConverter.getColor(prefStore, PREF_TIME_COMP);
        if (timeCompressionSelectionColor != null)
            timeCompressionSelectionColor.dispose();
        timeCompressionSelectionColor = new ColorImpl(display, rgb.red, rgb.green, rgb.blue);
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        prefStore.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        prefStore.removePropertyChangeListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!event.getProperty().equals("PREFOK")) //$NON-NLS-1$
        {
            buildFontsAndColors();
            prefStore.firePropertyChangeEvent("PREFOK", null, null); //$NON-NLS-1$	
        }
    }

    public void setNoFocusSelection(boolean v) {
        noFocusSelection = v;
    }
}
