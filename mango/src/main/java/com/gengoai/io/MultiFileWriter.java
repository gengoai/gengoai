package com.gengoai.io;

import com.gengoai.Validation;
import com.gengoai.io.resource.Resource;
import com.gengoai.string.Strings;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A writer that will write content to different files using a round robin strategy. It is important that the full
 * content is written using one call to the write method to ensure the content goes to the same file. </p>
 *
 * @author David B. Bracewell
 */
public class MultiFileWriter extends Writer implements Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;
   private final Writer[] writers;
   private final AtomicInteger lockId = new AtomicInteger();
   private final int numberOfFiles;

   /**
    * Instantiates a new Multi file writer.
    *
    * @param baseDirectory the base directory which will contain the files
    * @param filePrefix    the file prefix (each file have XXXXX appended on where XX is the file number)
    * @param numberOfFiles the number of files
    * @throws IOException Something went wrong initializing the files
    */
   public MultiFileWriter(Resource baseDirectory, String filePrefix, int numberOfFiles) throws IOException {
      Validation.checkArgument(!baseDirectory.exists() || baseDirectory.isDirectory(),
                               "Resource base must not exist or be a directory");
      Validation.checkArgument(numberOfFiles > 0, "Must specify at least one file");
      baseDirectory.mkdirs();
      this.writers = new Writer[numberOfFiles];
      this.numberOfFiles = numberOfFiles;
      for(int i = 0; i < numberOfFiles; i++) {
         String name = filePrefix + Strings.padStart(Integer.toString(i), 5, '0');
         this.writers[i] = new AsyncWriter(baseDirectory.getChild(name).writer());
      }
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      int lock = lockId.accumulateAndGet(1, (x1, x2) -> {
         if(x1 + x2 >= numberOfFiles) {
            return 0;
         }
         return x1 + x2;
      });
      this.writers[lock].write(cbuf, off, len);
   }

   @Override
   public void flush() throws IOException {
      IOException ioe = null;
      for(int i = 0; i < numberOfFiles; i++) {
         try {
            this.writers[i].flush();
         } catch(IOException e) {
            ioe = e;
         }
      }
      if(ioe != null) {
         throw ioe;
      }
   }

   @Override
   public void close() throws IOException {
      IOException ioe = null;
      for(int i = 0; i < numberOfFiles; i++) {
         try {
            this.writers[i].close();
         } catch(IOException e) {
            ioe = e;
         }
      }
      if(ioe != null) {
         throw ioe;
      }
   }
}//END OF MultiFileWriter
