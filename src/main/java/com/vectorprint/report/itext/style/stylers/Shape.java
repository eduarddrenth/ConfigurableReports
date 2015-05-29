/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style.stylers;

/*
 * #%L
 * VectorPrintReport4.0
 * %%
 * Copyright (C) 2012 - 2013 VectorPrint
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import static com.vectorprint.report.itext.style.BaseStyler.COLOR_PARAM;

import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.ShapeParameter;
import static com.vectorprint.report.itext.style.stylers.Border.BORDERWIDTH;
import static com.vectorprint.report.itext.style.stylers.Padding.PADDING;
import java.awt.Color;
import java.util.Arrays;

/**
 * drawing shapes with / without border or fill, with a nice feature to surround text
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Shape<DATATYPE> extends AbstractPositioning<DATATYPE> {

   public enum SHAPE {

      rectangle, roundrectangle, ellipse, free, bezier
   }
   public static final String BORDERCOLOR = "bordercolor";
   public static final String RADIUS = "radius";
   public static final String FILL = "fill";
   public static final String CLOSE = "close";
   public static final String ROUNDED = "rounded";
   public static final String POINTS = "points";
   public static final String ENCLOSING = "enclosing";

   public Shape() {
      initParams();
   }

   private void initParams() {
      addParameter(new ShapeParameter(SHAPE.class.getSimpleName(), Arrays.asList(SHAPE.values()).toString()),Shape.class);
      addParameter(new FloatParameter(DocumentSettings.WIDTH, "float"),Shape.class);
      addParameter(new FloatParameter(DocumentSettings.HEIGHT, "float"),Shape.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(RADIUS, "for rounded corners").setDefault(2f),Shape.class);
      addParameter(new BooleanParameter(FILL, "fill shapes or not").setDefault(Boolean.TRUE),Shape.class);
      addParameter(new BooleanParameter(ENCLOSING, "enclosing shape rectangle, ellipse"),Shape.class);
      addParameter(new BooleanParameter(CLOSE, "connect first and last point").setDefault(Boolean.TRUE),Shape.class);
      addParameter(new BooleanParameter(ROUNDED, "rounded line connections").setDefault(Boolean.TRUE),Shape.class);
      addParameter(new ColorParameter(BaseStyler.COLOR_PARAM, "color").setDefault(Color.BLACK),Shape.class);
      addParameter(new ColorParameter(BORDERCOLOR, "color").setDefault(Color.BLACK),Shape.class);
      addParameter(new FloatParameter(BORDERWIDTH, "float"),Shape.class);
      addParameter(new FloatParameter(PADDING, "padding for enclosing shape"),Shape.class);
      addParameter(new FloatArrayParameter(POINTS, "line and free shapes"),Shape.class);

   }

   public Shape(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(document, writer, settings);
      initParams();
   }
   

   @Override
   protected void draw(PdfContentByte canvas, float x, float y, float width, float height, String genericTag) {
      if (getBorderWidth() > 0) {
         canvas.setLineWidth(getBorderWidth());
         canvas.setColorStroke(itextHelper.fromColor((isDrawShadow())?getShadowColor():getBorderColor()));
      }
      if (width == -1) {
         width = getWidth();
      }
      if (height == -1) {
         height = getHeight();
      }
      canvas.setColorFill(itextHelper.fromColor((isDrawShadow())?getShadowColor():getColor()));
      if (isRounded()) {
         canvas.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);
      }
      float xx = x, yy = y;
      float[] points = getPoints();
      float padding = getPadding();
      switch (getShape()) {
         case free:
            float xdif = x - points[0];
            float ydif = y - points[1];
            xx = points[0] + xdif;
            yy = points[1] + ydif;
            canvas.moveTo(points[0] + xdif, points[1] + ydif);
            for (int i = 2; i < points.length; i = i + 2) {
               canvas.lineTo(points[i], points[i + 1]);
            }
            break;
         case bezier:
            xdif = x - points[0];
            ydif = y - points[1];
            xx = points[0] + xdif;
            yy = points[1] + ydif;
            canvas.moveTo(points[0] + xdif, points[1] + ydif);
            for (int i = 2; i < points.length; i = i + 4) {
               canvas.curveTo(points[i] + xdif, points[i + 1] + ydif, points[i + 2] + xdif, points[i + 3] + ydif);
            }
            break;
         case rectangle:
            if (isClose()) {
               xx = x - padding;
               yy = y - padding - height;
               canvas.rectangle(xx, yy, width + padding * 2, height + padding * 2);
            } else {
               canvas.rectangle(x, y, width, height);
            }
            break;
         case roundrectangle:
            if (isEnclosing()) {
               xx = x - padding;
               yy = y - padding - height;
               canvas.roundRectangle(xx, yy, width + padding * 2, height + padding * 2, getRadius());
            } else {
               canvas.roundRectangle(x, y, width, height, getRadius());
            }
            break;
         case ellipse:
            if (isEnclosing()) {
               xx = x - padding;
               yy = y - padding - height;
               canvas.ellipse(xx, yy, x + width + 2 * padding, y + 2 * padding);
            } else {
               canvas.ellipse(x, y, x + width, y + height);
            }
            break;
      }
      if (isClose()) {
         if (isFill()) {
            canvas.closePathFillStroke();
         } else {
            canvas.closePathStroke();
         }
      } else {
         canvas.stroke();
      }
      if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.DEBUG)) {
         DebugHelper.styleLink(canvas, getStyleClass(), 
             " (styling)",
             xx, yy, getSettings(), getLayerManager());
      }
   }

   public float getRadius() {
      return getValue(RADIUS,Float.class);
   }

   public void setRadius(float radius) {
      setValue(RADIUS, radius);
   }

   public float getBorderWidth() {
      return getValue(BORDERWIDTH,Float.class);
   }

   public void setBorderWidth(float borderWidth) {
      setValue(BORDERWIDTH, borderWidth);
   }

   public float getWidth() {
      return getValue(DocumentSettings.WIDTH,Float.class);
   }

   public void setWidth(float width) {
      setValue(DocumentSettings.WIDTH, width);
   }

   public float getHeight() {
      return getValue(DocumentSettings.HEIGHT,Float.class);
   }

   public void setHeight(float height) {
      setValue(DocumentSettings.HEIGHT, height);
   }

   public float getPadding() {
      return getValue(PADDING,Float.class);
   }

   public void setPadding(float padding) {
      setValue(PADDING, padding);
   }
   public float[] getPoints() {
      return getValue(POINTS,float[].class);
   }

   public void setPoints(float[] points) {
      setValue(POINTS, points);
   }

   public SHAPE getShape() {
      return getValue(SHAPE.class.getSimpleName(),SHAPE.class);
   }

   public void setShape(SHAPE shape) {
      setValue(SHAPE.class.getSimpleName(), shape);
   }

   public Color getColor() {
      return getValue(COLOR_PARAM,Color.class);
   }

   public void setColor(Color color) {
      setValue(COLOR_PARAM, color);
   }

   public Color getBorderColor() {
      return getValue(BORDERCOLOR,Color.class);
   }

   public void setBorderColor(Color borderColor) {
      setValue(BORDERCOLOR, borderColor);
   }

   public boolean isFill() {
      return getValue(FILL,Boolean.class);
   }

   public void setFill(boolean fill) {
      setValue(FILL, fill);
   }

   public boolean isClose() {
      return getValue(CLOSE,Boolean.class);
   }

   public void setClose(boolean close) {
      setValue(CLOSE, close);
   }

   public boolean isRounded() {
      return getValue(ROUNDED,Boolean.class);
   }

   public void setRounded(boolean rounded) {
      setValue(ROUNDED, rounded);
   }

   public boolean isEnclosing() {
      return getValue(ENCLOSING,Boolean.class);
   }

   public void setEnclosing(boolean enclosing) {
      setValue(ENCLOSING, enclosing);
   }
   @Override
   public String getHelp() {
      return "Draw a shape at a position or near text or an image. " + super.getHelp(); 
   }
}
