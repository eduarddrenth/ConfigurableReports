package com.vectorprint.report.itext.style.stylers;

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
//~--- non-JDK imports --------------------------------------------------------
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.VectorPrintBaseFont;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.vectorprint.ArrayHelper;
import com.vectorprint.IOHelper;
import com.vectorprint.VectorPrintException;
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.certificates.CertificateHelper;
import com.vectorprint.configuration.EnhancedMap;
import com.vectorprint.configuration.parameters.BooleanParameter;
import com.vectorprint.configuration.parameters.CharPasswordParameter;
import com.vectorprint.configuration.parameters.IntParameter;
import com.vectorprint.configuration.parameters.PasswordParameter;
import com.vectorprint.configuration.parameters.StringParameter;
import com.vectorprint.configuration.parameters.URLParameter;
import com.vectorprint.report.ReportConstants;
import com.vectorprint.report.data.ReportDataHolder;
import com.vectorprint.report.itext.ItextHelper;
import com.vectorprint.report.itext.style.BaseStyler;
import com.vectorprint.report.itext.style.DocumentStyler;
import com.vectorprint.report.itext.style.StylerFactoryHelper;
import com.vectorprint.report.itext.style.parameters.DigestParameter;
import com.vectorprint.report.itext.style.parameters.EncryptionParameter;
import com.vectorprint.report.itext.style.parameters.FloatArrayParameter;
import com.vectorprint.report.itext.style.parameters.FloatParameter;
import com.vectorprint.report.itext.style.parameters.KeyStoreParameter;
import com.vectorprint.report.itext.style.parameters.PermissionsParameter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- JDK imports ------------------------------------------------------------
/**
 * settings for a pdf, supports page size, margins and encryption. Sets pdf version to 5 unless pdf/1a is required and a
 * viewer preference to toggle visibility of content groups.
 *
 * @author Eduard Drenth at VectorPrint.nl
 * @param <RD>
 */
public class DocumentSettings<RD extends ReportDataHolder> extends AbstractStyler implements DocumentStyler<RD> {

   /**
    * key for looking up stylers in the settings that will be used for styling the titles of the table of contents
    */
   public static final String TOCTITLESTYLEKEY = "toctitle";
   /**
    * the default stylers used for styling the titles of the table of contents
    */
   public static final String[] TOCTITLESTYLE = new String[]{"Font(size=12)", "Padding(padding=0)", "Border(position=none)"};
   /**
    * key for looking up stylers in the settings that will be used for styling the page numbers of the table of contents
    */
   public static final String TOCNRSTYLEKEY = "tocnumber";
   /**
    * the default stylers used for styling the page numbers of the table of contents
    */
   public static final String[] TOCNRSTYLE = new String[]{"Font(size=12)", "Alignment(align=RIGHT)", "Border(position=none)"};
   /**
    * key for looking up stylers in the settings that will be used for styling the table of contents table. The default
    * value for this will be calculated based on document measures
    * ("Table(columns=2,widths=&lt;0.90%>|&lt;0.10%>);AddCell(data=Table of
    * Contents,styleclass=toccaption);AddCell(data=title,styleclass=tocheader);AddCell(data=page,styleclass=tocheader)")
    */
   public static final String TOCTABLEKEY = "toctable";
   /**
    * parameter key for setting the width percentage of the left column in the table of contents
    */
   public static final String TOCLEFTWIDTH = "tocleftwidth";
   /**
    * parameter key for setting the width percentage of the right column in the table of contents
    */
   public static final String TOCRIGHTWIDTH = "tocrightwidth";
   /**
    * parameter to set the maximum depth to include in the table of contents
    */
   public static final String TOCMAXDEPTH = "tocmaxdepth";
   /**
    * key for looking up stylers in the settings that will be used for styling the header in the table of contents
    * table.
    */
   public static final String TOCHEADERSTYLEKEY = "tocheader";
   /**
    * default style for the header in the table of contents table.
    */
   public static final String[] TOCHEADER = new String[]{"Font(style=bold)", "Padding(position=bottom,padding=3)", "Padding(position=top,padding=5)", "Border(position=none)"};
   /**
    * key for looking up stylers in the settings that will be used for the caption of the table of contents.
    */
   public static final String TOCCAPTIONKEY = "toccaption";
   /**
    * default style for the caption of the table of contents.
    */
   public static final String[] TOCCAPTION = new String[]{"Font(style=bold,size=14)", "Padding(position=bottom,padding=3)", "Border(position=none)", "Alignment(align=center_middle)", "ColRowSpan(colspan=2)"};
   /**
    * parameter to print a table of contents or not
    */
   public static final String TOC = "toc";
   /**
    * parameter to print dots between titles and page numbers
    */
   public static final String TOCDOTS = "tocdots";
   /**
    * parameter to print the table of contents first or last.
    */
   public static final String TOCAPPEND = "appendtoc";

   public static final String WIDTH = "width";
   public static final String HEIGHT = "height";
   public static final String USER_PASSWORD = "userpassword";
   /**
    * parameter to set both user- and owner password
    */
   public static final String PASSWORD = "password";
   public static final String OWNER_PASSWORD = "ownerpassword";
   public static final String KEYSTORE_PASSWORD = "keystorepassword";
   public static final String KEYSTORETYPE_PARAM = "keystoretype";
   public static final String CERTIFICATE = "certificate";
   public static final String KEYSTORE = "keystore";
   public static final String ENCRYPTION_PARAM = "encryption";
   public static final String DIGESTPARAM = "digestalgorithm";
   public static final String PERMISSIONS = "permissions";
   public static final String PDFA = "pdfa";
   public static final String TITLE = "title";
   public static final String SUBJECT = "subject";
   public static final String KEYWORDS = "keywords";
   public static final String CREATOR = "creator";
   public static final String AUTHOR = "author";
   public static final String FONTS = "fonts";
   private static final Logger log = Logger.getLogger(DocumentSettings.class.getName());
   private Document document;
   private PdfWriter writer;
   private RD data;

   /**
    * adds a visible signature of 200 / 100 at top left of the first page of the pdf with "verify origin" as reason, the
    * localhost name as location. Uses MakeSignature.signDetached(psa, as, pks, certificateChain, null, null, null, 0,
    * MakeSignature.CryptoStandard.CMS)
    *
    * @see #loadKeyStore(char[])
    * @see #getKey(java.security.KeyStore, java.lang.String, char[]) }
    * @param psa
    * @throws KeyStoreException
    * @throws NoSuchAlgorithmException
    * @throws UnrecoverableKeyException
    * @throws VectorPrintException
    */
   @Override
   public void configureVisualSignature(PdfSignatureAppearance psa)
       throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, VectorPrintException {
      psa.setReason("verify origin");
      try {
         psa.setLocation(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException ex) {
         log.log(Level.WARNING, "unable to set location for pdf signature", ex);
      }

      char[] pw = getValue(KEYSTORE_PASSWORD, char[].class);
      char[] clone = pw.clone();
      KeyStore ks = loadKeyStore(pw);

      PrivateKey key = getKey(ks, (String) ks.aliases().nextElement(), clone);
      Certificate[] certificateChain = ks.getCertificateChain((String) ks.aliases().nextElement());

      PrivateKeySignature pks = new PrivateKeySignature(key, getValue(DIGESTPARAM, DIGESTALGORITHM.class).name(), "BC");
      ExternalDigest as = new BouncyCastleDigest();

      psa.setVisibleSignature(new Rectangle(0, getHeight() - 100, 200, getHeight()), 1, "signature");
      try {
         MakeSignature.signDetached(psa, as, pks, certificateChain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
      } catch (IOException ex) {
         throw new VectorPrintException(ex);
      } catch (DocumentException ex) {
         throw new VectorPrintException(ex);
      } catch (GeneralSecurityException ex) {
         throw new VectorPrintException(ex);
      }
   }

   /**
    * If your settings contain a key {@link #FONTS} it is assumed to be a list of directory names where fonts are loaded
    * from.
    *
    * @see EnhancedMap#getStringProperties(java.lang.String..., java.lang.String...)
    * @see FontFactory#registerDirectory(java.lang.String)
    * @throws VectorPrintException
    */
   @Override
   public void loadFonts() throws VectorPrintException {
      if (getSettings().containsKey(FONTS)) {
         if (getValue(PDFA, Boolean.class)) {
            FontFactory.defaultEmbedding = true;
         }
         log.info("loading fonts from " + Arrays.asList(getSettings().getStringProperties(null, FONTS)));
         for (String dir : getSettings().getStringProperties(null, FONTS)) {
            int i = FontFactory.registerDirectory(dir);
            log.fine(String.format("%s fonts loaded from %s", i, dir));
         }
         log.info("fonts available: " + FontFactory.getRegisteredFonts());
      } else {
         log.warning(String.format("setting \"%s\" for font directory not found, not loading fonts", FONTS));
      }
   }

   @Override
   public final <E> E style(E element, Object data) throws VectorPrintException {
      document.setPageSize(new Rectangle(getValue(WIDTH, Float.class), getValue(HEIGHT, Float.class)));
      document.setMargins(
          getValue(ReportConstants.MARGIN.margin_left.name(), Float.class),
          getValue(ReportConstants.MARGIN.margin_right.name(), Float.class),
          getValue(ReportConstants.MARGIN.margin_top.name(), Float.class),
          getValue(ReportConstants.MARGIN.margin_bottom.name(), Float.class));
      writer.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
      writer.addViewerPreference(PdfName.NONFULLSCREENPAGEMODE, PdfName.USEOC);
      writer.addViewerPreference(PdfName.PRINT, PdfName.DUPLEX);
      document.addProducer();
      document.addCreationDate();
      byte[] password = getValue(PASSWORD, byte[].class);
      byte[] userpassword = getValue(USER_PASSWORD, byte[].class);
      byte[] ownerpassword = getValue(OWNER_PASSWORD, byte[].class);
      if (userpassword == null) {
         userpassword = password;
      }
      if (ownerpassword == null) {
         ownerpassword = password;
      }
      int permissions = ((PermissionsParameter) getParameter(PERMISSIONS, PERMISSION[].class)).getPermission();
      ENCRYPTION encryption = getValue(ENCRYPTION_PARAM, ENCRYPTION.class);
      if (userpassword != null) {
         int enc = encryption != null ? encryption.encryption : PdfWriter.ENCRYPTION_AES_128;
         try {
            writer.setEncryption(userpassword, ownerpassword, permissions, enc);
            ArrayHelper.clear(password);
            ArrayHelper.clear(ownerpassword);
            ArrayHelper.clear(userpassword);
         } catch (DocumentException ex) {
            throw new VectorPrintException(ex);
         }
      } else if (getValue(CERTIFICATE, URL.class) != null) {
         int enc = encryption != null ? encryption.encryption : PdfWriter.ENCRYPTION_AES_128;
         try {
            writer.setEncryption(new Certificate[]{loadCertificate()}, new int[]{permissions}, enc);
         } catch (DocumentException ex) {
            throw new VectorPrintException(ex);
         }
      }

      for (PDFBOX b : PDFBOX.values()) {
         if (getValue(b.name(), float[].class) != null) {
            float[] size = getValue(b.name(), float[].class);
            writer.setBoxSize(b.name(), new Rectangle(size[0], size[1], size[2], size[3]));
         }
      }

      document.addSubject(getValue(SUBJECT, String.class));
      document.addAuthor(getValue(AUTHOR, String.class));
      document.addTitle(getValue(TITLE, String.class));
      document.addCreator(getValue(CREATOR, String.class));
      document.addKeywords(getValue(KEYWORDS, String.class));

      if (getValue(PDFA, Boolean.class)) {
         try {
            pdfA1A(writer);
         } catch (IOException ex) {
            throw new VectorPrintException(ex);
         } catch (DocumentException ex) {
            throw new VectorPrintException(ex);
         }
      }
      writer.createXmpMetadata();

      return (E) document;
   }

   @Override
   public final boolean canStyle(Object element) {
      return element instanceof Document;
   }

   @Override
   public final boolean creates() {
      return false;
   }

   @Override
   public String getHelp() {
      return "Manipulate document settings i.e. margins, size, signing, etc.. " + super.getHelp();
   }

   @Override
   public final BaseStyler setStyleClass(String key) {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public final String getStyleClass() {
      return ReportConstants.DOCUMENTSETTINGS;
   }

   @Override
   public <E extends Document> E styleAfterOpen(E element, Object data) throws VectorPrintException {
      boolean icc = false;
      if (getSettings().containsKey(ReportConstants.ICCCOLORPROFILE)) {
         try {
            itextHelper.loadICC(getSettings().getURLProperty(null, ReportConstants.ICCCOLORPROFILE).openStream());
            writer.setOutputIntents("Custom", "", "http://www.color.org",
                getSettings().getProperty("sRGB IEC61966-2.1", ReportConstants.ICCINFO), itextHelper.getiCC_Profile());
            icc = true;
         } catch (IOException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      if (isPdfa() && !icc) {
         try {
            itextHelper.loadICC(DocumentSettings.class.getResourceAsStream(ReportConstants.DEFAULTICCPROFILE));
            writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", itextHelper.getiCC_Profile());
         } catch (IOException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
      return (E) document;
   }

   @Override
   public void setDocument(Document document, PdfWriter writer) {
      this.document = document;
      this.writer = writer;
   }

   @Override
   public void setReportDataHolder(RD data) {
      this.data = data;
   }

   @Override
   public RD getReportDataHolder() {
      return data;
   }

   public enum PDFBOX {

      cropbox, trimbox, bleedbox, artbox
   }

   public enum ENCRYPTION {

      ENC40(PdfWriter.STANDARD_ENCRYPTION_40), ENC128(PdfWriter.STANDARD_ENCRYPTION_128), ENC128AES(PdfWriter.ENCRYPTION_AES_128);

      private ENCRYPTION(int encryption) {
         this.encryption = encryption;
      }

      public int getEncryption() {
         return encryption;
      }
      private int encryption;
   }

   public enum KEYSTORETYPE {

      jks, pkcs12
   }

   public enum PERMISSION {

      PRINT(PdfWriter.ALLOW_PRINTING),
      ASSEMBLY(PdfWriter.ALLOW_ASSEMBLY),
      COPY(PdfWriter.ALLOW_COPY),
      DEGRADEDPRINT(PdfWriter.ALLOW_DEGRADED_PRINTING),
      FILLIN(PdfWriter.ALLOW_FILL_IN),
      ANNOTATE(PdfWriter.ALLOW_MODIFY_ANNOTATIONS),
      CHANGE(PdfWriter.ALLOW_MODIFY_CONTENTS),
      SCREEN(PdfWriter.ALLOW_SCREENREADERS),
      ALL(PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_ASSEMBLY | PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_DEGRADED_PRINTING | PdfWriter.ALLOW_FILL_IN | PdfWriter.ALLOW_MODIFY_ANNOTATIONS | PdfWriter.ALLOW_MODIFY_CONTENTS | PdfWriter.ALLOW_SCREENREADERS);
      private int permission;

      private PERMISSION(int permission) {
         this.permission = permission;
      }

      public int getPermission() {
         return permission;
      }
   }

   public enum DIGESTALGORITHM {

      SHA1, SHA256, SHA384, SHH512
   }

   private void initParams() {
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_top.name(), "float"), DocumentSettings.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_right.name(), "float "), DocumentSettings.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_bottom.name(), "float "), DocumentSettings.class);
      addParameter(new FloatParameter(ReportConstants.MARGIN.margin_left.name(), "float "), DocumentSettings.class);
      addParameter(new EncryptionParameter(ENCRYPTION_PARAM, "type of encryption to use when protecting with a password or a certificate: " + Arrays.asList(ENCRYPTION.values()).toString()), DocumentSettings.class);
      addParameter(new FloatParameter(WIDTH, "float ").setDefault(ItextHelper.mmToPts(210)), DocumentSettings.class);
      addParameter(new FloatParameter(HEIGHT, "float ").setDefault(ItextHelper.mmToPts(297)), DocumentSettings.class);
      addParameter(new BooleanParameter(PDFA, "create a PDF/X-1a pdf"), DocumentSettings.class);
      addParameter(new PasswordParameter(PASSWORD, "password for the document (owner and user)"), DocumentSettings.class);
      addParameter(new PasswordParameter(USER_PASSWORD, "a user password for the document"), DocumentSettings.class);
      addParameter(new PasswordParameter(OWNER_PASSWORD, "owner password for the document"), DocumentSettings.class);
      addParameter(new URLParameter(CERTIFICATE, "the certificate to use for document encryption"), DocumentSettings.class);
      addParameter(new URLParameter(KEYSTORE, "the keystore (.p12, .pfx, .jks) to use for document signing"), DocumentSettings.class);
      addParameter(new CharPasswordParameter(KEYSTORE_PASSWORD, "the password for the signing keystore and key", false), DocumentSettings.class);
      addParameter(new KeyStoreParameter(KEYSTORETYPE_PARAM, "the type of the signing certificate: " + Arrays.asList(KEYSTORETYPE.values()).toString()).setDefault(KEYSTORETYPE.pkcs12), DocumentSettings.class);
      addParameter(new DigestParameter(DIGESTPARAM, "the algorithm for a signature: " + Arrays.asList(DIGESTALGORITHM.values()).toString()).setDefault(DIGESTALGORITHM.SHA1), DocumentSettings.class);
      addParameter(new PermissionsParameter(PERMISSIONS, "permissions for the pdf, use in conjunction with password / encryption: "
          + Arrays.asList(PERMISSION.values()).toString()), DocumentSettings.class);
      addParameter(new StringParameter(TITLE, "a title for the document").setDefault(""), DocumentSettings.class);
      addParameter(new StringParameter(SUBJECT, "a subject for the document").setDefault(""), DocumentSettings.class);
      addParameter(new StringParameter(KEYWORDS, "comma separated keywords for the document").setDefault(""), DocumentSettings.class);
      addParameter(new StringParameter(CREATOR, "creator").setDefault("VectorPrint"), DocumentSettings.class);
      addParameter(new StringParameter(AUTHOR, "author").setDefault(""), DocumentSettings.class);
      for (PDFBOX b : PDFBOX.values()) {
         addParameter(new FloatArrayParameter(b.name(), "llx, lly, urx, ury for this pdf box"), DocumentSettings.class);
      }
      addParameter(new BooleanParameter(TOC, "print table of contents"), DocumentSettings.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(TOCLEFTWIDTH, "width percentage of the left column in table of contents").setDefault(0.9f), DocumentSettings.class);
      addParameter(new com.vectorprint.configuration.parameters.FloatParameter(TOCRIGHTWIDTH, "width percentage of the right column in table of contents").setDefault(0.1f), DocumentSettings.class);
      addParameter(new BooleanParameter(TOCAPPEND, "print table of contents at the end of the document"), DocumentSettings.class);
      addParameter(new BooleanParameter(TOCDOTS, "print dots between titles and page numbers in the table of contents").setDefault(Boolean.TRUE), DocumentSettings.class);
      addParameter(new IntParameter(TOCMAXDEPTH, "the maximum depth to show in the table of contents"), DocumentSettings.class);
   }

   public DocumentSettings() {
      initParams();
   }

   public DocumentSettings(Document document, PdfWriter writer, EnhancedMap settings) throws VectorPrintException {
      StylerFactoryHelper.initStylingObject(this, writer, document, null, null, settings);
      initParams();
   }

   /**
    * loads a certificate using {@link CertificateHelper#loadCertificate(java.io.InputStream) }
    *
    * @return
    * @throws VectorPrintException
    */
   protected Certificate loadCertificate() throws VectorPrintException {
      try {
         return CertificateHelper.loadCertificate(getValue(CERTIFICATE, URL.class).openStream());
      } catch (IOException ex) {
         throw new VectorPrintException(ex);
      } catch (CertificateException ex) {
         throw new VectorPrintException(ex);
      }
   }

   /**
    * loads a keystore using {@link #getSignKeystore() } and {@link #getKeystoretype() }, uses configured
    * {@link #KEYSTORE_PASSWORD keystorepassword}.
    *
    * @return
    * @throws VectorPrintException
    */
   protected KeyStore loadKeyStore(char[] pw) throws VectorPrintException {
      try {
         return CertificateHelper.loadKeyStore(getValue(KEYSTORE, URL.class).openStream(),
             getValue(KEYSTORETYPE_PARAM, KEYSTORETYPE.class).name(), pw);
      } catch (IOException ex) {
         throw new VectorPrintException(ex);
      } catch (KeyStoreException ex) {
         throw new VectorPrintException(ex);
      } catch (NoSuchAlgorithmException ex) {
         throw new VectorPrintException(ex);
      } catch (CertificateException ex) {
         throw new VectorPrintException(ex);
      }
   }

   /**
    * returns a private key from the given keystore, uses configured {@link #KEYSTORE_PASSWORD keystorepassword}
    *
    * @param ks
    * @param alias
    * @return
    * @throws KeyStoreException
    * @throws NoSuchAlgorithmException
    * @throws UnrecoverableKeyException
    */
   protected PrivateKey getKey(KeyStore ks, String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
      return CertificateHelper.getKey(ks, alias, password);
   }

   /**
    * Puts /HELVETICA.pfb in the fontCache as an embedded font.
    *
    * @throws IOException
    * @throws DocumentException
    * @throws VectorPrintException when the argument is not one of the built in fonts
    * @see #pdfA1A(com.itextpdf.text.pdf.PdfWriter)
    *
    */
   protected void builtInFontHack() throws IOException, DocumentException, VectorPrintException {
      ByteArrayOutputStream out = new ByteArrayOutputStream(30720);
      InputStream in = DocumentSettings.class.getResourceAsStream("/" + BaseFont.HELVETICA + ".pfb");
      IOHelper.load(in, out, getSettings().getIntegerProperty(ReportConstants.DEFAULTBUFFERSIZE, ReportConstants.BUFFERSIZE), true);
      VectorPrintBaseFont.cacheAndEmbedBuiltInFont(BaseFont.HELVETICA, out.toByteArray());
   }

   /**
    * called as the last step from {@link #style(java.lang.Object, java.lang.Object) }, calls {@link #builtInFontHack()
    * } which is necessary when creating PDF/X-1a with iText.
    *
    * @param writer
    * @throws IOException
    * @throws DocumentException
    * @throws VectorPrintException
    */
   protected void pdfA1A(PdfWriter writer) throws IOException, DocumentException, VectorPrintException {
      builtInFontHack();
      writer.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
      writer.setTagged();
      writer.setPDFXConformance(PdfWriter.PDFX1A2001);
   }

   public float getMargin_top() {
      return getValue(ReportConstants.MARGIN.margin_top.name(), Float.class);
   }

   public void setMargin_top(float margin_top) {
      setValue(ReportConstants.MARGIN.margin_top.name(), margin_top);
   }

   public float getMargin_right() {
      return getValue(ReportConstants.MARGIN.margin_right.name(), Float.class);
   }

   public void setMargin_right(float margin_right) {
      setValue(ReportConstants.MARGIN.margin_right.name(), margin_right);
   }

   public float getMargin_bottom() {
      return getValue(ReportConstants.MARGIN.margin_bottom.name(), Float.class);
   }

   public void setMargin_bottom(float margin_bottom) {
      setValue(ReportConstants.MARGIN.margin_bottom.name(), margin_bottom);
   }

   public float getMargin_left() {
      return getValue(ReportConstants.MARGIN.margin_left.name(), Float.class);
   }

   public void setMargin_left(float margin_left) {
      setValue(ReportConstants.MARGIN.margin_left.name(), margin_left);
   }

   public float getWidth() {
      return getValue(WIDTH, Float.class);
   }

   public void setWidth(float width) {
      setValue(WIDTH, width);
   }

   public float getHeight() {
      return getValue(HEIGHT, Float.class);
   }

   public void setHeight(float height) {
      setValue(HEIGHT, height);
   }

   public ENCRYPTION getEncryption() {
      return getValue(ENCRYPTION_PARAM, ENCRYPTION.class);
   }

   public void setEncryption(ENCRYPTION encryption) {
      setValue(ENCRYPTION_PARAM, encryption);
   }

   public void setPassword(byte[] password) {
      setValue(PASSWORD, byte[].class);
   }

   public String getTitle() {
      return getValue(TITLE, String.class);
   }

   public void setTitle(String title) {
      setValue(TITLE, title);
   }

   public String getSubject() {
      return getValue(SUBJECT, String.class);
   }

   public void setSubject(String subject) {
      setValue(SUBJECT, subject);
   }

   public String getCreator() {
      return getValue(CREATOR, String.class);
   }

   public void setCreator(String creator) {
      setValue(CREATOR, creator);
   }

   public String getAuthor() {
      return getValue(AUTHOR, String.class);
   }

   public void setAuthor(String author) {
      setValue(AUTHOR, author);
   }

   public String getKeywords() {
      return getValue(KEYWORDS, String.class);
   }

   public void setKeywords(String keywords) {
      setValue(KEYWORDS, keywords);
   }

   public boolean isPdfa() {
      return getValue(PDFA, Boolean.class);
   }

   public void setPdfa(boolean pdfa) {
      setValue(PDFA, pdfa);
   }

   public void setOwnerpassword(byte[] ownerpassword) {
      setValue(OWNER_PASSWORD, ownerpassword);
   }

   public URL getCertificate() {
      return getValue(CERTIFICATE, URL.class);
   }

   public void setCertificate(URL certificate) {
      setValue(CERTIFICATE, certificate);
   }

   public int getPermissions() {
      return ((PermissionsParameter) getParameters().get(PERMISSIONS)).getPermission();
   }

   public void setPermissions(int permissions) {
      setValue(PERMISSIONS, permissions);
   }

   public URL getSignKeystore() {
      return getValue(KEYSTORE, URL.class);
   }

   public void setSignKeystore(URL keystore) {
      setValue(KEYSTORE, keystore);
   }

   public KEYSTORETYPE getKeystoretype() {
      return getValue(KEYSTORETYPE_PARAM, KEYSTORETYPE.class);
   }

   public void setKeystoretype(KEYSTORETYPE keystoretype) {
      setValue(KEYSTORETYPE_PARAM, keystoretype);
   }

   public void setKeystorepassword(char[] keystorepassword) {
      setValue(KEYSTORE_PASSWORD, keystorepassword);
   }

   public float[] getArt() {
      return getValue(PDFBOX.artbox.name(), float[].class);
   }

   public void setArt(float[] art) {
      setValue(PDFBOX.artbox.name(), art);
   }

   public float[] getBleed() {
      return getValue(PDFBOX.bleedbox.name(), float[].class);
   }

   public void setBleed(float[] bleed) {
      setValue(PDFBOX.bleedbox.name(), bleed);
   }

   public float[] getCrop() {
      return getValue(PDFBOX.cropbox.name(), float[].class);
   }

   public void setCrop(float[] crop) {
      setValue(PDFBOX.cropbox.name(), crop);
   }

   public float[] getTrim() {
      return getValue(PDFBOX.trimbox.name(), float[].class);
   }

   public void setTrim(float[] trim) {
      setValue(PDFBOX.trimbox.name(), trim);
   }
   private static final Class<Object>[] classes = new Class[]{Document.class};
   private static final Set<Class> c = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(classes)));

   @Override
   public Set<Class> getSupportedClasses() {
      return c;
   }

   @Override
   public ReportDataHolder getData() {
      return data;
   }
}
