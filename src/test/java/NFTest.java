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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class NFTest {
   
   @Test
   public void testNF() {
      DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
      dfs.setCurrencySymbol("€");
      DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance();
      nf.setDecimalFormatSymbols(dfs);
      nf.applyPattern("\u00a4 #,###.##");
      
      System.out.println(nf.format(9999123506100.4555));
      System.out.println(nf.format(123506100.4555));
      System.out.println(nf.format(023506100.4555));
   }

}
