package com.gengoai.news.gui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlProject {
   public String indexLocation;
   public List<String> rssFeeds;
}
