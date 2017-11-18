package org.hanuna.gitalk.printmodel;

import org.hanuna.gitalk.common.compressedlist.Replace;
import org.hanuna.gitalk.graph.Graph;
import org.hanuna.gitalk.printmodel.layout.LayoutModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author erokhins
 */
public class PrintCellModel {
    private final LayoutModel layoutModel;
    private final SelectController selectController;

    public PrintCellModel(Graph graph) {
        this.layoutModel = new LayoutModel(graph);
        this.selectController = new SelectController();
    }

    private List<ShortEdge> getUpEdges(int rowIndex) {
        PrePrintCell prevPre = new PrePrintCell(layoutModel, rowIndex - 1, selectController);
        return prevPre.downShortEdges();
    }

    public void recalculate(@NotNull Replace replace) {
        layoutModel.recalculate(replace);
    }

    public SelectController getSelectController() {
        return selectController;
    }

    @NotNull
    public PrintCell getPrintCellRow(final int rowIndex) {
        final PrePrintCell prePrintCell = new PrePrintCell(layoutModel, rowIndex, selectController);

        return new PrintCell() {
            @Override
            public int countCell() {
                return prePrintCell.getCountCells();
            }

            @NotNull
            @Override
            public List<ShortEdge> getUpEdges() {
                return PrintCellModel.this.getUpEdges(rowIndex);
            }

            @NotNull
            @Override
            public List<ShortEdge> getDownEdges() {
                return prePrintCell.downShortEdges();
            }

            @NotNull
            @Override
            public List<SpecialCell> getSpecialCell() {
                return prePrintCell.specialCells();
            }
        };
    }
}