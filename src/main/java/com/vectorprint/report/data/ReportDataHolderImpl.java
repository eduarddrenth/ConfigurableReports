/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.report.ReportGenerator;
import com.vectorprint.report.itext.annotations.DataType;
import com.vectorprint.report.itext.annotations.Element;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Holder for data for reports, you can annotate classes of data Objects you add using {@link Element} and
 * {@link DataType} to easily create and style report parts and format your data.
 * @see ReportGenerator#processData(com.vectorprint.report.data.ReportDataHolder) 
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportDataHolderImpl implements com.vectorprint.report.data.ReportDataHolder {

   private DataCollectionMessages<String> messages = new DefaultDataCollectionMessages();
   private Queue data = new LinkedList();

   @Override
   public Queue<IdData> getData() {
      return data;
   }

   @Override
   public DataCollectionMessages getMessages() {
      return messages;
   }

   /**
    *
    * @param data the value of data
    */
   @Override
   public void add(IdData data) {
      if (!this.data.offer(data)) {
         throw new VectorPrintRuntimeException(String.format("data not added: %s", data));
      }
   }

   protected void setData(Queue<IdData> data) {
      this.data = data;
   }

   
}
