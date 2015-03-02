/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vectorprint.report.data.types;

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

import java.util.Date;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class PeriodValue extends ReportValue<Period>{

}

class Period implements Comparable<Period> {
   
   private Date start, end;

   public Period(Date start, Date end) {
      this.start = start;
      this.end = end;
   }

   @Override
   public int compareTo(Period o) {
      return o.start.compareTo(start);
   }

   public Date getStart() {
      return start;
   }

   public Date getEnd() {
      return end;
   }
   
}
