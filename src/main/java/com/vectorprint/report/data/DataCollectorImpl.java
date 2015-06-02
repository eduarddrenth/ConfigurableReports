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

import com.vectorprint.report.data.ReportDataHolder.IdData;
import com.vectorprint.report.itext.style.parameters.ReportBindingHelper;

/**
 * Default implementation of a DataCollector, uses ReportDataHolderImpl as generic parameters
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class DataCollectorImpl implements DataCollector<ReportDataHolderImpl> {

   private ReportDataHolderImpl rd = new ReportDataHolderImpl();

   @Override
   public ReportDataHolderImpl getDataHolder() {
      return rd;
   }

   @Override
   public void add(Object data, String id) {
      getDataHolder().add(new IdData(data, id));
   }

   @Override
   public Class<? extends ReportBindingHelper> getDefaultBindingHelperClass() {
      return ReportBindingHelper.class;
   }

}
