package com.vectorprint.report.running;

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

import com.vectorprint.VectorPrintException;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.data.DataCollector;
import com.vectorprint.report.data.ReportDataHolder;
import java.io.OutputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 * A report can be build by an implementor by calling {@link #buildReport(java.lang.Object) } which in turn can/should
 * use a {@link DataCollector} and a {@link ReportGenerator} to build the report.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <RD> the type of data for the report
 * @param <ARGS> the type of arguments for building the report
 */
public interface ReportBuilder<ARGS, RD extends ReportDataHolder> {

   /**
    * @param args the arguments for building the report
    * @return the status code for the building of the report
    */
   int buildReport(ARGS args) throws Exception;
   /**
    * @param args the arguments for building the report
    * @param out the stream that will be used
    * @return the status code for the building of the report
    */
   int buildReport(ARGS args, OutputStream out) throws Exception;

   /**
    *
    * @return the datacollector that will collect the data
    */
   DataCollector< RD> getDataCollector() throws VectorPrintException;

   /**
    *
    * @return the report generator that will write the report
    */
   ReportGenerator<RD> getReportGenerator() throws VectorPrintException;
}
