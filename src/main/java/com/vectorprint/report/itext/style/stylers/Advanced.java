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

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.VectorPrintDocument;
import com.vectorprint.report.itext.style.BaseStyler;

/**
 * An advanced styler is a styler that is meant to draw directly on the canvas as opposed to regular stylers that only
 * configure elements later to be added to the document. By adding an Advanced styler to the chain of stylers it is
 * able to draw based on information about the element styled, the data it contains
 * (only for {@link Chunk} and {@link TextElementArray}) and the document.
 * 
 * @see VectorPrintDocument.AddElementHook
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface Advanced<DATATYPE> extends BaseStyler, PdfPTableEvent, PdfPCellEvent {
   
   /**
    * draw upon text added, table drawn, cell drawn or all three.
    */
   public enum EVENTMODE { TEXT, TABLE, CELL, ALL, NONE }

   public enum BLENDMODE {

      OVERLAY(PdfGState.BM_OVERLAY), BURN(PdfGState.BM_COLORBURN), DODGE(PdfGState.BM_COLORDODGE),
      DARKEN(PdfGState.BM_DARKEN), LIGHTEN(PdfGState.BM_LIGHTEN), COMPATIBLE(PdfGState.BM_COMPATIBLE),
      MULTIPLY(PdfGState.BM_MULTIPLY), DIFF(PdfGState.BM_DIFFERENCE), EXCLUDE(PdfGState.BM_EXCLUSION),
      HARDLIGHT(PdfGState.BM_HARDLIGHT), SOFTLIGHT(PdfGState.BM_SOFTLIGHT), SCREEN(PdfGState.BM_SCREEN),
      NORMAL(PdfGState.BM_NORMAL);

      private BLENDMODE(PdfName blend) {
         this.blend = blend;
      }
      private PdfName blend;

      public PdfName getBlend() {
         return blend;
      }
            
   }
   /**
    * NOTE: transform matrix and rotate option in image don't play well together
    */
   public enum TRANSFORMMATRIX {
      
      SCALEX(0), SHEARX(1), SHEARY(2),
      SCALEY(3), TRANSLATEX(4), TRANSLATEY(5);

      private TRANSFORMMATRIX(int index) {
         this.index = index;
      }
      
      private int index;

      @Override
      public String toString() {
         return name() + ": " + index;
      }

      public int getIndex() {
         return index;
      }
      
      
      
   }
   /**
    * a holder for data added to the report by using stylers
    */
   public static class DelayedData {

      private Chunk c;
      private Object data;

      public DelayedData( Chunk c) {
         this.c = c;
         if (c!=null) {
            this.data = c.getContent();
         }
      }

      public Chunk getChunk() {
         return c;
      }

      public Object getData() {
         return data;
      }
      
      public String getStringData() {
         return String.valueOf(data);
      }
      
      public DelayedData setData(Object data) {
         this.data= data;
         return this;
      }

      @Override
      public String toString() {
         return "DelayedData{" + "data=" + data + '}';
      }
      
   }

   /**
    * keeps track of the element styled previously in the chain of stylers and the data for the element.
    *
    * @see #draw(com.itextpdf.text.Rectangle, java.lang.String)
    * @param genericTag
    * @param c
    */
   void addDelayedData(String genericTag, Chunk c);

   /**
    * should the styler draw on a canvas, based on the data for the element and possibly other state in the styler.
    *
    * @param data
    * @return
    */
   boolean shouldDraw(Object data);

   /**
    * draw method for drawing near a Chunk of text.
    *
    * @see PdfPageEventHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document,
    * com.itextpdf.text.Rectangle, java.lang.String)
    * @see EventHelper#addDelayedStyler(java.lang.String, java.util.Collection, com.itextpdf.text.Chunk) 
    * @see #getDelayed(java.lang.String)
    * @param rect the rectangle where the text was printed
    * @param genericTag  the genericTag event that was fired
    * @throws VectorPrintException
    */
   void draw(Rectangle rect, String genericTag) throws VectorPrintException;

   /**
    * can be used by subclasses as input for {@link #draw(com.itextpdf.text.Rectangle, String) }
    *
    * @param genericTag
    * @return
    */
   DelayedData getDelayed(String genericTag);

   public DATATYPE getData();

   public void setData(DATATYPE data);
   public DATATYPE convert(Object s);

   /**
    * get a canvas for drawing, prepared according to settings
    *
    * @see #draw(com.itextpdf.text.Rectangle, java.lang.String) 
    * @see #resetCanvas(com.itextpdf.text.pdf.PdfContentByte) 
    * @return
    */
   public PdfContentByte getPreparedCanvas();
   
   /**
    * Reset the canvas after usage, making sure that pdf constructs are in balance
    * @see #getPreparedCanvas() 
    * @param canvas 
    */
   public void resetCanvas(PdfContentByte canvas);

   /**
    * the current eventmode for this styler, determines the type of event this styler will be used for.
    * @return 
    */
   EVENTMODE getEventmode();
   
}
