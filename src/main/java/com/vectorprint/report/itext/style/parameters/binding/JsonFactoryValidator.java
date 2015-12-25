/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.parameters.binding;

import com.vectorprint.configuration.binding.parameters.ParamFactoryValidator;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;
import com.vectorprint.report.ReportConstants;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class JsonFactoryValidator implements ParamFactoryValidator {

   @Override
   public boolean isValid(ParameterizableBindingFactory bindingFactory) {
      String json = System.getProperty(ReportConstants.JSON);
      return json == null || bindingFactory instanceof JsonReportParameterBindingFactory;
   }

}
