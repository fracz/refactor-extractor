/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.mssql.views;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.ext.mssql.Activator;
import org.jkiss.dbeaver.ext.mssql.MSSQLConstants;
import org.jkiss.dbeaver.ext.mssql.MSSQLMessages;
import org.jkiss.dbeaver.ext.ui.ICompositeDialogPage;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ClientHomesSelector;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionPageAbstract;
import org.jkiss.dbeaver.ui.dialogs.connection.DriverPropertiesDialogPage;
import org.jkiss.utils.CommonUtils;

import java.util.Locale;

/**
 * MSSQLConnectionPage
 */
public class MSSQLConnectionPage extends ConnectionPageAbstract implements ICompositeDialogPage
{
    private Text hostText;
    private Text portText;
    private Text dbText;
    private Text usernameText;
    private Text passwordText;
    private ClientHomesSelector homesSelector;

    private static ImageDescriptor logoImage = Activator.getImageDescriptor("icons/mssql_logo.png");


    @Override
    public void dispose()
    {
        super.dispose();
    }

    @Override
    public void createControl(Composite composite)
    {
        //Composite group = new Composite(composite, SWT.NONE);
        //group.setLayout(new GridLayout(1, true));
        setImageDescriptor(logoImage);

        ModifyListener textListener = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                evaluateURL();
            }
        };

        Composite addrGroup = UIUtils.createPlaceholder(composite, 4);
        GridLayout gl = new GridLayout(4, false);
        addrGroup.setLayout(gl);
        GridData gd = new GridData(GridData.FILL_BOTH);
        addrGroup.setLayoutData(gd);

        Label hostLabel = UIUtils.createControlLabel(addrGroup, MSSQLMessages.dialog_connection_host);
        hostLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        hostText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        hostText.setLayoutData(gd);
        hostText.addModifyListener(textListener);

        Label portLabel = UIUtils.createControlLabel(addrGroup, MSSQLMessages.dialog_connection_port);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        portLabel.setLayoutData(gd);

        portText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.widthHint = 40;
        portText.setLayoutData(gd);
        portText.addVerifyListener(UIUtils.getIntegerVerifyListener(Locale.getDefault()));
        portText.addModifyListener(textListener);

        Label dbLabel = UIUtils.createControlLabel(addrGroup, MSSQLMessages.dialog_connection_database);
        dbLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        dbText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 3;
        dbText.setLayoutData(gd);
        dbText.addModifyListener(textListener);

        Label usernameLabel = UIUtils.createControlLabel(addrGroup, MSSQLMessages.dialog_connection_user_name);
        usernameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        usernameText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        usernameText.setLayoutData(gd);
        usernameText.addModifyListener(textListener);

        Label passwordLabel = UIUtils.createControlLabel(addrGroup, MSSQLMessages.dialog_connection_password);
        passwordLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        passwordText = new Text(addrGroup, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        passwordText.setLayoutData(gd);
        passwordText.addModifyListener(textListener);

        homesSelector = new ClientHomesSelector(addrGroup, SWT.NONE, "Local Client");
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        //gd.minimumWidth = 300;
        homesSelector.setLayoutData(gd);

        setControl(addrGroup);
    }

    @Override
    public boolean isComplete()
    {
        return hostText != null && portText != null &&
            !CommonUtils.isEmpty(hostText.getText()) &&
            !CommonUtils.isEmpty(portText.getText());
    }

    @Override
    public void loadSettings()
    {
        // Load values from new connection info
        DBPConnectionInfo connectionInfo = site.getConnectionInfo();
        if (connectionInfo != null) {
            if (hostText != null) {
                hostText.setText(CommonUtils.getString(connectionInfo.getHostName()));
            }
            if (portText != null) {
                if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
                    portText.setText(String.valueOf(connectionInfo.getHostPort()));
                } else {
                    portText.setText(String.valueOf(MSSQLConstants.DEFAULT_PORT));
                }
            }
            if (dbText != null) {
                dbText.setText(CommonUtils.getString(connectionInfo.getDatabaseName()));
            }
            if (usernameText != null) {
                usernameText.setText(CommonUtils.getString(connectionInfo.getUserName()));
            }
            if (passwordText != null) {
                passwordText.setText(CommonUtils.getString(connectionInfo.getUserPassword()));
            }
            homesSelector.populateHomes(site.getDriver(), connectionInfo.getClientHomeId());
        } else {
            if (portText != null) {
                portText.setText(String.valueOf(MSSQLConstants.DEFAULT_PORT));
            }
        }

        super.loadSettings();
    }

    @Override
    protected void saveSettings(DBPConnectionInfo connectionInfo)
    {
        if (connectionInfo != null) {
            if (hostText != null) {
                connectionInfo.setHostName(hostText.getText());
            }
            if (portText != null) {
                connectionInfo.setHostPort(portText.getText());
            }
            if (dbText != null) {
                connectionInfo.setDatabaseName(dbText.getText());
            }
            if (usernameText != null) {
                connectionInfo.setUserName(usernameText.getText());
            }
            if (passwordText != null) {
                connectionInfo.setUserPassword(passwordText.getText());
            }
            if (homesSelector != null) {
                connectionInfo.setClientHomeId(homesSelector.getSelectedHome());
            }
            super.saveSettings(connectionInfo);
        }
    }

    private void evaluateURL()
    {
        site.updateButtons();
    }

    @Override
    public IDialogPage[] getSubPages()
    {
        return new IDialogPage[] {
            new DriverPropertiesDialogPage(this)
        };
    }

}