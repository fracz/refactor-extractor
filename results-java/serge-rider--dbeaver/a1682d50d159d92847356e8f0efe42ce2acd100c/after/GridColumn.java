/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package  org.jkiss.dbeaver.ui.controls.lightgrid;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.jkiss.code.Nullable;
import org.jkiss.utils.CommonUtils;

/**
 * Grid column info.
 * Holds information about column width and other UI properties
 *
 * @author serge@jkiss.org
 */
public class GridColumn {

	/**
	 * Default width of the column.
	 */
	private static final int DEFAULT_WIDTH = 10;

    private static final int topMargin = 3;
    private static final int bottomMargin = 3;
    private static final int leftMargin = 6;
    private static final int rightMargin = 6;
    private static final int imageSpacing = 3;
    private static final int insideMargin = 3;

	/**
	 * Parent table.
	 */
	private final LightGrid grid;
    private final Object element;

	private int width = DEFAULT_WIDTH;

	public GridColumn(LightGrid grid, Object element) {
		this(grid, -1, element);
	}

	public GridColumn(LightGrid grid, int index, Object element) {
        this.grid = grid;
        this.element = element;
        grid.newColumn(this, index);
	}

    public Object getElement() {
        return element;
    }

    public int getIndex()
    {
        return getGrid().indexOf(this);
    }

    public void dispose() {
		if (!grid.isDisposing()) {
			grid.removeColumn(this);
		}
	}

	/**
	 * Returns the width of the column.
	 *
	 * @return width of column
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width of the column.
	 *
	 * @param width
	 *            new width
	 */
	public void setWidth(int width) {
		setWidth(width, true);
	}

	void setWidth(int width, boolean redraw) {
		this.width = width;
		if (redraw) {
			grid.setScrollValuesObsolete();
			grid.redraw();
		}
	}

    public int computeHeaderHeight()
    {
        int y = topMargin + grid.sizingGC.getFontMetrics().getHeight() + bottomMargin;
        Image image = grid.getColumnLabelProvider().getImage(element);
        if (image != null) {
            y = Math.max(y, topMargin + image.getBounds().height + bottomMargin);
        }

		return y;
    }

    public boolean isOverSortArrow(int x)
    {
        if (!isSortable()) {
            return false;
        }
        int arrowEnd = getBounds().width - rightMargin;
        int arrowBegin = arrowEnd - GridColumnRenderer.getSortControlBounds().width;
        return x >= arrowBegin && x <= arrowEnd;
    }

    public int computeHeaderWidth()
    {
        int x = leftMargin;
        Image image = grid.getColumnLabelProvider().getImage(element);
        String text = grid.getColumnLabelProvider().getText(element);
        if (image != null) {
            x += image.getBounds().width + imageSpacing;
        }
        x += grid.sizingGC.stringExtent(text).x + rightMargin;
        if (isSortable()) {
            x += rightMargin + GridColumnRenderer.getSortControlBounds().width;
        }

        return x;
    }

    public boolean isSortable()
    {
        return grid.getContentProvider().getColumnSortOrder(element) != SWT.NONE;
    }

	/**
	 * Causes the receiver to be resized to its preferred size.
	 *
	 */
	public void pack() {
		int newWidth = computeHeaderWidth();
        //int columnIndex = getIndex();
        int topIndex = grid.getTopIndex();
        int bottomIndex = grid.getBottomIndex();
        if (topIndex >= 0 && bottomIndex >= topIndex) {
            int itemCount = grid.getItemCount();
            GridCell cell = new GridCell(element, element);
            for (int i = topIndex; i <= bottomIndex && i < itemCount; i++) {
                cell.row = grid.getRowElement(i);
                newWidth = Math.max(newWidth, computeCellWidth(cell));
            }
        }

		setWidth(newWidth);
	}

    private int computeCellWidth(GridCell cell) {
        int x = 0;

        x += leftMargin;

        Image image = grid.getContentProvider().getCellImage(cell);
        if (image != null) {
            x += image.getBounds().width + insideMargin;
        }

        x += grid.sizingGC.textExtent(grid.getCellText(cell)).x + rightMargin;
        return x;
    }

	/**
	 * Returns the bounds of this column's header.
	 *
	 * @return bounds of the column header
	 */
	Rectangle getBounds() {
		Rectangle bounds = new Rectangle(0, 0, 0, 0);

		Point loc = grid.getOrigin(this, -1);
		bounds.x = loc.x;
		bounds.y = loc.y;
		bounds.width = getWidth();
		bounds.height = grid.getHeaderHeight();

		return bounds;
	}

	/**
	 * Returns the parent grid.
	 *
	 * @return the parent grid.
	 */
	public LightGrid getGrid() {
		return grid;
	}

	/**
	 * Returns the tooltip of the column header.
	 *
	 * @return the tooltip text
	 */
	@Nullable
    public String getHeaderTooltip() {
        String tip = grid.getColumnLabelProvider().getTooltip(element);
        String text = grid.getColumnLabelProvider().getText(element);
        if (tip == null) {
            tip = text;
        }
        if (!CommonUtils.equalObjects(tip, text)) {
            return tip;
        }
        Point ttSize = getGrid().sizingGC.textExtent(tip);
        if (ttSize.x > getWidth()) {
            return tip;
        }

		return null;
	}

}