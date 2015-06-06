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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.awt.Color;
import java.util.logging.Level;


/**
 * This styler is meant to draw near an Chunk using the generic tag mechanism of Chunk.
 * @see PageHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.itextpdf.text.Rectangle, java.lang.String) 
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class AbstractPositioning<DATATYPE> extends AdvancedImpl<DATATYPE> {
   public static final String SHADOW = "shadow";
   public static final String SHADOWX = "shadowx";
   public static final String SHADOWY = "shadowy";
   public static final String SHADOWOPACITY = "shadowopacity";
   public static final String SHADOWCOLOR = "shadowcolor";
   
   private boolean drawShadow = false;

   public AbstractPositioning() {
      initParams();
   }

   private void initParams() {
      addParameter(new BooleanParameter(SHADOW, "do we draw a dropshadow"),AbstractPositioning.class);
      addParameter(new ColorParameter(SHADOWCOLOR, "color of the dropshadow, default black").setDefault(Color.BLACK),AbstractPositioning.class);
      addParameter(new FloatParameter(SHADOWX, "x offset of the shadow, default 2mm").setDefault(ItextHelper.mmToPts(2f)),AbstractPositioning.class);
      addParameter(new FloatParameter(SHADOWY, "y offset of the shadow, default -2mm").setDefault(ItextHelper.mmToPts(-2f)),AbstractPositioning.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(SHADOWOPACITY, "opacity for the shadow, defaullt 0.3").setDefault(0.3f),AbstractPositioning.class);
   }

   public AbstractPositioning(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(document, writer, settings);
      initParams();
   }

   /**
    * Calls {@link #draw(com.itextpdf.text.pdf.PdfContentByte, float, float, float, float, String) } with
    * rect.getLeft() + getShiftx(), rect.getTop() + getShifty(), rect.getWidth(), rect.getHeight(), genericTag.
    * When {@link #isShadow() } is true {@link #isDrawShadow() } will be set to true, prior to calling
    * {@link #draw(com.itextpdf.text.pdf.PdfContentByte, float, float, float, float, java.lang.String) } and to false afterwards.
    * @param rect
    * @param genericTag passed on to abstract method
    * @throws VectorPrintException 
    * @see PageHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.itextpdf.text.Rectangle, java.lang.String) 
    */
   @Override
   public final void draw(Rectangle rect, String genericTag) throws VectorPrintException {
      if (getValue(SHADOW, Boolean.class)) {
         drawShadow(rect.getLeft() + getShiftx(),rect.getTop() + getShifty(), rect.getWidth(), rect.getHeight() , genericTag);
      }
      PdfContentByte canvas = getPreparedCanvas();
      draw(canvas, rect.getLeft() + getShiftx(), rect.getTop() + getShifty(), rect.getWidth(), rect.getHeight(), genericTag);
      resetCanvas(canvas);
   }
   
   public final void drawShadow(float x, float y, float width, float height, String genericTag) throws VectorPrintException {
         boolean bg = isBg();
         if (isBg()) {
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("possibly drawing shadow on top of content, see setting: %s", getStyleClass()));
            }
         }
         setBg(true);
         PdfContentByte canvas = getPreparedCanvas(getShadowOpactiy());
         drawShadow = true;
         draw(canvas,x + getValue(SHADOWX, Float.class), y + getValue(SHADOWY, Float.class), width, height, genericTag);
         drawShadow = false;
         resetCanvas(canvas);
         setBg(bg);
   }

   /**
    *
    *
    * @param canvas
    * @param x the calculated x position to draw, influenced by {@link #getShiftx() }
    * @param y the calculated y position to draw, influenced by {@link #getShifty() }
    * @param width width of the Chunk that was drawn or -1
    * @param height height of the Chunk that was drawn or -1
    * @param genericTag the generic tag that caused the call to this method
    * @see Advanced#getDelayed(java.lang.String) 
    * @throws VectorPrintException
    */
   
   protected abstract void draw(PdfContentByte canvas, float x, float y, float width, float height, String genericTag) throws VectorPrintException;


   @Override
   public String getHelp() {
      return "Draw graphics at a certain position or near text or an image. " + super.getHelp();
   }

   public boolean isShadow() {
      return getValue(SHADOW, Boolean.class);
   }

   public void setShadow(boolean shadow) {
      setValue(SHADOW, shadow);
   }

   public float getShadowX() {
      return getValue(SHADOWX, Float.class);
   }

   public void setShadowX(float shadowX) {
      setValue(SHADOWX, shadowX);
   }

   public float getShadowY() {
      return getValue(SHADOWY, Float.class);
   }

   public void setShadowY(float shadowY) {
      setValue(SHADOWY, shadowY);
   }

   public float getShadowOpactiy() {
      return getValue(SHADOWOPACITY, Float.class);
   }

   public void setShadowOpactiy(float shadowOpactiy) {
      setValue(SHADOWOPACITY, shadowOpactiy);
   }

   public Color getShadowColor() {
      return getValue(SHADOWCOLOR, Color.class);
   }

   public void setShadowColor(Color shadowColor) {
      setValue(SHADOWCOLOR, shadowColor);
   }

   public boolean isDrawShadow() {
      return drawShadow;
   }   

}
