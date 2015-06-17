/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.vectorprint.VectorPrintException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.cert.Certificate;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ImageLoader {

   /**
    * Load an image from a URL
    *
    * @param image
    * @param opacity the value of opacity
    * @throws VectorPrintException
    * @return the com.itextpdf.text.Image
    */
   Image loadImage(URL image, float opacity) throws VectorPrintException;

   /**
    *
    * @param image the value of image
    * @param opacity the value of opacity
    * @throws VectorPrintException
    */
   Image loadImage(InputStream image, float opacity) throws VectorPrintException;

   /**
    * Load pages in a pdf as images from a URL and call an imageProcessor to process them
    *
    * @param pdf
    * @param writer
    * @param password the value of password
    * @param pages when null assume all pages
    * @throws VectorPrintException
    */
   void loadPdf(URL pdf, PdfWriter writer, byte[] password, ImageProcessor imageProcessor, int... pages) throws VectorPrintException;

   /**
    * Load pages in a pdf as images from a stream and call an imageProcessor to process them
    *
    * @param pdf
    * @param writer
    * @param password the value of password
    * @param pages when null assume all pages
    * @throws VectorPrintException
    */
   void loadPdf(InputStream pdf, PdfWriter writer, byte[] password,  ImageProcessor imageProcessor, int... pages) throws VectorPrintException;

   /**
    * Load pages in a tiff as images from a stream and call an imageProcessor to process them
    *
    * @param tiff
    * @param pages when null assume all pages
    * @throws VectorPrintException
    */
   void loadTiff(InputStream tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException;

   /**
    * Load pages in a tiff as images from a URL and call an imageProcessor to process them
    *
    * @param tiff
    * @param pages when null assume all pages
    * @throws VectorPrintException
    */
   void loadTiff(URL tiff, ImageProcessor imageProcessor, int... pages) throws VectorPrintException;
   
   /**
    * read a certificate protected pdf.
    * @param pdf
    * @param writer
    * @param certificate
    * @param key
    * @param securityProvider
    * @param imageProcessor
    * @param pages
    * @throws VectorPrintException 
    */
   void loadPdf(InputStream pdf, PdfWriter writer, Certificate certificate, Key key, String securityProvider, ImageProcessor imageProcessor, int... pages) throws VectorPrintException;
}
