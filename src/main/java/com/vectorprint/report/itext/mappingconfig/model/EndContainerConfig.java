package com.vectorprint.report.itext.mappingconfig.model;

import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;

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
public class EndContainerConfig {

   private CONTAINER_ELEMENT containertype;

   private int depthtoend = 1;

   public CONTAINER_ELEMENT getContainertype() {
      return containertype;
   }

   public EndContainerConfig setContainertype(CONTAINER_ELEMENT value) {
      this.containertype = value;
      return this;
   }

   public int getDepthtoend() {
      return depthtoend;
   }

   public EndContainerConfig setDepthtoend(int value) {
      this.depthtoend = value;
      return this;
   }

}
