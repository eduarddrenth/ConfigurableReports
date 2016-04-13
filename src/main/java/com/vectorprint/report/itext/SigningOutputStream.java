package com.vectorprint.report.itext;

/*
 * #%L
 * ConfigurableReports
 * %%
 * Copyright (C) 2014 - 2016 VectorPrint
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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
class SigningOutputStream extends AbstractTwoPassStream {

   private final BaseReportGenerator outer;

   public SigningOutputStream(OutputStream out, int bufferSize, final BaseReportGenerator outer) throws IOException {
      super(out, bufferSize);
      this.outer = outer;
   }

   @Override
   public void secondPass(InputStream firstPass, OutputStream orig) throws IOException {
      PdfReader reader = null;
      try {
         reader = new PdfReader(firstPass);
         PdfStamper stamper = PdfStamper.createSignature(reader, orig, '\0');
         outer.getDocumentStyler().configureVisualSignature(stamper.getSignatureAppearance());
         stamper.close();
      } catch (DocumentException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | VectorPrintException ex) {
         throw new VectorPrintRuntimeException(ex);
      } finally {
         if (reader != null) {
            reader.close();
         }
      }
   }

}
