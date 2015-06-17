package com.vectorprint.report.itext.style;

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
import com.itextpdf.text.FontFactory;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.ReportConstants;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * support for loading of iText and awt fonts, also from URL or stream, note that you have to call {@link #setLoadAWTFonts(boolean) }
 * and / or {@link #setLoadiText(boolean) } to load any fonts.
 * 
 * By default uses embedding of fonts.
 *
 * @author Eduard Drenth: VectorPrint
 *
 */
public class FontLoader {

   private static final FontLoader instance = new FontLoader().setLoadiText(true);
   {
      FontFactory.defaultEmbedding = true;
   }
   private final static Logger log = Logger.getLogger(FontLoader.class.getName());
   private boolean loadAWTFonts = false, loadiText=false;

   /**
    * for feedback on the loading of fonts.
    *
    * @see #loadFont(java.lang.String) 
    */
   public enum LOADSTATUS {

      LOADED_ITEXT_AND_AWT, LOADED_ONLY_AWT, LOADED_ONLY_ITEXT, NOT_LOADED
   }

   /**
    * a fontloader that loads iText fonts only
    * @return 
    */
   public static FontLoader getInstance() {
      return instance;
   }

   /**
    * calls {@link #loadFont(java.io.InputStream, java.lang.String) } with the file extension from the
    * URL
    *
    * @param url
    * @return
    * @throws IOException
    */
   public LOADSTATUS loadFont(URL url) throws IOException, VectorPrintException {
      return loadFont(url.openStream(), url.toString().substring(url.toString().lastIndexOf('.')));
   }


   /**
    * allows loading font from a stream by first saving the bytes from the stream to a tempfile and then calling
	 * {@link #loadFont(java.lang.String)  }.
    *
    * @param in
    * @param extension e.g. .ttf
    * @return
    * @throws IOException
    */
   public LOADSTATUS loadFont(InputStream in, String extension) throws IOException, VectorPrintException {
      // first download, then load
      File f = File.createTempFile("font.", extension);
      f.deleteOnExit();
      IOHelper.load(in, new FileOutputStream(f), ReportConstants.DEFAULTBUFFERSIZE, true);
      return loadFont(f.getPath());
   }


   /**
    * Bottleneck method for loading fonts, calls {@link FontFactory#register(java.lang.String) } for iText, {@link GraphicsEnvironment#registerFont(java.awt.Font) }
    * for awt.
    * @param path the path to the font file 
ed
    * @return
    * @throws VectorPrintException 
    */
   public LOADSTATUS loadFont(String path) throws VectorPrintException {
      try {
         File f = new File(path);
         LOADSTATUS stat = LOADSTATUS.NOT_LOADED;

         if (loadAWTFonts) {
            Font fo = Font.createFont(Font.TRUETYPE_FONT, f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
            stat = LOADSTATUS.LOADED_ONLY_AWT;
         }

         if (loadiText) {
            FontFactory.register(path);
            stat = (stat.equals(LOADSTATUS.LOADED_ONLY_AWT)) ? LOADSTATUS.LOADED_ITEXT_AND_AWT : LOADSTATUS.LOADED_ONLY_ITEXT;
            log.info(String.format("font loaded from %s", f.getAbsolutePath()));
         }

         return stat;
      } catch (FontFormatException ex) {
         log.log(Level.SEVERE, null, ex);

         throw new VectorPrintException("failed to load " + path, ex);
      } catch (IOException ex) {
         log.log(Level.SEVERE, null, ex);

         throw new VectorPrintException("failed to load " + path, ex);
      }
   }

   public FontLoader setLoadAWTFonts(boolean loadAWTFonts) {
      this.loadAWTFonts = loadAWTFonts;
      return this;
   }

   public boolean isEmbedFonts() {
      return FontFactory.defaultEmbedding;
   }

   public FontLoader setEmbedFonts(boolean embedFonts) {
      FontFactory.defaultEmbedding = embedFonts;
      return this;
   }

   public boolean isLoadiText() {
      return loadiText;
   }

   public FontLoader setLoadiText(boolean loadiText) {
      this.loadiText = loadiText;
      return this;
   }
}
