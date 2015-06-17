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
import com.vectorprint.report.itext.mappingconfig.model.DataMapping;
import java.util.Queue;

//~--- JDK imports ------------------------------------------------------------
/**
 * Container for data also holding messages that were produced when collecting the data.
 *
 * @see DataCollector
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface ReportDataHolder {

   /**
    * the data
    *
    * @return
    */
   Queue<IdData> getData();

   /**
    *
    * @param data the value of data, possibly with an id for {@link DataMapping}
    */
   void add(IdData data);

   /**
    * the messages
    *
    * @return
    */
   DataCollectionMessages getMessages();

   /**
    * holder to keep track of an optional user provided id for data
    * @see DataCollector#add(java.lang.Object, java.lang.String) 
    */
   public static class IdData {

      public IdData(Object data, String id) {
         this.data = data;
         this.id = id;
      }

      private final Object data;
      private final String id;

      public Object getData() {
         return data;
      }

      public String getId() {
         return id;
      }

   }

}
