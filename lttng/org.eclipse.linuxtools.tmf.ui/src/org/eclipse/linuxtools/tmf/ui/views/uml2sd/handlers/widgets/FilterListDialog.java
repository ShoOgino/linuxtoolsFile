/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FilterListDialog.java,v 1.4 2008/01/24 02:29:09 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;

/**
 * This is the filters list dialog.<br>
 * It is associated to an SDView and to a ISDFilterProvider.<br>
 */
public class FilterListDialog extends Dialog {

    protected static final String FILTERS_LIST_CRITERIA = "filtersListsCriteria"; //$NON-NLS-1$
    protected static final String FILTERS_LIST_SIZE = "filtersListSize"; //$NON-NLS-1$

    /**
     * viewer and provided are kept here as attributes
     */
    protected IViewPart viewer = null;
    protected ISDFilterProvider provider = null;

    /**
     * filters are the result of editing this list
     */
    protected List<FilterCriteria> filters;

    /**
     * add, remove and edit buttons
     */
    protected Button add, remove, edit;

    /**
     * table
     */
    protected Table table;

    /**
     * A class to map TableItems that can be toggled active or inactive and Criterias
     */
    protected class CriteriaTableItem {

        protected Criteria criteria;
        protected boolean positive;
        protected String loaderClassName;
        protected TableItem tableItem;

        public CriteriaTableItem(Table parent, boolean checked_, boolean positive_, String loaderClassName_) {
            tableItem = new TableItem(parent, SWT.NONE);
            tableItem.setData(this);
            tableItem.setChecked(checked_);
            positive = positive_;
            loaderClassName = loaderClassName_;
        }

        public CriteriaTableItem(Table parent, boolean checked_, boolean positive_, String loaderClassName_, int index) {
            tableItem = new TableItem(parent, SWT.NONE, index);
            tableItem.setChecked(checked_);
            positive = positive_;
            loaderClassName = loaderClassName_;
        }

        public void setCriteria(Criteria criteria_) {
            criteria = criteria_;
            tableItem.setText((positive ? SDMessages._59 : SDMessages._58) + " " + criteria.getExpression() + " " + criteria.getGraphNodeSummary(provider, loaderClassName)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public Criteria getCriteria() {
            return criteria;
        }

        public boolean getPositive() {
            return positive;
        }

        public String getLoaderClassName() {
            return loaderClassName;
        }
    }

    /**
     * @param c
     * @param checked
     */
    protected void addCriteria(Criteria c, boolean checked, boolean positive, String loaderClassName) {
        CriteriaTableItem cti = new CriteriaTableItem(table, checked, positive, loaderClassName);
        cti.setCriteria(c);
    }

    /**
     * @param new_
     */
    protected void replaceSelectedCriteria(Criteria new_) {
        CriteriaTableItem cti = (CriteriaTableItem) table.getSelection()[0].getData();
        cti.setCriteria(new_);
    }

    /**
	 * 
	 */
    protected void handleTableSelectionCount() {
        int count = table.getSelectionCount();
        edit.setEnabled(count == 1);
        remove.setEnabled(count > 0);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createDialogArea(Composite parent) {

        Group ret = new Group(parent, SWT.NONE);
        ret.setText(SDMessages._57);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = false;
        rowLayout.pack = true;
        rowLayout.justify = false;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 4;
        rowLayout.marginTop = 4;
        rowLayout.marginRight = 4;
        rowLayout.marginBottom = 4;
        rowLayout.spacing = 8;
        ret.setLayout(rowLayout);

        table = new Table(ret, SWT.MULTI | SWT.CHECK);
        table.setLayoutData(new RowData(220, 84));
        table.setHeaderVisible(false);
        table.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                int count = table.getSelectionCount();
                if (count == 1) {
                    Criteria c = openFilterDialog(((CriteriaTableItem) table.getSelection()[0].getData()).getCriteria(), SDMessages._63);
                    if (c != null) {
                        replaceSelectedCriteria(c);
                    }
                }
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleTableSelectionCount();
            }
        });
        if (filters != null) {
            for (Iterator<FilterCriteria> i = filters.iterator(); i.hasNext();) {
                FilterCriteria filterCriteria = (FilterCriteria) i.next();
                addCriteria(filterCriteria.getCriteria(), filterCriteria.isActive(), filterCriteria.isPositive(), filterCriteria.getLoaderClassName());
            }
        }

        Composite commands = new Composite(ret, SWT.NONE);
        RowLayout rowLayoutCommands = new RowLayout();
        rowLayoutCommands.wrap = false;
        rowLayoutCommands.pack = false;
        rowLayoutCommands.justify = true;
        rowLayoutCommands.type = SWT.VERTICAL;
        rowLayoutCommands.marginLeft = 0;
        rowLayoutCommands.marginTop = 4;
        rowLayoutCommands.marginRight = 0;
        rowLayoutCommands.marginBottom = 4;
        rowLayoutCommands.spacing = 8;
        commands.setLayout(rowLayoutCommands);
        add = new Button(commands, SWT.NONE);
        add.setText(SDMessages._61);
        add.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                Criteria init = new Criteria();
                Criteria c = openFilterDialog(init, SDMessages._62);
                if (c != null) {
                    addCriteria(c, true, false, null);
                }
            }
        });

        edit = new Button(commands, SWT.NONE);
        edit.setText(SDMessages._60);
        edit.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
            
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                Criteria c = openFilterDialog(((CriteriaTableItem) table.getSelection()[0].getData()).getCriteria(), SDMessages._63);
                if (c != null) {
                    replaceSelectedCriteria(c);
                }
            }
        });
        edit.setEnabled(false);

        remove = new Button(commands, SWT.NONE);
        remove.setText(SDMessages._64);
        remove.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                table.remove(table.getSelectionIndices());
                handleTableSelectionCount();
            }
        });
        remove.setEnabled(false);

        getShell().setText(SDMessages._65);
        /*
         * for (int i=0;i<filters.size();i++) { if (filters.get(i) instanceof FilterCriteria)
         * addCriteria(((FilterCriteria)filters.get(i)).getCriteria(),true); }
         */
        return ret;
    }

    /**
     * @param view_
     * @param loader_
     */
    public FilterListDialog(IViewPart view_, ISDFilterProvider loader_) {
        super(view_.getSite().getShell());
        viewer = view_;
        provider = loader_;
        filters = null;
        // filters = provider.getCurrentFilters();
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    /**
     * @param criteria
     * @param action between "Update" and "Create"
     * @return the criteria that has been updated or created
     */
    protected Criteria openFilterDialog(Criteria criteria, String action) {
        SearchFilterDialog filter = new SearchFilterDialog((SDView) viewer, provider, true, SWT.APPLICATION_MODAL);
        filter.setCriteria(criteria);
        filter.setOkText(action);
        filter.setTitle(SDMessages._66);
        filter.open();
        return filter.getCriteria();
    }

    /**
     * Open the dialog box
     */
    @Override
    public int open() {
        create();
        getShell().pack();
        getShell().setLocation(getShell().getDisplay().getCursorLocation());
        loadFiltersCriteria();
        return super.open();
    }

    /**
     * Called when the dialog box ok button is pressed
     */
    @Override
    public void okPressed() {
        if (table.getItemCount() > 0) {
            filters = new ArrayList<FilterCriteria>();
        } else {
            filters = null;
        }
        for (int i = 0; i < table.getItemCount(); i++) {
            TableItem item = table.getItem(i);
            CriteriaTableItem cti = (CriteriaTableItem) item.getData();
            FilterCriteria fc = new FilterCriteria(cti.getCriteria(), item.getChecked(), cti.getPositive(), cti.getLoaderClassName());
            FilterCriteria efc = FilterCriteria.find(fc, filters);
            if (efc == null) {
                filters.add(fc);
            } else {
                efc.setActive(efc.isActive() || fc.isActive());
            }
        }
        super.close();
        provider.filter(filters);
        saveFiltersCriteria(filters);
    }

    /**
     * @param filters_
     */
    public void setFilters(ArrayList<FilterCriteria> filters_) {
        filters = filters_;
    }

    /**
     * @return the filters list after editing
     */
    public List<FilterCriteria> getFilters() {
        return filters;
    }

    protected void loadFiltersCriteria() {
        List<FilterCriteria> globalFilters = getGlobalFilters();
        for (Iterator<FilterCriteria> i = globalFilters.iterator(); i.hasNext();) {
            FilterCriteria filterCriteria = (FilterCriteria) i.next();
            addCriteria(filterCriteria.getCriteria(), filterCriteria.isActive(), filterCriteria.isPositive(), filterCriteria.getLoaderClassName());
        }
    }

    public static List<FilterCriteria> getGlobalFilters() {
        DialogSettings settings = (DialogSettings) TmfUiPlugin.getDefault().getDialogSettings().getSection(FILTERS_LIST_CRITERIA);
        int i = 0;
        DialogSettings section = null;
        int size = 0;
        if (settings != null) {
            try {
                size = settings.getInt(FILTERS_LIST_SIZE);
            } catch (NumberFormatException e) {
                // This is not a problem
                size = 0;
            }
            section = (DialogSettings) settings.getSection(FILTERS_LIST_CRITERIA + i);
        }

        List<FilterCriteria> globalFilters = new ArrayList<FilterCriteria>();

        while ((section != null) && (i < size)) {
            FilterCriteria criteria = new FilterCriteria();
            criteria.setCriteria(new Criteria());
            criteria.load(section);
            globalFilters.add(criteria);
            section = (DialogSettings) settings.getSection(FILTERS_LIST_CRITERIA + (++i));
        }

        return globalFilters;
    }

    public static void saveFiltersCriteria(List<FilterCriteria> globalFilters) {
        DialogSettings settings = (DialogSettings) TmfUiPlugin.getDefault().getDialogSettings();
        DialogSettings section = (DialogSettings) settings.getSection(FILTERS_LIST_CRITERIA);
        if (section == null) {
            section = (DialogSettings) settings.addNewSection(FILTERS_LIST_CRITERIA);
        }

        if (globalFilters == null) {
            section.put(FILTERS_LIST_SIZE, 0);
            return;
        }

        section.put(FILTERS_LIST_SIZE, globalFilters.size());

        FilterCriteria criteria;

        for (int j = 0; j < globalFilters.size(); j++) {
            if (!(globalFilters.get(j) instanceof FilterCriteria))
                return;

            criteria = (FilterCriteria) globalFilters.get(j);
            DialogSettings subSection = (DialogSettings) section.getSection(FILTERS_LIST_CRITERIA + j);
            ;
            if (subSection == null) {
                subSection = (DialogSettings) section.addNewSection(FILTERS_LIST_CRITERIA + j);
            }
            criteria.save(subSection);
        }
    }
    
    public static void deactivateSavedGlobalFilters() {
     // Deactivate all filters
        List<FilterCriteria> filters = getGlobalFilters();
        for(FilterCriteria criteria : filters) {
            criteria.setActive(false);
        }
        // Save settings
        FilterListDialog.saveFiltersCriteria(filters);
    }

}
