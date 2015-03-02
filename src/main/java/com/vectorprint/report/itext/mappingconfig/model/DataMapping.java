package com.vectorprint.report.itext.mappingconfig.model;

import java.util.ArrayList;
import java.util.List;

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
public class DataMapping {

   private List<StartContainerConfig> containers = new ArrayList<StartContainerConfig>(1);
   private List<ElementConfig> elements = new ArrayList<ElementConfig>(1);
   private ElementsFromData elementsfromdata;
   private EndContainerConfig endcontainer;
   private String id;

   public List<StartContainerConfig> getStartcontainer() {
      return containers;
   }

   public DataMapping addStartcontainer(StartContainerConfig value) {
      this.containers.add(value);
      return this;
   }


   public List<ElementConfig> getElement() {
      return elements;
   }

   public DataMapping addElement(ElementConfig value) {
      this.elements.add(value);
      return this;
   }

   public ElementsFromData getElementsfromdata() {
      return elementsfromdata;
   }

   public DataMapping setElementsfromdata(ElementsFromData value) {
      this.elementsfromdata = value;
      return this;
   }

   public EndContainerConfig getEndcontainer() {
      return endcontainer;
   }

   public DataMapping setEndcontainer(EndContainerConfig value) {
      this.endcontainer = value;
      return this;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }
   
}
