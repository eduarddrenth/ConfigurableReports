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
import com.vectorprint.IOHelper;
import com.vectorprint.report.data.types.Formatter;
import com.vectorprint.report.data.types.ReportValue;
import com.vectorprint.report.data.types.TextValue;
import com.vectorprint.report.itext.annotations.CONTAINER_ELEMENT;
import com.vectorprint.report.itext.annotations.ContainerEnd;
import com.vectorprint.report.itext.annotations.ContainerStart;
import com.vectorprint.report.itext.annotations.Containers;
import com.vectorprint.report.itext.annotations.Element;
import com.vectorprint.report.itext.annotations.Elements;
import com.vectorprint.report.itext.annotations.MultipleFromData;
import com.vectorprint.report.itext.jaxb.Datamappingstype;
import com.vectorprint.report.itext.jaxb.Datamappingtype;
import com.vectorprint.report.itext.jaxb.Elementtype;
import com.vectorprint.report.itext.jaxb.ObjectFactory;
import com.vectorprint.report.itext.jaxb.Startcontainertype;
import com.vectorprint.report.itext.mappingconfig.model.DataMapping;
import com.vectorprint.report.itext.mappingconfig.model.DatatypeConfig;
import com.vectorprint.report.itext.mappingconfig.model.ElementConfig;
import com.vectorprint.report.itext.mappingconfig.model.ElementsFromData;
import com.vectorprint.report.itext.mappingconfig.model.EndContainerConfig;
import com.vectorprint.report.itext.mappingconfig.model.StartContainerConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class DatamappingHelper {

   private static Logger logger = Logger.getLogger(DatamappingHelper.class.getName());

   public static final String XSD = "/xsd/DataMappingConfig.xsd";

   public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

   /**
    * when the first argument to {@link #main(java.lang.String[]) } equals this constant an xml file, given in the
    * second arguments as a URL, will be validated
    */
   public static final String VALIDATE = "validate";

   private static JAXBContext JAXBCONTEXT = null;
   private static Schema schema = null;

   static {
      try {
         SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         schema = sf.newSchema(new SAXSource(new InputSource(DatamappingHelper.class.getResourceAsStream(XSD))));
         JAXBCONTEXT = JAXBContext.newInstance("com.vectorprint.report.itext.jaxb");
      } catch (JAXBException ex) {
         logger.log(Level.SEVERE, "failed to load jaxb context", ex);
      } catch (SAXException ex) {
         logger.log(Level.SEVERE, "failed to load schema", ex);
      }
   }

   public static Datamappingstype fromXML(Reader xml) throws JAXBException {
      Unmarshaller um = JAXBCONTEXT.createUnmarshaller();
      um.setSchema(schema);
      JAXBElement jb = (JAXBElement) um.unmarshal(xml);
      return (Datamappingstype) jb.getValue();
   }

   public static JAXBContext getJAXBCONTEXT() {
      return JAXBCONTEXT;
   }

   public static Schema getSchema() {
      return schema;
   }

   public static void validateXml(InputStream xml) throws SAXException, IOException {
      schema.newValidator().validate(new SAXSource(new InputSource(xml)));
   }

   public static void validateXml(Reader xml) throws SAXException, IOException {
      schema.newValidator().validate(new SAXSource(new InputSource(xml)));
   }

   public static void validateXml(URL xml) throws SAXException, IOException {
      validateXml(xml.openStream());
   }

   public static void validateXml(String xml) throws SAXException, IOException {
      validateXml(new StringReader(xml));
   }

   /**
    * prints xsd, or validate xml (no exceptions => ok)
    *
    * @see #VALIDATE
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException, SAXException {
      if (args != null && args.length > 1 && VALIDATE.equals(args[0])) {
         validateXml(new URL(args[1]));
      } else {
         System.out.println(getXsd());
      }
   }

   public static String getXsd() throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      IOHelper.load(DatamappingHelper.class.getResourceAsStream(XSD), out);
      return out.toString();
   }

   private static boolean idOK(String requestedId, String actualId) {
      return (requestedId == null && actualId == null) || requestedId.equals(actualId);
   }

   /**
    * returns the first Datamapping found for an object or Class. A datamapping is valid for an object when either a
    * regex is found in the classname of the object or the classname of the object equals the configured classname, when
    * an id is provided the id in the datamapping found must match this id, when an id is not provided an id a datamapping
    * with an id is not valid.
    *
    * @param data the object or class for which a data mapping is searched
    * @param id an optional id that must be matched by the datamapping
    * @param dataMappingConfig
    * @return
    */
   public static Datamappingtype findDataMapping(Object data, String id, Datamappingstype dataMappingConfig) {
      if (null != data) {
         Class clazz = (Class) ((data instanceof Class) ? data : data.getClass());
         for (Datamappingtype dt : dataMappingConfig.getDatamapping()) {
            if (dt.isRegex() && Pattern.compile(dt.getClassname()).matcher(clazz.getName()).find() && idOK(id, dt.getId())) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine(String.format("Datamapping found: %s matches regex %s (requested id: %s)", clazz.getName(), dt.getClassname(), id));
               }
               return dt;
            } else if (clazz.getName().equals(dt.getClassname()) && idOK(id, dt.getId())) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine(String.format("Datamapping found: %s matches %s (requested id: %s)", clazz.getName(), dt.getClassname(), id));
               }
               return dt;
            }
            if (logger.isLoggable(Level.FINE)) {
               logger.fine(String.format("%s does not match %s (regex: %s, requested id %s)", dt.getClassname(), clazz.getName(), dt.isRegex(), id));
            }
         }
      }
      return null;
   }

   private final Map<String, DataMapping> dataMappings = new HashMap<String, DataMapping>(10);

   private static final Map<Class, List<StartContainerConfig>> cacheSCC = new HashMap<Class, List<StartContainerConfig>>(1);
   private static final Map<Class, List<ElementConfig>> cacheEC = new HashMap<Class, List<ElementConfig>>(1);
   private static final Map<Class, ElementsFromData> cacheM = new HashMap<Class, ElementsFromData>(1);
   private static final Map<Class, EndContainerConfig> cacheECC = new HashMap<Class, EndContainerConfig>(1);

   /**
    * Find a datamapping to start a container based on class annotation, uses static cache.
    * @param clazz
    * @return 
    */
   public static List<StartContainerConfig> getContainers(Class clazz) {
      if (!cacheSCC.containsKey(clazz)) {
         cacheSCC.put(clazz, new ArrayList<StartContainerConfig>(1));
         ContainerStart cs = (ContainerStart) clazz.getAnnotation(ContainerStart.class);
         if (cs != null) {
            cacheSCC.get(clazz).add(fromContainerStart(cs));
         }
         Containers c = (Containers) clazz.getAnnotation(com.vectorprint.report.itext.annotations.Containers.class);
         if (c != null) {
            for (ContainerStart s : c.containers()) {
               cacheSCC.get(clazz).add(fromContainerStart(s));
            }
         }
      }
      return cacheSCC.get(clazz);
   }

   private static StartContainerConfig fromContainerStart(ContainerStart cs) {
      return (StartContainerConfig) new StartContainerConfig()
          .setAdddata(cs.addData())
          .setContainertype(cs.containerType())
          .setContainertypemethod(cs.containerTypeMethod())
          .setSectionlevel(cs.sectionLevel())
          .addStyleClasses(cs.styleClasses())
          .setStyleclassesmethod(cs.styleClassesMethod())
          .setValueasstringmethod(cs.dataFunction().getValueAsStringMethod());
   }

   private static ElementConfig fromAnnotation(Element e) {
      if (e != null) {
         return (ElementConfig) new ElementConfig()
             .setElementtype(e.iTextClass())
             .setElementtypemethod(e.iTextClassMethod())
             .addStyleClasses(e.styleClasses())
             .setStyleclassesmethod(e.styleClassesMethod())
             .setValueasstringmethod(e.dataFunction().getValueAsStringMethod())
             .setDatatype(new DatatypeConfig()
                 .setDataclass(e.dataType().dataClass())
                 .setFormat(e.dataType().format()));
      } else {
         return null;
      }
   }

   /**
    * Find a datamapping to create an element based on class annotation, uses static cache.
    * @param clazz
    * @return 
    */
   public static List<ElementConfig> getElements(Class clazz) {
      if (!cacheEC.containsKey(clazz)) {
         cacheEC.put(clazz, new ArrayList<ElementConfig>(1));
         Element e = (Element) clazz.getAnnotation(Element.class);
         if (e != null) {
            cacheEC.get(clazz).add(fromAnnotation(e));
         }
         Elements es = (Elements) clazz.getAnnotation(com.vectorprint.report.itext.annotations.Elements.class);
         if (es != null) {
            for (Element s : es.elements()) {
               cacheEC.get(clazz).add(fromAnnotation(s));
            }
         }
      }
      return cacheEC.get(clazz);
   }

   private static ElementConfig fromJaxb(Elementtype e) throws ClassNotFoundException {
      return (ElementConfig) new ElementConfig()
          .setElementtype((Class<? extends com.itextpdf.text.Element>) Class.forName(com.itextpdf.text.Element.class.getPackage().getName() + '.'
                  + e.getElementtype().value()))
          .setElementtypemethod(e.getElementtypemethod())
          .addStyleClasses(e.getStyleclass().toArray(new String[e.getStyleclass().size()]))
          .setStyleclassesmethod(e.getStyleclassesmethod())
          .setValueasstringmethod(e.getValueasstringmethod())
          .setDatatype(new DatatypeConfig()
              .setDataclass((e.getDatatype() != null) ? (Class<? extends ReportValue>) Class.forName(e.getDatatype().getDataclass()) : TextValue.class)
              .setFormat((e.getDatatype() != null) ? e.getDatatype().getFormat() : ""));
   }

   /**
    * Translate annotations (only when {@link Datamappingstype#isUseannotations() } is true) and xml configuration into {@link DataMapping} for use in a {@link DatamappingProcessor}, uses instance cache.
    * XML config will override Annotations on classes.
    *
    * @param dataClass
    * @param id an optional id to find in xml configuration
    * @param datamappingstype may be null if no xml config is used
    * @see #fromXML(java.io.Reader)
    * @return
    */
   public final DataMapping toDataConfig(Class dataClass, String id, Datamappingstype datamappingstype) throws ClassNotFoundException {
      if (!dataMappings.containsKey(dataClass.getName()+id)) {
         
         boolean useAnnotations = datamappingstype == null || datamappingstype.isUseannotations();

         Datamappingtype jaxbMapping = (datamappingstype != null) ? findDataMapping(dataClass, id, datamappingstype) : null;

         DataMapping dm = new DataMapping();
         dataMappings.put(dataClass.getName()+id, dm);

         if (jaxbMapping != null && jaxbMapping.getStartcontainer() != null && jaxbMapping.getStartcontainer().size() > 0) {
            dm.setId(jaxbMapping.getId());
            for (Startcontainertype cs : jaxbMapping.getStartcontainer()) {
               dm.addStartcontainer((StartContainerConfig) new StartContainerConfig()
                   .setAdddata(cs.isAdddata())
                   .setContainertype(CONTAINER_ELEMENT.valueOf(cs.getContainertype().name()))
                   .setContainertypemethod(cs.getContainertypemethod())
                   .setSectionlevel(cs.getSectionlevel().intValue())
                   .addStyleClasses(cs.getStyleclass().toArray(new String[cs.getStyleclass().size()]))
                   .setStyleclassesmethod(cs.getStyleclassesmethod())
                   .setValueasstringmethod(cs.getValueasstringmethod()));
            }

         }

         if (useAnnotations && dm.getStartcontainer().isEmpty()) {
            List<StartContainerConfig> scc = getContainers(dataClass);
            if (scc != null) {
               for (StartContainerConfig s : scc) {
                  dm.addStartcontainer(s);
               }
            }
         }

         if (jaxbMapping != null && jaxbMapping.getElement() != null && jaxbMapping.getElement().size() > 0) {
            for (Elementtype e : jaxbMapping.getElement()) {
               dm.addElement(fromJaxb(e));
            }
         }

         if (useAnnotations && dm.getElement().isEmpty()) {
            List<ElementConfig> ec = getElements(dataClass);
            if (ec != null) {
               for (ElementConfig e : ec) {
                  dm.addElement(e);
               }
            }
         }

         if (jaxbMapping != null && jaxbMapping.getElementsfromdata() != null) {
            dm.setElementsfromdata(new ElementsFromData()
                .setElement(fromJaxb(jaxbMapping.getElementsfromdata().getElement()))
                .setDatalistmethod(jaxbMapping.getElementsfromdata().getDatalistmethod()));
         }

         if (useAnnotations && dm.getElementsfromdata() == null) {
            if (cacheM.containsKey(dataClass)) {
               dm.setElementsfromdata(cacheM.get(dataClass));
            } else {
               MultipleFromData mfd = (MultipleFromData) dataClass.getAnnotation(MultipleFromData.class);
               if (mfd != null) {
                  dm.setElementsfromdata(new ElementsFromData()
                      .setDatalistmethod(mfd.dataListMethod())
                      .setElement(fromAnnotation(mfd.element())));
                  cacheM.put(dataClass, dm.getElementsfromdata());
               }

            }
         }

         if (jaxbMapping != null && jaxbMapping.getEndcontainer() != null) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine(String.format("using xml configuration for end of container of %s ", dataClass.getName()));
            }
            dm.setEndcontainer(new EndContainerConfig().setContainertype(
                CONTAINER_ELEMENT.valueOf(jaxbMapping.getEndcontainer().getContainertype().name())).
                setDepthtoend(jaxbMapping.getEndcontainer().getDepthtoend().intValue()));
         }

         if (useAnnotations && dm.getEndcontainer() == null) {
            if (cacheECC.containsKey(dataClass)) {
               dm.setEndcontainer(cacheECC.get(dataClass));
            } else {
               ContainerEnd endAnnotation = (ContainerEnd) dataClass.getAnnotation(com.vectorprint.report.itext.annotations.ContainerEnd.class);
               if (endAnnotation != null) {
                  if (logger.isLoggable(Level.FINE)) {
                     logger.fine(String.format("using annotation for end of container of %s ", dataClass.getName()));
                  }
                  dm.setEndcontainer(new EndContainerConfig().setContainertype(endAnnotation.containerType())
                      .setDepthtoend(endAnnotation.depthToEnd()));
                  cacheECC.put(dataClass, dm.getEndcontainer());
               }
            }
         }
      }
      return dataMappings.get(dataClass.getName()+id);
   }

}
