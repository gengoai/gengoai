package com.gengoai.news.gui;

import com.gengoai.io.Resources;
import com.gengoai.json.Json;
import com.gengoai.swing.Menus;
import com.gengoai.swing.SwingApplication;
import com.gengoai.swing.component.VBox;
import com.gengoai.swing.component.listener.FluentAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.gengoai.hermes.Hermes.HERMES_PACKAGE;

public class CrawlerGUI extends SwingApplication {

   FluentAction newProject = new FluentAction("New", this::newProject);
   FluentAction open = new FluentAction("Open", this::openProject);
   FluentAction save = new FluentAction("Save", this::saveProject);
   FluentAction exit = new FluentAction("Exit", this::exit);
   JFileChooser fileChooser = new JFileChooser();
   Project project = null;
   File projectFile = null;
   JTextField indexLocation = new JTextField();
   JPanel pnl = new JPanel();
   JButton btnSelectIndex = new JButton("...");
   JPanel indexPanel = new JPanel();

   public static class Project {
      public String indexLocation;
      public java.util.List<String> rssFeeds;
   }

   private void newProject(ActionEvent e) {
      project = new Project();
      project.rssFeeds = new java.util.ArrayList<>();
      project.rssFeeds.add("");
      projectFile = null;
      enableControls(true);
   }

   private void openProject(ActionEvent e) {
      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
         projectFile = fileChooser.getSelectedFile();
         try {
            project = Json.parse(Resources.fromFile(projectFile), Project.class);
            if (project.indexLocation == null || project.rssFeeds == null) {
               JOptionPane.showMessageDialog(null, "Invalid project file", "Error", JOptionPane.ERROR_MESSAGE);
               project = null;
               projectFile = null;
            } else {
               enableControls(true);
               indexLocation.setText(project.indexLocation);
            }
         } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error loading project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            project = null;
            projectFile = null;
         }
      }
   }

   private void saveProject(ActionEvent e) {
      if (project == null) {
         return;
      }
      if (project.indexLocation == null || project.rssFeeds == null || project.rssFeeds.isEmpty()) {
         JOptionPane.showMessageDialog(null, "Invalid project: Please set the index location and add at least one RSS feed.", "Error", JOptionPane.ERROR_MESSAGE);
         return;
      }
      if (projectFile == null) {
         if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            projectFile = fileChooser.getSelectedFile();
         } else {
            return;
         }
      }
      try {
         Json.dump(project, Resources.fromFile(projectFile));
      } catch (IOException ex) {
         JOptionPane.showMessageDialog(null, "Error saving project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private void exit(ActionEvent e) {
      System.exit(0);
   }

   @Override
   public Set<String> getDependentPackages() {
      return Collections.singleton(HERMES_PACKAGE);
   }

   public void enableControls(boolean enable) {
      save.setEnabled(enable);
      btnSelectIndex.setEnabled(enable);
      indexPanel.setVisible(enable);
   }

   @Override
   public void initControls() throws Exception {
      save.setEnabled(false);
      menuBar(Menus.menu("File",
                         'F',
                         newProject,
                         open,
                         save,
                         Menus.SEPARATOR,
                         exit));

      indexLocation.setEnabled(false);
      btnSelectIndex.setEnabled(false);
      indexPanel.setVisible(false);

      pnl.setPreferredSize(new Dimension(400, 200));
      pnl.setLayout(new BorderLayout());
      btnSelectIndex.addActionListener(e -> {
         var folderChooser = new JFileChooser();
         folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            indexLocation.setText(folderChooser.getSelectedFile().getAbsolutePath());
            if (project != null) {
               project.indexLocation = indexLocation.getText();
            }
         }
      });

      indexPanel.setLayout(new BorderLayout());
      indexPanel.add(new JLabel("Index:"), BorderLayout.WEST);
      indexPanel.add(indexLocation, BorderLayout.CENTER);
      indexPanel.add(btnSelectIndex, BorderLayout.EAST);
      pnl.add(indexPanel, BorderLayout.NORTH);

      var progressPanel = new VBox();
//      progressPanel.setLayout(new BorderLayout());
      var feedProgress = new JProgressBar();
      feedProgress.setPreferredSize(new Dimension(400, 20));
      feedProgress.setStringPainted(true);
      progressPanel.add(new JLabel("Feeds:"));//, BorderLayout.NORTH);
      progressPanel.add(feedProgress);//, BorderLayout.NORTH);
      feedProgress.setValue(100);

      var pageProgress = new JProgressBar();
      pageProgress.setPreferredSize(new Dimension(400, 20));
      pageProgress.setStringPainted(true);
      progressPanel.add(new JLabel("Pages:"));//, BorderLayout.SOUTH);
      progressPanel.add(pageProgress);//, BorderLayout.SOUTH);
      pageProgress.setMaximum(200);
      pageProgress.setValue(100);


      pnl.add(progressPanel, BorderLayout.CENTER);
      pnl.add(new JPanel(), BorderLayout.SOUTH);
      setCenterComponent(pnl);
      fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Project Files", "json"));
      setResizable(false);
   }

   public static void main(String[] args) {
      SwingApplication.runApplication(CrawlerGUI::new, "CrawlerGUI", "CrawlerGUI", args);
   }
}//END OF CrawlerGUI
