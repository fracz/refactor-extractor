package org.hanuna.gitalk.swing_ui;

import org.hanuna.gitalk.ui_controller.EventsController;
import org.hanuna.gitalk.ui_controller.table_models.GraphCommitCell;
import org.hanuna.gitalk.ui_controller.UI_Controller;
import org.hanuna.gitalk.graph.graph_elements.GraphElement;
import org.hanuna.gitalk.printmodel.PrintCell;
import org.hanuna.gitalk.swing_ui.render.GraphCommitCellRender;
import org.hanuna.gitalk.swing_ui.render.painters.GraphCellPainter;
import org.hanuna.gitalk.swing_ui.render.painters.SimpleGraphCellPainter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author erokhins
 */
public class UI_GraphTable extends JTable {
    private final UI_Controller ui_controller;
    private final GraphCellPainter graphPainter = new SimpleGraphCellPainter();
    private final MouseAdapter mouseAdapter = new MyMouseAdapter();

    public UI_GraphTable(UI_Controller ui_controller) {
        super(ui_controller.getGraphTableModel());
        this.ui_controller = ui_controller;
        prepare();
    }

    private void prepare() {
        setDefaultRenderer(GraphCommitCell.class, new GraphCommitCellRender(graphPainter));
        setRowHeight(GraphCommitCell.HEIGHT_CELL);
        setShowHorizontalLines(false);
        setIntercellSpacing(new Dimension(0, 0));

        getColumnModel().getColumn(0).setPreferredWidth(800);
        getColumnModel().getColumn(1).setMinWidth(80);
        getColumnModel().getColumn(2).setMinWidth(80);

        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);

        ui_controller.addControllerListener(new EventsController.ControllerListener() {
            @Override
            public void jumpToRow(int rowIndex) {
                scrollRectToVisible(getCellRect(rowIndex, 0, false));
                setRowSelectionInterval(rowIndex, rowIndex);
                scrollRectToVisible(getCellRect(rowIndex, 0, false));
            }

            @Override
            public void updateTable() {
                updateUI();
            }
        });

    }

    private class MyMouseAdapter extends MouseAdapter {
        @Nullable
        private GraphElement overCell(MouseEvent e) {
            int rowIndex = e.getY() / GraphCommitCell.HEIGHT_CELL;
            int y = e.getY() - rowIndex * GraphCommitCell.HEIGHT_CELL;
            int x = e.getX();
            PrintCell row = ui_controller.getGraphPrintCell(rowIndex);
            return graphPainter.mouseOver(row, x, y);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            ui_controller.click(overCell(e));
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            ui_controller.over(overCell(e));
        }
    }

}