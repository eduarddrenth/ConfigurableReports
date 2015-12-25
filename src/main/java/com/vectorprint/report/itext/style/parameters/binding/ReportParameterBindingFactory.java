/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.parameters.binding;

/*
 * #%L
 * ConfigurableReports
 * %%
 * Copyright (C) 2014 - 2015 VectorPrint
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

import com.vectorprint.configuration.binding.parameters.ParamBindingHelper;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactoryImpl;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class ReportParameterBindingFactory extends ParameterizableBindingFactoryImpl {
   
   private static final ParamBindingHelper bindingHelper = new ReportBindingHelper();

   @Override
   public ParamBindingHelper getBindingHelper() {
      return bindingHelper;
   }

}
