package com.gengoai.news.gui;

import com.gengoai.LogUtils;
import com.gengoai.collection.Lists;
import com.gengoai.concurrent.Threads;
import com.gengoai.io.Resources;
import com.gengoai.io.Xml;
import com.gengoai.io.resource.Resource;
import com.gengoai.lucene.IndexDocument;
import com.gengoai.lucene.LuceneIndex;
import com.gengoai.lucene.field.Fields;
import com.gengoai.rss.RSSItem;
import lombok.extern.java.Log;
import org.apache.lucene.index.Term;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Log
public class CrawlTask extends TimerTask {
   private final JProgressBar pbFeed;
   private final JProgressBar pbPage;
   private final CrawlProject project;
   private final LuceneIndex index;

   public CrawlTask(JProgressBar pbFeed, JProgressBar pbPage, CrawlProject project) {
      this.pbFeed = pbFeed;
      this.pbPage = pbPage;
      this.project = project;
      try {
         this.index = LuceneIndex.at(project.indexLocation)
                                 .storedField("guid", Fields.KEYWORD)
                                 .storedField("url", Fields.KEYWORD)
                                 .storedField("html", Fields.BLOB)
                                 .createIfNotExists();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void run() {
      pbFeed.setValue(0);
      pbFeed.setMaximum(project.rssFeeds.size());
      for (String feed : project.rssFeeds) {
         pbPage.setValue(0);
         Resource url = Resources.from(feed);
         try {
            List<Document> pages = Lists.asArrayList(Xml.parse(url, "item"));
            pbPage.setMaximum(pages.size());
            for (Document item : pages) {
               RSSItem rssItem = RSSItem.from(item);
               try {
                  if (!index.get("guid", rssItem.getGuid()).hasField("html")) {
                     LogUtils.logInfo(log, "Fetching: {0}", rssItem.getLink());
                     IndexDocument indexDocument = IndexDocument.from(Map.of("url", rssItem.getLink(),
                                                                             "guid", rssItem.getGuid(),
                                                                             "html", Resources.from(rssItem.getLink())
                                                                                              .readToString()));
                     index.updateWhere(new Term("guid", rssItem.getGuid()), List.of(indexDocument));
                     index.commit();
                     Threads.sleep(3, TimeUnit.SECONDS);
                  }
               } catch (FileNotFoundException fnfe) {
                  IndexDocument indexDocument = IndexDocument.from(Map.of("url", rssItem.getLink(),
                                                                          "guid", rssItem.getGuid(),
                                                                          "html", ""));
                  index.updateWhere(new Term("guid", rssItem.getGuid()), List.of(indexDocument));
                  index.commit();
               } catch (Exception e) {
                  LogUtils.logSevere(log, "Error processing {0}", rssItem.getLink());
                  e.printStackTrace();
               } finally {
                  pbPage.setValue(pbPage.getValue() + 1);
               }
            }
         } catch (IOException | XMLStreamException e) {
            LogUtils.logSevere(log, "Error processing {0}", url);
            e.printStackTrace();
         }
         pbFeed.setValue(pbFeed.getValue() + 1);
      }
      try {
         index.close();
      } catch (IOException e) {
         LogUtils.logSevere(log, "Error closing index");
      }
   }
}
