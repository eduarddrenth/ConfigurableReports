package com.vectorprint.report.data;

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
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;
import com.vectorprint.report.itext.mappingconfig.model.DataMapping;
import com.vectorprint.report.itext.style.parameters.ReportBindingHelper;
import com.vectorprint.report.running.ReportBuilder;

//~--- JDK imports ------------------------------------------------------------
/**
 * To generate a report we first need to collect data.
 *
 * @param RD the type of data that the collector will yield
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface DataCollector<RD extends ReportDataHolder> {

   /**
    * get a reference to the object that will contain report data
    *
    * @return 
    */
   RD getDataHolder();
      
   /**
    * called by {@link ReportBuilder#buildReport(java.lang.Object) } to collect data.
    *
    * @see #add(java.lang.Object, java.lang.String) 
    * @return
    */
   RD collect();

   /**
    * adds peaces of data to the {@link ReportDataHolder}
    *
    * @param data
    * @param id the optional id of the data for {@link DataMapping}
    */
   void add(Object data, String id);
   
   /**
    * return the class to be used as {@link ParameterizableBindingFactory#getBindingHelper() }, NOTE
    * that this will not override the class indicated by {@link ParameterizableBindingFactoryImpl#PARAMHELPER}.
    * 
    * @see ParameterizableBindingFactoryImpl#PARAMHELPER
    * @return 
    */
   Class<? extends ReportBindingHelper> getDefaultBindingHelperClass();
}

