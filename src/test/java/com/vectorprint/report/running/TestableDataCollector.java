/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.report.running;

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

import com.vectorprint.report.data.DataCollectionMessages;
import com.vectorprint.report.data.BlockingDataCollector;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class TestableDataCollector extends BlockingDataCollector {

   private static boolean produceError = false;

   public static void setProduceError(boolean produceError) {
      TestableDataCollector.produceError = produceError;
   }

   @Override
   public void collectData() {
      if (produceError) {
         super.getDataHolder().getMessages().addMessage(DataCollectionMessages.Level.ERROR, "foutje");
      }
      add(new MyChapter(), null);
      add(new MyData(), null);
      add(new MySection(), null);
      add(new MyMultiColumns(), null);
      /*
          TODO setting this to a higher value (i.e. 115) makes the table with nested table disappear completely
          this occurs with nested tables 
      */
      for (int i = 0; i < 100; i++) {
         add(new MyData(), null);
      }
      add(new MyEndMultiColumns(), null);
      add(new MySection(), null);
      add(new MyTable(), null);
         add(new MyCell(), null);
         add(new MyNestedTable(), null);
            add(new MyCell(), null);
            add(new MyCell(), null);
            add(new MyEndNested(), null);
         add(new MyCell(), null);
         add(new MyEndCell(), null);
      add(new MyChapter(), null);
      add(new MyData(), null);
      add(new MyData(), null);
      add(new MyData(), null);
      add(new MySection(), null);
      add(new MyListFromData(), null);
      add(new MyData(), null);
      add(new MyData(), null);
      add(new MyTableFromData(), null);
      add(new MyData(), null);
   }

}
