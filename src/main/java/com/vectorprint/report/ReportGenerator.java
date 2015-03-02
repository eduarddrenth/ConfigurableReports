package com.vectorprint.report;

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

import com.itextpdf.text.DocumentException;
import com.vectorprint.VectorPrintException;
import com.vectorprint.report.data.DataCollectionMessages;
import com.vectorprint.report.data.DataCollector;
import com.vectorprint.report.data.DataCollectorImpl;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.data.ReportDataHolderImpl;
import com.vectorprint.report.itext.annotations.DataType;
import com.vectorprint.report.itext.annotations.Element;
import java.io.OutputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 * Implementers will be responsible for writing a report to an OutputStream
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param RD The data based on which the report is build
 * @see DataCollector
 */
public interface ReportGenerator<RD extends ReportDataHolder>  {

   /**
    * generate a report
    *
    * @param data
    * @param outputStream
    * @return 0 when successful
    */
   int generate(RD data, OutputStream outputStream) throws VectorPrintException;

   /**
    * handle messages set in the data collection phase, will be called prior to building the report.
    *
    * @param messages
    * @param document the value of document
    * @return a value of true means the messages set by the {@link DataCollector} are dealt with,
    * the report will be generated in this case, a value of false will cause the report to fail
    */
   boolean continueOnDataCollectionMessages(DataCollectionMessages messages, com.itextpdf.text.Document document) throws VectorPrintException;
   
   /**
    * process data to generate a report, the suggested way is to look for {@link Element} and {@link DataType} annotations
    * on the {@link ReportDataHolder#getData() peaces of data} and create and style report parts accordingly.
    * @see ReportDataHolderImpl
    * @see DataCollectorImpl
    * @param data
    * @throws VectorPrintException 
    */
   void processData(RD data) throws VectorPrintException, DocumentException;
}
