/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.style;

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
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.debug.DebugStyler;
import com.vectorprint.report.itext.VectorPrintDocument;
import com.vectorprint.report.itext.style.stylers.AbstractStyler;
import com.vectorprint.report.itext.style.stylers.Advanced;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class StyleHelper {

   private VectorPrintDocument vpd;

   private StylerFactory stylerFactory;

   private static final Logger log = Logger.getLogger(StyleHelper.class.getName());

   public <E> E style(Class<E> clazz, Object data, Collection<? extends BaseStyler> stylers)
       throws VectorPrintException {
      E e = null;
      return (E) style(e, data, stylers);
   }

   /**
    * style elements, prepends the argument stylers with stylers by calling {@link StylerFactory#getElementStylers(java.lang.String[])
    * } with the simpleName of the class of the element to be styled. When {@link BaseStyler#shouldStyle(java.lang.Object, java.lang.Object)
    * } is true and {@link BaseStyler#styleAfterAdding() } is true, {@link VectorPrintDocument#scheduleStylerAfterAdding(com.vectorprint.report.itext.style.BaseStyler)
    * } will be called to do styling after adding the element. This way you can extend styling setup without using
    * styleClasses in your code.
    *
    * @param <E>
    * @param e
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public <E> E style(E e, Object data, Collection<? extends BaseStyler> stylers)
       throws VectorPrintException {
      if (e != null && stylerFactory.getSettings().containsKey(e.getClass().getSimpleName())) {
         /*
          * we get here with a stylers based on class names, first we prepend stylers based on the element class.
          * this way we can style for example all PdfPCell without the need to define classes.
          */
         Collection forElement = stylerFactory.getStylers(e.getClass().getSimpleName());
         forElement.addAll(stylers);
         stylers = forElement;
      }
      if (stylers != null) {
         for (BaseStyler styler : stylers) {
            if (styler.canStyle(e)) {
               if (styler.shouldStyle(data, e)) {
                  if (styler instanceof Advanced) {
                     Advanced.EVENTMODE mode = ((Advanced)styler).getEventmode();
                     if (e instanceof PdfPTable && (Advanced.EVENTMODE.ALL.equals(mode) || Advanced.EVENTMODE.TABLE.equals(mode))) {
                        ((PdfPTable)e).setTableEvent((PdfPTableEvent) styler);
                     }
                     if (e instanceof PdfPCell && (Advanced.EVENTMODE.ALL.equals(mode) || Advanced.EVENTMODE.CELL.equals(mode))) {
                        ((PdfPCell)e).setCellEvent((PdfPCellEvent) styler);
                     }
                  }
                  if (styler.styleAfterAdding()) {
                     if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "schedule styling {0} after adding to document: {1}",
                            new Object[]{e, styler.toString()});
                     }
                     vpd.addHook(new VectorPrintDocument.AddElementHook(VectorPrintDocument.AddElementHook.INTENTION.STYLELATER,(Element) e, styler,styler.getStyleClass()));
                  } else {
                     e = (E) styler.style(e, data);
                  }
               } else {
                  if (styler.getConditions().isEmpty()) {
                     log.log(Level.WARNING, "the implementation of shouldStyle in {0} prevents styling {1}",
                         new Object[]{styler.getClass().getSimpleName(), (null != e)
                            ? e.getClass().getSimpleName()
                            : "null"});
                  } else {
                     log.log(Level.WARNING, "a condition ({2}) prevents {0} from styling {1}",
                         new Object[]{styler.getClass().getSimpleName(), (null != e)
                            ? e.getClass().getSimpleName()
                            : "null", getConditionConfig(styler.getConditions())});
                  }
               }
            } else {
               if (log.isLoggable(Level.FINE)) {
                  log.log(Level.FINE, "{0} cannot style {1}",
                      new Object[]{styler.getClass().getName(), (null != e)
                         ? e.getClass().getName()
                         : "null"});
               }
            }
         }
      }

      return e;
   }

   public static final String getConditionConfig(Collection<StylingCondition> conditions) {
      StringBuilder sb = new StringBuilder(20);
      for (StylingCondition sc : conditions) {
         if (AbstractStyler.NOT_FROM_CONFIGURATION.equals(sc.getConfigKey())) {
            sb.append(sc.getClass().getSimpleName()).append(": ").append(sc.getParameters());
         } else {
            sb.append(Arrays.toString(sc.getSettings().get(sc.getConfigKey()))).append(", ");
         }
      }
      return sb.toString();
   }

   /**
    * get stylers with a certain baseclass from a collection of stylers.
    *
    * @param <E>
    * @param stylers
    * @param clazz
    * @return
    * @throws VectorPrintException
    */
   public static <E extends BaseStyler> List<E> getStylers(Collection<? extends BaseStyler> stylers, Class<E> clazz)
       throws VectorPrintException {
      List<E> st = new ArrayList<E>(1);
      for (BaseStyler s : stylers) {
         if (clazz.isAssignableFrom(s.getClass())) {
            st.add((E) s);
         }
      }
      return st;
   }

   public static <E extends BaseStyler> Collection<E> toCollection(E style) {
      Collection<E> stylers = new ArrayList<E>(1);

      stylers.add(style);

      return stylers;
   }

   /**
    * for debugging, uses {@link DebugStyler#getStyleClasses() } to return classNames
    *
    * @param l
    * @return
    */
   public static String styleClasses(Collection<? extends BaseStyler> l) {
      if (l == null) {
         return "argument is null";
      }

      for (BaseStyler st : l) {
         if (st instanceof DebugStyler) {
            return ((DebugStyler) st).getStyleSetup().toString();
         }
      }

      return "";
   }

   /**
    * Call {@link #delayedStyle(com.itextpdf.text.Chunk, java.lang.String, java.util.Collection, com.vectorprint.report.itext.EventHelper, com.itextpdf.text.Rectangle) }
    * with null for Rectangle
    */
   public void delayedStyle(Chunk c, String tag, Collection<? extends Advanced> stylers, EventHelper eventHelper) {
      delayedStyle(c, tag, stylers, eventHelper, null);
   }

   /**
    * register advanced stylers with the EventHelper to do the styling later
    * @param c
    * @param tag
    * @param stylers
    * @param eventHelper
    * @param img the value of rect
    * @see PageHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.itextpdf.text.Rectangle, java.lang.String)
    */
   public void delayedStyle(Chunk c, String tag, Collection<? extends Advanced> stylers, EventHelper eventHelper, Image img) {
      // add to pagehelper and set generic tag
      eventHelper.addDelayedStyler(tag, stylers, c, img);
      c.setGenericTag(tag);
   }

   /**
    * called from {@link BaseReportGenerator}
    *
    * @param stylerFactory
    */
   public void setStylerFactory(StylerFactory stylerFactory) {
      this.stylerFactory = stylerFactory;
   }

   /**
    * called from {@link BaseReportGenerator}
    *
    * @param vpd
    */
   public void setVpd(VectorPrintDocument vpd) {
      this.vpd = vpd;
   }

}
