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
import com.vectorprint.report.itext.ImageLoaderAware;
import com.vectorprint.VectorPrintException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.itext.ElementProducer;
import com.vectorprint.report.itext.LayerManager;
import com.vectorprint.report.itext.LayerManagerAware;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 * Responsible for providing {@link ReportGenerator}s and {@link ElementProducer}s
 * with stylers.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface StylerFactory extends DocumentAware, ImageLoaderAware, LayerManagerAware {

   /**
    * for debugging, find all stylers used in the cache
    * @param styleClasses
    * @return
    * @throws VectorPrintException 
    */
   List<BaseStyler> getBaseStylersFromCache(String... styleClasses) throws VectorPrintException;
   
   /**
    * find Stylers for report parts
    * @param styleClasses
    * @return
    * @throws VectorPrintException 
    */
   List<BaseStyler> getStylers(String... styleClasses) throws VectorPrintException;


   /**
    * find a document styler, based on {@link ReportConstants#DOCUMENTSETTINGS}
    *
    * @return
    * @throws VectorPrintException 
    */
   
   DocumentStyler getDocumentStyler() throws VectorPrintException;

   /**
    * return a map of style classes and style class setup
    *
    * @return
    */
   Map<String, String> getStylerSetup();
   
   LayerManager getLayerManager();
   
   EnhancedMap getSettings();

}
