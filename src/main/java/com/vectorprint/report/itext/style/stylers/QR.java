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
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.report.itext.ImageLoader;
import com.vectorprint.report.itext.LayerManager;
import static com.vectorprint.report.itext.style.stylers.DocumentSettings.HEIGHT;
import static com.vectorprint.report.itext.style.stylers.DocumentSettings.WIDTH;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * printing QR as images
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class QR extends com.vectorprint.report.itext.style.stylers.Image<String> {

   public QR() {
      super();
      initParams();
   }

   private void initParams() {
      addParameter(new IntParameter(WIDTH, "width of the QR code").setDefault(1), QR.class);
      addParameter(new IntParameter(HEIGHT, "height of the QR code").setDefault(1), QR.class);
   }

   public QR(ImageLoader imageLoader, LayerManager layerManager, Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      super(imageLoader, layerManager, document, writer, settings);
      initParams();
   }

   @Override
   protected Image createImage(PdfContentByte canvas, String data, float opacity) throws VectorPrintException, BadElementException {
      Image img = null;
      try {
         if (data == null && getData() == null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(100);
            IOHelper.load(getUrl().openStream(), out);
            img = new BarcodeQRCode(out.toString(), getValue(WIDTH, Integer.class), getValue(HEIGHT, Integer.class), null).getImage();
         } else {
            img = new BarcodeQRCode(data == null ? getData() : data, getValue(WIDTH, Integer.class), getValue(HEIGHT, Integer.class), null).getImage();
         }
      } catch (IOException ex) {
         throw new VectorPrintException(ex);
      }
      applySettings(img);
      return img;
   }

   @Override
   public String getHelp() {
      return "draw a QR code." + " " + super.getHelp();
   }

}
