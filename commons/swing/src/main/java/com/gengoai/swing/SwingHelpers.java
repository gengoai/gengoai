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

import com.gengoai.conversion.Cast;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class SwingHelpers {

    public static Insets insets(int top, int left, int bottom, int right) {
        return new Insets(top, left, bottom, right);
    }

    public static JToolBar createToolBar(@NonNull Object... components) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        for (Object component : components) {
            if (component == null) {
                toolBar.addSeparator();
            } else if (component instanceof Dimension) {
                toolBar.addSeparator(Cast.as(component));
            } else if (component instanceof View) {
                toolBar.add(Cast.<View>as(component).getRoot());
            } else {
                toolBar.add(Cast.<Component>as(component));
            }
        }
        return toolBar;
    }

}//END OF SwingHelpers
