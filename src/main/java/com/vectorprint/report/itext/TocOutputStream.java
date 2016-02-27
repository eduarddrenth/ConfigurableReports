package com.vectorprint.report.itext;

/*
 * #%L
 * ConfigurableReports
 * %%
 * Copyright (C) 2014 - 2016 VectorPrint
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Responsible for printing a table of contents according to settings in {@link DocumentSettings}.
 * @author Eduard Drenth at VectorPrint.nl
 */
class TocOutputStream extends AbstractTwoPassStream {

   private final BaseReportGenerator outer;

   public TocOutputStream(OutputStream out, int bufferSize, final BaseReportGenerator outer) throws IOException {
      super(out, bufferSize);
      this.outer = outer;
   }

   @Override
   public void secondPass(InputStream firstPass, OutputStream orig) throws IOException {
      PdfReader reader = null;
      VectorPrintDocument vpd = (VectorPrintDocument) outer.getDocument();
      try {
         reader = new PdfReader(firstPass);
         prepareToc();
         // init fresh components for second pass styling
         StylerFactory _stylerFactory = outer.getStylerFactory().getClass().newInstance();
         StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(_stylerFactory, outer.getSettings());
         _stylerFactory.setLayerManager(outer.getElementProducer());
         _stylerFactory.setImageLoader(outer.getElementProducer());
         outer.getStyleHelper().setStylerFactory(_stylerFactory);
         EventHelper event = outer.getEventHelper().getClass().newInstance();
         event.setItextStylerFactory(_stylerFactory);
         event.setElementProvider(outer.getElementProducer());
         ((DefaultElementProducer) outer.getElementProducer()).setPh(event);
         Document d = new VectorPrintDocument(event, _stylerFactory, outer.getStyleHelper());
         PdfWriter w = PdfWriter.getInstance(d, orig);
         w.setPageEvent(event);
         outer.getStyleHelper().setVpd((VectorPrintDocument) d);
         _stylerFactory.setDocument(d, w);
         DocumentStyler ds = _stylerFactory.getDocumentStyler();
         outer.getStyleHelper().style(d, null, StyleHelper.toCollection(ds));
         d.open();
         ds.styleAfterOpen(d, null);
         List outline = SimpleBookmark.getBookmark(reader);
         if (!ds.getValue(DocumentSettings.TOCAPPEND, Boolean.class)) {
            printToc(d, w, vpd);
            if (outline != null) {
               int cur = w.getCurrentPageNumber();
               SimpleBookmark.shiftPageNumbers(outline, cur, null);
            }
            d.newPage();
         }
         outer.getSettings().put(ReportConstants.DEBUG, Boolean.FALSE.toString());
         for (int p = 1; p <= reader.getNumberOfPages(); p++) {
            Image page = Image.getInstance(w.getImportedPage(reader, p));
            page.setAbsolutePosition(0, 0);
            d.setPageSize(page);
            d.newPage();
            Chunk i = new Chunk(" ");
            if (vpd.getToc().containsKey(p)) {
               Section s = null;
               for (Map.Entry<Integer, List<Section>> e : vpd.getToc().entrySet()) {
                  if (e.getKey() == p) {
                     s = e.getValue().get(0);
                     break;
                  }
               }
               i.setLocalDestination(s.getTitle().getContent());
            }
            d.add(i);
            w.getDirectContent().addImage(page);
            w.freeReader(reader);
         }
         if (_stylerFactory.getDocumentStyler().getValue(DocumentSettings.TOCAPPEND, Boolean.class)) {
            printToc(d, w, vpd);
         }
         w.setOutlines(outline);
         if (outer.isWasDebug()) {
            event.setLastPage(outer.getWriter().getCurrentPageNumber());
            d.setPageSize(new Rectangle(ItextHelper.mmToPts(297), ItextHelper.mmToPts(210)));
            d.setMargins(5, 5, 5, 5);
            d.newPage();
            outer.getSettings().put(ReportConstants.DEBUG, Boolean.TRUE.toString());
            event.setDebugHereAfter(true);
            DebugHelper.appendDebugInfo(w, d, outer.getSettings(), _stylerFactory);
         }
         d.close();
      } catch (VectorPrintException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (DocumentException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException(ex);
      } finally {
         if (reader != null) {
            reader.close();
         }
      }
   }

   private void prepareToc() throws VectorPrintException {
      DocumentStyler ds = outer.getStylerFactory().getDocumentStyler();
      EnhancedMap settings = outer.getSettings();
      if (!settings.containsKey(DocumentSettings.TOCTITLESTYLEKEY)) {
         settings.put(DocumentSettings.TOCTITLESTYLEKEY, DocumentSettings.TOCTITLESTYLE);
      }
      if (!settings.containsKey(DocumentSettings.TOCNRSTYLEKEY)) {
         settings.put(DocumentSettings.TOCNRSTYLEKEY, DocumentSettings.TOCNRSTYLE);
      }
      if (!settings.containsKey(DocumentSettings.TOCHEADERSTYLEKEY)) {
         settings.put(DocumentSettings.TOCHEADERSTYLEKEY, DocumentSettings.TOCHEADER);
      }
      if (!settings.containsKey(DocumentSettings.TOCCAPTIONKEY)) {
         settings.put(DocumentSettings.TOCCAPTIONKEY, DocumentSettings.TOCCAPTION);
      }
      settings.remove(DefaultStylerFactory.PRESTYLERS);
      settings.remove(DefaultStylerFactory.POSTSTYLERS);
      settings.remove(DefaultStylerFactory.PAGESTYLERS);
      settings.put(ReportConstants.PRINTFOOTER, "false");
      if (!settings.containsKey(DocumentSettings.TOCTABLEKEY)) {
         float tot = ItextHelper.ptsToMm(outer.getDocument().getPageSize().getWidth() - outer.getDocument().leftMargin() - outer.getDocument().rightMargin());
         settings.put(DocumentSettings.TOCTABLEKEY, new String[]{"Table(columns=2,widths=" + (Math.round(tot * ds.getValue(DocumentSettings.TOCLEFTWIDTH, Float.class))) + '|' + (Math.round(tot * ds.getValue(DocumentSettings.TOCRIGHTWIDTH, Float.class))) + ')', "AddCell(data=Table of Contents,styleclass=toccaption)", "AddCell(data=title,styleclass=tocheader)", "AddCell(data=page,styleclass=tocheader)"});
      }
   }

   private void printToc(Document d, PdfWriter w, VectorPrintDocument vpd) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException {
      DocumentStyler ds = outer.getStylerFactory().getDocumentStyler();
      if (ds.getValue(DocumentSettings.TOCAPPEND, Boolean.class)) {
         d.add(Chunk.NEXTPAGE);
      }
      if (outer.isWasDebug()) {
         outer.getSettings().put(ReportConstants.DEBUG, Boolean.TRUE.toString());
         PdfContentByte canvas = w.getDirectContent();
         outer.startLayerInGroup(ReportConstants.DEBUG, canvas);
         BaseFont bf = DebugHelper.debugFont(canvas, outer.getSettings());
         canvas.showTextAligned(Element.ALIGN_RIGHT, "FOR DEBUG INFO IN THE DOCUMENT TURN OFF TOC (-DocumentSettings.toc=false)", d.right(), d.getPageSize().getHeight() - ItextHelper.getTextHeight("F", bf, 8), 0);
         canvas.endLayer();
      }
      ElementProducer ep = outer.getElementProducer();
      StylerFactory sf = outer.getStylerFactory();
      PdfPTable tocTable = ep.createElement(null, PdfPTable.class, sf.getStylers(DocumentSettings.TOCTABLEKEY));
      for (Map.Entry<Integer, List<Section>> e : vpd.getToc().entrySet()) {
         String link = null;
         for (Section s : e.getValue()) {
            if (ds.isParameterSet(DocumentSettings.TOCMAXDEPTH) && ds.getValue(DocumentSettings.TOCMAXDEPTH, Integer.class) < s.getDepth()) {
               continue;
            }
            if (link == null) {
               link = s.getTitle().getContent();
            }
            Chunk c = ep.createElement(s.getTitle().getContent(), Chunk.class, sf.getStylers(DocumentSettings.TOCTITLESTYLEKEY));
            if (ds.getValue(DocumentSettings.TOCDOTS, Boolean.class)) {
               float tw = ItextHelper.getTextWidth(c);
               float cw = tocTable.getAbsoluteWidths()[0];
               float dw = ItextHelper.getTextWidth(ep.createElement(".", Chunk.class, sf.getStylers(DocumentSettings.TOCTITLESTYLEKEY))) * 1.5f;
               int numDots = (int) ((cw > tw) ? (cw - tw) / dw : 0);
               char[] dots = new char[numDots];
               Arrays.fill(dots, '.');
               c = ep.createElement(s.getTitle().getContent() + "  " + String.valueOf(dots), Chunk.class, sf.getStylers(DocumentSettings.TOCTITLESTYLEKEY));
            }
            c.setLocalGoto(link);
            tocTable.addCell(ep.createElement(c, PdfPCell.class, sf.getStylers(DocumentSettings.TOCTITLESTYLEKEY)));
            c = ep.createElement(e.getKey(), Chunk.class, sf.getStylers(DocumentSettings.TOCNRSTYLEKEY));
            c.setLocalGoto(link);
            tocTable.addCell(ep.createElement(c, PdfPCell.class, sf.getStylers(DocumentSettings.TOCNRSTYLEKEY)));
         }
      }
      d.add(tocTable);
   }

}
