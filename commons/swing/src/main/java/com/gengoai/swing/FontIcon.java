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

import com.gengoai.io.resource.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

final class FontIcon implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Font font;

   protected FontIcon(Resource fontResource) {
      try {
         this.font = Font.createFont(0, fontResource.inputStream());
         GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .registerFont(this.font);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public final Icon createIcon(String text, float size, Color foreground, Color background) {
      return new ImageIcon(buildImage(text, size, foreground, background));
   }

   public final Icon createIcon(String text, float size, Color foreground) {
      return new ImageIcon(buildImage(text, size, foreground, null));
   }

   public final Icon createIcon(String text, float size) {
      return new ImageIcon(buildImage(text, size, Color.BLACK, null));
   }

   public String getFontName() {
      return font.getFontName();
   }

   protected Image buildImage(String text, float size, Color foreground, Color background) {
      final Font sized = font.deriveFont(size);
      Rectangle2D stringBounds = sized.getStringBounds(text, new FontRenderContext(null, true, true));
      int width = (int) stringBounds.getWidth();
      int height = (int) stringBounds.getHeight();

      width = Math.max(width, height);
      height = Math.max(width, height);

      BufferedImage bufImage = new BufferedImage(width, height, 2);
      Graphics2D g2d = bufImage.createGraphics();
      g2d.setFont(sized);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      if (background != null) {
         g2d.setColor(background);
         g2d.fillRect(0, 0, width, height);
      } else {
         g2d.setComposite(AlphaComposite.Clear);
         g2d.fillRect(0, 0, width, height);
         g2d.setComposite(AlphaComposite.Src);
      }
      g2d.setColor(foreground);
      FontMetrics metrics = g2d.getFontMetrics(sized);
      int x = (width - metrics.stringWidth(text)) / 2;
      int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
      g2d.drawString(text, x, y);
      g2d.dispose();
      return bufImage;
   }

}//END OF FontIcon
