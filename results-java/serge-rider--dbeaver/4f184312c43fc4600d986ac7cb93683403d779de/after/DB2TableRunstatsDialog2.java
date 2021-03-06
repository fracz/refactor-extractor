/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPartSite;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.DB2Table;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Collection;

/**
 * DB2TableRunstatsDialog2
 */
public class DB2TableRunstatsDialog2 extends DB2TableToolDialog {

    private enum ColStats {
        COLS_ALL_AND_DISTRIBUTION, COLS_ALL, COLS_NO
    }

    private enum IndexStats {
        INDEXES_DETAILED, INDEX_ALL, INDEXES_NO
    }

    private Button dlgColsAllAndDistribution;
    private Button dlgColsAll;

    private Button dlgSample;
    private Spinner dlgSampleValue;

    private Button dlgIndexesDetailed;
    private Button dlgIndexesAll;

    public DB2TableRunstatsDialog2(IWorkbenchPartSite partSite, DB2DataSource dataSource, Collection<DB2Table> tables)
    {
        super(partSite, "Runstats Table options", dataSource, tables);
    }

    @Override
    protected void createControls(Composite parent)
    {
        SelectionAdapter changeListener = new SQLChangeListener();

        Composite composite = new Composite(parent, 2);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // RUNSTATS ON COLUMNS
        UIUtils.createTextLabel(composite, "Stats on Columns:")
            .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        Composite groupCols = new Composite(composite, SWT.NONE);
        groupCols.setLayout(new RowLayout(SWT.VERTICAL));
        dlgColsAllAndDistribution = new Button(groupCols, SWT.RADIO);
        dlgColsAllAndDistribution.setText(ColStats.COLS_ALL_AND_DISTRIBUTION.name());
        dlgColsAllAndDistribution.addSelectionListener(changeListener);
        dlgColsAll = new Button(groupCols, SWT.RADIO);
        dlgColsAll.setText(ColStats.COLS_ALL.name());
        dlgColsAll.addSelectionListener(changeListener);
        Button dlgColsNo = new Button(groupCols, SWT.RADIO);
        dlgColsNo.setText(ColStats.COLS_NO.name());
        dlgColsNo.addSelectionListener(changeListener);

        // RUNSTATS ON INDEXES
        UIUtils.createTextLabel(composite, "Stats on Indexes:")
            .setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        Composite groupIx = new Composite(composite, SWT.NULL);
        groupIx.setLayout(new RowLayout(SWT.VERTICAL));
        dlgIndexesDetailed = new Button(groupIx, SWT.RADIO);
        dlgIndexesDetailed.setText(IndexStats.INDEXES_DETAILED.name());
        dlgIndexesDetailed.addSelectionListener(changeListener);
        dlgIndexesAll = new Button(groupIx, SWT.RADIO);
        dlgIndexesAll.setText(IndexStats.INDEX_ALL.name());
        dlgIndexesAll.addSelectionListener(changeListener);
        Button dlgIndexesNo = new Button(groupIx, SWT.RADIO);
        dlgIndexesNo.setText(IndexStats.INDEXES_NO.name());
        dlgIndexesNo.addSelectionListener(changeListener);

        // SAMPLING
        dlgSample = UIUtils.createCheckbox(composite, "Sample (%) ", false);
        dlgSample.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                dlgSampleValue.setEnabled(dlgSample.getSelection());
                updateSQL();
            }
        });

        dlgSampleValue = new Spinner(composite, SWT.BORDER);
        dlgSampleValue.setMinimum(0);
        dlgSampleValue.setMaximum(100);
        dlgSampleValue.setIncrement(1);
        Rectangle clientArea = getShell().getClientArea();
        dlgSampleValue.setLocation(clientArea.x, clientArea.y);
        dlgSampleValue.pack();
        dlgSampleValue.addSelectionListener(changeListener);

        // Read only Resulting RUNSTATS Command
        GridData gd = new GridData();
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalAlignment = GridData.FILL;
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;

        // Initial setup
        dlgColsAllAndDistribution.setSelection(true);
        dlgIndexesDetailed.setSelection(true);
        dlgSampleValue.setSelection(0);
        dlgSampleValue.setEnabled(false);
    }

    @Override
    protected String generateTableCommand(DB2Table db2Table)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("RUNSTATS ON TABLE ").append(db2Table.getFullQualifiedName());

        if (dlgColsAllAndDistribution.getSelection()) {
            sb.append(" ON ALL COLUMNS WITH DISTRIBUTION ON ALL COLUMNS");
        }
        if (dlgColsAll.getSelection()) {
            sb.append(" ON ALL COLUMNS");
        }
        if (dlgIndexesDetailed.getSelection()) {
            sb.append(" AND SAMPLED DETAILED INDEXES ALL");
        }
        if (dlgIndexesAll.getSelection()) {
            sb.append(" AND INDEXES ALL");
        }
        if (dlgSample.getSelection()) {
            sb.append(" TABLESAMPLE SYSTEM(").append(dlgSampleValue.getSelection()).append(")");
        }

        return sb.toString();
    }

}