/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.swing.component;

import com.gengoai.LogUtils;
import com.gengoai.MangoLogFormatter;
import com.gengoai.conversion.Cast;
import com.gengoai.swing.Colors;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.gengoai.function.Functional.with;

public class MangoLoggingWindow extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final Color LIGHT_SEVERE = new Color(116, 58, 58);
    private static final Color DARK_SEVERE = new Color(197, 75, 75);
    private static final Color LIGHT_WARNING = new Color(189, 170, 0);
    private static final Color DARK_WARNING = new Color(189, 170, 0);
    private static final Color LIGHT_INFO = new Color(6, 120, 12);
    private static final Color DARK_INFO = new Color(6, 180, 12);
    private final MangoTextPane logWindow = new MangoTextPane();
    @Getter
    @Setter
    private int maxBufferSize = 100;

    public MangoLoggingWindow() {
        setLayout(new BorderLayout());
        var logHandler = new LogWindowHandler();
        logHandler.setLevel(Level.ALL);

        var levels = new JComboBox<>(new Object[]{
                Level.SEVERE,
                Level.WARNING,
                Level.INFO,
                Level.CONFIG,
                Level.FINE,
                Level.FINER,
                Level.FINEST,
        });
        levels.setSelectedIndex(2);
        levels.addItemListener(i -> {
            Level newLevel = Cast.as(i.getItem());
            LogUtils.ROOT.setLevel(newLevel);
            Logger.getLogger("javax").setLevel(Level.OFF);
            Logger.getLogger("java").setLevel(Level.OFF);
            Logger.getLogger("sun").setLevel(Level.OFF);
            logHandler.setLevel((Level) i.getItem());
        });
        var toolbar = new JToolBar();
        toolbar.add(levels);
        toolbar.setFloatable(false);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(logWindow), BorderLayout.CENTER);

        with(logWindow, $ -> {
            $.setEditable(false);
            $.setOpaque(true);
            $.setBackground(UIManager.getColor("TextArea.background"));
            final Color severe;
            final Color warning;
            final Color info;
            if (Colors.isDark($.getBackground())) {
                severe = DARK_SEVERE;
                warning = DARK_WARNING;
                info = DARK_INFO;
            } else {
                severe = LIGHT_SEVERE;
                warning = LIGHT_WARNING;
                info = LIGHT_INFO;
            }
            $.addAnSetStyle(Level.SEVERE.getName(), s -> s.foreground(severe));
            $.addAnSetStyle(Level.WARNING.getName(), s -> s.foreground(warning));
            $.addAnSetStyle(Level.INFO.getName(), s -> s.foreground(info));
            $.addAnSetStyle(Level.FINE.getName(), s -> s.foreground($.getForeground()));
            $.addAnSetStyle(Level.FINER.getName(), s -> s.foreground($.getForeground()));
            $.addAnSetStyle(Level.FINEST.getName(), s -> s.foreground($.getForeground()));
            $.addAnSetStyle(Level.CONFIG.getName(), s -> s.foreground($.getForeground().darker()));
        });

        LogUtils.addHandler(logHandler);
        System.setOut(new ConsoleOutputStream());
        System.setErr(new ConsoleOutputStream());
    }

    class ConsoleOutputStream extends PrintStream {
        public ConsoleOutputStream() {
            super(new BufferedOutputStream(System.out));
        }

        @Override
        public void print(String s) {
            try {
                logWindow.getDocument()
                         .insertString(logWindow.getText().length(),
                                       s,
                                       logWindow.getStyle(Level.INFO.getName()));
                logWindow.setCaretPosition(logWindow.getDocument().getLength());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void println(String s) {
            try {
                logWindow.getDocument()
                         .insertString(logWindow.getText().length(),
                                       s + "\n",
                                       logWindow.getStyle(Level.INFO.getName()));
                logWindow.setCaretPosition(logWindow.getDocument().getLength());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class LogWindowHandler extends Handler {
        MangoLogFormatter formatter = new MangoLogFormatter();
        private AtomicInteger msgCount = new AtomicInteger();

        @Override
        public void close() throws SecurityException {

        }

        @Override
        public void flush() {

        }

        @Override
        public void publish(LogRecord record) {
            String text = formatter.format(record);
            try {
                if (msgCount.incrementAndGet() > maxBufferSize) {
                    msgCount.set(1);
                    logWindow.setText("");
                    logWindow.getDocument()
                             .insertString(logWindow.getText().length(),
                                           text,
                                           logWindow.getStyle(record.getLevel().getName()));
                    logWindow.setCaretPosition(logWindow.getDocument().getLength());
                } else {
                    logWindow.getDocument()
                             .insertString(logWindow.getText().length(),
                                           text,
                                           logWindow.getStyle(record.getLevel().getName()));
                    logWindow.setCaretPosition(logWindow.getDocument().getLength());
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

}//END OF MangoLoggingWindow
