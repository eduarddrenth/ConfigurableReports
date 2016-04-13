
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
//~--- non-JDK imports --------------------------------------------------------
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.debug.DebugHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.ElementProducing;
import com.vectorprint.report.itext.style.StylerFactory;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.ModeParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * Printing content (text, tables, lists and images) in rectangular columns of equal width with spacing between them.
 * Calculates column dimensions based on some parameter values.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SimpleColumns extends AdvancedImpl<Object> implements ElementProducing {

   private static final Logger log = Logger.getLogger(SimpleColumns.class.getName());
   private List<BaseStyler> stylers = new ArrayList<>(3);

   /**
    * This styler will process images and call {@link #style(java.lang.Object, java.lang.Object)  } using all
    * stylers configured after this styler.
    *
    * @param bs
    */
   public final void addStyler(BaseStyler bs) {
      stylers.add(bs);
   }

   public enum MODE {

      TEXT, COMPOSITE
   }

   public static final String TOP = "top";
   public static final String RIGHT = "right";
   public static final String BOTTOM = "bottom";
   public static final String LEFT = "left";
   public static final String SPACING = "spacing";
   public static final String NUMCOLUMNS = "columns";
   public static final String MODEPARAM = "mode";
   /**
    * name of the param to specify after how many {@link #addContent(java.lang.Object, java.lang.String...) }
    * {@link #write(boolean) write(false)} should be called.
    */
   public static final String WRITECOUNT = "writecount";
   private final java.util.List<Rectangle> columns = new ArrayList<>(2);
   private ColumnText ct = null;
   private StylerFactory stylerFactory;
   private ElementProducer elementProducer;

   public SimpleColumns() {

      addParameter(new IntParameter(WRITECOUNT, "after how many pieces of content added to the columns should we write to the document").setDefault(-1),SimpleColumns.class);
      addParameter(new FloatParameter(LEFT, "left of first column defaults to left margin"),SimpleColumns.class);
      addParameter(new FloatParameter(RIGHT, "right of last column defaults to right margin"),SimpleColumns.class);
      addParameter(new FloatParameter(TOP, "top of the columns defaults to top margin"),SimpleColumns.class);
      addParameter(new FloatParameter(BOTTOM, "bottom columns defaults to bottom margin"),SimpleColumns.class);
      addParameter(new FloatParameter(SPACING, "space between columns").setDefault(ItextHelper.mmToPts(2.5f)),SimpleColumns.class);
      addParameter(new FloatParameter(Spacing.SPACEBEFOREPARAM, "space before columns"),SimpleColumns.class);
      addParameter(new FloatParameter(Spacing.SPACEAFTERPARAM, "space after columns"),SimpleColumns.class);
      addParameter(new IntParameter(NUMCOLUMNS, "number of columns").setDefault(2),SimpleColumns.class);
      addParameter(new ModeParameter(MODEPARAM, "text mode supports Chunk and Phrase, composite also supports PdfPTable, List, Paragraph and Image").setDefault(MODE.TEXT),SimpleColumns.class);
   }

   @Override
   public boolean creates() {
      return true;
   }

   /**
    * calls {@link #getPreparedCanvas() } to create a new {@link ColumnText#ColumnText(com.itextpdf.text.pdf.PdfContentByte)
    * } and calculates column Rectangles based on parameters.
    *
    * @return
    * @throws VectorPrintException
    */
   protected ColumnText prepareColumns() throws VectorPrintException {
      ct = new ColumnText(getPreparedCanvas());
      float width = getRight() - getLeft();
      float colWidth = (width - (getNumColumns() - 1) * getSpacing()) / getNumColumns();
      for (int i = 0; i < getNumColumns(); i++) {
         float left = (i == 0) ? getLeft() : getLeft() + colWidth * i + getSpacing() * i;
         float right = (i == 0) ? getLeft() + colWidth : getLeft() + colWidth * i + getSpacing() * i + colWidth;
         int shift = i * 4;
         columns.add(new Rectangle(left, getValue(BOTTOM, Float.class), right, getValue(TOP, Float.class)));
      }
      return ct;
   }

   private boolean firstWrite = true;
   private int column = 0;
   private float pageTop;
   private float currentY;
   private float minY;
   private int contentWritten;

   /**
    * Calls {@link #write(boolean) } with true;
    *
    * @throws DocumentException
    */
   public SimpleColumns write() throws DocumentException {
      return write(true);
   }

   /**
    * writes out content taking into account the current vertical position in the document and making sure that the next
    * call to {@link Document#add(com.itextpdf.text.Element)} will start at the correct vertical position.
    *
    * @see #getSpaceBefore()
    * @see #getSpaceAfter()
    *
    * @param last when true space will be appended to start adding content at the correct position after the columns
    * @throws DocumentException
    */
   public SimpleColumns write(boolean last) throws DocumentException {
      int status = ColumnText.START_COLUMN;
      ct.setSimpleColumn(columns.get(column));
      if (firstWrite) {
         float top = getWriter().getVerticalPosition(false) > getTop() ? getTop() : getWriter().getVerticalPosition(false);
         pageTop = minY = top - getValue(Spacing.SPACEBEFOREPARAM, Float.class);
         ct.setYLine(pageTop);
         firstWrite = false;
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("first write of columns top for content determined: %s", top - getValue(Spacing.SPACEBEFOREPARAM, Float.class)));
         }
      } else {
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("following write of columns, using current top for content: %s", currentY));
         }
         ct.setYLine(currentY);
      }
      while (ColumnText.hasMoreText(status)) {
         if (getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.DEBUG)) {
            Rectangle rect = new Rectangle(columns.get(column));
            rect.setTop(ct.getYLine());
            DebugHelper.debugRect(ct.getCanvas(), rect, new float[]{2, 2}, 0.3f, getSettings(), elementProducer);
         }
         status = ct.go();
         currentY = ct.getYLine();
         if (ct.getYLine() < minY) {
            minY = ct.getYLine();
         }
         if (ColumnText.hasMoreText(status)) {
            if (column == getNumColumns() - 1) {
               column = 0;
               getDocument().newPage();
               minY = pageTop = currentY = getTop();
               if (log.isLoggable(Level.FINE)) {
                  log.fine(String.format("starting next page for columns"));
               }
            } else {
               column++;
               if (log.isLoggable(Level.FINE)) {
                  log.fine(String.format("going to column %s",column));
               }
            }
            ct.setSimpleColumn(columns.get(column));
            ct.setYLine(pageTop);
         } else {
            contentWritten = addedContent;
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("column content written %s",addedContent));
            }
         }
      }
      if (last) {
         float space = (pageTop - minY < getBottom()) ? 
             getBottom()  + getValue(Spacing.SPACEAFTERPARAM, Float.class) :
             pageTop - minY + getValue(Spacing.SPACEAFTERPARAM, Float.class);
         // add necessary spacing, space otherwise ignored!
         if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("appending %s mm to start following content at the correct position",
                ItextHelper.ptsToMm(space)));
         }
         Paragraph p = new Paragraph(" ");
         p.setSpacingAfter(space);
         getDocument().add(p);
      }
      return this;
   }

   /**
    * The maximum amount of space taken by any column on a page until now, calculated from {@link PdfWriter#getVerticalPosition(boolean)
    * }, taking into account {@link #getSpaceBefore() } if needed. You can use this method after a series of {@link #addContent(java.lang.Object, java.lang.String...)
    * } and {@link #write(boolean) } to know how much vertical spacing is needed before adding other content to the
    * document.
    *
    * @see #getSpaceAfter()
    * @return
    */
   public float getVerticalAdvance() {
      return pageTop - minY;
   }

   /**
    *
    * @return the configured space after the columns
    */
   public float getSpaceAfter() {
      return getValue(Spacing.SPACEAFTERPARAM, Float.class);
   }

   /**
    *
    * @return the configured space before the columns
    */
   public float getSpaceBefore() {
      return getValue(Spacing.SPACEBEFOREPARAM, Float.class);
   }

   @Override
   public <E> E style(E text, Object data) throws VectorPrintException {
      if (text == null) {
         return (E) prepareColumns();
      } else {
         throw new VectorPrintException("don't pass your own ColumnText to this styler");
      }
   }
   private static final Class<Object>[] classes = new Class[]{ColumnText.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   @Override
   public void setDocument(Document document, com.itextpdf.text.pdf.PdfWriter writer) {
      super.setDocument(document, writer);
      setValue(LEFT, document.leftMargin());
      setValue(RIGHT, document.right() - document.rightMargin());
      setValue(BOTTOM, getDocument().bottomMargin());
      setValue(TOP, getDocument().top() - getDocument().topMargin());
   }

   public int getNumColumns() {
      return getValue(NUMCOLUMNS, Integer.class);
   }

   public void setNumColumns(int numColumns) {
      setValue(NUMCOLUMNS, numColumns);
   }

   public float getLeft() {
      return getValue(LEFT, Float.class);
   }

   public void setLeft(float left) {
      setValue(LEFT, left);
   }

   public float getTop() {
      return getValue(TOP, Float.class);
   }

   public void setTop(float top) {
      setValue(TOP, top);
   }

   public float getBottom() {
      return getValue(BOTTOM, Float.class);
   }

   public void setBottom(float bottom) {
      setValue(BOTTOM, bottom);
   }

   public float getRight() {
      return getValue(RIGHT, Float.class);
   }

   public void setRight(float right) {
      setValue(RIGHT, right);
   }

   public float getSpacing() {
      return getValue(SPACING, Float.class);
   }

   public void setSpacing(float spacing) {
      setValue(SPACING, spacing);
   }

   @Override
   public String getHelp() {
      return "Create columns in your report." + " " + super.getHelp();
   }

   public List<Rectangle> getColumns() {
      return Collections.unmodifiableList(columns);
   }

   private int addedContent = 0;

   /**
    * adds content to the columns and calls {@link #write(boolean) write(false)} when {@link #WRITECOUNT} is greater
    * then zero and reached
    *
    * @param data Chunk, Phrase, Image, List, Paragraph, PdfPTable or String
    * @param styleClasses stylers to apply to added content, they will be appended to the stylers defined after
    * the SimpleColumns styler (i.e. the Font in this definition: kols=SimpleColumns(columns=3,mode=composite,writecount=10,opacity=0.7);Font(style=italic))
    * @return
    * @throws VectorPrintException
    */
   public SimpleColumns addContent(Object data, String... styleClasses) throws VectorPrintException, DocumentException {
      if (data != null) {
         List<BaseStyler> l = new ArrayList<>(stylers);
         if (styleClasses!=null) {
            l.addAll(stylerFactory.getStylers(styleClasses));
         }
         if ((data instanceof Element)) {
            elementProducer.getStyleHelper().style(data, null, stylers);
            if (log.isLoggable(Level.FINE)) {
               log.fine(String.format("trying to add %s to columns",data.getClass().getName()));
            }
            if (getValue(MODEPARAM, MODE.class) == MODE.TEXT) {
               if (data instanceof Chunk) {
                  ct.addText((Chunk) data);
               } else if (data instanceof Phrase) {
                  ct.addText((Phrase) data);
               } else {
                  throw new VectorPrintException(String.format("%s not supported in text mode", data.getClass().getName()));
               }
            } else {
               ct.addElement((Element) data);
            }
         } else {
            try {
               if (log.isLoggable(Level.FINE)) {
                  log.fine(String.format("trying to add %s to columns as a Phrase",data));
               }
               if (getValue(MODEPARAM, MODE.class) == MODE.TEXT) {
                  ct.addText(
                      elementProducer.createElement(data, Phrase.class, l));
               } else {
                  ct.addElement(
                      elementProducer.createElement(data, Phrase.class, l));
               }
            } catch (InstantiationException | IllegalAccessException ex) {
               throw new VectorPrintException(ex);
            }
         }
         addedContent++;
         if (getValue(WRITECOUNT, Integer.class) > 0) {
            if (addedContent % getValue(WRITECOUNT, Integer.class) == 0) {
               if (log.isLoggable(Level.FINE)) {
                  log.fine(String.format("writing content of columns to document after %s pieces added", getValue(WRITECOUNT, Integer.class)));
               }
               write(false);
            }
         }
      }
      return this;
   }

   /**
    * indicates whether the next call to {@link #addContent(java.lang.Object, java.lang.String...) } will trigger a call
    * to {@link #write(boolean) write(false) }.
    *
    * @return
    */
   public boolean willWriteAfterNextAdd() {
      return getValue(WRITECOUNT, Integer.class) > 0 && (addedContent + 1) % getValue(WRITECOUNT, Integer.class) == 0;
   }

   @Override
   public void setElementProducer(ElementProducer elementProducer) {
      this.elementProducer = elementProducer;
   }

   @Override
   public void setStylerFactory(StylerFactory stylerFactory) {
      this.stylerFactory = stylerFactory;
   }

   protected ColumnText getColumnText() {
      return ct;
   }

   /**
    * how many times did we call {@link #addContent(java.lang.Object, java.lang.String...) }
    *
    * @return
    */
   public int getAddedContent() {
      return addedContent;
   }

   /**
    * how many pieces of content are written to the document until now.
    *
    * @return
    */
   public int getContentWritten() {
      return contentWritten;
   }

   /**
    * how many pieces of content are left to be written to the document.
    *
    * @return
    */
   public int getContentToBeWritten() {
      return addedContent - contentWritten;
   }

}
