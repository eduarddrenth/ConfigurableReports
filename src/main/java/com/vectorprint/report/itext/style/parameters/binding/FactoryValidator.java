/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.parameters.binding;

import com.vectorprint.configuration.binding.parameters.ParamFactoryValidator;
import com.vectorprint.configuration.binding.parameters.ParameterizableBindingFactory;

/**
 * Validator published through SPI, validates if a ParameterizableBindingFactory is a subclass of ReportParameterBindingFactory or
 * JsonReportParameterBindingFactory.
 * @author Eduard Drenth at VectorPrint.nl
 */
public class FactoryValidator implements ParamFactoryValidator {

   @Override
   public boolean isValid(ParameterizableBindingFactory bindingFactory) {
      return bindingFactory instanceof ReportParameterBindingFactory || bindingFactory instanceof JsonReportParameterBindingFactory;
   }

}
