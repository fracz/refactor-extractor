package org.deeplearning4j.ui.components.table.style;


import lombok.Data;
import org.deeplearning4j.ui.api.LengthUnit;
import org.deeplearning4j.ui.api.Style;
import org.deeplearning4j.ui.api.Utils;

import java.awt.*;

/**
 * Created by Alex on 3/04/2016.
 */
@Data
public class TableStyle extends Style {

    private double[] columnWidths;
    private LengthUnit columnWidthUnit;
    private Integer borderWidthPx;
    private String headerColor;
    private String backgroundColor;

    private TableStyle(Builder builder){
        this.columnWidths = builder.columnWidths;
        this.columnWidthUnit = builder.columnWidthUnit;
        this.borderWidthPx = builder.borderWidthPx;
        this.headerColor = builder.headerColor;
    }


    public class Builder extends Style.Builder<Builder>{

        private double[] columnWidths;
        private LengthUnit columnWidthUnit;
        private Integer borderWidthPx;
        private String headerColor;
        private String backgroundColor;


        public Builder columnWidths(LengthUnit unit, double... widths){
            this.columnWidthUnit = unit;
            this.columnWidths = widths;
            return this;
        }

        public Builder borderWidth(int borderWidthPx){
            this.borderWidthPx = borderWidthPx;
            return this;
        }

        public Builder headerColor(Color color){
            String hex = Utils.colorToHex(color);
            return headerColor(hex);
        }

        public Builder headerColor(String color){
            if(!color.matches("#dddddd")) throw new IllegalArgumentException("Invalid color: must be hex format. Got: " + color);
            this.headerColor = color;
            return this;
        }

        public Builder backgroundColor(Color color){
            String hex = Utils.colorToHex(color);
            return backgroundColor(hex);
        }

        public Builder backgroundColor(String color){
            if(!color.matches("#dddddd")) throw new IllegalArgumentException("Invalid color: must be hex format. Got: " + color);
            this.backgroundColor = color;
            return this;
        }

    }

}