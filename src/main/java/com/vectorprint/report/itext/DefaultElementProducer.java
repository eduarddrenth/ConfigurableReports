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
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.ChapterAutoNumber;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLayer;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.configuration.annotation.Settings;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.itext.debug.DebuggablePdfPCell;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.StyleHelper;
import static com.vectorprint.report.itext.style.StyleHelper.toCollection;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.stylers.Advanced;
import com.vectorprint.report.itext.style.stylers.SimpleColumns;
import com.vectorprint.report.itext.style.stylers.Link;
import com.vectorprint.report.itext.style.stylers.NoWrap;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

//~--- JDK imports ------------------------------------------------------------
/**
 * Responsible for creating and styling parts of the report, for formatting data and adding data to the report part. For
 * styling {@link BaseStyler}s are used that can be found using a {@link StylerFactory}, Formatting data is done using a
 * {@link Formatter}.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DefaultElementProducer implements ElementProducer, LayerManager {

   private static final Logger log = Logger.getLogger(DefaultElementProducer.class.getName());
   /**
    * the suffix used when
    * {@link #startLayerInGroup(java.lang.String, com.itextpdf.text.pdf.PdfContentByte) starting a layer}.
    */
   public static final String CHILD_LAYERSUFFIX = "_child";
   /**
    * prefix for generic tags used for {@link Advanced advanced stylers}.
    *
    * @see PageHelper#addDelayedStyler(java.lang.String, java.util.Collection)
    */
   public static final String ADV = "adv";
   @Setting(key = ReportConstants.DEBUG)
   private boolean debug = false;
   private int genericTag = -1;
   private final Formatter formatter;
   @Settings
   private EnhancedMap settings;
   private EventHelper ph;
   private StyleHelper styleHelper;

   public DefaultElementProducer() {
      this(new Formatter());
   }

   public DefaultElementProducer(Formatter f) {
      this.formatter = f;
   }

   /**
    * leaves object creation to the first styler in the list
    *
    * @param <E>
    * @param stylers
    * @param data
    * @param clazz
    * @return
    * @throws VectorPrintException
    */
   public <E extends Element> E createElementByStyler(Collection<? extends BaseStyler> stylers, Object data, Class<E> clazz) throws VectorPrintException {

      // pdfptable, Section and others do not have a default constructor, a styler creates it
      return styleHelper.style(clazz, data, stylers);
   }

   /**
    * Creates and styles a cell with data in it. If the data is an instance of Element it is added as is to the cell,
    * otherwise a {@link #createPhrase(java.lang.Object, java.util.List) phrase} is created from the data.
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public PdfPCell createCell(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      DebuggablePdfPCell cell;

      /*
       * only when creating an instance of PdfPCell with its content (Phrase/Image/Chunk) in the constructor
       * alignment will work!
       */
      if (null != data) {
         if (data instanceof Phrase) {
            cell = new DebuggablePdfPCell((Phrase) data);
         } else if (data instanceof Chunk) {
            cell = new DebuggablePdfPCell(new Phrase((Chunk) data));
         } else if (data instanceof Image) {
            cell = new DebuggablePdfPCell((Image) data);
         } else if (data instanceof Element) {
            if (data instanceof PdfPTable) {
               cell = new DebuggablePdfPCell((PdfPTable) data);
            } else {
               throw new VectorPrintException(String.format("%s not supported for a cell", data.getClass().getName()));
            }
         } else {

            //
            cell = new DebuggablePdfPCell(createPhrase(data, stylers));
         }
      } else {

         //
         cell = new DebuggablePdfPCell();
      }

      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(cell, settings);

      return styleHelper.style(cell, data, stylers);
   }

   /**
    * Create a piece of text (part of a Phrase), style it and ad the data.
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public Chunk createChunk(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      Chunk c = styleHelper.style(new Chunk(), data, stylers);

      if (data != null) {
         c.append(formatValue(data));
      }

      if (notDelayedStyle(c, ADV + (++advancedTag), stylers) && debug) {
         c.setGenericTag(String.valueOf(++genericTag));
      }

      return c;
   }

   /**
    * Create a Phrase, style it and add the data
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public Phrase createPhrase(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      return initTextElementArray(styleHelper.style(new Phrase(), data, stylers), data, stylers);
   }

   /**
    * Create a Paragraph, style it and add the data
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public Paragraph createParagraph(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      return initTextElementArray(styleHelper.style(new Paragraph(), data, stylers), data, stylers);
   }

   /**
    * Create a Anchor, style it and add the data
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public Anchor createAnchor(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      return initTextElementArray(styleHelper.style(new Anchor(), data, stylers), data, stylers);
   }

   /**
    * Create a ListItem, style it and add the data
    *
    * @param data
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   public ListItem createListItem(Object data, Collection<? extends BaseStyler> stylers) throws VectorPrintException {
      return initTextElementArray(styleHelper.style(new ListItem(), data, stylers), data, stylers);
   }

   private <P extends TextElementArray> P initTextElementArray(P text, Object data, Collection<? extends BaseStyler> stylers) {
      if (data != null) {
         text.add(new Chunk(formatValue(data)));
      }

      boolean first = true;
      for (Chunk c : (List<Chunk>) text.getChunks()) {
         styleLink(stylers, first, c, data);
         if (first) {
            first = false;
         }
         if (notDelayedStyle(c, ADV + (++advancedTag), stylers) && debug) {
            c.setGenericTag(String.valueOf(++genericTag));
         }
      }

      return text;
   }

   private void styleLink(Collection<? extends BaseStyler> stylers, boolean first, Chunk c, Object data) {
      if (stylers != null && !stylers.isEmpty()) {
         try {
            Collection<Link> l = StyleHelper.getStylers(stylers, Link.class);
            for (Link link : l) {
               if (first && link.isParameterSet(Link.ANCHOR)) {
                  link.style(c, data);
               } else if (link.isParameterSet(Link.GOTO) || link.isParameterSet(com.vectorprint.report.itext.style.stylers.Image.URLPARAM)) {
                  link.style(c, data);
               }
            }
         } catch (VectorPrintException ex) {
            log.log(Level.WARNING, null, ex);
         }
      }
   }

   private int advancedTag = -1;

   int getAdvancedTag() {
      return advancedTag;
   }

   /**
    * register advanced stylers together with data(part) with the EventHelper to do the styling later
    *
    * @param c
    * @param data
    * @param stylers
    * @return
    * @see PageHelper#onGenericTag(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document,
    * com.itextpdf.text.Rectangle, java.lang.String)
    */
   private boolean notDelayedStyle(Chunk c, String gt, Collection<? extends BaseStyler> stylers) {
      if (stylers == null) {
         return true;
      }
      try {
         Collection<Advanced> a = new ArrayList<Advanced>(stylers.size());
         for (Advanced adv : StyleHelper.getStylers(stylers, Advanced.class)) {
            Advanced.EVENTMODE mode = adv.getEventmode();
            if (Advanced.EVENTMODE.ALL.equals(mode) || Advanced.EVENTMODE.TEXT.equals(mode)) {
               // remember data and chunk
               adv.addDelayedData(gt, c);
               a.add(adv);
            }
         }
         if (a.size() > 0) {
            styleHelper.delayedStyle(c, gt, a, ph);
            return false;
         }
      } catch (VectorPrintException ex) {
         log.log(Level.SEVERE, null, ex);
      }
      return true;
   }

   /**
    * Calls {@link #createTableCell(java.lang.Object, java.util.Collection, boolean) }
    *
    * @param val
    * @param style
    * @param noWrap
    * @return
    * @throws com.vectorprint.VectorPrintException
    */
   public PdfPCell createTableCell(Object val, BaseStyler style, boolean noWrap) throws VectorPrintException {
      return createTableCell(val, toCollection(style), noWrap);
   }

   /**
    * When noWrap is true a {@link NoWrap} is added to the stylers. Calls
    * {@link #createCell(java.lang.Object, java.util.Collection) }
    *
    * @param val
    * @param stylers
    * @param noWrap
    * @return
    * @throws com.vectorprint.VectorPrintException
    */
   public PdfPCell createTableCell(Object val, Collection<BaseStyler> stylers, boolean noWrap) throws VectorPrintException {
      if (noWrap) {
         stylers.add(new NoWrap());
      }

      return createCell(val, stylers);
   }

   /**
    * When data is an instance of {@link ReportValue},  {@link Formatter#formatValue(com.vectorprint.itextreport.datatypes.ReportValue)
    * } is called, otherwise String.valueOf is used.
    *
    * @param data
    * @return
    */
   @Override
   public String formatValue(Object data) {
      if (data instanceof ReportValue) {
         return formatter.formatValue((ReportValue) data);
      } else {
         return String.valueOf(data);
      }
   }

   @Override
   public void loadPdf(URL pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      try {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("loading pdf from %s", String.valueOf(pdf)));
         }
         loadPdf(pdf.openStream(), writer, password, imageProcessor, pages);
      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", pdf.toString()), ex);
      }
   }

   /**
    * loads an image using {@link Toolkit#getImage(java.net.URL) }
    *
    * @param image
    * @param opacity the value of opacity
    * @throws VectorPrintException
    * @return the com.itextpdf.text.Image
    */
   @Override
   public Image loadImage(URL image, float opacity) throws VectorPrintException {
      try {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("loading image from %s", String.valueOf(image)));
         }
         BufferedImage awtim = makeImageTranslucent(ImageIO.read(image), opacity);
         Image img = Image.getInstance(awtim, null);
         return img;
      } catch (BadElementException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", image.toString()), ex);
      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", image.toString()), ex);
      }
   }

   public static BufferedImage makeImageTranslucent(BufferedImage source, float opacity) {
      BufferedImage translucent = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TRANSLUCENT);
      Graphics2D g = translucent.createGraphics();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
      g.drawImage(source, null, 0, 0);
      g.dispose();
      return translucent;
   }

   @Override
   public void loadPdf(InputStream pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      BufferedInputStream in = null;
      try {
         in = new BufferedInputStream(pdf, settings.getIntegerProperty(ReportConstants.BUFFERSIZE, ReportConstants.DEFAULTBUFFERSIZE));
         PdfReader reader = new PdfReader(in, password);
         if (pages == null) {
            for (int i = 0; i < reader.getNumberOfPages();) {
               imageProcessor.processImage(Image.getInstance(writer.getImportedPage(reader, ++i)));
               writer.freeReader(reader);
            }
         } else {
            for (int i : pages) {
               imageProcessor.processImage(Image.getInstance(writer.getImportedPage(reader, i)));
               writer.freeReader(reader);
            }
         }
      } catch (BadElementException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", pdf.toString()), ex);
      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", pdf.toString()), ex);
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException ex) {
            }
         }
      }
   }

   @Override
   public void loadPdf(InputStream pdf, PdfWriter writer, Certificate certificate, Key key, String securityProvider, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      // first download, then load
      BufferedInputStream in = null;
      File f = null;
      try {
         f = File.createTempFile("pdf.", "pdf");
         f.deleteOnExit();
         IOHelper.load(pdf, new FileOutputStream(f), ReportConstants.DEFAULTBUFFERSIZE, true);
         PdfReader reader = new PdfReader(f.getPath(), certificate, key, securityProvider);
         if (pages == null) {
            for (int i = 0; i < reader.getNumberOfPages();) {
               imageProcessor.processImage(Image.getInstance(writer.getImportedPage(reader, ++i)));
               writer.freeReader(reader);
            }
         } else {
            for (int i : pages) {
               imageProcessor.processImage(Image.getInstance(writer.getImportedPage(reader, i)));
               writer.freeReader(reader);
            }
         }
      } catch (BadElementException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", pdf.toString()), ex);
      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", pdf.toString()), ex);
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException ex) {
            }
         }
         if (f != null) {
            f.delete();
         }
      }
   }

   /**
    *
    * @param image the value of image
    * @param opacity the value of opacity
    * @throws VectorPrintException
    */
   @Override
   public Image loadImage(InputStream image, float opacity) throws VectorPrintException {
      try {
         BufferedImage awtim = makeImageTranslucent(ImageIO.read(image), opacity);
         Image img = Image.getInstance(awtim, null);
         return img;
      } catch (BadElementException ex) {
         throw new VectorPrintException(ex);
      } catch (IOException ex) {
         throw new VectorPrintException(ex);
      }
   }

   @Override
   public <E extends Element> E createElement(Object data, Class<E> elementClass, List<? extends BaseStyler> stylers)
       throws VectorPrintException, InstantiationException, IllegalAccessException {
      if (PdfPCell.class.equals(elementClass)) {
         return (E) createCell(data, stylers);
      } else if (Chunk.class.equals(elementClass)) {
         return (E) createChunk(data, stylers);
      } else if (Phrase.class.isAssignableFrom(elementClass)) {
         return (E) initTextElementArray(styleHelper.style((Phrase) elementClass.newInstance(), data, stylers), data, stylers);
      } else if (PdfPTable.class.equals(elementClass)) {
         return createElementByStyler(stylers, data, elementClass);
      } else if (Image.class.equals(elementClass)) {
         return createElementByStyler(stylers, data, elementClass);
      } else if (ChapterAutoNumber.class.equals(elementClass)) {
         ChapterAutoNumber can = new ChapterAutoNumber(formatValue(data));
         return (E) styleHelper.style(can, data, stylers);
      }

      return styleHelper.style(elementClass.newInstance(), data, stylers);
   }

   private final Map<Integer, List<Section>> sections = new HashMap<Integer, List<Section>>(10);

   public void clearSections() {
      sections.clear();
   }

   /**
    * create the Section, style the title, style the section and return the styled section.
    *
    * @param title
    * @param nesting
    * @param stylers
    * @return
    * @throws VectorPrintException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   @Override
   public Section getIndex(String title, int nesting, List<? extends BaseStyler> stylers) throws VectorPrintException, InstantiationException, IllegalAccessException {
      if (nesting < 1) {
         throw new VectorPrintException("chapter numbering starts with 1, wrong number: " + nesting);
      }
      if (sections.get(nesting) == null) {
         sections.put(nesting, new ArrayList<Section>(10));
      }
      Section current;
      if (nesting == 1) {
         List<Section> chapters = sections.get(1);
         current = new Chapter(createElement(title, Paragraph.class, stylers), chapters.size() + 1);
         chapters.add(current);
      } else {
         List<Section> parents = sections.get(nesting - 1);
         Section parent = parents.get(parents.size() - 1);
         current = parent.addSection(createParagraph(title, stylers));
         sections.get(nesting).add(current);
      }
      return styleHelper.style(current, null, stylers);
   }

   void setPh(EventHelper ph) {
      this.ph = ph;
   }

   private final Map<String, PdfLayer> layerGroups = new HashMap<String, PdfLayer>(2);

   @Override
   public PdfLayer startLayerInGroup(String groupId, PdfContentByte canvas) {
      PdfLayer pl;
      try {
         pl = new PdfLayer(groupId + CHILD_LAYERSUFFIX, canvas.getPdfWriter());
      } catch (IOException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
      initLayerGroup(groupId, canvas).addChild(pl);
      canvas.beginLayer(pl);
      return pl;
   }

   @Override
   public PdfLayer initLayerGroup(String layerId, PdfContentByte canvas) {
      if (!layerGroups.containsKey(layerId)) {
         PdfLayer parent;
         try {
            parent = new PdfLayer(layerId, canvas.getPdfWriter());
         } catch (IOException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
         layerGroups.put(layerId, parent);
         canvas.beginLayer(parent);
         canvas.endLayer();
      }
      return layerGroups.get(layerId);
   }

   public void setStyleHelper(StyleHelper styleHelper) {
      this.styleHelper = styleHelper;
   }

   @Override
   public void loadTiff(InputStream tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      BufferedInputStream in = null;
      File f = null;
      RandomAccessFileOrArray ra = null;
      try {
         f = File.createTempFile("tiff.", "tiff");
         f.deleteOnExit();
         IOHelper.load(tiff, new FileOutputStream(f), ReportConstants.DEFAULTBUFFERSIZE, true);
         ra = new RandomAccessFileOrArray(f.getPath());
         int p = TiffImage.getNumberOfPages(ra);
         if (pages == null) {
            for (int i = 0; i < p;) {
               imageProcessor.processImage(TiffImage.getTiffImage(ra, ++i));
               if (p > 1) {
                  ra.close();
                  ra = new RandomAccessFileOrArray(f.getPath());
               }
            }
         } else {
            for (int i : pages) {
               imageProcessor.processImage(TiffImage.getTiffImage(ra, i));
               if (p > 1) {
                  ra.close();
                  ra = new RandomAccessFileOrArray(f.getPath());
               }
            }
         }

      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load tiff %s", tiff.toString()), ex);
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException ex) {
            }
         }
         if (ra != null) {
            try {
               ra.close();
            } catch (IOException ex) {
            }
         }
         if (f != null) {
            f.delete();
         }
      }
   }

   @Override
   public void loadTiff(URL tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException {
      try {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("loading tiff from %s", String.valueOf(tiff)));
         }
         loadTiff(tiff.openStream(), imageProcessor, pages);
      } catch (IOException ex) {
         throw new VectorPrintException(String.format("unable to load image %s", tiff.toString()), ex);
      }
   }

   @Override
   public Formatter getFormatter() {
      return formatter;
   }

   @Override
   public StyleHelper getStyleHelper() {
      return styleHelper;
   }

   /**
    * Creates a ColumnText, adds the data using addText and returns the {@link SimpleColumns} that can be used to
    * {@link SimpleColumns#write() write out} or to {@link SimpleColumns#addText(java.lang.Object) add more data} to the
    * document.
    *
    * @param stylers
    * @return
    * @throws VectorPrintException
    */
   @Override
   public SimpleColumns createColumns(List<? extends BaseStyler> stylers) throws VectorPrintException {
      ColumnText mct = styleHelper.style(ColumnText.class, null, stylers);
      SimpleColumns sc = StyleHelper.getStylers(stylers, SimpleColumns.class).get(0);
      return sc;
   }

   public void setSettings(EnhancedMap settings) {
      this.settings = settings;
      StylerFactoryHelper.SETTINGS_ANNOTATION_PROCESSOR.initSettings(formatter, settings);
   }

}
