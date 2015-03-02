/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext;

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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.stylers.Advanced;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calls {@link BaseStyler#style(java.lang.Object, java.lang.Object) } with no data on elements in
 * {@link #scheduleStylerAfterAdding(com.vectorprint.report.itext.style.BaseStyler) a queue}, see {@link BaseStyler#styleAfterAdding()
 * } and {@link StyleHelper#style(java.lang.Object, java.lang.Object, java.util.Collection) }. For debugging images by
 * wrapping it in a Chunk.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class VectorPrintDocument extends Document {

   private static final Logger log = Logger.getLogger(VectorPrintDocument.class.getName());

   /**
    * prefix for generic tags used for debugging images.
    */
   public static final String IMG_DEBUG = "img_debug_";
   public static final String DRAWNEAR = "draw_near_";
   public static final String DRAWSHADOW = "draw_shadow_";

   private final EventHelper eventHelper;
   private final StylerFactory factory;
   private PdfWriter writer;
   private StyleHelper styleHelper;
   private Queue<AddElementHook> hooks = new ArrayDeque<AddElementHook>(20);
   private Map<Integer, List<Section>> toc = new TreeMap<Integer, List<Section>>();

   /**
    *
    * @param eventHelper the value of eventHelper
    * @param factory the value of factory
    * @param styleHelper the value of styleHelper
    */
   public VectorPrintDocument(EventHelper eventHelper, StylerFactory factory, StyleHelper styleHelper) {
      this.eventHelper = eventHelper;
      this.factory = factory;
      this.styleHelper = styleHelper;
   }

   /**
    * Provides support for debugging, table of contents and styling elements after adding to the document
    *
    * @param element
    * @return
    * @throws DocumentException
    * @see PageHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document,
    * com.itextpdf.text.Rectangle, java.lang.String)
    */
   @Override
   public boolean add(Element element) throws DocumentException {
      boolean rv = false;
      AddElementHook hook = null;
      if (element instanceof Image) {
         Image image = (Image) element;
         // see if we should style after adding
         hook = find(element, AddElementHook.INTENTION.PRINTIMAGESHADOW);
         /*
          we want to draw a shadow, write a chunk to get the position of the image later
          */
         if (hook != null) {
            String gt = DRAWSHADOW + hook.styleClass;
            Chunk chunk = positionChunk();
            styleHelper.delayedStyle(chunk, gt, StyleHelper.toCollection((Advanced) hook.bs), eventHelper, image);
            super.add(chunk);
         }

         hook = find(element, AddElementHook.INTENTION.DEBUGIMAGE);
         if (hook != null && ((EnhancedMap) factory.getSettings()).getBooleanProperty(ReportConstants.DEBUG, false)) {
            rv = tracePosition(image, hook, IMG_DEBUG,true);
         }
         hook = find(element, AddElementHook.INTENTION.DRAWNEARIMAGE);
         if (hook != null) {
            if (rv) {
               tracePosition(image, hook, DRAWNEAR,false);
            } else {
               rv = tracePosition(image, hook, DRAWNEAR,true);
            }
         }
         if (!rv) {
            rv = super.add(element);
         }

      } else if (element instanceof Section) {
         rv = super.add(element);
         if (!toc.containsKey(writer.getCurrentPageNumber())) {
            toc.put(writer.getCurrentPageNumber(), new ArrayList<Section>(3));
         }
         toc.get(writer.getCurrentPageNumber()).add((Section) element);
      } else {
         rv = super.add(element);
      }
      // see if we should style after adding
      Iterator<AddElementHook> it = hooks.iterator();
      while (it.hasNext() && (hook = it.next()) != null) {
         if (hook.intention == AddElementHook.INTENTION.STYLELATER && hook.e.type() == element.type() && hook.e.equals(element) && hook.bs.canStyle(element) && hook.bs.shouldStyle(null, element)) {
            it.remove();
            try {
               hook.bs.style(element, null);
            } catch (VectorPrintException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
      }
      return rv;
   }

   /**
    * 
    * @param image
    * @param hook
    * @param prefix
    * @param wrap
    * @return true when the image was added to the document
    * @throws DocumentException 
    */
   private boolean tracePosition(Image image, AddElementHook hook, String prefix, boolean wrap) throws DocumentException {
      String gt = prefix + hook.styleClass;
      if (wrap&&image.hasAbsoluteX() && image.hasAbsoluteY()) {
         // tracing position when an image is absolutely positioned by wrapping it in a chunk
         Chunk wrapper = new Chunk(image,
             (Float.NaN == image.getAbsoluteX()) ? 0 : image.getAbsoluteX(),
             (Float.NaN == image.getAbsoluteY()) ? 0 : image.getAbsoluteY(), true);
         try {
            styleHelper.delayedStyle(wrapper, gt,
                StyleHelper.getStylers(factory.getStylers(gt.replaceFirst(prefix, "")), Advanced.class), eventHelper);
         } catch (VectorPrintException ex) {
            throw new DocumentException(ex);
         }
         return super.add(wrapper);
      } else {
         // the chunk will provide feedback on the actual x and y of the image when onGenericTag is fired,
         // the width and height of the image are passed to the eventhelper
         Chunk chunk = positionChunk();
         try {
            styleHelper.delayedStyle(chunk, gt,
                StyleHelper.getStylers(factory.getStylers(gt.replaceFirst(prefix, "")), Advanced.class), eventHelper, image);
         } catch (VectorPrintException ex) {
            throw new DocumentException(ex);
         }
         super.add(chunk);
         return false;
      }
   }

   private Chunk positionChunk() {
      return new Chunk(" ");
   }

   private AddElementHook find(Element element, AddElementHook.INTENTION intention) {
      AddElementHook hook = null;
      Iterator<AddElementHook> it = hooks.iterator();
      while (it.hasNext() && (hook = it.next()) != null) {
         if (hook.intention == intention && hook.e.type() == element.type() && hook.e.equals(element)) {
            it.remove();
            if (log.isLoggable(Level.FINE)) {
               log.log(Level.FINE, "found {0}",
                   new Object[]{hook});
            }
            return hook;
         }
      }
      return null;
   }

   public void setWriter(PdfWriter writer) {
      this.writer = writer;
   }

   /**
    *
    * @param hook the value of hook
    */
   public void addHook(AddElementHook hook) {
      hooks.add(hook);
   }

   /**
    * Before or after adding elements to the document we can perform actions. Supoorted are:
    * <ul>
    * <li>print debugging info near the element</li>
    * <li>print a shadow</li>
    * <li>draw near an image</li>
    * <li>style an element after adding it to the document</li>
    * </ul>
    */
   public static class AddElementHook {

      public enum INTENTION {

         DEBUGIMAGE, PRINTIMAGESHADOW, STYLELATER, DRAWNEARIMAGE;
      }

      private final INTENTION intention;
      private final Element e;
      private final BaseStyler bs;
      private final String styleClass;

      public AddElementHook(INTENTION intention, Element e, BaseStyler bs, String styleClass) {
         this.intention = intention;
         this.e = e;
         this.bs = bs;
         this.styleClass = styleClass;
      }

      @Override
      public String toString() {
         return "AddHook{" + "intention=" + intention + ", e=" + e + ", bs=" + bs + ", styleClass=" + styleClass + '}';
      }

   }

   public Map<Integer, List<Section>> getToc() {
      return toc;
   }

}
