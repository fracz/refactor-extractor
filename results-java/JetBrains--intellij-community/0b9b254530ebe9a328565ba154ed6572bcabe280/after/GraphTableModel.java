package org.hanuna.gitalk.ui_controller.table_models;

import org.hanuna.gitalk.commitmodel.Commit;
import org.hanuna.gitalk.commitmodel.CommitData;
import org.hanuna.gitalk.graph.Graph;
import org.hanuna.gitalk.graph.graph_elements.GraphElement;
import org.hanuna.gitalk.graph.graph_elements.Node;
import org.hanuna.gitalk.printmodel.GraphPrintCellModel;
import org.hanuna.gitalk.printmodel.SpecialPrintElement;
import org.hanuna.gitalk.refs.Ref;
import org.hanuna.gitalk.refs.RefsModel;
import org.hanuna.gitalk.ui_controller.DateConverter;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
* @author erokhins
*/
public class GraphTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Subject", "Author", "Date"};
    private final RefsModel refsModel;
    private Graph graph;
    private GraphPrintCellModel graphPrintCellModel;

    public GraphTableModel(Graph graph, RefsModel refsModel, GraphPrintCellModel graphPrintCellModel) {
        this.graph = graph;
        this.refsModel = refsModel;
        this.graphPrintCellModel = graphPrintCellModel;
    }

    public void rewriteGraph(Graph graph, GraphPrintCellModel graphPrintCellModel) {
        this.graph = graph;
        this.graphPrintCellModel = graphPrintCellModel;
    }

    @Nullable
    private Commit getCommitInRow(int rowIndex) {
        List<SpecialPrintElement> printElements = graphPrintCellModel.getGraphPrintCell(rowIndex).getSpecialPrintElements();
        for (SpecialPrintElement printElement : printElements) {
            if (printElement.getType() == SpecialPrintElement.Type.COMMIT_NODE) {
                GraphElement element = printElement.getGraphElement();
                Node node =  element.getNode();
                assert node != null;
                return node.getCommit();
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return graph.getNodeRows().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Commit commit = getCommitInRow(rowIndex);
        CommitData data;
        if (commit == null) {
            data = null;
        } else {
            data = commit.getData();
            assert data != null;
        }
        switch (columnIndex) {
            case 0:
                String message = "";
                List<Ref> refs = Collections.emptyList();
                if (data != null) {
                    message = data.getMessage();
                    refs = refsModel.refsToCommit(commit.hash());
                }
                return new GraphCommitCell(graphPrintCellModel.getGraphPrintCell(rowIndex), message, refs);
            case 1:
                if (data == null) {
                    return "";
                } else {
                    return data.getAuthor();
                }
            case 2:
                if (data == null) {
                    return "";
                } else {
                    return DateConverter.getStringOfDate(data.getTimeStamp());
                }
            default:
                throw new IllegalArgumentException("columnIndex > 2");
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return GraphCommitCell.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                throw new IllegalArgumentException("column > 2");
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}