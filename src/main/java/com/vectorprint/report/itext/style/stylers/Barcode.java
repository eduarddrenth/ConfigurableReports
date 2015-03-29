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

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodePostnet;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.ColorParameter;
import com.vectorprint.report.itext.ImageLoader;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.style.parameters.BarcodeParameter;
import com.vectorprint.report.itext.style.parameters.BaseFontWrapper;
import com.vectorprint.report.itext.style.parameters.BasefontParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import java.awt.Color;
import java.util.Arrays;

/**
 * printing barcodes as images
 * @author Eduard Drenth at VectorPrint.nl
 */
public class Barcode extends com.vectorprint.report.itext.style.stylers.Image<String> {

   public static final String CODETYPE = "codetype";
   public static final String BARHEIGHT = "barheight";
   public static final String MINBARWIDTH = "minbarwidth";
   public static final String BARCOLOR = "barcolor";
   public static final String TXTCOLOR = "txtcolor";
   public static final String FONTSIZE = "fontsize";
   public enum BARCODE { EAN8, EAN13, UPCA, UPCE, CODE128, CODE128UCC, CODE128RAW, PLANET, POSTNET }
   
   public Barcode() {
      super();
      initParams();
   }

   private void initParams() {
      addParameter(new BarcodeParameter(CODETYPE, "type of the barcode: " + Arrays.asList(BARCODE.values()).toString()).setDefault(BARCODE.EAN13),Barcode.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(FONTSIZE, "size of text").setDefault(8f),Barcode.class);
      addParameter(new BasefontParameter(Font.FAMILY_PARAM, "alias of text font"),Barcode.class);
      addParameter(new ColorParameter(BARCOLOR, "color of bars").setDefault(Color.BLACK),Barcode.class);
      addParameter(new ColorParameter(TXTCOLOR, "color of text").setDefault(Color.BLACK),Barcode.class);
      addParameter(new FloatParameter(BARHEIGHT, "height of bars").setDefault(ItextHelper.mmToPts(10)),Barcode.class);
      addParameter(new FloatParameter(MINBARWIDTH, "minimal width of bars").setDefault(ItextHelper.mmToPts(1)),Barcode.class);
   }

   public Barcode(ImageLoader imageLoader, LayerManager layerManager, Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(imageLoader,layerManager,document, writer, settings);
      initParams();
   }

   @Override
   protected Image createImage(PdfContentByte canvas, String data, float opacity) throws VectorPrintException, BadElementException {
      com.itextpdf.text.pdf.Barcode code = null;
      switch (getValue(CODETYPE, BARCODE.class)) {
         case CODE128:
         case CODE128UCC:
         case CODE128RAW:
            code = new Barcode128();
            break;
         case EAN13:
         case EAN8:
         case UPCA:
         case UPCE:
            code = new BarcodeEAN();
            break;
         case PLANET:
         case POSTNET:
            code = new BarcodePostnet();
            break;
      }
      switch (getValue(CODETYPE, BARCODE.class)) {
         case CODE128:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.CODE128);
            break;
         case CODE128UCC:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.CODE128_UCC);
            break;
         case CODE128RAW:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.CODE128_RAW);
            break;
         case EAN13:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.EAN13);
            break;
         case EAN8:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.EAN8);
            break;
         case UPCA:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.UPCA);
            break;
         case UPCE:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.UPCE);
            break;
         case PLANET:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.PLANET);
            break;
         case POSTNET:
            code.setCodeType(com.itextpdf.text.pdf.Barcode.POSTNET);
            break;
      }
      code.setCode(data);
      if (getValue(Font.FAMILY_PARAM, BaseFontWrapper.class)!=null) {
         code.setFont(getValue(Font.FAMILY_PARAM, BaseFontWrapper.class).getBaseFont());
      }
      code.setBarHeight(getValue(BARHEIGHT, Float.class));
      code.setSize(getValue(FONTSIZE, Float.class));
      code.setX(getValue(MINBARWIDTH, Float.class));
      Image img;
      try {
         img = code.createImageWithBarcode(canvas,
             itextHelper.fromColor(getValue(BARCOLOR, Color.class)),
             itextHelper.fromColor(getValue(TXTCOLOR, Color.class)));
      } catch (Exception e) {
         throw new VectorPrintException("invalid barcode: " + data, e);
      }
      return img;
   }

   public BARCODE getBarcode() {
      return getValue(CODETYPE, BARCODE.class);
   }

   public void setBarcode(BARCODE barcode) {
      setValue(CODETYPE, barcode);
   }

   public float getFontSize() {
      return getValue(FONTSIZE, Float.class);
   }

   public void setFontSize(float fontSize) {
      setValue(FONTSIZE, fontSize);
   }

   public float getBarHeight() {
      return getValue(BARHEIGHT, Float.class);
   }

   public void setBarHeight(float barHeight) {
      setValue(BARHEIGHT, barHeight);
   }

   public float getMinBarWidth() {
      return getValue(MINBARWIDTH, Float.class);
   }

   public void setMinBarWidth(float minBarWidth) {
      setValue(MINBARWIDTH, minBarWidth);
   }

   public Color getBarColor() {
      return getValue(BARCOLOR, Color.class);
   }

   public void setBarColor(Color barColor) {
      setValue(BARCOLOR, barColor);
   }

   public Color getTxtColor() {
      return getValue(TXTCOLOR, Color.class);
   }

   public void setTxtColor(Color txtColor) {
      setValue(TXTCOLOR, txtColor);
   }

   public BaseFontWrapper getFont() {
      return getValue(Font.FAMILY_PARAM, BaseFontWrapper.class);
   }

   public void setFont(BaseFontWrapper font) {
      setValue(Font.FAMILY_PARAM, font);
   }

   @Override
   public String getHelp() {
      return "Draw a barcode at a position or near text or an image. " + super.getHelp();
   }

}
