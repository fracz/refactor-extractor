package com.metaweb.gridworks.commands.row;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import com.metaweb.gridworks.browsing.Engine;
import com.metaweb.gridworks.browsing.FilteredRecords;
import com.metaweb.gridworks.browsing.FilteredRows;
import com.metaweb.gridworks.browsing.RecordVisitor;
import com.metaweb.gridworks.browsing.RowVisitor;
import com.metaweb.gridworks.browsing.Engine.Mode;
import com.metaweb.gridworks.commands.Command;
import com.metaweb.gridworks.model.Project;
import com.metaweb.gridworks.model.Record;
import com.metaweb.gridworks.model.Row;
import com.metaweb.gridworks.util.Pool;

public class GetRowsCommand extends Command {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Project project = getProject(request);
            Engine engine = getEngine(request, project);

            int start = Math.min(project.rows.size(), Math.max(0, getIntegerParameter(request, "start", 0)));
            int limit = Math.min(project.rows.size() - start, Math.max(0, getIntegerParameter(request, "limit", 20)));

            Pool pool = new Pool();
            Properties options = new Properties();
            options.put("project", project);
            options.put("reconCandidateOmitTypes", true);
            options.put("pool", pool);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");

            JSONWriter writer = new JSONWriter(response.getWriter());
            writer.object();

            RowAccumulator acc = new RowAccumulator(start, limit) {
                JSONWriter  writer;
                Properties  options;

                public RowAccumulator init(JSONWriter writer, Properties options) {
                    this.writer = writer;
                    this.options = options;

                    return this;
                }

                @Override
                public boolean internalVisit(Project project, int rowIndex, Row row) {
                    try {
                        options.put("rowIndex", rowIndex);
                        row.write(writer, options);
                    } catch (JSONException e) {
                    }
                    return false;
                }

                @Override
                protected boolean internalVisit(Project project, Record record) {
                    options.put("recordIndex", record.recordIndex);

                	for (int r = record.fromRowIndex; r < record.toRowIndex; r++) {
                        try {
                        	Row row = project.rows.get(r);

                            options.put("rowIndex", r);

                            row.write(writer, options);

                        } catch (JSONException e) {
                        }

                        options.remove("recordIndex");
                	}
                	return false;
                }
            }.init(writer, options);

            if (engine.getMode() == Mode.RowBased) {
                FilteredRows filteredRows = engine.getAllFilteredRows();

                writer.key("mode"); writer.value("row-based");
                writer.key("rows"); writer.array();
                filteredRows.accept(project, acc);
                writer.endArray();
                writer.key("filtered"); writer.value(acc.total);
                writer.key("total"); writer.value(project.rows.size());
            } else {
                FilteredRecords filteredRecords = engine.getFilteredRecords();

                writer.key("mode"); writer.value("record-based");
                writer.key("rows"); writer.array();
                filteredRecords.accept(project, acc);
                writer.endArray();
                writer.key("filtered"); writer.value(acc.total);
                writer.key("total"); writer.value(project.recordModel.getRecordCount());
            }


            writer.key("start"); writer.value(start);
            writer.key("limit"); writer.value(limit);
            writer.key("pool"); pool.write(writer, options);

            writer.endObject();
        } catch (Exception e) {
            respondException(response, e);
        }
    }

    static protected class RowAccumulator implements RowVisitor, RecordVisitor {
        final public int start;
        final public int limit;

        public int total;

        public RowAccumulator(int start, int limit) {
            this.start = start;
            this.limit = limit;
        }

        public boolean visit(Project project, int rowIndex, Row row) {
            boolean r = false;

            if (total >= start && total < start + limit) {
                r = internalVisit(project, rowIndex, row);
            }
        	total++;
            return r;
        }

        @Override
        public boolean visit(Project project, Record record) {
            boolean r = false;

            if (total >= start && total < start + limit) {
                r = internalVisit(project, record);
            }
        	total++;
            return r;
        }

        protected boolean internalVisit(Project project, int rowIndex, Row row) {
            return false;
        }

        protected boolean internalVisit(Project project, Record record) {
            return false;
        }
    }
}