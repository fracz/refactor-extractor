package com.metaweb.gridworks.operations.row;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.metaweb.gridworks.history.HistoryEntry;
import com.metaweb.gridworks.model.AbstractOperation;
import com.metaweb.gridworks.model.Cell;
import com.metaweb.gridworks.model.Project;
import com.metaweb.gridworks.model.Row;
import com.metaweb.gridworks.model.RecordModel.CellDependency;
import com.metaweb.gridworks.model.RecordModel.RowDependency;
import com.metaweb.gridworks.model.changes.MassRowChange;
import com.metaweb.gridworks.operations.OperationRegistry;

public class DenormalizeOperation extends AbstractOperation {
    static public AbstractOperation reconstruct(Project project, JSONObject obj) throws Exception {
        return new DenormalizeOperation();
    }

    public DenormalizeOperation() {
    }

   public void write(JSONWriter writer, Properties options)
           throws JSONException {

       writer.object();
       writer.key("op"); writer.value(OperationRegistry.s_opClassToName.get(this.getClass()));
       writer.key("description"); writer.value("Denormalize");
       writer.endObject();
    }


    protected String getBriefDescription(Project project) {
        return "Denormalize";
    }

    protected HistoryEntry createHistoryEntry(Project project, long historyEntryID) throws Exception {
        List<Row> newRows = new ArrayList<Row>();

        List<Row> oldRows = project.rows;
        for (int r = 0; r < oldRows.size(); r++) {
            Row oldRow = oldRows.get(r);
            Row newRow = null;

            RowDependency rd = project.recordModel.getRowDependency(r);
            if (rd.cellDependencies != null) {
                newRow = oldRow.dup();

                for (int c = 0; c < rd.cellDependencies.length; c++) {
                	CellDependency cd = rd.cellDependencies[c];
                	if (cd != null) {
	                    int contextRowIndex = cd.rowIndex;
	                    int contextCellIndex = cd.cellIndex;

	                    if (contextRowIndex >= 0 && contextRowIndex < oldRows.size()) {
	                        Row contextRow = oldRows.get(contextRowIndex);
	                        Cell contextCell = contextRow.getCell(contextCellIndex);

	                        newRow.setCell(contextCellIndex, contextCell);
	                    }
                	}
                }
            }

            newRows.add(newRow != null ? newRow : oldRow);
        }

        return new HistoryEntry(
            historyEntryID,
            project,
            getBriefDescription(project),
            DenormalizeOperation.this,
            new MassRowChange(newRows)
        );
    }
}