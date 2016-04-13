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
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLayer;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.report.ReportConstants;
import static com.vectorprint.report.ReportConstants.DEBUG;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.BlockingDataCollector.QUEUECONTROL;
import com.vectorprint.report.data.DataCollectionMessages;
import com.vectorprint.report.data.DataCollectionMessages.Level;
import com.vectorprint.report.data.DataCollector;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.data.ReportDataHolder.IdData;
import com.vectorprint.report.data.types.DateValue;
import com.vectorprint.report.data.types.DurationValue;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.MoneyValue;
import com.vectorprint.report.data.types.NumberValue;
import com.vectorprint.report.data.types.PercentageValue;
import com.vectorprint.report.data.types.PeriodValue;
import com.vectorprint.report.data.types.TextValue;
import com.vectorprint.report.data.types.ValueHelper;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.itext.mappingconfig.AbstractDatamappingProcessor;
import com.vectorprint.report.itext.mappingconfig.DatamappingHelper;
import com.vectorprint.report.itext.mappingconfig.model.DataMapping;
import com.vectorprint.report.itext.mappingconfig.model.ElementConfig;
import com.vectorprint.report.itext.mappingconfig.model.StartContainerConfig;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DefaultStylerFactory;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

//~--- JDK imports ------------------------------------------------------------
/**
 * This implementation of ReportGenerator uses a {@link EventHelper} and a {@link ElementProducer} to build an iText
 * report. Subclasses should provide a constructor and implement {@link #createReportBody(com.itextpdf.text.Document, com.vectorprint.report.data.ReportDataHolder, com.itextpdf.text.pdf.PdfWriter) }
 * }
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <RD> The type of data this generator can use
 */
public class BaseReportGenerator<RD extends ReportDataHolder> extends AbstractDatamappingProcessor
    implements ReportGenerator<RD> {

   /**
    * exitcode from {@link #generate(com.vectorprint.report.data.ReportDataHolder, java.io.OutputStream) }
    * indicating the error can be found in the report itsself
    */
   public static final int ERRORINREPORT = -1;
   private static final Logger log = Logger.getLogger(BaseReportGenerator.class.getName());
   public static final String DEBUGPAGE = "debugpage";
   public static final String FAILUREPAGE = "failurepage";
   /**
    * factory ({@link DefaultStylerFactory see default}) to provide {@link BaseStyler stylers} for elements
    */
   private StylerFactory stylerFactory = new DefaultStylerFactory();
   /**
    * the pdf document that elements can be added to
    */
   private Document document;
   /**
    * the writer that produces the pdf
    */
   private PdfWriter writer;
   /**
    * the pagehelper responsible for header/footer
    */
   private EventHelper<RD> eventHelper;
   /**
    * helper for creating elements to add to the document
    */
   private final ElementProducer elementProducer;
   @SettingsField
   private EnhancedMap settings;
   private final StyleHelper styleHelper = new StyleHelper();
   private ItextHelper itextHelper;

   /**
    * Basic constructor
    *
    * @param eventHelper
    * @param elementProducer {@link DefaultElementProducer see default}
    * @throws com.vectorprint.VectorPrintException
    */
   public BaseReportGenerator(EventHelper<RD> eventHelper, ElementProducer elementProducer)
       throws VectorPrintException {
      super();

      if (eventHelper == null) {
         throw new VectorPrintException("PageHelper may not be null");
      }

      if (elementProducer == null) {
         throw new VectorPrintException("ElementProvider may not be null");
      }

      this.eventHelper = eventHelper;
      this.elementProducer = elementProducer;
      this.eventHelper.setElementProvider(elementProducer);
      eventHelper.setItextStylerFactory(stylerFactory);
      eventHelper.setLayerManager(elementProducer);
      stylerFactory.setImageLoader(elementProducer);
      stylerFactory.setLayerManager(elementProducer);
      styleHelper.setStylerFactory(stylerFactory);
      if (elementProducer instanceof DefaultElementProducer) {
         ((DefaultElementProducer) elementProducer).setStyleHelper(styleHelper);
      }
   }

   public BaseReportGenerator() throws VectorPrintException {
      this(new EventHelper<>(), new DefaultElementProducer());
   }

   @Override
   public List<BaseStyler> getStylers(String... styleClasses) throws VectorPrintException {
      return stylerFactory.getStylers(styleClasses);
   }

   private boolean wasDebug = false;

   /**
    * prepare the report, call {@link #continueOnDataCollectionMessages(com.vectorprint.report.data.DataCollectionMessages, com.itextpdf.text.Document)
    * }
    * and when this returns true call {@link #createReportBody(com.itextpdf.text.Document, com.vectorprint.report.data.ReportDataHolder, com.itextpdf.text.pdf.PdfWriter)
    * }. When a Runtime, VectorPrint, IO and DocumentException occurs {@link #handleException(java.lang.Exception, java.io.OutputStream)
    * } will be called.
    *
    * @param data
    * @param outputStream
    * @return 0 or {@link #ERRORINREPORT}
    * @throws com.vectorprint.VectorPrintException
    */
   @Override
   public final int generate(RD data, OutputStream out) throws VectorPrintException {
      try {
         DocumentStyler ds = stylerFactory.getDocumentStyler();
         ds.setReportDataHolder(data);

         wasDebug = getSettings().getBooleanProperty(Boolean.FALSE, DEBUG);
         if (ds.getValue(DocumentSettings.TOC, Boolean.class)) {
            out = new TocOutputStream(out, bufferSize, this);
            getSettings().put(DEBUG, Boolean.FALSE.toString());
         }

         if (ds.isParameterSet(DocumentSettings.KEYSTORE)) {
            out = new SigningOutputStream(out, bufferSize, this);
         }

         document = new VectorPrintDocument(eventHelper, stylerFactory, styleHelper);
         writer = PdfWriter.getInstance(document, out);
         styleHelper.setVpd((VectorPrintDocument) document);
         ((VectorPrintDocument) document).setWriter(writer);

         eventHelper.setReportDataHolder(data);
         writer.setPageEvent(eventHelper);
         stylerFactory.setDocument(document, writer);
         stylerFactory.setImageLoader(elementProducer);
         stylerFactory.setLayerManager(elementProducer);

         StylerFactoryHelper.initStylingObject(ds, writer, document, this, elementProducer, settings);
         ds.loadFonts();
         styleHelper.style(document, data, StyleHelper.toCollection(ds));
         document.open();
         if (ds.canStyle(document) && ds.shouldStyle(data, document)) {
            ds.styleAfterOpen(document, data);
         }

         // data from the data collection phase doesn't have to be present
         if (data == null || continueOnDataCollectionMessages(data.getMessages(), document)) {
            createReportBody(document, data, writer);
            /*
             * when using queueing we may have run into failures in the data collection thread
             */
            if (data != null && !data.getData().isEmpty()) {
               Object t = data.getData().poll();
               if (t instanceof Throwable) {
                  throw new VectorPrintException((Throwable) t);
               }
            }
         }
         eventHelper.setLastPage(writer.getCurrentPageNumber());

         if (getSettings().getBooleanProperty(false, DEBUG)) {
            eventHelper.setLastPage(writer.getCurrentPageNumber());
            document.setPageSize(new Rectangle(ItextHelper.mmToPts(297), ItextHelper.mmToPts(210)));
            document.setMargins(5, 5, 5, 5);
            document.newPage();
            eventHelper.setDebugHereAfter(true);
            if (!ds.getValue(DocumentSettings.TOC, Boolean.class)) {
               DebugHelper.appendDebugInfo(writer, document, settings, stylerFactory);
            }
         }

         document.close();
         writer.close();

         return 0;
      } catch (RuntimeException | DocumentException | VectorPrintException | IOException e) {
         return handleException(e, out);
      }
   }

   /**
    * This method will be called when exceptions are thrown in
    * {@link #createReportBody(com.itextpdf.text.Document, com.vectorprint.report.data.ReportDataHolder)} or
    * {@link DebugHelper#appendDebugInfo(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, com.vectorprint.configuration.EnhancedMap, com.vectorprint.report.itext.style.StylerFactory) },
    * it will rethrow the exception by default. If you provide a property {@link ReportConstants#STOPONERROR} with a
    * value of false, the stacktrace will be appended to the pdf and the document and writer will be closed.
    *
    * @param ex
    * @param output the pdf document output stream
    * @return 1
    */
   protected int handleException(Exception ex, OutputStream output) throws VectorPrintRuntimeException {
      if (getSettings().getBooleanProperty(Boolean.TRUE, ReportConstants.STOPONERROR)) {
         throw (ex instanceof VectorPrintRuntimeException)
             ? (VectorPrintRuntimeException) ex
             : new VectorPrintRuntimeException("failed to generate the report: " + ex.getMessage(), ex);
      } else {
         PrintStream out;

         ByteArrayOutputStream bo = new ByteArrayOutputStream();

         out = new PrintStream(bo);
         ex.printStackTrace(out);
         out.close();

         try {
            Font f = FontFactory.getFont(FontFactory.COURIER, 8);

            f.setColor(itextHelper.fromColor(getSettings().getColorProperty(Color.MAGENTA, "debugcolor")));

            String s = getSettings().getProperty(bo.toString(), "renderfault");
            eventHelper.setLastPage(writer.getCurrentPageNumber());
            document.setPageSize(new Rectangle(ItextHelper.mmToPts(297), ItextHelper.mmToPts(210)));
            document.setMargins(5, 5, 5, 5);

            document.newPage();
            eventHelper.setFailuresHereAfter(true);

            document.add(new Chunk("Below you find information that help solving the problems in this report.", f).setLocalDestination(FAILUREPAGE));
            newLine();
            document.add(new Paragraph(new Chunk(s, f)));
            document.newPage();
            DebugHelper.appendDebugInfo(writer, document, settings, stylerFactory);
         } catch (VectorPrintException | DocumentException e) {
            log.severe("Could not append to PDF:\n" + bo.toString());
            log.log(java.util.logging.Level.SEVERE, null, e);
         } finally {
            document.close();
            writer.close();
         }
      }

      return ERRORINREPORT;
   }

   /**
    * Calls {@link #processData(com.vectorprint.report.data.ReportDataHolder) }. If you don't want to use the {@link DataCollector} /
    * {@link ReportDataHolder} mechanism extend this class, override this method and provide it's name in a setting
    * {@link ReportConstants#REPORTCLASS}.
    *
    * @throws com.vectorprint.VectorPrintException
    * @see com.vectorprint.report.itext.annotations.Element
    *
    * @param document
    * @param data may be null, for example when no {@link DataCollector} is used
    * @param writer the value of writer
    * @throws DocumentException
    */
   protected void createReportBody(Document document, RD data, com.itextpdf.text.pdf.PdfWriter writer) throws DocumentException, VectorPrintException {
      processData(data);
   }

   /**
    *
    * @param mm measure in millimeters
    * @return measure in points
    */
   protected float mmToPts(float mm) {
      return ItextHelper.mmToPts(mm);
   }

   public EventHelper<RD> getPageEventHelper() {
      return eventHelper;
   }

   public final void setStylerFactory(StylerFactory stylerFactory) {
      if (stylerFactory != null) {
         eventHelper.setItextStylerFactory(stylerFactory);
         this.stylerFactory = stylerFactory;
         StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(stylerFactory, settings);
         stylerFactory.setDocument(document, writer);
         styleHelper.setStylerFactory(stylerFactory);
      }
   }

   public StylerFactory getItextStylerFactory() {
      return stylerFactory;
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @see ValueHelper
    * @param n
    * @return
    */
   public NumberValue getNumber(int n) {
      return ValueHelper.createNumber(n);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @see ValueHelper
    * @param n
    * @return
    */
   public NumberValue getNumber(long n) {
      return ValueHelper.createNumber(n);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @see ValueHelper
    * @param n
    * @return
    */
   public NumberValue getNumber(double n) {
      return ValueHelper.createNumber(n);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param d
    * @see ValueHelper
    * @return
    */
   public MoneyValue getMoney(double d) {
      return ValueHelper.createMoney(d);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param millis
    * @see ValueHelper
    * @return
    */
   public DateValue getDate(long millis) {
      return ValueHelper.createDate(new Date(millis));
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param d
    * @see ValueHelper
    * @return
    */
   public DateValue getDate(Date d) {
      return ValueHelper.createDate(d);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param txt
    * @see ValueHelper
    * @return
    */
   public TextValue getText(String txt) {
      return ValueHelper.createText(txt);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param p
    * @see ValueHelper
    * @return
    */
   public PercentageValue getPercentage(double p) {
      return ValueHelper.createPercentage(p);
   }

   /**
    * create a value that supports {@link Formatter configurable formatting of datatypes}.
    *
    * @param millis
    * @see ValueHelper
    * @return
    */
   public DurationValue getDuration(long millis) {
      return ValueHelper.createDuration(millis);
   }

   public PeriodValue getPeriod(long start, long end) {
      return ValueHelper.createPeriod(start, end);
   }

   /**
    *
    */
   @Override
   public EnhancedMap getSettings() {
      return settings;
   }

   /**
    * sets settings and calls {@link StylerFactoryHelper#SETTINGS_ANNOTATION_PROCESSOR#initStylingObject(java.lang.Object, com.itextpdf.text.pdf.PdfWriter,
    * com.itextpdf.text.Document, com.vectorprint.report.itext.ImageLoader, com.vectorprint.report.itext.LayerManager, com.vectorprint.configuration.EnhancedMap)
    * } on the helpers (elementProducer, stylerFactory, eventHelper, itextHelper) this class uses.
    *
    * @param settings
    */
   public void setSettings(EnhancedMap settings) {
      bufferSize = settings.getIntegerProperty(ReportConstants.DEFAULTBUFFERSIZE, ReportConstants.BUFFERSIZE);
      this.settings = settings;
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(elementProducer, settings);
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(stylerFactory, settings);
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(eventHelper, settings);
      itextHelper = new ItextHelper();
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(itextHelper, settings);
   }

   @Override
   public Document getDocument() {
      return document;
   }

   @Override
   public PdfWriter getWriter() {
      return writer;
   }

   @Override
   public LayerManager getLayerManager() {
      return elementProducer;
   }

   @Override
   public void loadTiff(InputStream tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadTiff(tiff, imageProcessor, pages);
   }

   @Override
   public void loadTiff(URL tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadTiff(tiff, imageProcessor, pages);
   }

   @Override
   public StyleHelper getStyleHelper() {
      return styleHelper;
   }

   @Override
   public SimpleColumns createColumns(List<? extends BaseStyler> stylers) throws VectorPrintException {
      return elementProducer.createColumns(stylers);
   }

   private int bufferSize = ReportConstants.DEFAULTBUFFERSIZE;

   public void newLine() throws DocumentException {
      newLine(1);
   }

   public void newLine(int n) throws DocumentException {
      if (n < 1) {
         n = 1;
      }
      for (int i = 0; i < n; i++) {
         document.add(Chunk.NEWLINE);
      }
   }

   /**
    *
    * @param <E> the element to be returned
    * @param data the value of data
    * @param elementClass the value of elementClass
    * @param styleClass the value of styleClass
    * @return the element created
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   public <E extends Element> E createElement(Object data, Class<E> elementClass, String... styleClass) throws VectorPrintException, InstantiationException, IllegalAccessException {
      return createElement(data, elementClass, getStylers(styleClass));
   }

   @Override
   public <E extends Element> E createElement(Object data, Class<E> elementClass, List<? extends BaseStyler> stylers) throws VectorPrintException, InstantiationException, IllegalAccessException {
      return elementProducer.createElement(data, elementClass, stylers);
   }

   /**
    *
    * @param <E> the element created and added to the document
    * @param data the value of data
    * @param elementClass the value of elementClass
    * @param styleClass the styleClasses to use for styling
    * @return the element created and added to the document
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws DocumentException
    */
   public <E extends Element> E createAndAddElement(Object data, Class<E> elementClass, String... styleClass) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException {
      return createAndAddElement(data, getStylers(styleClass), elementClass);
   }

   public <E extends Element> E createAndAddElement(Object data, List<? extends BaseStyler> stylers, Class<E> elementClass) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException {
      E e = createElement(data, elementClass, stylers);
      if (e == null) {
         if (stylers != null && !stylers.isEmpty()) {
            if (!(stylers.get(0) instanceof com.vectorprint.report.itext.style.stylers.Image) && stylers.get(0).creates()) {
               throw new VectorPrintRuntimeException(String.format("this styler did not create an element: %s.", stylers.get(0)));
            } else {
               List<com.vectorprint.report.itext.style.stylers.Image> stylers1 = StyleHelper.getStylers(stylers, com.vectorprint.report.itext.style.stylers.Image.class);
               throw new VectorPrintRuntimeException(String.format("Perhaps set Image.%s to true, stylers did not create an element: %s.", com.vectorprint.report.itext.style.stylers.Image.DOSTYLE, stylers1));
            }
         }
      }
      document.add(e);
      return e;
   }

   public Section getIndex(String title, int nesting, String... styleClass) throws VectorPrintException, InstantiationException, IllegalAccessException {
      return getIndex(title, nesting, getStylers(styleClass));
   }

   @Override
   public Section getIndex(String title, int nesting, List<? extends BaseStyler> stylers) throws VectorPrintException, InstantiationException, IllegalAccessException {
      return elementProducer.getIndex(title, nesting, stylers);
   }

   public Section getAndAddIndex(String title, int nesting, String... styleClass) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException {
      return getAndAddIndex(title, nesting, getStylers(styleClass));
   }

   public Section getAndAddIndex(String title, int nesting, List<? extends BaseStyler> stylers) throws VectorPrintException, InstantiationException, IllegalAccessException, DocumentException {
      Section s = getIndex(title, nesting, stylers);
      document.add(s);
      return s;
   }

   @Override
   public String formatValue(Object data) {
      return elementProducer.formatValue(data);
   }

   @Override
   public void loadPdf(URL pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadPdf(pdf, writer, password, imageProcessor, pages);
   }

   @Override
   public Image loadImage(URL image, float opacity) throws VectorPrintException {
      return elementProducer.loadImage(image, opacity);
   }

   @Override
   public Image loadImage(File image, float opacity) throws VectorPrintException {
      return elementProducer.loadImage(image, opacity);
   }

   @Override
   public void loadPdf(InputStream pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadPdf(pdf, writer, password, imageProcessor, pages);
   }

   @Override
   public void loadPdf(File pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadPdf(pdf, writer, password, imageProcessor, pages);
   }

   @Override
   public void loadTiff(File tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadTiff(tiff, imageProcessor, pages);
   }

   @Override
   public void loadPdf(InputStream pdf, PdfWriter writer, Certificate certificate, Key key, String securityProvider, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      elementProducer.loadPdf(pdf, writer, certificate, key, securityProvider, imageProcessor, pages);
   }

   /**
    *
    * @param image the value of image
    * @param opacity the value of opacity
    * @throws VectorPrintException
    */
   @Override
   public Image loadImage(InputStream image, float opacity) throws VectorPrintException {
      return elementProducer.loadImage(image, opacity);
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
   public void setDocument(Document document, PdfWriter writer) {
   }

   @Override
   public void setImageLoader(ImageLoader imageLoader) {
   }

   @Override
   public PdfLayer initLayerGroup(String layerId, PdfContentByte canvas) {
      return elementProducer.initLayerGroup(layerId, canvas);
   }

   @Override
   public PdfLayer startLayerInGroup(String layerId, PdfContentByte canvas) {
      return elementProducer.startLayerInGroup(layerId, canvas);
   }

   @Override
   public void setLayerManager(LayerManager layerManager) {
   }

   @Override
   public Formatter getFormatter() {
      return elementProducer.getFormatter();
   }

   @Override
   public List<BaseStyler> getBaseStylersFromCache(String... styleClasses) throws VectorPrintException {
      return stylerFactory.getBaseStylersFromCache(styleClasses);
   }

   private final DatamappingHelper dmh = new DatamappingHelper();

   /**
    * looks for configuration of data mapping ({@link DatamappingHelper#toDataConfig(java.lang.Class, java.lang.String, com.vectorprint.report.itext.jaxb.Datamappingstype)
    * }) and calls {@link AbstractDatamappingProcessor} methods accordingly.
    *
    * @param dw
    * @param containers
    * @param dmt
    *
    * @throws VectorPrintException
    * @throws DocumentException
    */
   protected void processDataObject(IdData dw, Deque containers, Datamappingstype dmt) throws VectorPrintException, DocumentException {
      Object o = dw.getData();
      Class dataClass = o.getClass();

      if (log.isLoggable(java.util.logging.Level.FINE)) {
         log.log(java.util.logging.Level.FINE, "processing {0}", dataClass.getName());
      }

      try {

         DataMapping dataMapping = dmh.toDataConfig(dataClass, dw.getId(), dmt);

         if (dataMapping != null) {

            if (!dataMapping.getStartcontainer().isEmpty()) {
               for (StartContainerConfig scc : dataMapping.getStartcontainer()) {
                  addContainer(scc, containers, o);
               }
            }

            if (!dataMapping.getElement().isEmpty()) {
               for (ElementConfig ec : dataMapping.getElement()) {
                  addElement(ec, containers, o);
               }
            }

            if (dataMapping.getElementsfromdata() != null) {
               Method m = dataClass.getMethod(dataMapping.getElementsfromdata().getDatalistmethod(), (Class[]) null);
               Object result = m.invoke(o, (Object[]) null);
               if (result instanceof java.util.List) {
                  for (Object lo : (java.util.List) result) {
                     addElement(dataMapping.getElementsfromdata().getElement(), containers, lo);
                  }
               } else {
                  throw new VectorPrintException(String.format("%s not supported as return type, required java.util.List", (result == null) ? "null" : result.getClass().getName()));
               }
            }

            if (dataMapping.getEndcontainer() != null) {
               endContainer(dataMapping.getEndcontainer().getContainertype(), dataMapping.getEndcontainer().getDepthtoend(), containers);
            }

         } else {
            throw new VectorPrintException(String.format("no datamapping configuration found for %s", dataClass.getName()));
         }
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
         throw new VectorPrintException(ex);
      }

   }

   /**
    * Processes the queue in the data holder to call
    * {@link #processDataObject(com.vectorprint.report.data.ReportDataHolder.IdData, java.util.Deque, com.vectorprint.report.itext.jaxb.Datamappingstype)}.
    * For a BlockingQueue stops processing when a {@link QUEUECONTROL#END} is found on the queue.
    *
    * @param dataHolder may be null, for example when no {@link DataCollector} is used
    * @throws VectorPrintException
    * @throws DocumentException
    */
   @Override
   public final void processData(RD dataHolder) throws VectorPrintException, DocumentException {
      if (dataHolder == null) {
         throw new VectorPrintException("No data to process, does your DataCollector return a ReportDataHolder? Or perhaps you should override createReportBody if don't use a DataCollector.");
      }
      Deque containers = new LinkedList();
      Datamappingstype dmt = null;
      if (settings.containsKey(ReportConstants.DATAMAPPINGXML)) {
         try {
            dmt = DatamappingHelper.fromXML(
                new InputStreamReader(
                    settings.getURLProperty(null, ReportConstants.DATAMAPPINGXML).openStream()
                )
            );
         } catch (JAXBException ex) {
            throw new VectorPrintException(ex);
         } catch (MalformedURLException ex) {
            throw new VectorPrintException(ex);
         } catch (IOException ex) {
            throw new VectorPrintException(ex);
         }
      }
      if (dataHolder.getData() instanceof BlockingQueue) {
         try {
            BlockingQueue<IdData> bq = (BlockingQueue<IdData>) dataHolder.getData();
            Object o;
            IdData dw;
            /*
             * wait for data in a blocking way
             */
            while ((dw = bq.take()) != null) {
               o = dw.getData();
               if (QUEUECONTROL.END.equals(o)) {
                  break;
               } else if (o instanceof Throwable) {
                  throw new VectorPrintException((Throwable) o);
               }
               processDataObject(dw, containers, dmt);
            }
         } catch (InterruptedException ex) {
            throw new VectorPrintException(ex);
         }
      } else {
         IdData dw;
         while ((dw = (IdData) dataHolder.getData().poll()) != null) {
            processDataObject(dw, containers, dmt);
         }
      }
      // process any containers not added to the document yet
      if (!containers.isEmpty()) {
         if (containers.getLast() instanceof Element) {
            document.add((Element) containers.getLast());
         } else {
            throw new VectorPrintException(String.format("don't know what to do with container %s", containers.getLast().getClass().getName()));
         }
      }
   }

   /**
    * When messages of level {@link Level error} are present, put these in the document and return false. Otherwise log
    * messages and return true.
    *
    * @param messages
    * @param document
    * @return
    * @throws VectorPrintException
    */
   @Override
   public boolean continueOnDataCollectionMessages(DataCollectionMessages messages, com.itextpdf.text.Document document) throws VectorPrintException {
      if (!messages.getMessages(DataCollectionMessages.Level.ERROR).isEmpty()) {
         try {
            createAndAddElement(messages.getMessages(DataCollectionMessages.Level.ERROR), Phrase.class, "");
         } catch (InstantiationException | IllegalAccessException | DocumentException ex) {
            throw new VectorPrintException(ex);
         }
         return false;
      } else {
         for (DataCollectionMessages.Level l : DataCollectionMessages.Level.values()) {
            for (Object m : messages.getMessages(l)) {
               log.warning(String.format("message of level %s during data collection: %s", l.name(), String.valueOf(m)));
            }
         }
      }
      return true;
   }

   public ItextHelper getItextHelper() {
      return itextHelper;
   }

   public StylerFactory getStylerFactory() {
      return stylerFactory;
   }

   public EventHelper<RD> getEventHelper() {
      return eventHelper;
   }

   public ElementProducer getElementProducer() {
      return elementProducer;
   }

   boolean isWasDebug() {
      return wasDebug;
   }
}
