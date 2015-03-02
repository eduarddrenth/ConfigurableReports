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
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.EventHelper;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.LayerManagerAware;
import com.vectorprint.report.itext.style.DefaultStylerFactory;

import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.StylingCondition;
import com.vectorprint.report.itext.style.parameters.BlendParameter;
import com.vectorprint.report.itext.style.parameters.EventModeParameter;
import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * an advanced styler for directly drawing on a canvas, can be used from setup in two ways: draw (conditionally) on each
 * page, draw after a certain Chunk is drawn. Subclasses that {@link ElementStyler#creates() can create elements} can as
 * well be used in combination with the {@link ElementProducer} methods to add elements to the document. Furthermore an
 * advanced styler can be used programmatically, use the constructor with arguments for this and the
 * {@link #draw(com.itextpdf.text.Rectangle, java.lang.String)} method. Like all stylers setup of these stylers can be
 * done from properties, see {@link AbstractStyler#setup(java.util.Map, java.util.Map)}
 * }.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class AdvancedImpl<DATATYPE> extends AbstractStyler implements Advanced<DATATYPE>, LayerManagerAware {

   public static final String SHIFTX = "shiftx";
   public static final String SHIFTY = "shifty";
   public static final String BLEND = "blend";
   public static final String DATA = "data";
   public static final String DYNAMICDATA = "dynamicdata";
   public static final String WHICHEVENT = "whichevent";
   public static final String USEPADDING = "usepadding";
   private LayerManager layerManager;

   @Override
   public void setLayerManager(LayerManager layerManager) {
      this.layerManager = layerManager;
   }

   public static final String LAYERNAME = "layername";
   public static final String TRANSFORM = "transform";
   public static final String BACKGROUND = "background";
   public static final String OPACITY = "opacity";
   private Map<String, DelayedData> delayedData = new HashMap<String, DelayedData>(10);
   private DATATYPE data;

   public AdvancedImpl() {
      initParams();
   }

   private void initParams() {
      addParameter(new BooleanParameter(BACKGROUND, "draw on background"));
      addParameter(new BlendParameter(BLEND, "how to blend layers: " + Arrays.asList(BLENDMODE.values()))
          .setDefault(BLENDMODE.NORMAL));
      addParameter(new FloatParameter(OPACITY, "0 for fully transparent, 1 for fully opace", false).setDefault(1f));
      addParameter(new FloatArrayParameter(TRANSFORM, "transformation matrix (6 values: "
          + Arrays.asList(TRANSFORMMATRIX.values()).toString() + ")", false));
      addParameter(new FloatParameter(SHIFTX, "how much do we shift x from a known position"));
      addParameter(new FloatParameter(SHIFTY, "how much do we shift y from a known position"));
      addParameter(new StringParameter(LAYERNAME, "name of the layer to use"));
      addParameter(new StringParameter(DATA, "Textual data, input for conversion to: " + AdvancedImpl.class.getTypeParameters()[0].getName()));
      addParameter(new BooleanParameter(DYNAMICDATA, "when true override (static) DATA with (dynamic) data added to the report"));
      addParameter(new EventModeParameter(WHICHEVENT, "which event to use this styler for " + Arrays.asList(EVENTMODE.values())).setDefault(EVENTMODE.TEXT));
      addParameter(new BooleanParameter(USEPADDING, "when true take padding of a cell into account"));
   }

   public AdvancedImpl(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      StylerFactoryHelper.initStylingObject(this, writer, document, null, layerManager, settings);
      initParams();
   }
   private boolean needRestore = false;

   /**
    * get a canvas for drawing, prepared according to settings
    *
    * @see #draw(com.itextpdf.text.pdf.PdfContentByte)
    * @return
    */
   @Override
   public PdfContentByte getPreparedCanvas() {
      return getPreparedCanvas(getOpacity());
   }
   /**
    * get a canvas for drawing, prepared according to settings
    *
    * @see #draw(com.itextpdf.text.pdf.PdfContentByte)
    * @return
    */
   protected final PdfContentByte getPreparedCanvas(float opacity) {
      needRestore = false;
      PdfContentByte canvas = (tableForeground != null) ? tableForeground : (isBg())
          ? getWriter().getDirectContentUnder()
          : getWriter().getDirectContent();
      if (getWriter().getPDFXConformance() == PdfWriter.PDFX1A2001) {
         // check blend, opacity, layers
         if (!PdfGState.BM_NORMAL.equals(getBlend().getBlend()) && !PdfGState.BM_COMPATIBLE.equals(getBlend().getBlend())) {
            throw new VectorPrintRuntimeException("blend not supported in PDF/X-1a: " + getBlend());
         }
         if (getLayerName() != null) {
            throw new VectorPrintRuntimeException("layers not supported in PDF/X-1a: " + getLayerName());
         }
         if (opacity < 1) {
            throw new VectorPrintRuntimeException("opacity not supported in PDF/X-1a: " + opacity);
         }
      }
      if (getLayerName() != null) {
         layerManager.startLayerInGroup(getLayerName(), canvas);
      }
//					 pgs.setAlphaIsShape(true);
      if (opacity <= 1) {
//								PdfShading shading = PdfShading.simpleAxial(getWriter(), 0, 0, getDocument().right() - getDocument().getPageSize().getWidth() * 0.6f, getDocument().top() - getDocument().getPageSize().getHeight() * 0.6f, Color.green, Color.orange,true,true);
//								canvas.paintShading(shading);
         canvas.saveState();
         needRestore = true;
         PdfGState pgs = new PdfGState();
         pgs.setFillOpacity(opacity);
         pgs.setStrokeOpacity(opacity);
         canvas.setGState(pgs);
      }
      if (!BLENDMODE.NORMAL.equals(getBlend())) {
         if (!needRestore) {
            canvas.saveState();
            needRestore = true;
         }
         PdfGState pgs = new PdfGState();
         pgs.setBlendMode(getBlend().getBlend());
         canvas.setGState(pgs);
      }
      if (getTransform() != null && !(this instanceof Image)) {
         canvas.transform(new AffineTransform(getTransform()));
      }
      return canvas;
   }

   @Override
   public void resetCanvas(PdfContentByte canvas) {
      if (needRestore) {
         canvas.restoreState();
         needRestore = false;
      }
      if (getLayerName() != null) {
         canvas.endLayer();
      }
   }

   @Override
   public void setup(Map<String, String> args, Map<String, String> settings) {
      super.setup(args, settings);
      if (args.containsKey(DATA)) {
         data = convert(getValue(DATA, String.class));
      }
   }

   /**
    * when {@link #DYNAMICDATA} is true call {@link #setData(java.lang.Object) } with {@link #convert(java.lang.Object)
    * }.
    *
    * @param <E>
    * @param element the element to be returned
    * @param data
    * @return
    * @throws VectorPrintException
    */
   @Override
   public <E> E style(E element, Object data) throws VectorPrintException {
      if (getValue(DYNAMICDATA, Boolean.class)) {
         setData(convert(data));
      }
      return element;
   }

   /**
    * returns false when a condition is present for which {@link StylingCondition#shouldNotDraw(java.lang.Object) }
    * is true
    *
    * @param data
    * @return
    */
   @Override
   public boolean shouldDraw(Object data) {
      for (StylingCondition sc : getConditions()) {
         if (sc.shouldNotDraw(data)) {
            return false;
         }
      }
      return true;
   }

   /**
    * called when a {@link EVENTMODE document event} or when {@link DefaultStylerFactory#PAGESTYLERS a page is written}.
    *
    * @see EventHelper#addDelayedStyler(java.lang.String, java.util.Collection, com.itextpdf.text.Chunk,
    * com.itextpdf.text.Rectangle)
    * @see EVENTMODE
    * @param rect the rectangle where the text was printed
    * @param genericTag event that was fired
    * @throws VectorPrintException
    */
   @Override
   public void draw(Rectangle rect, String genericTag) throws VectorPrintException {
   }

   public float getOpacity() {
      return getValue(OPACITY, Float.class);
   }

   public void setOpacity(float opacity) {
      setValue(OPACITY, opacity);
   }

   public boolean isBg() {
      return getValue(BACKGROUND, Boolean.class);
   }

   public void setBg(boolean bg) {
      setValue(BACKGROUND, bg);
   }

   public float[] getTransform() {
      return ArrayHelper.unWrap(getValue(TRANSFORM, Float[].class));
   }

   public void setTransform(Float[] transform) {
      setValue(TRANSFORM, transform);
   }
   private static final Class<Object>[] classes = new Class[]{Element.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   private final DelayedData dd = new DelayedData(null);

   /**
    * can be used by subclasses as input for {@link #draw(com.itextpdf.text.Rectangle, String) }
    *
    * @param genericTag
    * @return
    */
   @Override
   public DelayedData getDelayed(String genericTag) {
      DelayedData d = delayedData.get(genericTag);
      return d == null ? dd : d;
   }

   @Override
   public void addDelayedData(String genericTag, Chunk c) {
      delayedData.put(genericTag, new DelayedData(c));
   }

   /**
    * adaptation of X coordinate to be used when x is known from the previous addition of an iText element
    *
    * @return
    */
   public float getShiftx() {
      return getValue(SHIFTX, Float.class);
   }

   /**
    * adaptation of X coordinate to be used when x is known from the previous addition of an iText element
    *
    * @return
    */
   public void setShiftx(float shiftx) {
      setValue(SHIFTX, shiftx);
   }

   /**
    * adaptation of y coordinate to be used when y is known from the previous addition of an iText element
    *
    * @return
    */
   public float getShifty() {
      return getValue(SHIFTY, Float.class);
   }

   /**
    * adaptation of y coordinate to be used when y is known from the previous addition of an iText element
    *
    * @param shifty
    */
   public void setShifty(float shifty) {
      setValue(SHIFTY, shifty);
   }

   @Override
   public String getHelp() {
      return "Supports advanced effects (transform, transparency, etc.) and direct drawing from code. " + super.getHelp();
   }

   public BLENDMODE getBlend() {
      return getValue(BLEND, BLENDMODE.class);
   }

   public void setBlend(BLENDMODE blend) {
      setValue(BLEND, blend);
   }

   /**
    * Return data directly set through {@link #setData(java.lang.Object) } or by calling
    * {@link #convert(java.lang.Object) } on data provided in {@link #DATA the data parameter}
    *
    * @return
    */
   @Override
   public final DATATYPE getData() {
      return (data != null) ? data : convert(getValue(DATA, String.class));
   }

   @Override
   public void setData(DATATYPE data) {
      this.data = data;
   }

   /**
    * this implementation returns the argument casting it to DATATYPE
    *
    * @param s
    * @return
    */
   @Override
   public DATATYPE convert(Object s) {
      return (DATATYPE) s;
   }

   protected LayerManager getLayerManager() {
      return layerManager;
   }

   public String getLayerName() {
      return getValue(LAYERNAME, String.class);
   }

   public void setLayerName(String layerName) {
      setValue(LAYERNAME, layerName);
   }

   /**
    * Calls {@link #draw(com.itextpdf.text.Rectangle, java.lang.String) } with null as genericTag.
    *
    * @param table
    * @param widths
    * @param heights
    * @param headerRows
    * @param rowStart
    * @param canvases
    */
   @Override
   public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvases) {
      final int footer = widths.length - table.getFooterRows();
      final int header = table.getHeaderRows();
      int columns = widths[header].length - 1;
      float w = widths[header][columns];
      try {
         tableForeground = (isBg()) ? canvases[PdfPTable.BASECANVAS] : canvases[PdfPTable.TEXTCANVAS];
         draw(new Rectangle(widths[header][0], heights[footer - 1], w, heights[header]), null);
         tableForeground = null;
      } catch (VectorPrintException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   /**
    * Calls {@link #draw(com.itextpdf.text.Rectangle, java.lang.String) } with null as genericTag, when
    * {@link #USEPADDING} is true, calculate a rectangle taking padding into account before calling {@link #draw(com.itextpdf.text.Rectangle, java.lang.String)
    * }. When {@link #isBg() } is true {@link PdfPTable#BASECANVAS} is used for drawing, otherwise
    * {@link PdfPTable#TEXTCANVAS}.
    *
    * @param cell
    * @param position
    * @param canvases
    */
   @Override
   public final void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
      try {
         tableForeground = (isBg()) ? canvases[PdfPTable.BASECANVAS] : canvases[PdfPTable.TEXTCANVAS];
         Rectangle box = (getValue(USEPADDING, Boolean.class)) ? new Rectangle(
             position.getLeft() + cell.getPaddingLeft(),
             position.getBottom() + cell.getPaddingBottom(),
             position.getRight() - cell.getPaddingRight(),
             position.getTop() - cell.getPaddingTop()) : position;
         draw(box, null);
         tableForeground = null;
      } catch (VectorPrintException ex) {
         throw new VectorPrintRuntimeException(ex);
      }
   }

   private PdfContentByte tableForeground = null;

   @Override
   public EVENTMODE getEventmode() {
      return getValue(WHICHEVENT, EVENTMODE.class);
   }

}
