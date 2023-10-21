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

package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.collection.counter.Counter;
import com.gengoai.collection.counter.Counters;
import com.gengoai.config.Config;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.corpus.SearchResults;
import com.gengoai.hermes.extraction.keyword.KeywordExtractor;
import com.gengoai.hermes.extraction.keyword.KeywordExtractors;
import com.gengoai.hermes.tools.swing.HermesGUI;
import com.gengoai.hermes.tools.swing.LoggingTask;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.parsing.ParseException;
import com.gengoai.string.Strings;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.Menus;
import com.gengoai.swing.SwingApplication;
import com.gengoai.swing.component.MangoCombobox;
import com.gengoai.swing.component.MangoList;
import com.gengoai.swing.component.MangoTabbedPane;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;
import com.gengoai.swing.component.model.MangoListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gengoai.swing.component.Components.button;
import static com.gengoai.tuple.Tuples.$;

public class Hermes extends HermesGUI {

    private final FluentAction quitAction = new FluentAction("Quit", this::quit)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
    // File
    private final FluentAction newCorpusAction = new FluentAction("New...", this::newCorpus)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.MAGIC.createIcon(16))
            .largeIcon(FontAwesome.MAGIC.createIcon(32));
    private final MangoTabbedPane tabbedPane = new MangoTabbedPane();
    private final FluentAction openCorpusAction = new FluentAction("Open...", this::openCorpus)
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK))
            .smallIcon(FontAwesome.FOLDER_OPEN.createIcon(16))
            .largeIcon(FontAwesome.FOLDER_OPEN.createIcon(32));
    private SearchPanel searchPanel;
    private final FluentAction closeCorpusAction = new FluentAction("Close", this::closeCorpus);
    private CorpusView corpusView;
    private Corpus currentCorpus = null;
    private final FluentAction annotateCoreAction = new FluentAction("Annotate Core...", this::doAnnotateCore);
    private final FluentAction calcKeywordsAction = new FluentAction("Keywords...", this::doExtractKeywords);


    public static void main(String[] args) {
        SwingApplication.runApplication(Hermes::new, "Hermes", "Hermes", args);
    }

    private void newCorpus(ActionEvent e) {
        var newCorpusWindow = new NewCorpusDialog(this::doOpenCorpus);
        newCorpusWindow.setVisible(true);
    }

    private void doAnnotateCore(ActionEvent e) {
        Config.setProperty(DocumentCollection.REPORT_LEVEL, "INFO");
        LoggingTask loggingTask = new LoggingTask(
                mainWindowFrame,
                "Annotating Core",
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        currentCorpus.annotate(Types.BASE_ANNOTATIONS);
                        return null;
                    }
                }
        );
        loggingTask.setModal(true);
        loggingTask.setVisible(true);
    }

    private void doExtractKeywords(ActionEvent e) {
        MangoCombobox<KeywordExtractors> combobox = new MangoCombobox<>();
        for (KeywordExtractors value : KeywordExtractors.values()) {
            combobox.addItem(value);
        }
        combobox.setEditable(false);
        if (JOptionPane.showConfirmDialog(
                null,
                combobox,
                "Select Algorithm",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            var progressWindow = new ProgressMonitor(mainWindowFrame,
                                                     "Extracting Keywords",
                                                     "",
                                                     0,
                                                     (int) currentCorpus.size());
            var task = new KeywordTask(progressWindow, Cast.as(combobox.getSelectedItem(), KeywordExtractors.class));
            task.execute();
        }
    }

    private void openCorpus(ActionEvent e) {
        var od = new JFileChooser();
        od.setDialogTitle("Open Corpus");
        od.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (od.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            doOpenCorpus(Resources.from(od.getSelectedFile().getAbsolutePath()));
        }
    }

    private void doOpenCorpus(Resource r) {
        closeCorpus(null);
        this.currentCorpus = Corpus.open(r);
        this.closeCorpusAction.setEnabled(true);
        this.calcKeywordsAction.setEnabled(true);
        this.annotateCoreAction.setEnabled(true);
        this.searchPanel = new SearchPanel();
        this.corpusView = new CorpusView(r.path(), this.currentCorpus);
        this.corpusView.setOnDocumentOpen(id -> {
            var docView = new DocumentView(currentCorpus.getDocument(id));
            tabbedPane.addTab(id, docView);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        });
        this.tabbedPane.addTab("Browse", this.corpusView);
        this.tabbedPane.addTab("Search", this.searchPanel);
        this.tabbedPane.setTabComponentAt(0, null);
        this.tabbedPane.setTabComponentAt(1, null);
    }

    private void closeCorpus(ActionEvent e) {
        this.currentCorpus = null;
        this.calcKeywordsAction.setEnabled(false);
        this.closeCorpusAction.setEnabled(false);
        this.annotateCoreAction.setEnabled(false);
        while (tabbedPane.getTabCount() > 0) {
            tabbedPane.remove(0);
        }
    }

    private void quit(ActionEvent e) {
        mainWindowFrame.dispose();
    }

    @Override
    public void initControls() throws Exception {
        calcKeywordsAction.setEnabled(false);
        closeCorpusAction.setEnabled(false);
        annotateCoreAction.setEnabled(false);

        menuBar(Menus.menu("File",
                           'F',
                           Menus.menuItem(newCorpusAction),
                           Menus.menuItem(openCorpusAction),
                           Menus.SEPARATOR,
                           Menus.menuItem(closeCorpusAction),
                           Menus.SEPARATOR,
                           Menus.menuItem(quitAction)),
                Menus.menu("Corpus Analysis",
                           'A',
                           Menus.menuItem(calcKeywordsAction),
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
        ImageIcon ii = new ImageIcon(Resources.fromClasspath("img/editor.png").readBytes());
        setIcon(ii.getImage());
    }

    class KeywordTask extends SwingWorker<Void, Void> {
        private final ProgressMonitor progressMonitor;
        private final KeywordExtractor extractor;
        private final KeywordExtractors extractorsFactory;

        KeywordTask(ProgressMonitor progressMonitor, KeywordExtractors extractor) {
            this.progressMonitor = progressMonitor;
            this.extractor = extractor.create();
            this.extractorsFactory = extractor;
        }

        @Override
        protected Void doInBackground() throws Exception {
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
                searchPanel.performSearch(new ActionEvent(searchPanel, 0, ""));
                tabbedPane.setSelectedIndex(0);
            }));
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(new JScrollPane(mt), BorderLayout.CENTER);
            JToolBar btnBar = new JToolBar();
            FluentAction saveCsv = SwingListeners.fluentAction("Save..", l -> {
                                                     try {
                                                         JFileChooser fileChooser = new JFileChooser();
                                                         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                         fileChooser.setDialogTitle("Save CSV");
                                                         fileChooser.setApproveButtonText("Save");
                                                         if (fileChooser.showOpenDialog(mainWindowFrame) == JFileChooser.APPROVE_OPTION) {
                                                             mt.getModel().toCSV(Resources.fromFile(fileChooser.getSelectedFile()));
                                                         }
                                                     } catch (IOException e) {
                                                         e.printStackTrace();
                                                     }
                                                 }).smallIcon(FontAwesome.SAVE.createIcon(16))
                                                 .largeIcon(FontAwesome.SAVE.createIcon(16));
            btnBar.add(Box.createHorizontalGlue());
            var save = button(saveCsv, false, false, 0);
            save.setToolTipText("Save...");
            btnBar.add(save);
            btnBar.setFloatable(false);
            panel.add(btnBar, BorderLayout.NORTH);
            tabbedPane.addTab(extractorsFactory.name() + " Keywords", panel);
            return null;
        }
    }

    private class SearchPanel extends JPanel {

        private final JTextField txt = new JTextField();
        private final MangoList<String> lst = new MangoList<>();
        private final List<String> ids = new ArrayList<>();
        private final JLabel lbl = new JLabel(" ", SwingConstants.RIGHT);
        private final FluentAction searchAction = new FluentAction("Search", this::performSearch)
                .smallIcon(FontAwesome.SEARCH.createIcon(16))
                .largeIcon(FontAwesome.SEARCH.createIcon(32));
        private final JButton searchBtn = button(searchAction, false, true, 0);


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
            options.add(lbl, BorderLayout.WEST);

            lst.addMouseListener(SwingListeners.mouseDoubleClicked(e -> {
                String id = ids.get(lst.getSelectedIndex());
                var docView = new DocumentView(currentCorpus.getDocument(id));
                tabbedPane.addTab(id, docView);
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }));
        }

        private void performSearch(ActionEvent l) {
            try {
                if (Strings.isNullOrBlank(txt.getText())) {
                    return;
                }
                SearchResults results = Hermes.this.currentCorpus.query(txt.getText());
                MangoListModel<String> model = Cast.as(lst.getModel());
                model.clear();
                ids.clear();
                long size = results.size();
                lbl.setText("Showing " + Math.min(size, 100) + " of " + size + " Total Results");
                int count = 0;
                for (Document doc : results) {
                    model.add("<html><b>* Id: </b>" +
                                      doc.getId() + "<br><b>&nbsp;&nbsp;Snippet: </b><i>" +
                                      doc.substring(0, Math.min(50, doc.length())) +
                                      "</i></html>"
                             );
                    ids.add(doc.getId());
                    count++;
                    if (count >= 100) {
                        break;
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

    }


}//END OF Hermes

