/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.style.parameters;

import com.vectorprint.configuration.parameters.ParameterImpl;
import com.vectorprint.report.itext.style.stylers.DocumentSettings;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DigestParameter extends ParameterImpl<DocumentSettings.DIGESTALGORITHM> {

   public DigestParameter(String key, String help) {
      super(key, help);
   }

}
