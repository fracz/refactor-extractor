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

package org.jkiss.dbeaver.model.data;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.exec.DBCLogicalOperator;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.utils.CommonUtils;

/**
 * Attribute constraint
 */
public class DBDAttributeConstraint {

    private final DBSAttributeBase attribute;
    private int orderPosition;
    private boolean orderDescending;

    private String criteria;
    private DBCLogicalOperator operator;
    private boolean reverseOperator;
    private Object value;

    private boolean visible;
    private int visualPosition;
    private final int originalVisualPosition;

    public DBDAttributeConstraint(DBDAttributeBinding attribute)
    {
        this.attribute = attribute;
        this.originalVisualPosition = this.visualPosition = attribute.getOrdinalPosition();
    }

    public DBDAttributeConstraint(DBSAttributeBase attribute, int visualPosition)
    {
        this.attribute = attribute;
        this.originalVisualPosition = this.visualPosition = visualPosition;
    }

    public DBDAttributeConstraint(DBDAttributeConstraint source)
    {
        this.attribute = source.attribute;
        this.orderPosition = source.orderPosition;
        this.orderDescending = source.orderDescending;
        this.criteria = source.criteria;
        this.operator = source.operator;
        this.reverseOperator = source.reverseOperator;
        this.value = source.value;
        this.visible = source.visible;
        this.originalVisualPosition = source.originalVisualPosition;
        this.visualPosition = source.visualPosition;
    }

    public DBSAttributeBase getAttribute()
    {
        return attribute;
    }

    public int getOrderPosition()
    {
        return orderPosition;
    }

    public void setOrderPosition(int orderPosition)
    {
        this.orderPosition = orderPosition;
    }

    public boolean isOrderDescending()
    {
        return orderDescending;
    }

    public void setOrderDescending(boolean orderDescending)
    {
        this.orderDescending = orderDescending;
    }

    public String getCriteria()
    {
        return criteria;
    }

    public void setCriteria(@Nullable String criteria)
    {
        this.criteria = criteria;
        this.operator = null;
        this.reverseOperator = false;
        this.value = null;
    }

    public DBCLogicalOperator getOperator() {
        return operator;
    }

    public void setOperator(DBCLogicalOperator operator) {
        this.criteria = null;
        this.operator = operator;
    }

    public boolean isReverseOperator() {
        return reverseOperator;
    }

    public void setReverseOperator(boolean reverseOperator) {
        this.reverseOperator = reverseOperator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.criteria = null;
        this.value = value;
    }

    public boolean hasFilter()
    {
        return hasCondition() || orderPosition > 0;
    }

    public boolean hasCondition() {
        return !CommonUtils.isEmpty(criteria) || operator != null;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public int getOriginalVisualPosition() {
        return originalVisualPosition;
    }

    public int getVisualPosition()
    {
        return visualPosition;
    }

    public void setVisualPosition(int visualPosition)
    {
        this.visualPosition = visualPosition;
    }

    public void reset()
    {
        this.orderPosition = 0;
        this.orderDescending = false;
        this.criteria = null;
        this.operator = null;
        this.reverseOperator = false;
        this.value = null;
        this.visible = true;
        this.visualPosition = originalVisualPosition;
    }

    public boolean equalFilters(DBDAttributeConstraint source)
    {
        return CommonUtils.equalObjects(this.attribute, source.attribute) &&
            this.orderPosition == source.orderPosition &&
            this.orderDescending == source.orderDescending &&
            CommonUtils.equalObjects(this.criteria, source.criteria) &&
            CommonUtils.equalObjects(this.operator, source.operator) &&
            this.reverseOperator == source.reverseOperator &&
            CommonUtils.equalObjects(this.value, source.value) &&
            this.visible == source.visible &&
            this.visualPosition == source.visualPosition;
    }

    @Override
    public int hashCode()
    {
        return this.attribute.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof DBDAttributeConstraint &&
            CommonUtils.equalObjects(this.attribute, ((DBDAttributeConstraint) obj).attribute);
    }

    @Override
    public String toString() {
        String clause = operator == null ?
            (criteria == null ? "" : criteria) :
            (reverseOperator ? "NOT " : "") + operator.getStringValue() + " " + value;
        return attribute.getName() + " " + clause;
    }

    public boolean matches(DBSAttributeBase attr) {
        return attribute == attr ||
            (attribute instanceof DBDAttributeBinding && ((DBDAttributeBinding) attribute).matches(attr));
    }
}