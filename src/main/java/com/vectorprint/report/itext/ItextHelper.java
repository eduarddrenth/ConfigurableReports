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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ExtendedColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.annotation.SettingsField;
import com.vectorprint.report.ReportConstants;
import java.awt.Color;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Helper for Itext.
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ItextHelper {
   
   public static float getTextHeight(Chunk c) {
      return c.getFont().getSize();
   }
   
   public static float getTextWidth(Chunk c) {
      return c.getWidthPoint();
   }
   
   public static float getTextWidth(String text, Font f) {
      return getTextWidth(new Chunk(text, f));
   }
   
   public static float getTextWidth(String text, BaseFont bf, float size) {
      return getTextWidth(new Chunk(text, new Font(bf, size)));
   }
   
   public static float getTextHeight(String text, Font f) {
      return getTextHeight(new Chunk(text, f));
   }
   
   public static float getTextHeight(String text, BaseFont bf, float size) {
      return getTextHeight(new Chunk(text, new Font(bf, size)));
   }

   /**
    * uses {@link #round(float, int) } with 3 decimals
    *
    * @param mm
    * @return
    */
   public static float mmToPts(float mm) {
      return round((mm / 25.4f) * 72, 3);
   }

   /**
    * uses {@link #round(float, int) } with 3 decimals
    *
    * @param pts
    * @return
    */
   public static float ptsToMm(float pts) {
      return round(pts * (25.4f / 72), 3);
   }

   /**
    * Round to certain number of decimals
    *
    * @param f
    * @param decimalPlace
    * @return
    */
   public static float round(float f, int decimalPlace) {
      return new BigDecimal(Float.toString(f)).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
   }
   
   public static void resetLineDash(PdfContentByte canvas) {
      canvas.setLineDash(new float[]{1, 0}, 0);
   }
   
   public enum COLORSPACE {
      
      RGB(ExtendedColor.TYPE_RGB),
      CMYK(ExtendedColor.TYPE_CMYK),
      DEVICEN(ExtendedColor.TYPE_DEVICEN),
      GRAY(ExtendedColor.TYPE_GRAY),
      LAB(ExtendedColor.TYPE_LAB),
      SEPARATION(ExtendedColor.TYPE_SEPARATION),
      SHADING(ExtendedColor.TYPE_SHADING),
      PATTERN(ExtendedColor.TYPE_PATTERN);
      private int type;
      
      private COLORSPACE(int type) {
         this.type = type;
      }
      
   }
   
   private ICC_ColorSpace icc = null;
   private com.itextpdf.text.pdf.ICC_Profile iCC_Profile = null;
   
   @SettingsField
   private EnhancedMap settings;
   
   public BaseColor fromColor(Color color) {
      if (settings != null && settings.containsKey(ReportConstants.ICCCOLORPROFILE)) {
         try {
            loadICC(settings.getURLProperty(null, ReportConstants.ICCCOLORPROFILE).openStream());
         } catch (IOException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
         float[] toCIEXYZ = icc.fromRGB(color.getRGBComponents(null));//color.getColorSpace().toCIEXYZ(color.getComponents(null));
         if (toCIEXYZ.length > 3) {
            return new CMYKColor(toCIEXYZ[0], toCIEXYZ[1], toCIEXYZ[2], toCIEXYZ[3]);
         } else {
            return new CMYKColor(toCIEXYZ[0], toCIEXYZ[1], toCIEXYZ[2], 0);
         }
      } else {
         return new BaseColor(color.getRGB());
      }
   }
   
   public void loadICC(InputStream in) throws IOException {
      if (settings != null && settings.containsKey(ReportConstants.ICCCOLORPROFILE)) {
         if (icc == null) {
            synchronized (this) {
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               IOHelper.load(in, out);
               iCC_Profile = com.itextpdf.text.pdf.ICC_Profile.getInstance(out.toByteArray());
               icc = new ICC_ColorSpace(ICC_Profile.getInstance(out.toByteArray()));
            }
         }
      }
   }
      
   public com.itextpdf.text.pdf.ICC_Profile getiCC_Profile() {
      return iCC_Profile;
   }
   
}
