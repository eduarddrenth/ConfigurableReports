package com.vectorprint.report.itext.mappingconfig.model;

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
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;

public class StartContainerConfig
    extends DataConfig {

   private CONTAINER_ELEMENT containertype;
   private String containertypemethod;
   private int sectionlevel = 1;
   private boolean adddata = false;

   public CONTAINER_ELEMENT getContainertype() {
      return containertype;
   }

   public StartContainerConfig setContainertype(CONTAINER_ELEMENT value) {
      this.containertype = value;
      return this;
   }

   public String getContainertypemethod() {
      return containertypemethod;
   }

   public StartContainerConfig setContainertypemethod(String value) {
      this.containertypemethod = value;
      return this;
   }

   public int getSectionlevel() {
      return sectionlevel;
   }

   public StartContainerConfig setSectionlevel(int value) {
      this.sectionlevel = value;
      return this;
   }

   public boolean isAdddata() {
      return adddata;
   }

   public StartContainerConfig setAdddata(boolean value) {
      this.adddata = value;
      return this;
   }

}
