/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements_  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership_  The ASF licenses this file
 * to you under the Apache License, Version 2_0 (the
 * "License"); you may not use this file except in compliance
 * with the License_  You may obtain a copy of the License at
 *
 *   http://www_apache_org/licenses/LICENSE-2_0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied_  See the License for the
 * specific language governing permissions and limitations
 * under the License_
 */

package com.gengoai.swing;

import javax.swing.UIManager;
import java.awt.Color;

public enum FlatLafColors {
   inactiveCaption("inactiveCaption"),
   activeCaption("activeCaption"),
   inactiveCaptionText("inactiveCaptionText"),
   activeCaptionText("activeCaptionText"),
   Button_background("Button.background"),
   Button_borderColor("Button.borderColor"),
   Button_darkShadow("Button.darkShadow"),
   Button_default_background("Button.default.background"),
   Button_default_borderColor("Button.default.borderColor"),
   Button_default_focusColor("Button.default.focusColor"),
   Button_default_focusedBorderColor("Button.default.focusedBorderColor"),
   Button_default_foreground("Button.default.foreground"),
   Button_default_hoverBackground("Button.default.hoverBackground"),
   Button_default_hoverBorderColor("Button.default.hoverBorderColor"),
   Button_default_pressedBackground("Button.default.pressedBackground"),
   Button_disabledBorderColor("Button.disabledBorderColor"),
   Button_disabledText("Button.disabledText"),
   Button_disabledToolBarBorderBackground("Button.disabledToolBarBorderBackground"),
   Button_focusedBorderColor("Button.focusedBorderColor"),
   Button_focus("Button.focus"),
   Button_foreground("Button.foreground"),
   Button_highlight("Button.highlight"),
   Button_hoverBackground("Button.hoverBackground"),
   Button_hoverBorderColor("Button.hoverBorderColor"),
   Button_light("Button.light"),
   Button_pressedBackground("Button.pressedBackground"),
   Button_select("Button.select"),
   Button_shadow("Button.shadow"),
   Button_toolBarBorderBackground("Button.toolBarBorderBackground"),
   Button_toolbar_hoverBackground("Button.toolbar.hoverBackground"),
   Button_toolbar_pressedBackground("Button.toolbar.pressedBackground");

   private final String key;

   FlatLafColors(String key) {
      this.key = key;
   }

   public Color color() {
      return UIManager.getColor(key);
   }

   public String getUIManagerKey() {
      return key;
   }

}//END OF FlatLafColors
