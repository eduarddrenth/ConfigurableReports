
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
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.IntArrayParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.ImageProcessor;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.conditions.NumberCondition;
import java.net.URL;
import java.util.ArrayList;

//~--- JDK imports ------------------------------------------------------------
/**
 * import pages from a tiff image near a certain element
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters(
    parameters = {
       @Param(
           key = NumberCondition.NUMBERS,
           help = "numbers of the pages to import",
           clazz = IntArrayParameter.class),
       @Param(
           key = ImportTiff.NOFOOTER,
           help = "When true don't print footer on added pages. Requires separatepage to be true.",
           clazz = BooleanParameter.class,
           defaultValue = "true"),
       @Param(
           key = ImportTiff.SEPARATE_PAGE,
           help = "When true imported images will be written out on a seperate page each.",
           clazz = BooleanParameter.class,
           defaultValue = "true"),
       @Param(
           key = ImportTiff.FINALNEWPAGE,
           help = "When true a newpage will be started after importing.",
           clazz = BooleanParameter.class,
           defaultValue = "true"),
       @Param(
           key = ImportTiff.FITTOPAGE,
           help = "When true imported images will be sized to fit the page. Requires separatepage to be true.",
           clazz = BooleanParameter.class,
           defaultValue = "false"),
       @Param(
           key = ImportTiff.FITTOIMAGE,
           help = "When true pages will be sized to fit the image. Requires separatepage to be true.",
           clazz = BooleanParameter.class,
           defaultValue = "true"),
       @Param(
           key = ImportTiff.DRAWDIRECT,
           help = "When true images will be drawn instead of added to the document.",
           clazz = BooleanParameter.class,
           defaultValue = "true")
    })
public class ImportTiff extends Image<Object> implements ImageProcessor {

   /**
    * Parameter key to configure printing footers when importing as separate pages.
    */
   public static final String NOFOOTER = "nofooter";
   /**
    * Parameter key to configure a new page after importing.
    */
   public static final String FINALNEWPAGE = "finalnewpage";
   /**
    * Parameter key to configure printing importing as separate pages.
    */
   public static final String SEPARATE_PAGE = "separatepage";
   /**
    * Parameter key to configure sizing images to pages when importing as separate pages.
    */
   public static final String FITTOPAGE = "fittopage";
   /**
    * Parameter key to configure sizing pages to images when importing as separate pages.
    */
   public static final String FITTOIMAGE = "fittoimage";
   /**
    * Parameter key to configure whether to {@link AbstractPositioning#draw(com.itextpdf.text.Rectangle, java.lang.String) } or {@link Document#add(com.itextpdf.text.Element) }.
    */
   public static final String DRAWDIRECT = "drawdirect";

   private final java.util.List<BaseStyler> stylers = new ArrayList<BaseStyler>(3);

   public ImportTiff() {
   }

   @Override
   public String getHelp() {
      return "Import images from tiff (or pdf via ImportPdf) near a certain element. " + super.getHelp();
   }
   
   private com.itextpdf.text.Image imageBeingProcessed = null;

   protected void drawOnPage(com.itextpdf.text.Image img) throws VectorPrintException {
      /*
       * we call draw here to use the positioning and shadow intelligence of the AbstractPositining styler.
       * this styler uses the top left corner of the argument rectangle as its positioning base.
       * therefore we call this method with the image rectangle moved down by its height, the effect is
       * that the image will be positioned as expected.
       */
      draw(new Rectangle(img.getLeft(), img.getBottom() - img.getHeight(), img.getRight(), img.getBottom()), null);
   }

   protected void addToDocument(com.itextpdf.text.Image img) throws VectorPrintException {
      try {
         getDocument().add(img);
      } catch (DocumentException ex) {
         throw new VectorPrintException(ex);
      }
   }

   /**
    * applies settigns to the image and after this adds the image to the report based on
    * {@link #SEPARATE_PAGE}, {@link #FITTOIMAGE}, {@link #FITTOPAGE}, {@link #NOFOOTER} and {@link #DRAWDIRECT}.
    *
    * @param img
    * @throws VectorPrintException
    */
   @Override
   public final void processImage(com.itextpdf.text.Image img) throws VectorPrintException {
      applySettings(img);
      for (BaseStyler bs : stylers) {
         // these stylers are configured in the setup after this importing stylers, apply here
         if (bs.canStyle(img) && bs.shouldStyle(data, img)) {
            bs.style(img, data);
         }
      }
      
      if (getValue(SEPARATE_PAGE, Boolean.class)) {
         if (getValue(FITTOIMAGE, Boolean.class)) {
            getDocument().setPageSize(new Rectangle(img.getScaledWidth(), img.getScaledHeight()));
         } else if (getValue(FITTOPAGE, Boolean.class)) {
            img.scaleToFit(getDocument().getPageSize().getWidth(),getDocument().getPageSize().getHeight());
         }
         if (getValue(NOFOOTER, Boolean.class)) {
            getSettings().put(ReportConstants.PRINTFOOTER, "false");
         }
         getDocument().newPage();
      }
      if (getValue(DRAWDIRECT, Boolean.class)) {
         this.imageBeingProcessed = img;
         drawOnPage(imageBeingProcessed);
         this.imageBeingProcessed = null;
      } else {
         addToDocument(img);
      }
   }
   protected Object data = null;

   /**
    * calls {@link #processImage(com.itextpdf.text.Image) } on pages imported from the tiff in the URL, always returns
    * null, because each page from a tiff is imported as an image.
    *
    * @param canvas
    * @param data
    * @param opacity the value of opacity
    * @throws VectorPrintException
    * @throws BadElementException
    * @return the com.itextpdf.text.Image
    */
   @Override
   protected com.itextpdf.text.Image createImage(PdfContentByte canvas, Object data, float opacity) throws VectorPrintException, BadElementException {
      if (imageBeingProcessed!=null) {
         return imageBeingProcessed;
      }
      this.data = data;
      boolean doFooter = getSettings().getBooleanProperty(Boolean.FALSE, ReportConstants.PRINTFOOTER);

      // remember page size
      Rectangle r = getDocument().getPageSize();

      getImageLoader().loadTiff(getValue(Image.URLPARAM, URL.class), this, ArrayHelper.unWrap(getValue(NumberCondition.NUMBERS, Integer[].class)));

      // restore settings
      getDocument().setPageSize(r);
      if (getValue(FINALNEWPAGE, Boolean.class)) {
         getDocument().newPage();
      }
      if (doFooter && getValue(NOFOOTER, Boolean.class)) {
         getSettings().put(ReportConstants.PRINTFOOTER, "true");
      }
      return null;
   }

   @Override
   public final boolean shouldStyle(Object data, Object element) {
      return true;
   }

   @Override
   public final boolean shouldDraw(Object data) {
      return false;
   }

   @Override
   public final Boolean doStyle() {
      return false;
   }

   /**
    * This styler will process images and call {@link #style(java.lang.Object, java.lang.Object)  } using all
    * stylers configured after this styler.
    *
    * @param bs
    */
   public final void addStyler(BaseStyler bs) {
      stylers.add(bs);
   }

   public com.itextpdf.text.Image getImageBeingProcessed() {
      return imageBeingProcessed;
   }

}
