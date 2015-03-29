
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.vectorprint.ArrayHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.certificates.CertificateHelper;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.CharPasswordParameter;
import com.vectorprint.configuration.parameters.URLParameter;
import com.vectorprint.configuration.parameters.annotation.Param;
import com.vectorprint.configuration.parameters.annotation.Parameters;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.itext.style.conditions.NumberCondition;
import com.vectorprint.report.itext.style.parameters.KeyStoreParameter;
import static com.vectorprint.report.itext.style.stylers.DocumentSettings.KEYSTORETYPE_PARAM;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

//~--- JDK imports ------------------------------------------------------------
/**
 * import pages from a pdf near a certain element
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
@Parameters(
    parameters = {
       @Param(
           key = DocumentSettings.KEYSTORE,
           help = "the url to the keystore",
           clazz = URLParameter.class)
    })
public class ImportPdf extends ImportTiff {
   /**
    * name of the setting to override {@link #DEFAULTSECURITYPROVIDER}
    * @see PdfReader#PdfReader(java.lang.String, java.security.cert.Certificate, java.security.Key, java.lang.String) 
    */
   public static final String SECURITYPROVIDER = "securityprovider";
   
   public ImportPdf() {
      addParameter(new KeyStoreParameter(KEYSTORETYPE_PARAM, "the type of the signing certificate: " + Arrays.asList(DocumentSettings.KEYSTORETYPE.values()).toString()).setDefault(DocumentSettings.KEYSTORETYPE.pkcs12),ImportPdf.class);
      addParameter(new CharPasswordParameter( DocumentSettings.KEYSTORE_PASSWORD, "a password for the keystore", false),ImportPdf.class);
   }

   /**
    * calls {@link #processImage(com.itextpdf.text.Image) } on pages imported from the pdf in the URL, always returns null, because each page from a pdf is imported as an image.
    * @param canvas
    * @param data
    * @param opacity the value of opacity
    * @throws VectorPrintException
    * @throws BadElementException 
    * @return the com.itextpdf.text.Image 
    */
   @Override
   protected com.itextpdf.text.Image createImage(PdfContentByte canvas, Object data, float opacity) throws VectorPrintException, BadElementException {
      if (getImageBeingProcessed()!=null) {
         return getImageBeingProcessed();
      }
      this.data = data;
      boolean doFooter = getSettings().getBooleanProperty(ReportConstants.PRINTFOOTER, Boolean.FALSE);
      
      if (doFooter && getValue(NOFOOTER, Boolean.class)) {
         getSettings().put(ReportConstants.PRINTFOOTER, "false");
      }

      // remember page size
      Rectangle r = getDocument().getPageSize();

      // each page on its own page in the pdf to be written
      if (getValue(DocumentSettings.KEYSTORE, URL.class)!=null) {
         char[] pw = getValue(DocumentSettings.KEYSTORE_PASSWORD, char[].class);
         KeyStore ks = null;
         try {
            ks = CertificateHelper.loadKeyStore(getValue(DocumentSettings.KEYSTORE, URL.class).openStream(), getValue(KEYSTORETYPE_PARAM, DocumentSettings.KEYSTORETYPE.class).name(), pw.clone());
            String alias = getSettings().getProperty(KEYSTOREALIAS, DEFAULTKEYSTORE_ALIAS);
            String provider = getSettings().getProperty(SECURITYPROVIDER, DEFAULTSECURITYPROVIDER);
            getImageLoader().loadPdf(
                getValue(Image.URLPARAM, URL.class).openStream(),
                getWriter(), ks.getCertificate(alias), CertificateHelper.getKey(ks, alias, pw.clone()), provider, this,
                ArrayHelper.unWrap(getValue(NumberCondition.NUMBERS, Integer[].class)));
         } catch (KeyStoreException ex) {
            throw new VectorPrintException(ex);
         } catch (IOException ex) {
            throw new VectorPrintException(ex);
         } catch (NoSuchAlgorithmException ex) {
            throw new VectorPrintException(ex);
         } catch (CertificateException ex) {
            throw new VectorPrintException(ex);
         } catch (UnrecoverableKeyException ex) {
            throw new VectorPrintException(ex);
         }
      } else {
         getImageLoader().loadPdf(
             getValue(Image.URLPARAM, URL.class),
             getWriter(), getValue(DocumentSettings.PASSWORD, byte[].class), this,
             ArrayHelper.unWrap(getValue(NumberCondition.NUMBERS, Integer[].class)));
      }
      
      // restore settings
      getDocument().setPageSize(r);
      getDocument().newPage();
      if (doFooter && getValue(NOFOOTER, Boolean.class)) {
         getSettings().put(ReportConstants.PRINTFOOTER, "true");
      }
      return null;
   }
   public static final String DEFAULTSECURITYPROVIDER = "BC";
   /**
    * name of the property to change {@link #DEFAULTKEYSTORE_ALIAS}
    * @see EnhancedMap
    * @see CertificateHelper#getKey(java.security.KeyStore, java.lang.String, char[]) 
    * @see KeyStore#getCertificate(java.lang.String) 
    */
   public static final String KEYSTOREALIAS = "keystorealias";
   /**
    * default value for keystore alias, can be changed by setting {@link #KEYSTOREALIAS}
    */
   public static final String DEFAULTKEYSTORE_ALIAS = "1";

   
}
