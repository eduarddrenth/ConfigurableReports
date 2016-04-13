package com.vectorprint.report.data;

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

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DefaultDataCollectionMessages implements DataCollectionMessages<String> {

   private Map<Level, List<String>> messages = new EnumMap<>(Level.class);

   {
      for (Level l : Level.values()) {
         messages.put(l, new ArrayList<>(3));
      }
   }

   @Override
   public List<String> getMessages(Level l) {
      return messages.get(l);
   }

   @Override
   public void addMessage(Level l, String message) {
      messages.get(l).add(message);
   }
}
