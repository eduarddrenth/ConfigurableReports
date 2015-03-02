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

import com.vectorprint.report.itext.DocumentAware;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.vectorprint.VectorPrintException;
import com.vectorprint.certificates.CertificateHelper;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.Parameterizable;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.ReportDataAware;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.BaseReportGenerator;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Responsible for setting properties of the document such as page size, margins, encryption etc.
 * @author Eduard Drenth at VectorPrint.nl
 * @param <RD>
 */
public interface DocumentStyler<RD extends ReportDataHolder> extends Parameterizable, BaseStyler, DocumentAware, ReportDataAware<RD> {
   
   /**
    * Called before styling the document by the {@link ReportGenerator}
    * @throws VectorPrintException 
    */
   void loadFonts() throws VectorPrintException;
   
   /**
    * Used by {@link BaseReportGenerator#createReportBody(com.itextpdf.text.Document, com.vectorprint.report.data.ReportDataHolder, com.itextpdf.text.pdf.PdfWriter)  } after the document is opened
    *
    * @param <E>
    * @param element
    * @param data
    * @return
    * @throws VectorPrintException
    */
   <E extends Document> E styleAfterOpen(E element, Object data) throws VectorPrintException;
   
   ReportDataHolder getData();
   
   /**
    * add a visible signature to the document when a {@link DocumentSettings#KEYSTORE} is provided.
    *
    * @see CertificateHelper#loadKeyStore(java.io.InputStream, java.lang.String, char[]) 
    * @see CertificateHelper#getKey(java.security.KeyStore, java.lang.String, char[]) 
    * @param psa
    * @throws KeyStoreException
    * @throws NoSuchAlgorithmException
    * @throws UnrecoverableKeyException
    * @throws VectorPrintException
    */
   void configureVisualSignature(PdfSignatureAppearance psa) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, VectorPrintException;
}
