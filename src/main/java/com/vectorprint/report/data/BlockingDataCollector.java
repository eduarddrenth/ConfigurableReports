/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import com.vectorprint.VectorPrintRuntimeException;
import com.vectorprint.configuration.annotation.Setting;
import com.vectorprint.report.itext.BaseReportGenerator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a DataCollector that uses a {@link BlockingDeque} for collecting and processing data, so it starts
 * processing data immediately after it becomes available. This implementation makes it possible to use a separate
 * Thread for collecting data and processing data.
 *
 * @see BaseReportGenerator#processData(com.vectorprint.report.data.ReportDataHolder)
 * @author Eduard Drenth at VectorPrint.nl
 */
public abstract class BlockingDataCollector extends DataCollectorImpl implements Thread.UncaughtExceptionHandler {

   private ReportDataHolderImpl holder;

   @Override
   public final void uncaughtException(Thread t, Throwable e) {
      Logger.getLogger(BlockingDataCollector.class.getName()).log(Level.SEVERE, "exception during data collection", e);
      try {
         ((BlockingQueue)holder.getData()).put(new ReportDataHolder.IdData(e, null));
      } catch (InterruptedException ex) {
        Logger.getLogger(BlockingDataCollector.class.getName()).log(Level.SEVERE, "failed to notify consumer", ex);
      }
   }
   public static final int EXITFROMCOLLECTIONTHREAD = -3;

   /**
    * Signals the end of the data stream to the queue consumer
    */
   public enum QUEUECONTROL { END }
   /**
    * name of the integer property that will determine the size of the queue for the data objects
    */
   public static final String DATAQUEUESIZE = "dataqueuesize";
   /**
    * the amount of milliseconds to wait when adding data to the queue for data objects
    */
   public static final String QUEUETIMEOUT = "queuetimeout";
   public static final Long DEFAULTTIMEOUT = 5000l;
   @Setting(keys = DATAQUEUESIZE)
   private int dataQueueSize = DEFAULTQUEUESIZE;
   @Setting(keys = QUEUETIMEOUT)
   private long queueTimeout = DEFAULTTIMEOUT;
   
   public static final int DEFAULTQUEUESIZE = 50;

   @Override
   public final ReportDataHolderImpl getDataHolder() {
      return holder;
   }

   /**
    * Starts a data collecting Thread that will call {@link #collectData() }
    * @return 
    */
   @Override
   public final ReportDataHolderImpl collect() {
      holder = new BlockingDataHolder(dataQueueSize, queueTimeout);
      Runnable r = new collectThread();
      Thread t = new Thread(r, DATA_COLLECTOR_THREAD);
      t.setUncaughtExceptionHandler(this);
      t.start();
      return holder;
   }
   public static final String DATA_COLLECTOR_THREAD = "data collector thread";

   /**
    * add your data using this method, BE SURE TO have a consumer for your data, by default {@link BaseReportGenerator#processData(com.vectorprint.report.data.ReportDataHolder)
    * }
    * will consume your data, but if you override {@link BaseReportGenerator#createReportBody(com.itextpdf.text.Document, com.vectorprint.report.data.ReportDataHolder, com.itextpdf.text.pdf.PdfWriter)
    * }, your report application may fail.
    */
   public abstract void collectData();

   private class collectThread implements Runnable {

      @Override
      public void run() {
         BlockingDataCollector.this.collectData();
         BlockingDataCollector.this.add(QUEUECONTROL.END, null);
      }
   }

   private static class BlockingDataHolder extends ReportDataHolderImpl {

      private long timeout;

      private BlockingDataHolder(int size, long timeout) {
         setData(new LinkedBlockingQueue<IdData>(size));
         this.timeout = timeout;
      }

      /**
       * Offers the data to the {@link BlockingQueue}
       * @param data the value of data
       */
      @Override
      public final void add(IdData data) {
         try {
            if (!((BlockingQueue) getData()).offer(data, timeout, TimeUnit.MILLISECONDS)) {
               throw new VectorPrintRuntimeException(String.format("Failed to queue data within %s milliseconds, your application should read the queue by calling BaseReportGenerator.processData()", timeout));
            }
         } catch (InterruptedException ex) {
            throw new VectorPrintRuntimeException(ex);
         }
      }
   }
}
