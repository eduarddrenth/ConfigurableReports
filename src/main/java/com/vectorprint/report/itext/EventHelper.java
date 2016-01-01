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
//~--- non-JDK imports --------------------------------------------------------
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.IllegalPdfSyntaxException;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.data.ReportDataAware;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.data.types.ValueHelper;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.debug.DebugStyler;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.stylers.Advanced;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * Responsible for printing headers, footers, debugging and failure information. Note that this handler uses a
 * {@link PdfTemplate} with the dimensions of the first page of the document to afterwards write information
 * (pagenumber, failure header) to pages, hence for now positioning may fail when using different page sizes.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param RD the type of data this helper can deal with
 */
public class EventHelper<RD extends ReportDataHolder> extends PdfPageEventHelper
    implements StylerFactory, ReportDataAware<RD> {
   
   public static final String UPTO = "upto";
   private static final Logger log = Logger.getLogger(EventHelper.class.getName());
   private ElementProducer elementProducer;
   private RD reportData;
   private PdfTemplate template;
   private StylerFactory stylerFactory;
   private boolean failuresHereAfter = false, debugHereAfter = false;
   private int lastPage;
   private final java.util.List<Advanced> doForAllPages = new ArrayList<Advanced>(1);
   private final Map<String, Collection<Advanced>> doOnGenericTag = new HashMap<String, Collection<Advanced>>(10);
   private final Map<String, Chunk> imageChunks = new HashMap<String, Chunk>(10);
   private final Map<String, Image> rectangles = new HashMap<String, Image>(10);
   private ItextHelper itextHelper;
   
   protected ElementProducer getElementProducer() {
      return elementProducer;
   }
   
   void setElementProvider(ElementProducer elementProvider) {
      this.elementProducer = elementProvider;
      if (elementProvider instanceof DefaultElementProducer) {
         ((DefaultElementProducer) elementProvider).setPh(this);
      }
   }

   /**
    * key for looking up stylers in the settings that will be used for styling the footer
    */
   public static final String PAGEFOOTERSTYLEKEY = "pagefooter";
   /**
    * the default stylers used to style the footer
    */
   public static final String[] PAGEFOOTERSTYLE = new String[]{"Font(size=12)","Border(position=top,borderwidth=1)","Padding(padding=0)"};
   /**
    * key for looking up stylers in the settings that will be used for styling the footer table. The default value for
    * this will be calculated based on document measures
    */
   public static final String PAGEFOOTERTABLEKEY = "pagefootertable";
   
   void setItextStylerFactory(StylerFactory itextStylerFactory) {
      this.stylerFactory = itextStylerFactory;
      itextHelper = new ItextHelper();
   }

   /**
    * prepares template for printing header and footer
    *
    * @param writer
    * @param document
    */
   @Override
   public final void onOpenDocument(PdfWriter writer, Document document) {
      super.onOpenDocument(writer, document);
      template = writer.getDirectContent().createTemplate(document.getPageSize().getWidth(), document.getPageSize().getHeight());
      if (!getSettings().containsKey(PAGEFOOTERSTYLEKEY)) {
         getSettings().put(PAGEFOOTERSTYLEKEY, PAGEFOOTERSTYLE);
      }
      if (!getSettings().containsKey(PAGEFOOTERTABLEKEY)) {
         float tot = ItextHelper.ptsToMm(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
         getSettings().put(PAGEFOOTERTABLEKEY, new StringBuilder("Table(columns=3,widths=")
             .append(Math.round(tot * getSettings().getFloatProperty(0.85f, "footerleftwidthpercentage"))).append('|')
             .append(Math.round(tot * getSettings().getFloatProperty(0.14f, "footermiddlewidthpercentage"))).append('|')
             .append(Math.round(tot * getSettings().getFloatProperty(0.01f, "footerrightwidthpercentage"))).append(')').toString()
         );
      }
   }

   /**
    * prints debug info when property debug is true, calls renderHeader and renderFooter and
    * {@link Advanced#draw(com.itextpdf.text.Rectangle, java.lang.String) } with {@link Document#getPageSize() }
    * and null for {@link DefaultStylerFactory#PAGESTYLERS}.
    *
    * @param writer
    * @param document
    */
   @Override
   public final void onEndPage(PdfWriter writer, Document document) {
      super.onEndPage(writer, document);
      sanitize(writer);
      try {
         if (failuresHereAfter || debugHereAfter) {
            PdfContentByte bg = writer.getDirectContentUnder();
            Rectangle rect = writer.getPageSize();
            rect.setBackgroundColor(itextHelper.fromColor(getSettings().getColorProperty(new Color(240, 240, 240), "legendbackground")));
            bg.rectangle(rect);
            bg.closePathFillStroke();
         } else {
            for (Advanced a : doForAllPages) {
               try {
                  if (a.shouldDraw(null)) {
                     a.draw(document.getPageSize(), null);
                  }
               } catch (VectorPrintException ex) {
                  throw new VectorPrintRuntimeException(ex);
               }
            }
         }
         if (!debugHereAfter && getSettings().getBooleanProperty(false, DEBUG)) {
            
            PdfContentByte canvas = writer.getDirectContent();
            
            Rectangle rect = new Rectangle(document.leftMargin(),
                document.bottomMargin(),
                document.right() - document.rightMargin(),
                document.top() - document.topMargin());
            
            DebugHelper.debugRect(canvas, rect, new float[]{10, 2}, 0.3f, getSettings(), stylerFactory.getLayerManager());
            
         }
         
         renderHeader(writer, document);
         maxTagForGenericTagOnPage = ((DefaultElementProducer) elementProducer).getAdvancedTag();
         if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.PRINTFOOTER)) {
            renderFooter(writer, document);
         } else {
            log.warning("not printing footer, if you want page footers set " + ReportConstants.PRINTFOOTER + " to true");
         }
         maxTagForGenericTagOnPage = Integer.MAX_VALUE;
      } catch (VectorPrintException e) {
         throw new VectorPrintRuntimeException("failed to create the report header or footer: ", e);
      } catch (DocumentException e) {
         throw new VectorPrintRuntimeException("failed to create the report header or footer: ", e);
      } catch (InstantiationException ex) {
         throw new VectorPrintRuntimeException("failed to create the report header or footer: ", ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintRuntimeException("failed to create the report header or footer: ", ex);
      }
   }
   
   public static void sanitize(PdfWriter writer) {
      while (true) {
         try {
            writer.getDirectContent().sanityCheck();
            break;
         } catch (IllegalPdfSyntaxException e) {
            if (e.getMessage().toLowerCase().contains("layer")) {
               writer.getDirectContent().endLayer();
            } else if (e.getMessage().toLowerCase().contains("state")) {
               writer.getDirectContent().restoreState();
            } else {
               break;
            }
         }
      }
      while (true) {
         try {
            writer.getDirectContentUnder().sanityCheck();
            break;
         } catch (IllegalPdfSyntaxException e) {
            if (e.getMessage().toLowerCase().contains("layer")) {
               writer.getDirectContentUnder().endLayer();
            } else if (e.getMessage().toLowerCase().contains("state")) {
               writer.getDirectContentUnder().restoreState();
            } else {
               break;
            }
         }
      }
   }

   /**
    * when failure information is appended to the report, a header on each page will be printed refering to this
    * information.
    *
    * @param template
    * @param x
    * @param y
    */
   protected void printFailureHeader(PdfTemplate template, float x, float y) {
      Font f = DebugHelper.debugFontLink(template, getSettings());
      Chunk c = new Chunk(getSettings().getProperty("failures in report, see end of report", "failureheader"), f);
      ColumnText.showTextAligned(template, Element.ALIGN_LEFT, new Phrase(c), x, y, 0);
   }

   /**
    * When the setting {@link ReportConstants#PRINTFOOTER} is true prints the total number of pages on each page when
    * the document is closed. Note that
    *
    * @param template
    * @see #PAGEFOOTERSTYLEKEY
    * @param x
    * @param y
    */
   protected void printTotalPages(PdfTemplate template, float x, float y) throws VectorPrintException, InstantiationException, IllegalAccessException {
      if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.PRINTFOOTER)) {
         Phrase p = elementProducer.createElement(String.valueOf(lastPage), Phrase.class, stylerFactory.getStylers(PAGEFOOTERSTYLEKEY));
         ColumnText.showTextAligned(template, Element.ALIGN_LEFT, p, x, y, 0);
      }
   }

   /**
    * prints a failure and / or a debug header when applicable.
    *
    * @see #getTemplateImage(com.itextpdf.text.pdf.PdfTemplate)
    * @param writer
    * @param document
    * @throws DocumentException
    * @throws VectorPrintException
    */
   private final void renderHeader(PdfWriter writer, Document document) throws DocumentException, VectorPrintException {
      if ((!debugHereAfter && getSettings().getBooleanProperty(false, DEBUG))
          || (!failuresHereAfter && !getSettings().getBooleanProperty(false, DEBUG))) {
         
         writer.getDirectContent().addImage(getTemplateImage(template));
         
         if (getSettings().getBooleanProperty(false, DEBUG)) {
            ArrayList a = new ArrayList(2);
            a.add(PdfName.TOGGLE);
            a.add(elementProducer.initLayerGroup(DEBUG, writer.getDirectContent()));
            PdfAction act = PdfAction.setOCGstate(a, true);
            Chunk h = new Chunk("toggle debug info", DebugHelper.debugFontLink(writer.getDirectContent(), getSettings())).setAction(act);
            
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(h), 10, document.top() - 15, 0);
            Font f = DebugHelper.debugFontLink(writer.getDirectContent(), getSettings());
//            act = PdfAction.gotoLocalPage("debugpage", true);

            elementProducer.startLayerInGroup(DEBUG, writer.getDirectContent());
            
            h = new Chunk(getSettings().getProperty("go to debug legend", "debugheader"), f).setLocalGoto(BaseReportGenerator.DEBUGPAGE);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase(h), 10, document.top() - 3, 0);
            
            writer.getDirectContent().endLayer();
            
         }
      }
   }

   /**
    * calls {@link #printTotalPages(com.itextpdf.text.pdf.PdfTemplate, float, float)  }
    * with {@link #PAGEFOOTERSTYLE a font from setup}, document.right() and the calculated bottom of the footertable.
    * Clears the layermanager. When applicable calls {@link #printFailureHeader(com.itextpdf.text.pdf.PdfTemplate, float, float)
    * }
    *
    * @param writer
    * @param document
    */
   @Override
   public void onCloseDocument(PdfWriter writer, Document document) {
      super.onCloseDocument(writer, document);
      if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.PRINTFOOTER)) {
         try {
            printTotalPages(template, document.right(), footerBottom);
         } catch (VectorPrintException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (InstantiationException ex) {
            throw new VectorPrintRuntimeException(ex);
         } catch (IllegalAccessException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      if (failuresHereAfter) {
         printFailureHeader(template, 10, document.top() - 10);
      }
   }

   /**
    * creates a footer cell using an instance of {@link com.vectorprint.report.itext.styleAndAddToReport.stylers.Font}
    *
    * @see #PAGEFOOTERSTYLEKEY
    * @see StylerFactory#getStylers(java.lang.String...) 
    * @param val
    * @return
    * @throws VectorPrintException
    */
   private PdfPCell createFooterCell(Object val) throws VectorPrintException {
      try {
         return elementProducer.createElement(val, PdfPCell.class, getStylers(PAGEFOOTERSTYLEKEY));
      } catch (InstantiationException ex) {
         throw new VectorPrintException(ex);
      } catch (IllegalAccessException ex) {
         throw new VectorPrintException(ex);
      }
   }
   private int maxTagForGenericTagOnPage = Integer.MAX_VALUE;

   /**
    * prints a footer table with a line at the top, a date and page numbering, second cell will be right aligned
    *
    * @see #PAGEFOOTERTABLEKEY
    * @see #PAGEFOOTERSTYLEKEY
    *
    * @param writer
    * @param document
    * @throws DocumentException
    * @throws VectorPrintException
    */
   protected void renderFooter(PdfWriter writer, Document document) throws DocumentException, VectorPrintException, InstantiationException, IllegalAccessException {
      if (!debugHereAfter && !failuresHereAfter) {
         PdfPTable footerTable = elementProducer.createElement(null, PdfPTable.class, getStylers(PAGEFOOTERTABLEKEY));
         
         footerTable.addCell(createFooterCell(ValueHelper.createDate(new Date())));
         
         String pageText = writer.getPageNumber() + getSettings().getProperty(" of ", UPTO);
         
         PdfPCell c = createFooterCell(pageText);
         c.setHorizontalAlignment(Element.ALIGN_RIGHT);
         footerTable.addCell(c);
         
         footerTable.addCell(createFooterCell(new Chunk()));
         footerTable.writeSelectedRows(0, -1, document.getPageSize().getLeft() + document.leftMargin(),
             document.getPageSize().getBottom(document.bottomMargin()), writer.getDirectContentUnder());
         footerBottom = document.bottom() - footerTable.getTotalHeight();
      }
   }
   private float footerBottom = 0;
   private Image templateImage;

   /**
    * this image will be used for painting the total number of pages and for a failure header when failures are printed
    * inside the report.
    *
    * @see #printFailureHeader(com.itextpdf.text.pdf.PdfTemplate, float, float)
    * @see #printTotalPages(com.itextpdf.text.pdf.PdfTemplate, float, float)
    * @param template
    * @return
    * @throws BadElementException
    */
   protected Image getTemplateImage(PdfTemplate template) throws BadElementException {
      if (templateImage == null) {
         templateImage = Image.getInstance(template);
         templateImage.setAbsolutePosition(0, 0);
      }
      
      return templateImage;
   }
   
   @Override
   public void setReportDataHolder(RD reportData) {
      this.reportData = reportData;
   }
   
   @Override
   public RD getReportDataHolder() {
      return this.reportData;
   }

   /**
    * Calls the super and {@link Advanced#draw(com.itextpdf.text.Rectangle, java.lang.String) } for each Advanced styler
    * registered. Adds a debugging link for images when in debug mode.
    *
    * @param writer
    * @param document
    * @param rect
    * @param genericTag
    * @see #addDelayedStyler(java.lang.String, java.util.Collection, com.itextpdf.text.Chunk) 
    * @see Advanced#addDelayedData(java.lang.String, com.itextpdf.text.Chunk)
    * @see VectorPrintDocument
    */
   @Override
   public final void onGenericTag(PdfWriter writer, Document document, final Rectangle rect, String genericTag) {
//      if (log.isLoggable(Level.FINE)) {
//         Collection<Advanced> av = doOnGenericTag.get(genericTag);
//         String data = null;
//         if (av!=null) {
//            for (Advanced a : av) {
//               data += a.getDelayed(genericTag).getDataPart();
//               break;
//            }
//         }
//         System.out.println("wrapped: " + carriageReturns.toString() + ", " + genericTag + " " + data + " " + rect.toString() + ", x=" + rect.getLeft());
//      }
      if (doOnGenericTag.get(genericTag) != null
          && !genericTag.startsWith(VectorPrintDocument.DRAWNEAR)
          && !genericTag.startsWith(VectorPrintDocument.DRAWSHADOW)) {
         int i = -1;
         for (Advanced a : doOnGenericTag.get(genericTag)) {
            ++i;
            if (genericTag.startsWith(DefaultElementProducer.ADV)
                && Integer.parseInt(genericTag.replace(DefaultElementProducer.ADV, "")) > maxTagForGenericTagOnPage) {
               continue;
            }
            try {
               if (a.shouldDraw(a.getDelayed(genericTag).getData())) {
                  if (a instanceof DebugStyler && imageChunks.containsKey(genericTag)) {
                     Chunk wrapper = imageChunks.get(genericTag);
                     Object[] atts = (Object[]) wrapper.getAttributes().get(Chunk.IMAGE);
                     Rectangle shifted = new Rectangle(rect);
                     shifted.setLeft(shifted.getLeft() + (Float) atts[1]);
                     shifted.setRight(shifted.getRight() + (Float) atts[1]);
                     shifted.setTop(shifted.getTop() + (Float) atts[2]);
                     shifted.setBottom(shifted.getBottom() + (Float) atts[2]);
                     a.draw(shifted, genericTag);
                  } else if (!genericTag.startsWith(VectorPrintDocument.IMG_DEBUG)) {
                     a.draw(rect, genericTag);
                  }
               }
            } catch (VectorPrintException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
         }
      }
      // images
      if (genericTag.startsWith(VectorPrintDocument.IMG_DEBUG) && getSettings().getBooleanProperty(false, DEBUG)) {
         // only now we can define a goto action, we know the position of the image
         if (rectangles.containsKey(genericTag)) {
            Rectangle rectangle = imageRectFromChunk(genericTag, rect);
            DebugHelper.debugAnnotation(rectangle,
                genericTag.replaceFirst(VectorPrintDocument.IMG_DEBUG, ""), writer);
         } else {
            DebugHelper.debugAnnotation(rect, genericTag.replaceFirst(VectorPrintDocument.IMG_DEBUG, ""), writer);
         }
      }
      if (genericTag.startsWith(VectorPrintDocument.DRAWNEAR)) {
         Rectangle rectangle = imageRectFromChunk(genericTag, rect);
         com.vectorprint.report.itext.style.stylers.Image image = (com.vectorprint.report.itext.style.stylers.Image) doOnGenericTag.get(genericTag).iterator().next();
         short i = -1;
         for (Advanced a : doOnGenericTag.get(genericTag)) {
            try {
               if (++i > 0 && a.shouldDraw(a.getDelayed(genericTag).getData())) {
                  if (getSettings().getBooleanProperty(false, DEBUG)) {
                     DebugHelper.styleLink(writer.getDirectContent(), a.getStyleClass(), "draw near", rectangle.getLeft(), rectangle.getTop(), getSettings(), elementProducer);
                  }
                  a.draw(rectangle, genericTag);
               }
            } catch (VectorPrintException ex) {
               throw new VectorPrintRuntimeException(ex);
            }
            
         }
      }
      if (genericTag.startsWith(VectorPrintDocument.DRAWSHADOW)) {
         // we know the position of the image
         Rectangle r = imageRectFromChunk(genericTag, rect);
         com.vectorprint.report.itext.style.stylers.Image image = (com.vectorprint.report.itext.style.stylers.Image) doOnGenericTag.get(genericTag).iterator().next();
         try {
            image.drawShadow(r.getLeft(), r.getBottom(), r.getWidth(), r.getHeight(),genericTag.replaceFirst(VectorPrintDocument.DRAWSHADOW, ""));
         } catch (VectorPrintException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }
  
   private Rectangle imageRectFromChunk(String genericTag, Rectangle rect) {
         Image r = rectangles.get(genericTag);
         return new Rectangle(rect.getLeft(),
             rect.getTop() - r.getScaledHeight() + Y_POSITION_FIX,
             rect.getLeft() + r.getScaledWidth(),
             rect.getTop() + Y_POSITION_FIX);
   }
   
   @Setting(keys = "yPositionImageFix")
   private Integer Y_POSITION_FIX = 2;

   @Override
   public EnhancedMap getSettings() {
      return stylerFactory.getSettings();
   }
   
   public boolean isFailuresHereAfter() {
      return failuresHereAfter;
   }
   
   public void setFailuresHereAfter(boolean failuresHereAfter) {
      this.failuresHereAfter = failuresHereAfter;
   }
   
   public boolean isDebugHereAfter() {
      return debugHereAfter;
   }
   
   public void setDebugHereAfter(boolean debugHereAfter) {
      this.debugHereAfter = debugHereAfter;
   }
   
   public int getLastPage() {
      return lastPage;
   }
   
   public void setLastPage(int lastPage) {
      this.lastPage = lastPage;
   }
   
   @Override
   public List<BaseStyler> getStylers(String... styleClasses) throws VectorPrintException {
      return stylerFactory.getStylers(styleClasses);
   }
   
   @Override
   public DocumentStyler getDocumentStyler() throws VectorPrintException {
      return stylerFactory.getDocumentStyler();
   }
   
   @Override
   public Map<String, String> getStylerSetup() {
      return stylerFactory.getStylerSetup();
   }
   
   @Override
   public void setImageLoader(ImageLoader imageLoader) {
   }
   
   public void addStylerForEachPage(Advanced advanced) {
      doForAllPages.add(advanced);
   }

   /**
    * add a styler that will do its styling in {@link #onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.itextpdf.text.Rectangle, java.lang.String)
    * }.
    *
    * @param tag
    * @param advanced
    * @param chunk used when debugging, for placing debug info at the right position in the pdf
    * @see Chunk#Chunk(com.itextpdf.text.Image, float, float, boolean)
    */
   public void addDelayedStyler(String tag, Collection<Advanced> advanced, Chunk chunk) {
      addDelayedStyler(tag, advanced, chunk, null);
   }

   /**
    * add a styler that will do its styling in
    * {@link #onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.itextpdf.text.Rectangle, java.lang.String)}.
    *
    * @param tag
    * @param advanced
    * @param chunk used when debugging, for placing debug info at the right position in the pdf
    * @param img the value of rect
    * @see Chunk#Chunk(com.itextpdf.text.Image, float, float, boolean)
    */
   public void addDelayedStyler(String tag, Collection<Advanced> advanced, Chunk chunk, Image img) {
      doOnGenericTag.put(tag, advanced);
      if (chunk != null && chunk.getImage() != null) {
         imageChunks.put(tag, chunk);
      }
      if (img != null) {
         rectangles.put(tag, img);
      }
   }
   
   @Override
   public void setLayerManager(LayerManager layerManager) {
   }
   
   @Override
   public List<BaseStyler> getBaseStylersFromCache(String... styleClasses) throws VectorPrintException {
      return stylerFactory.getBaseStylersFromCache(styleClasses);
   }
   
   @Override
   public void setDocument(Document document, PdfWriter writer) {
   }
   
   @Override
   public Document getDocument() {
      return stylerFactory.getDocument();
   }
   
   @Override
   public PdfWriter getWriter() {
      return stylerFactory.getWriter();
   }
   
   @Override
   public LayerManager getLayerManager() {
      return stylerFactory.getLayerManager();
   }
   
}
