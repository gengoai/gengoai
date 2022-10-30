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

package com.gengoai.swing;

import com.gengoai.swing.component.MangoLoggingWindow;
import com.gengoai.swing.component.MangoTabbedPane;
import com.gengoai.swing.component.MangoTextPane;
import com.gengoai.swing.component.view.MangoButtonedTextField;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class TestClass extends SwingApplication {
   public static void main(String[] args) {
      runApplication(TestClass::new, "TestClass", "TestClass", args);
   }

   @Override
   protected void initControls() throws Exception {
      mainWindowFrame.setTitle("Test Application");
      var tbPane = new MangoTabbedPane();
      tbPane.addTab("My Tab",
                    new MangoTextPane());
      setCenterComponent(tbPane);


      var btnSize = 24;
      var btnGroup = new MangoButtonGroup();
      btnGroup.add(FontAwesome.SAVE.createButton(btnSize));
      btnGroup.add(FontAwesome.CLOUD_MEATBALL.createButton(btnSize));
      toolBar(btnGroup.createHorizontalView(10),
              new MangoButtonedTextField(2, FontAwesome.TOGGLE_ON, "east"));
      statusBar(new MangoTextPane());

   }

}
