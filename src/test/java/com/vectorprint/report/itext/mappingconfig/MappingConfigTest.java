/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.itext.mappingconfig;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2014 VectorPrint
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

import com.itextpdf.text.Chunk;
import com.vectorprint.ClassHelper;
import com.vectorprint.report.data.types.DurationValue;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.itext.mappingconfig.model.DataMapping;
import com.vectorprint.report.running.MyCell;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class MappingConfigTest {
   
   @Test
   public void testMappingConfig() throws FileNotFoundException, JAXBException, IOException, ClassNotFoundException {
      Datamappingstype dm = DatamappingHelper.fromXML(new InputStreamReader(new FileInputStream("src/test/resources/DataMappingTest.xml")));
      Assert.assertTrue(dm.getDatamapping().get(0).isRegex());
      for (Class clazz : ClassHelper.fromPackage(MyCell.class.getPackage())) {
         if (clazz.getSimpleName().startsWith("My")) {
            DataMapping dmm = new DatamappingHelper().toDataConfig(clazz, "idee", dm);
            Assert.assertNotNull(dmm);
            Assert.assertEquals(dmm.getElement().size(),1);
            Assert.assertEquals(0,dmm.getElement().get(0).getStyleclasses().size());
            Assert.assertEquals(dmm.getElement().get(0).getDatatype().getDataclass(),DurationValue.class);
            Assert.assertEquals(dmm.getElementsfromdata().getDatalistmethod(),"getMe");
            Assert.assertEquals(dmm.getElementsfromdata().getElement().getElementtype(),Chunk.class);
         }
      }
   }

}
