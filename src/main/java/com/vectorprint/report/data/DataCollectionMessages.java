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

//~--- JDK imports ------------------------------------------------------------
import com.vectorprint.report.ReportGenerator;
import java.util.List;

/**
 * The idea is that in {@link DataCollector the datacollection phase} faults and notifications can be set that will be
 * dealt with by a {@link ReportGenerator}
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface DataCollectionMessages<MESGTYPE> {

   /**
    * severity
    */
   public enum Level {

      DEBUG, INFO, WARN, ERROR, FATAL
   }

   /**
    * The messages
    *
    * @param l
    * @return
    */
   List<MESGTYPE> getMessages(Level l);

   /**
    * add a message
    *
    * @param l
    * @param message
    */
   void addMessage(Level l, MESGTYPE message);
}
