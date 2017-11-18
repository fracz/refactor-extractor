/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.logging.console;

import org.gradle.api.UncheckedIOException;

import java.io.Flushable;
import java.io.IOException;

public class AnsiConsole implements Console {
    private final Flushable flushable;
    private final MultiLineBuildProgressArea buildStatusArea;
    private final DefaultTextArea buildOutputArea;
    private final AnsiExecutor ansiExecutor;

    public AnsiConsole(Appendable target, Flushable flushable, ColorMap colorMap, int numWorkersToDisplay, boolean forceAnsi) {
        this(target, flushable, colorMap, numWorkersToDisplay, new DefaultAnsiFactory(forceAnsi));
    }

    private AnsiConsole(Appendable target, Flushable flushable, ColorMap colorMap, int numWorkersToDisplay, AnsiFactory factory) {
        this.flushable = flushable;
        this.ansiExecutor = new DefaultAnsiExecutor(target, colorMap, factory, Cursor.newBottomLeft(), new Listener());

        buildOutputArea = new DefaultTextArea(ansiExecutor);
        buildStatusArea = new MultiLineBuildProgressArea(ansiExecutor, numWorkersToDisplay);
    }

    @Override
    public void flush() {
        redraw();
        try {
            flushable.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void redraw() {
        // Calculate how many rows of the status area overlap with the text area
        int numberOfOverlappedRows = buildStatusArea.getWritePosition().row - buildOutputArea.getWritePosition().row;

        // If textArea is on a status line but nothing was written, this means a new line was just written. While
        // we wait for additional text, we assume this row doesn't count as overlapping and use it as a status
        // line. In the opposite case, we want to scroll the progress area one more line. This avoid having an one
        // line gap between the text area and the status area.
        if (buildOutputArea.getWritePosition().col > 0) {
            numberOfOverlappedRows++;
        }

        if (numberOfOverlappedRows > 0) {
            buildStatusArea.scrollDownBy(numberOfOverlappedRows);
        }

        buildStatusArea.redraw();
    }

    @Override
    public StyledLabel getStatusBar() {
        return buildStatusArea.getProgressBar();
    }

    @Override
    public BuildProgressArea getBuildProgressArea() {
        return buildStatusArea;
    }

    @Override
    public TextArea getBuildOutputArea() {
        return buildOutputArea;
    }

    private class Listener implements DefaultAnsiExecutor.NewLineListener {
        @Override
        public void beforeNewLineWritten(AnsiContext ansi, Cursor writeCursor) {
            if (buildStatusArea.isOverlappingWith(writeCursor)) {
                ansi.eraseForward();
            }

            if (writeCursor.row == 0) {
                buildOutputArea.newLineAdjustment();
                buildStatusArea.newLineAdjustment();
            }
        }
    }
}