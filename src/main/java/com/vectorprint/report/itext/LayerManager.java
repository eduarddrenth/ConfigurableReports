/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext;

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

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfLayer;

/**
 * This interface facilitates grouping parts of reports together in logical layers, identified by a name.
 * @author Eduard Drenth at VectorPrint.nl
 */
public interface LayerManager {

   /**
    * initialize a layer, whose purpose is to contain child layers added by {@link #startLayerInGroup(java.lang.String, com.itextpdf.text.pdf.PdfContentByte) }. It is suggested to {@link PdfContentByte#beginLayer(com.itextpdf.text.pdf.PdfOCG) begin} and
    * {@link PdfContentByte#endLayer() end} the layer in this method, without adding content to it
    * @param layerId
    * @param canvas
    * @return 
    */
   PdfLayer initLayerGroup(String layerId, PdfContentByte canvas);

   /**
    * begin a child layer in the parent layer identified by groupId. After adding content the layer should be {@link PdfContentByte#endLayer() ended}.
    * @param groupId
    * @param canvas
    * @return 
    */
   PdfLayer startLayerInGroup(String groupId, PdfContentByte canvas);
   
}
