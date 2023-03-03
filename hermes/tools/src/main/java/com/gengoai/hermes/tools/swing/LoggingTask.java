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

package com.gengoai.hermes.tools.swing;

import com.gengoai.concurrent.Threads;
import com.gengoai.swing.component.MangoLoggingWindow;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.gengoai.swing.component.Components.dim;

public class LoggingTask extends JDialog {



   private SwingWorker<Void, Void> task;

   public LoggingTask(JFrame mainWindowFrame,
                      String title,
                      SwingWorker<Void, Void> task) {
      this.task = task;
      setMinimumSize(dim(800, 400));
      setResizable(false);
      setLocationRelativeTo(mainWindowFrame);
      setLayout(new BorderLayout());
      setTitle(title);
      add(new MangoLoggingWindow(), BorderLayout.CENTER);
      JButton start = new JButton("Start");
      start.addActionListener(l -> {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         start.setEnabled(false);
         this.task.execute();
      });
      JButton close = new JButton("Close");
      close.setEnabled(false);
      close.addActionListener(l -> dispose());
      JPanel panel = new JPanel();
      panel.add(start);
      panel.add(close);
      add(panel, BorderLayout.SOUTH);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      new SwingWorker<>() {
         @Override
         protected Object doInBackground() throws Exception {
            while (!task.isDone()) {
               Threads.sleep(3000);
            }
            close.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            return null;
         }

      }.execute();
   }

}
