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

package com.gengoai.hermes.tools.swing;

import com.gengoai.ParameterDef;
import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.corpus.SearchResults;
import com.gengoai.hermes.extraction.keyword.*;
import com.gengoai.hermes.format.DocFormatParameters;
import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.hermes.tools.ui.components.SimpleDocumentView;
import com.gengoai.io.resource.StringResource;
import com.gengoai.parsing.ParseException;
import com.gengoai.string.Strings;
import com.gengoai.string.TableFormatter;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Menus;
import com.gengoai.swing.SwingApplication;
import com.gengoai.swing.component.*;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.gengoai.swing.component.Components.button;
import static com.gengoai.swing.component.Components.dim;
import static com.gengoai.tuple.Tuples.$;

public class Hermes extends HermesGUI {

   //////////////////////////////////////////////////////////////////////////////////////////////
   // File
   //////////////////////////////////////////////////////////////////////////////////////////////
   private final FluentAction newCorpusAction = new FluentAction("New...", this::newCorpus)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK))
         .smallIcon(FontAwesome.MAGIC.createIcon(16))
         .largeIcon(FontAwesome.MAGIC.createIcon(32));
   private final FluentAction openCorpusAction = new FluentAction("Open...", this::openCorpus)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
         .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(16))
         .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(32));
   private final FluentAction closeCorpusAction = new FluentAction("Close", this::closeCorpus);
   private final FluentAction quitAction = new FluentAction("Quit", this::quit)
         .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
   //////////////////////////////////////////////////////////////////////////////////////////////


   //////////////////////////////////////////////////////////////////////////////////////////////
   // Analysis
   //////////////////////////////////////////////////////////////////////////////////////////////
   private final FluentAction annotateCoreAction = new FluentAction("Annotate Core", this::doAnnotateCore);
   private final FluentAction calcKeywordsAction = new FluentAction("Keywords...", this::doExtractKeywords);
   private final FluentAction calcSummariesAction = new FluentAction("Summaries...", this::doExtractSummaries);
   //////////////////////////////////////////////////////////////////////////////////////////////

   private final MangoTabbedPane tabbedPane = new MangoTabbedPane();
   private SearchPanel searchPanel;

   private Corpus currentCorpus = null;

   private void newCorpus(ActionEvent e) {
      var newCorpusWindow = new NewCorpusDialog();
      newCorpusWindow.setVisible(true);
      if (newCorpusWindow.corpus != null) {
         currentCorpus = newCorpusWindow.corpus;
         this.closeCorpusAction.setEnabled(true);
         this.calcKeywordsAction.setEnabled(true);
         this.calcSummariesAction.setEnabled(true);
         this.annotateCoreAction.setEnabled(true);
      }
   }

   private void doAnnotateCore(ActionEvent e) {
      Config.setProperty(DocumentCollection.REPORT_LEVEL, "INFO");
      currentCorpus.annotate(Types.ENTITY, Types.DEPENDENCY);
   }

   private void doExtractSummaries(ActionEvent e) {

   }


   class KeywordTask extends SwingWorker<Void, Void> {
      private final ProgressMonitor progressMonitor;

      KeywordTask(ProgressMonitor progressMonitor) {
         this.progressMonitor = progressMonitor;
      }

      @Override
      protected Void doInBackground() throws Exception {
         KeywordExtractor extractor = new TextRank();
         extractor.fit(currentCorpus);
         int i = 0;
         Counter<String> keywords = Counters.newConcurrentCounter();
         Counter<String> docFreq = Counters.newConcurrentCounter();
         for (Document document : currentCorpus) {
            Counter<String> kw = extractor.extract(document).count();
            keywords.merge(kw);
            docFreq.incrementAll(kw.items());
            progressMonitor.setProgress(++i);
         }
         var mt = new MangoTable($("Keyword", String.class),
                                 $("Score", Double.class),
                                 $("Document Freq.", Double.class));
         mt.setAutoCreateRowSorter(true);
         for (String kw : keywords.items()) {
            mt.addRow(kw, keywords.get(kw), docFreq.get(kw));
         }
         mt.addMouseListener(SwingListeners.mouseDoubleClicked(e -> {
            String keyword = mt.getValueAtModel(
                  mt.getRowSorter().convertRowIndexToModel(mt.getSelectedRow()),
                  0).toString();
            searchPanel.txt.setText(keyword);
            searchPanel.performSearch(new ActionEvent(searchPanel,0,""));
            tabbedPane.setSelectedIndex(0);
         }));
         tabbedPane.addTab("Keywords", new JScrollPane(mt));
         return null;
      }
   }

   private void doExtractKeywords(ActionEvent e) {
      var progressWindow = new ProgressMonitor(mainWindowFrame,
                                               "Extracting Keywords",
                                               "",
                                               0,
                                               (int) currentCorpus.size());
      var task = new KeywordTask(progressWindow);
      task.execute();
   }

   private void openCorpus(ActionEvent e) {
      var od = new JFileChooser();
      od.setDialogTitle("Open Corpus");
      od.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (od.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
         this.currentCorpus = Corpus.open(od.getSelectedFile().getAbsolutePath());
         this.closeCorpusAction.setEnabled(true);
         this.calcKeywordsAction.setEnabled(true);
         this.calcSummariesAction.setEnabled(true);
         this.annotateCoreAction.setEnabled(true);
         this.searchPanel = new SearchPanel();
         this.tabbedPane.addTab("Search", this.searchPanel);
         this.tabbedPane.setTabComponentAt(0, null);
      }
   }

   private void closeCorpus(ActionEvent e) {
      this.currentCorpus = null;
      this.closeCorpusAction.setEnabled(false);
      this.calcKeywordsAction.setEnabled(false);
      this.calcSummariesAction.setEnabled(false);
      this.annotateCoreAction.setEnabled(true);
   }

   private void quit(ActionEvent e) {
      mainWindowFrame.dispose();
   }

   @Override
   protected void initControls() throws Exception {
      closeCorpusAction.setEnabled(false);
      calcKeywordsAction.setEnabled(false);
      calcSummariesAction.setEnabled(false);
      annotateCoreAction.setEnabled(false);

      menuBar(Menus.menu("File",
                         'F',
                         Menus.menuItem(newCorpusAction),
                         Menus.menuItem(openCorpusAction),
                         Menus.SEPARATOR,
                         Menus.menuItem(closeCorpusAction),
                         Menus.menuItem(quitAction)),
              Menus.menu("Analysis",
                         'A',
                         Menus.menuItem(calcKeywordsAction),
                         Menus.menuItem(calcSummariesAction),
                         Menus.SEPARATOR,
                         Menus.menuItem(annotateCoreAction))
             );

      var newCorpusBtn = button(newCorpusAction, false, true, 0);
      newCorpusBtn.setToolTipText("New Corpus");
      var openCorpusBtn = button(openCorpusAction, false, true, 0);
      openCorpusBtn.setToolTipText("Open Corpus");

      toolBar(newCorpusBtn,
              openCorpusBtn,
              null);

      mainWindowFrame.add(tabbedPane);
   }

   public static void main(String[] args) {
      SwingApplication.runApplication(Hermes::new, "Hermes", "Hermes", args);
   }


   private class SearchPanel extends JPanel {

      private final FluentAction searchAction = new FluentAction("Search", this::performSearch)
            .smallIcon(FontAwesome.SEARCH.createIcon(16))
            .largeIcon(FontAwesome.SEARCH.createIcon(32));


      private final JTextField txt = new JTextField();
      private final JButton searchBtn = button(searchAction, false, true, 0);
      private final JButton saveBtn = new JButton("Save...");
      private final MangoList<String> lst = new MangoList<>();
      private final List<String> ids = new ArrayList<>();
      private final JLabel lbl = new JLabel(" ", SwingConstants.RIGHT);


      private void performSearch(ActionEvent l) {
         try {
            if (Strings.isNullOrBlank(txt.getText())) {
               return;
            }
            SearchResults results = Hermes.this.currentCorpus.query(txt.getText());
            DefaultListModel<String> model = Cast.as(lst.getModel());
            model.clear();
            ids.clear();
            long size = results.size();
            lbl.setText("Showing " + Math.min(size, 100) + " of " + size + " Total Results");
            int count = 0;
            for (Document doc : results) {
               model.addElement("<html><b>* Id: </b>" +
                                doc.getId() + "<br><b>&nbsp;&nbsp;Snippet: </b><i>" +
                                doc.substring(0, Math.min(255, doc.length())) +
                                "</i></html>"
                               );
               ids.add(doc.getId());
               count++;
               if (count >= 100) {
                  break;
               }
            }
            saveBtn.setVisible(true);
         } catch (ParseException e) {
            throw new RuntimeException(e);
         }
      }

      public SearchPanel() {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         var pnl = new JPanel();
         pnl.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         pnl.setLayout(new BorderLayout());
         lst.setFixedCellHeight(75);
         pnl.add(txt, BorderLayout.CENTER);
         txt.addActionListener(this::performSearch);
         searchBtn.addActionListener(this::performSearch);
         pnl.add(searchBtn, BorderLayout.EAST);
         add(pnl, BorderLayout.NORTH);
         var scroll = new JScrollPane(lst);
         scroll.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createEmptyBorder(12, 12, 12, 12),
               scroll.getBorder()));
         add(scroll, BorderLayout.CENTER);

         var options = new JPanel();
         options.setLayout(new BorderLayout());
         options.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         add(options, BorderLayout.SOUTH);
         options.add(saveBtn, BorderLayout.EAST);
         saveBtn.setVisible(false);
         options.add(lbl, BorderLayout.WEST);

         lst.addMouseListener(SwingListeners.mouseDoubleClicked(e -> {
            String id = ids.get(lst.getSelectedIndex());
            var docView = new SimpleDocumentView(currentCorpus.getDocument(id));
            tabbedPane.addTab(id, docView);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
         }));
      }

   }

   private class NewCorpusDialog extends JDialog {
      private final FluentAction chooseCorpusLocation = new FluentAction("...", this::doChooseCorpusLocation);
      private final FluentAction chooseImportLocation = new FluentAction("...", this::doChooseImportLocation);
      private final FluentAction createCorpusAction = new FluentAction("Create", this::doCreateCorpus);
      private final JButton chooseCorpusLocationBtn = button(chooseCorpusLocation, true, false, 0);
      private final JTextField corpusLocationTxt = new JTextField(15);
      private final JButton chooseDFLocationBtn = button(chooseImportLocation, true, false, 0);
      private final JTextField importLocationTxt = new JTextField(15);
      private final JComboBox<String> formats = new JComboBox<>();
      private final JTextField formatOptionsText = new JTextField(15);
      private final JCheckBox isOPL = new JCheckBox();
      private final JButton createCorpusBtn = button(createCorpusAction, true, false, 0);
      private Corpus corpus;

      private void doChooseCorpusLocation(ActionEvent e) {
         var od = new JFileChooser();
         od.setDialogTitle("Select Location for new Corpus");
         od.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (od.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            corpusLocationTxt.setText(od.getSelectedFile().getAbsolutePath());
         }
      }

      private void doChooseImportLocation(ActionEvent e) {
         var od = new JFileChooser();
         od.setDialogTitle("Select Documents for Import");
         od.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         if (od.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            importLocationTxt.setText(od.getSelectedFile().getAbsolutePath());
         }
      }

      private void doCreateCorpus(ActionEvent e) {
         if (Strings.isNullOrBlank(importLocationTxt.getText())) {
            JOptionPane.showMessageDialog(null, "No Import Location Specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         if (Strings.isNullOrBlank(corpusLocationTxt.getText())) {
            JOptionPane.showMessageDialog(null, "No Corpus Location Specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         StringBuilder format = new StringBuilder();
         format.append(formats.getSelectedItem());
         if (isOPL.isSelected()) {
            format.append("_OPL");
         }
         format.append("::").append(importLocationTxt.getText());
         if (Strings.isNotNullOrBlank(formatOptionsText.getText())) {
            format.append(";").append(formatOptionsText.getText());
         }
         corpus = Corpus.open(corpusLocationTxt.getText());
         DocumentCollection dc = DocumentCollection.create(format.toString());
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         corpus.addAll(dc);
         setCursor(Cursor.getDefaultCursor());
         dispose();
      }


      public NewCorpusDialog() {
         setMinimumSize(dim(400, 500));
         setMaximumSize(dim(400, 500));
         setLocationRelativeTo(Hermes.this.mainWindowFrame);
         setTitle("Create Corpus");
         setModal(true);
         setResizable(false);

         setLayout(new BorderLayout());
         var vbox = new VBox();
         add(vbox);
         vbox.setAlignmentY(0);
         vbox.setAlignmentX(0);

         corpusLocationTxt.setEditable(false);
         importLocationTxt.setEditable(false);


         var panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel("Corpus Location"), BorderLayout.NORTH);
         var btnPanel = new JPanel();
         btnPanel.add(corpusLocationTxt, BorderLayout.CENTER);
         btnPanel.add(chooseCorpusLocationBtn, BorderLayout.EAST);
         panel.add(btnPanel, BorderLayout.CENTER);
         vbox.add(panel);

         List<String> items = DocFormatService.getProviders().stream()
                                              .map(p -> p.getName().toLowerCase())
                                              .collect(Collectors.toList());
         Collections.sort(items);
         items.forEach(formats::addItem);
         panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel("Document Format"), BorderLayout.NORTH);
         panel.add(formats, BorderLayout.CENTER);
         vbox.add(panel);

         panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel("One Document Per Line"), BorderLayout.CENTER);
         panel.add(isOPL, BorderLayout.EAST);
         vbox.add(panel);

         panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel("Import Documents"), BorderLayout.NORTH);
         btnPanel = new JPanel();
         btnPanel.add(importLocationTxt, BorderLayout.CENTER);
         btnPanel.add(chooseDFLocationBtn, BorderLayout.EAST);
         panel.add(btnPanel, BorderLayout.CENTER);
         vbox.add(panel);

         var formatParamsBtn = new JButton("?");
         panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel("Format Options"), BorderLayout.NORTH);
         panel.add(formatOptionsText, BorderLayout.CENTER);
         panel.add(formatParamsBtn, BorderLayout.EAST);
         vbox.add(panel);

         formatParamsBtn.addActionListener(l -> {
            TableFormatter tableFormatter = new TableFormatter();
            var provider = DocFormatService.getProvider(formats.getSelectedItem().toString());
            tableFormatter.header(Arrays.asList("ParameterName", "ParameterType"));
            final DocFormatParameters parameters = provider.getDefaultFormatParameters();
            for (String parameterName : new TreeSet<>(parameters.parameterNames())) {
               ParameterDef<?> param = parameters.getParam(parameterName);
               tableFormatter.content(Arrays.asList(parameterName, param.type.getSimpleName()));
            }
            var so = new StringResource();
            try (OutputStream os = so.outputStream()) {
               var ps = new PrintStream(os);
               tableFormatter.print(ps);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }

            try {
               JTextArea ta = new JTextArea(20, 50);
               ta.setText(so.readToString());
               ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
               JOptionPane.showMessageDialog(null, new JScrollPane(ta), provider.getName(), JOptionPane.PLAIN_MESSAGE);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }


         });


         panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
         panel.setLayout(new BorderLayout());
         var cancel = new JButton("Cancel");
         cancel.addActionListener(l -> dispose());
         panel.add(cancel, BorderLayout.WEST);
         panel.add(createCorpusBtn, BorderLayout.EAST);
         vbox.add(panel);


      }

   }

}

