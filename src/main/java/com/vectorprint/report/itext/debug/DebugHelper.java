/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.itext.debug;

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

import com.itextpdf.text.Annotation;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.DEBUG;
import static com.vectorprint.report.itext.BaseReportGenerator.DEBUGPAGE;
import com.vectorprint.report.itext.Help;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.VectorPrintDocument;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylingCondition;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DebugHelper {

   private static final Logger log = Logger.getLogger(DebugHelper.class.getName());
   public static final String STYLE_CLASS_ = "style class: ";
   private static ItextHelper itextHelper = new ItextHelper();

   public static void debugBackground(PdfContentByte canvas, Rectangle rect, BaseColor color, String prefix, EnhancedMap settings, LayerManager layerAware) {
      canvas = canvas.getPdfWriter().getDirectContentUnder();
      int rgb = color.getRed() + color.getBlue() + color.getGreen();
      rect.setBackgroundColor(color);
      canvas.rectangle(rect);
      layerAware.startLayerInGroup(DEBUG, canvas);
      debugFont(canvas, settings);
      BaseColor txtColor = (rgb < 150) ? color.brighter().brighter() : color.darker().darker();
      canvas.setColorFill(txtColor);
      canvas.setColorStroke(txtColor);
      canvas.beginText();
      canvas.showTextAligned(Element.ALIGN_LEFT, prefix + color.toString().replace(Color.class.getName(), ""),
          rect.getLeft() + rect.getWidth() / 2,
          rect.getTop() - rect.getHeight() / 2, 0);
      canvas.endText();
      canvas.endLayer();
   }

   /**
    * When in debugging mode, adds a border to the image and calls {@link VectorPrintDocument#addHook(com.vectorprint.report.itext.VectorPrintDocument.AddElementHook)  } to 
    * be able to print debugging info and link for the image
    *
    * @param canvas
    * @param img
    * @param bordercolor
    * @param styleClass
    * @param extraInfo
    * @param settings
    * @param layerAware
    * @param document
    */
   public static void debugImage(PdfContentByte canvas, Image img, Color bordercolor, String styleClass, String extraInfo, EnhancedMap settings, LayerManager layerAware, VectorPrintDocument document) {
      if (null != img) {
         img.setBorder(Rectangle.BOX);
         img.setBorderWidth(0.3f);
         img.setBorderColor(itextHelper.fromColor(bordercolor));
         if (styleClass == null) {
            log.warning("not showing link to styleClass because there is no styleClass");
            return;
         }
         img.setAnnotation(new Annotation(DEBUG, "click for link to styleClass information (" + styleClass + extraInfo + ")"));
         document.addHook(new VectorPrintDocument.AddElementHook(VectorPrintDocument.AddElementHook.INTENTION.DEBUGIMAGE, img, null, styleClass));
      }
   }
   
   /**
    * adding a link (annotation) to information about the styleClass used
    * @param rectangle the value of rectangle
    * @param styleClass the value of styleClass
    * @param writer the value of writer
    */
   public static void debugAnnotation(Rectangle rectangle, String styleClass, PdfWriter writer) {
         if (styleClass == null) {
            log.warning("not showing link to styleClass because there is no styleClass");
            return;
         }
         // only now we can define a goto action, we know the position of the image
         PdfAction act = PdfAction.gotoLocalPage(styleClass, true);
         writer.getDirectContent().setAction(act, rectangle.getLeft(), rectangle.getBottom(), rectangle.getRight(), rectangle.getTop());
   }

   public static BaseFont debugFont(PdfContentByte canvas, EnhancedMap settings) {
      BaseFont bf = FontFactory.getFont(FontFactory.HELVETICA).getBaseFont();
      canvas.setFontAndSize(bf, 8);
      canvas.setColorFill(itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));
      canvas.setColorStroke(itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));
      return bf;
   }

   public static Font debugFontLink(PdfContentByte canvas, EnhancedMap settings) {
      BaseFont bf = debugFont(canvas, settings);
      return new Font(bf, 8, Font.UNDERLINE, itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));
   }

   public static void debugRect(PdfContentByte canvas, Rectangle rect, float[] dash, float borderWidth, EnhancedMap settings, LayerManager layerAware) {
      layerAware.startLayerInGroup(DEBUG, canvas);
      debugFont(canvas, settings);
      canvas.setLineWidth(borderWidth);
      canvas.setLineDash(dash, 0);
      canvas.moveTo(rect.getLeft(), rect.getBottom());
      canvas.lineTo(rect.getRight(), rect.getBottom());
      canvas.lineTo(rect.getRight(), rect.getTop());
      canvas.lineTo(rect.getLeft(), rect.getTop());
      canvas.closePathStroke();
      ItextHelper.resetLineDash(canvas);
      canvas.endLayer();
   }

   public static void styleLink(PdfContentByte canvas, String styleClass, String extraInfo, float x, float y, EnhancedMap settings, LayerManager layerAware) {
      if (styleClass == null) {
         log.warning("not showing link to styleClass because there is no styleClass");
         return;
      }
      Font dbf = DebugHelper.debugFontLink(canvas, settings);
      layerAware.startLayerInGroup(DEBUG, canvas);

      PdfAction act = PdfAction.gotoLocalPage(styleClass, true);
      Chunk c = new Chunk(styleClass + extraInfo, dbf);
      float w = ItextHelper.getTextWidth(c);
      float h = ItextHelper.getTextHeight(c);
      float tan = (float) Math.tan(Math.toRadians(8));
      canvas.setAction(act, x, y, x + w, y + h + tan * w);
      ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
          new Phrase(c),
          x, y, 8);
      canvas.endLayer();
   }

   static String getFirstNotDefaultStyleClass(Collection<String> styleClasses) {
      for (String s : styleClasses) {
         if (!DefaultStylerFactory.PRESTYLERS.equals(s) && !DefaultStylerFactory.POSTSTYLERS.equals(s)) {
            return s;
         }
      }
      return null;
   }

   /**
    * This method will append to the pdf a legend explaining the visual feedback in the pdf, an overview of the styles
    * used and an overview of the properties used.
    *
    * @throws DocumentException
    */
   public static void appendDebugInfo(PdfWriter writer, Document document, EnhancedMap settings, StylerFactory stylerFactory) throws DocumentException, VectorPrintException {

      PdfContentByte canvas = writer.getDirectContent();
      canvas.setFontAndSize(FontFactory.getFont(FontFactory.COURIER).getBaseFont(), 8);
      canvas.setColorFill(itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));
      canvas.setColorStroke(itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));

      Font f = FontFactory.getFont(FontFactory.COURIER,8);

      f.setColor(itextHelper.fromColor(settings.getColorProperty(Color.MAGENTA, ReportConstants.DEBUGCOLOR)));

      float top = document.getPageSize().getTop();

      document.add(new Phrase(new Chunk("table: ", f).setLocalDestination(DEBUGPAGE)));
      document.add(Chunk.NEWLINE);
      document.add(new Phrase("cell: ", f));
      document.add(Chunk.NEWLINE);
      document.add(new Phrase("image: ", f));
      document.add(Chunk.NEWLINE);
      document.add(new Phrase("text: ", f));
      document.add(Chunk.NEWLINE);
      document.add(new Phrase("background color is shown in a small rectangle", f));
      document.add(Chunk.NEWLINE);
      canvas.setLineWidth(2);
      canvas.setLineDash(new float[]{0.3f, 5}, 0);
      float left = document.leftMargin();
      canvas.rectangle(left + 80, top - 25, left + 80, 8);
      canvas.closePathStroke();
      canvas.setLineWidth(0.3f);
      canvas.setLineDash(new float[]{2, 2}, 0);
      canvas.rectangle(left + 80, top - 37, left + 80, 8);
      canvas.closePathStroke();
      canvas.setLineDash(new float[]{1, 0}, 0);
      canvas.rectangle(left + 80, top - 50, left + 80, 8);
      canvas.closePathStroke();
      canvas.setLineDash(new float[]{0.3f, 5}, 0);
      canvas.rectangle(left + 80, top - 63, left + 80, 8);
      canvas.closePathStroke();
      document.add(Chunk.NEWLINE);
      
      document.add(new Phrase("fonts available: " + FontFactory.getRegisteredFonts(), f));

      document.add(Chunk.NEWLINE);

      if (settings.getBooleanProperty(false, DEBUG)) {
         document.add(new Phrase("OVERVIEW OF STYLES FOR THIS REPORT", f));
         document.add(Chunk.NEWLINE);
         document.add(Chunk.NEWLINE);
      }

      Font b = new Font(f);
      b.setStyle("bold");
      Set<Map.Entry<String,String>> entrySet = stylerFactory.getStylerSetup().entrySet();
      for (Map.Entry<String, String> styleInfo : entrySet) {
         String key = styleInfo.getKey();
         document.add(new Chunk(key, b).setLocalDestination(key));
         document.add(new Chunk(": " + styleInfo.getValue(), f));
         document.add(Chunk.NEWLINE);
         document.add(new Phrase("   styling configured by " + key + ": ", f));
         for (BaseStyler st : (Collection<BaseStyler>)stylerFactory.getBaseStylersFromCache((key))) {
            document.add(Chunk.NEWLINE);
            document.add(new Chunk("      ", f));
            document.add(new Chunk(st.getClass().getSimpleName(), DebugHelper.debugFontLink(canvas, settings)).setLocalGoto(st.getClass().getSimpleName()));
            document.add(new Chunk(":", f));
            document.add(Chunk.NEWLINE);
            document.add(new Phrase("         parameters used: " + Help.getParamInfo(st), f));
            document.add(Chunk.NEWLINE);
            document.add(new Phrase("      conditions for this styler: ", f));
            for (StylingCondition sc : (Collection<StylingCondition>) st.getConditions()) {
               document.add(Chunk.NEWLINE);
               document.add(new Chunk("         ", f));
               document.add(new Chunk(sc.getClass().getSimpleName(), DebugHelper.debugFontLink(canvas, settings)).setLocalGoto(sc.getClass().getSimpleName()));
               document.add(Chunk.NEWLINE);
               document.add(new Phrase("            parameters used: " + Help.getParamInfo(sc), f));
            }
         }
         document.add(Chunk.NEWLINE);
         document.add(Chunk.NEWLINE);
      }

      document.newPage();

      document.add(new Phrase("Properties used for the report", f));
      document.add(Chunk.NEWLINE);

      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(bo);

      settings.listProperties(ps);
      ps.close();
      document.add(new Paragraph(bo.toString(), f));

      document.newPage();

      try {
         bo = new ByteArrayOutputStream();
         ps = new PrintStream(bo);
         Help.printHelpHeader(ps);
         ps.close();
         document.add(new Paragraph(bo.toString(), f));

         Help.printStylerHelp(document, f);

         Help.printHelpFooter(document, f);

      } catch (IOException ex) {
         log.log(Level.SEVERE, null, ex);
      } catch (ClassNotFoundException ex) {
         log.log(Level.SEVERE, null, ex);
      } catch (InstantiationException ex) {
         log.log(Level.SEVERE, null, ex);
      } catch (IllegalAccessException ex) {
         log.log(Level.SEVERE, null, ex);
      } catch (NoSuchMethodException ex) {
         log.log(Level.SEVERE, null, ex);
      } catch (InvocationTargetException ex) {
         log.log(Level.SEVERE, null, ex);
      }
   }
}
