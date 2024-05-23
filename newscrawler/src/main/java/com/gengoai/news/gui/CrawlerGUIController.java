package com.gengoai.news.gui;

import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

public class CrawlerGUIController {
   private final CrawlerGUIControls controls;
   public File projectFile = null;
   public Timer timer;
   public CrawlProject project;

   FluentAction newProject = new FluentAction("New", this::newProject);
//   FluentAction open = new FluentAction("Open", this::openProject);
//   FluentAction save = new FluentAction("Save", this::saveProject);
//   FluentAction exit = new FluentAction("Exit", this::exit);

   public CrawlerGUIController(CrawlerGUIControls controls) {
      this.controls = controls;

      controls.btnSelectIndex.addActionListener(e -> {
         controls.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (controls.fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            controls.txtIndexLocation.setText(controls.fileChooser.getSelectedFile().getAbsolutePath());
            project.indexLocation = controls.fileChooser.getSelectedFile().getAbsolutePath();
         }
      });

      controls.btnStart.addActionListener(e -> {
         controls.btnStart.setEnabled(false);
         controls.btnStop.setEnabled(true);
         timer = new Timer();
         timer.scheduleAtFixedRate(new CrawlTask(controls.pbFeed, controls.pbPage, project), 0, 300_000);
      });

      controls.btnStop.addActionListener(e -> {
         controls.btnStart.setEnabled(true);
         controls.btnStop.setEnabled(false);
         timer.cancel();
         timer = null;
      });

   }

   private void newProject(ActionEvent e) {
      projectFile = null;
      project = new CrawlProject();
      project.rssFeeds = new ArrayList<>();
      project.rssFeeds.add("https://abcnews.go.com/abcnews/topstories");
      project.rssFeeds.add("https://abcnews.go.com/abcnews/usheadlines");
      controls.pnlIndexLocation.setVisible(true);
      controls.pnlProgress.setVisible(true);
      controls.pnlButtons.setVisible(true);
   }
}
