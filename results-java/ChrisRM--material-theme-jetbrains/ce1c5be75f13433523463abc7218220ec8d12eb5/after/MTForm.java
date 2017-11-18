package com.chrisrm.idea.config.ui;

import com.chrisrm.idea.MTConfig;
import com.chrisrm.idea.MTTheme;
import com.chrisrm.idea.messages.MaterialThemeBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MTForm implements MTFormUI {
    private CheckBoxWithColorChooserImpl checkBoxWithColorChooserImpl;
    private JPanel content;
    private JSpinner highlightSpinner;
    private JButton reset;
    private JCheckBox isContrastModeCheckbox;
    private SpinnerModel highlightSpinnerModel;
    private JCheckBox isMaterialDesignCheckbox;
    private JCheckBox boldTabs;

    public MTForm() {

        reset.addActionListener(e -> {
            final MTTheme mtTheme = MTConfig.getInstance().getSelectedTheme();
            Color borderColor = mtTheme.getBorderColor();
            int thickness = mtTheme.getBorderThickness();

            this.setHighlightColor(borderColor);
            this.setHighlightColorEnabled(false);
            this.setHighlightThickness(thickness);
            this.setIsBoldTabs(false);
        });
    }

    @Override
    public void init() {
        MTConfig config = MTConfig.getInstance();
        highlightSpinnerModel = new SpinnerNumberModel(config.getHighlightThickness(), 1, 5, 1);
        highlightSpinner.setModel(highlightSpinnerModel);
    }

    @Override
    public JComponent getContent() {
        return content;
    }

    @Override
    public void afterStateSet() {

    }

    @Override
    public void dispose() {
        checkBoxWithColorChooserImpl.dispose();
    }

    public Color getHighlightColor() {
        return checkBoxWithColorChooserImpl.getColor();
    }

    public void setHighlightColor(@NotNull Color highlightColor) {
        checkBoxWithColorChooserImpl.setColor(highlightColor);
    }

    public boolean getHighlightColorEnabled() {
        return checkBoxWithColorChooserImpl.isSelected();
    }

    public void setHighlightColorEnabled(boolean enabled) {
        checkBoxWithColorChooserImpl.setSelected(enabled);
    }

    public Integer getHighlightThickness() {
        return (Integer) highlightSpinnerModel.getValue();
    }

    public void setHighlightThickness(Integer highlightThickness) {
        highlightSpinnerModel.setValue(highlightThickness);
    }

    public boolean getIsContrastMode() {
        return isContrastModeCheckbox.isSelected();
    }

    public void setIsContrastMode(boolean isContrastMode) {
        isContrastModeCheckbox.setSelected(isContrastMode);
    }

    public boolean getIsMaterialDesign() {
        return isMaterialDesignCheckbox.isSelected();
    }

    public void setIsMaterialDesign(boolean isMaterialDesign) {
        this.isMaterialDesignCheckbox.setSelected(isMaterialDesign);
    }

    public void setIsBoldTabs(boolean isBold) {
        this.boldTabs.setSelected(isBold);
    }

    public boolean getIsBoldTabs(){
        return this.boldTabs.isSelected();
    }

    private void createUIComponents() {
        checkBoxWithColorChooserImpl = new CheckBoxWithColorChooserImpl(MaterialThemeBundle.message("mt.activetab.text"));
    }
}