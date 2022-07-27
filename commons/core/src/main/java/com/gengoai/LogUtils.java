/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai;

import com.gengoai.config.Config;
import com.gengoai.io.Resources;
import com.gengoai.reflection.Reflect;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Utilities for working with java.util.Logger.
 */
public final class LogUtils {
   /**
    * Instance of the MangoLogFormatter to share amongst Handlers.
    */
   public static final Formatter FORMATTER = new MangoLogFormatter();
   public static final Filter LOG_FILTER = record -> !record.getSourceClassName().startsWith("java.") &&
         !record.getSourceClassName().startsWith("sun.") &&
         !record.getSourceClassName().startsWith("javax.");
   /**
    * The root logger
    */
   public static final Logger ROOT = Logger.getLogger("");

   private LogUtils() {
      throw new IllegalAccessError();
   }

   /**
    * Adds a file handler that writes to the location specified in <code>com.gengoai.logging.dir</code> or if not set
    * <code>USER_HOME/logs/</code>. The filenames are in the form of <code>basename%g</code> where %g is the rotated
    * file number. Max file size is 100MB and 50 files will be used.
    *
    * @param basename the basename
    * @throws IOException the io exception
    */
   public synchronized static void addFileHandler(String basename) throws IOException {
      String dir = Strings.appendIfNotPresent(Config.get("com.gengoai.logging.dir")
                                                    .asString(SystemInfo.USER_HOME + "/logs/"), "/");
      Resources.from(dir).mkdirs();
      FileHandler fh = new FileHandler(new File(dir + basename + ".log").getAbsolutePath(), false);
      fh.setFilter(LOG_FILTER);
      fh.setLevel(Config.get("com.gengoai.logging.fileLevel")
                        .as(Level.class, Level.FINE));
      fh.setFormatter(FORMATTER);
      addHandler(fh);
   }

   /**
    * Adds a handler to the root.
    *
    * @param handler the handler to add
    */
   public synchronized static void addHandler(Handler handler) {
      ROOT.addHandler(handler);
   }

   /**
    * Gets the global Logger
    *
    * @return The global logger
    */
   public static Logger getGlobalLogger() {
      return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   }

   /**
    * Gets a logger for the given class..
    *
    * @param clazz the class whose name will become the logger name
    * @return the logger
    */
   public static Logger getLogger(@NonNull Class<?> clazz) {
      return Logger.getLogger(clazz.getName());
   }

   /**
    * Gets the root logger.
    *
    * @return the root logger
    */
   public static Logger getRootLogger() {
      return Logger.getLogger("");
   }

   /**
    * Logs a message at a given level.
    *
    * @param logger  the logger
    * @param level   The level to log the message at.
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void log(@NonNull Logger logger,
                          @NonNull Level level,
                          String message,
                          Object... params) {
      logger.log(level, message, params);
   }

   /**
    * Logs the given throwable at a given level.
    *
    * @param logger    the logger
    * @param level     The level to log the message at.
    * @param throwable The throwable for the message.
    */
   public static void log(@NonNull Logger logger,
                          @NonNull Level level,
                          @NonNull Throwable throwable) {
      logger.log(level, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at a given level.
    *
    * @param logger    the logger
    * @param level     The level to log the message at.
    * @param message   The message accompanying the log
    * @param throwable The throwable for the message.
    */
   public static void log(@NonNull Logger logger,
                          @NonNull Level level,
                          String message,
                          @NonNull Throwable throwable) {
      logger.log(level, message, throwable);
   }

   /**
    * Logs a message at {@link Level#CONFIG}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logConfig(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.CONFIG, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#CONFIG}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logConfig(@NonNull Logger logger,
                                String message,
                                @NonNull Throwable throwable) {
      logger.log(Level.CONFIG, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#CONFIG}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logConfig(@NonNull Logger logger,
                                @NonNull Throwable throwable) {
      logger.log(Level.CONFIG, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#FINE}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logFine(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.FINE, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#FINE}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logFine(@NonNull Logger logger, String message, @NonNull Throwable throwable) {
      logger.log(Level.FINE, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#FINE}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logFine(@NonNull Logger logger, @NonNull Throwable throwable) {
      logger.log(Level.FINE, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#FINER}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logFiner(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.FINER, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#FINER}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logFiner(@NonNull Logger logger, String message, @NonNull Throwable throwable) {
      logger.log(Level.FINER, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#FINER}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logFiner(@NonNull Logger logger, @NonNull Throwable throwable) {
      logger.log(Level.FINER, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#FINEST}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logFinest(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.FINEST, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#FINEST}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logFinest(@NonNull Logger logger, String message, @NonNull Throwable throwable) {
      logger.log(Level.FINEST, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#FINEST}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logFinest(@NonNull Logger logger, @NonNull Throwable throwable) {
      logger.log(Level.FINEST, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#INFO}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logInfo(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.INFO, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#INFO}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logInfo(@NonNull Logger logger, String message, @NonNull Throwable throwable) {
      logger.log(Level.INFO, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#INFO}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logInfo(@NonNull Logger logger, @NonNull Throwable throwable) {
      logger.log(Level.INFO, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#SEVERE}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logSevere(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.SEVERE, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#SEVERE}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logSevere(@NonNull Logger logger,
                                String message,
                                @NonNull Throwable throwable) {
      logger.log(Level.SEVERE, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#SEVERE}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logSevere(@NonNull Logger logger,
                                @NonNull Throwable throwable) {
      logger.log(Level.SEVERE, throwable.getMessage(), throwable);
   }

   /**
    * Logs a message at {@link Level#WARNING}.
    *
    * @param logger  the logger
    * @param message The message accompanying the log
    * @param params  The arguments for the message.
    */
   public static void logWarning(@NonNull Logger logger, String message, Object... params) {
      logger.log(Level.WARNING, message, params);
   }

   /**
    * Logs the given throwable at {@link Level#WARNING}.
    *
    * @param logger    the logger
    * @param message   The message accompanying the log
    * @param throwable The throwable to log.
    */
   public static void logWarning(@NonNull Logger logger,
                                 String message,
                                 @NonNull Throwable throwable) {
      logger.log(Level.WARNING, message, throwable);
   }

   /**
    * Logs the given throwable at {@link Level#WARNING}.
    *
    * @param logger    the logger
    * @param throwable The throwable to log.
    */
   public static void logWarning(@NonNull Logger logger,
                                 @NonNull Throwable throwable) {
      logger.log(Level.WARNING, throwable.getMessage(), throwable);
   }

   /**
    * Sets the level of a logger
    *
    * @param logger The name of the logger to set the level for.
    * @param level  The level to set the logger at
    */
   public static void setLevel(String logger, Level level) {
      Logger log = Logger.getLogger(logger);
      log.setLevel(level);
      Class<?> c = Reflect.getClassForNameQuietly(logger);
      if (c != null) {
         try {
            Logger clog = Reflect.onClass(c)
                                 .allowPrivilegedAccess()
                                 .getField("log")
                                 .get();
            clog.setLevel(level);
         } catch (Exception e) {
            //ignore
         }
      }
      LogManager.getLogManager()
                .getLoggerNames()
                .asIterator()
                .forEachRemaining(loggerName -> {
                   if (loggerName.startsWith(logger) && !loggerName.equals(logger)) {
                      Logger.getLogger(loggerName).setLevel(level);
                   }
                });
   }

}//END OF LogUtils
