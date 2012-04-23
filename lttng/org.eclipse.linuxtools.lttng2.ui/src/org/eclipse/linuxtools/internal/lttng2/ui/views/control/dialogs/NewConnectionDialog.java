/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>NewConnectionDialog</u></b>
 * <p>
 * Dialog box for connection information.
 * </p>
 */
public class NewConnectionDialog extends Dialog implements INewConnectionDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/target_add.gif"; //$NON-NLS-1$ 

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dialog composite.
     */
    private Composite fDialogComposite = null;
    /**
     * The Group for the host combo box.
     */
    private Group fComboGroup = null;
    /**
     * The Group for the text input.
     */
    private Group fTextGroup = null;
    /**
     * The host combo box.
     */
    private CCombo fExistingHostsCombo = null;
    /**
     * The check box button for enabling/disabling the text input.
     */
    private Button fButton = null;
    /**
     * The text widget for the node name (alias)
     */
    private Text fConnectionNameText = null;
    /**
     * The text widget for the node address (IP or DNS name)
     */
    private Text fHostNameText = null;
    /**
     * The parent where the new node should be added.
     */
    private ITraceControlComponent fParent;
    /**
     * The node name (alias) string.
     */
    private String fConnectionName = null;
    /**
     * The node address (IP or DNS name) string.
     */
    private String fHostName = null;
    
    /**
     * Input list of existing RSE hosts available for selection.
     */
    private IHost[] fExistingHosts = new IHost[0];

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public NewConnectionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.INewConnectionDialog#getConnectionName()
     */
    @Override
    public String getConnectionName() {
        return fConnectionName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.INewConnectionDialog#getHostName()
     */
    @Override
    public String getHostName() {
        return fHostName;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.INewConnectionDialog#setTraceControlParent(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void setTraceControlParent(ITraceControlComponent parent) {
        fParent = parent;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.INewConnectionDialog#setHosts(org.eclipse.rse.core.model.IHost[])
     */
    @Override
    public void setHosts(IHost[] hosts) {
        if (hosts != null) {
            fExistingHosts = Arrays.copyOf(hosts, hosts.length);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_NewDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        
        // Main dialog panel
        fDialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        fDialogComposite.setLayout(layout);
        fDialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Existing connections group
        fComboGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        fComboGroup.setText(Messages.TraceControl_NewNodeExistingConnectionGroupName);
        layout = new GridLayout(2, true);
        fComboGroup.setLayout(layout); 
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        fComboGroup.setLayoutData(data);
        
        fExistingHostsCombo = new CCombo(fComboGroup, SWT.READ_ONLY);
        fExistingHostsCombo.setToolTipText(Messages.TraceControl_NewNodeComboToolTip);
        fExistingHostsCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        String items[] = new String[fExistingHosts.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(fExistingHosts[i].getAliasName() + " - " + fExistingHosts[i].getHostName()); //$NON-NLS-1$
        }

        fExistingHostsCombo.setItems(items);
        fExistingHostsCombo.setEnabled(fExistingHosts.length > 0);

        // Node information grop
        fTextGroup = new Group(fDialogComposite, SWT.SHADOW_NONE);
        layout = new GridLayout(3, true);
        fTextGroup.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        fTextGroup.setLayoutData(data);
        
        fButton = new Button(fTextGroup, SWT.CHECK);
        fButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        fButton.setText(Messages.TraceControl_NewNodeEditButtonName);
        fButton.setEnabled(fExistingHosts.length > 0);
        
        Label connectionNameLabel = new Label(fTextGroup, SWT.RIGHT);
        connectionNameLabel.setText(Messages.TraceControl_NewNodeConnectionNameLabel);
        fConnectionNameText = new Text(fTextGroup, SWT.NONE);
        fConnectionNameText.setToolTipText(Messages.TraceControl_NewNodeConnectionNameTooltip);
        fConnectionNameText.setEnabled(fExistingHosts.length == 0);
        
        Label hostNameLabel = new Label(fTextGroup, SWT.RIGHT);
        hostNameLabel.setText(Messages.TraceControl_NewNodeHostNameLabel);
        fHostNameText = new Text(fTextGroup, SWT.NONE);
        fHostNameText.setToolTipText(Messages.TraceControl_NewNodeHostNameTooltip);
        fHostNameText.setEnabled(fExistingHosts.length == 0);

        fButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fButton.getSelection()) {
                    fExistingHostsCombo.deselectAll();
                    fExistingHostsCombo.setEnabled(false);
                    fConnectionNameText.setEnabled(true);
                    fHostNameText.setEnabled(true);
                } else {
                    fExistingHostsCombo.setEnabled(true);
                    fConnectionNameText.setEnabled(false);
                    fHostNameText.setEnabled(false);
                }             
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fExistingHostsCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = fExistingHostsCombo.getSelectionIndex();
                fConnectionNameText.setText(fExistingHosts[index].getAliasName());
                fHostNameText.setText(fExistingHosts[index].getHostName());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        // layout widgets
        data = new GridData(GridData.FILL_HORIZONTAL);
        fHostNameText.setText("666.666.666.666"); //$NON-NLS-1$
        Point minSize = fHostNameText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = minSize.x + 5;
        data.horizontalSpan = 2;
        
        fConnectionNameText.setLayoutData(data);
        fHostNameText.setLayoutData(data);
        
        fHostNameText.setText(""); //$NON-NLS-1$
        
        return fDialogComposite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        // Validate input data
        fConnectionName = fConnectionNameText.getText();
        fHostName = fHostNameText.getText();

        if (!"".equals(fHostName)) { //$NON-NLS-1$
            // If no node name is specified use the node address as name
            if ("".equals(fConnectionName)) { //$NON-NLS-1$
                fConnectionName = fHostName;
            }
            // Check if node with name already exists in parent
            if(fParent.containsChild(fConnectionName)) {
                MessageDialog.openError(getShell(),
                        Messages.TraceControl_NewDialogTitle,
                        Messages.TraceControl_AlreadyExistsError + " (" + fConnectionName + ")");  //$NON-NLS-1$//$NON-NLS-2$
                return;
            }
        }
        else {
            return;
        }
        // validation successful -> call super.okPressed()
        super.okPressed();
    }
}
