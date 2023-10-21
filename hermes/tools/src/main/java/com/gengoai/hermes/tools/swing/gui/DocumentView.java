package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Document;
import com.gengoai.hermes.Types;
import com.gengoai.swing.Colors;
import com.gengoai.swing.FontAwesome;
import com.gengoai.swing.SwingHelpers;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.listener.FluentAction;
import com.gengoai.swing.component.listener.SwingListeners;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.component.listener.SwingListeners.fluentAction;
import static com.gengoai.tuple.Tuples.$;

public class DocumentView extends JPanel {
    private final HStringViewer hStringViewer;
    private final JTabbedPane tbPaneTools;
    private final EntityAnnotationTable tblAnnotations;
    private final SentenceAnnotationTable tblSentences;
    private final TokenAnnotationTable tblTokens;
    private final MangoTable tblDocumentAttributes;
    private final MangoTable tblSearchResults;
    private SearchBar searchBar = null;

    public void setSearchBarVisible(boolean visible) {
        searchBar.setVisible(visible);
    }

    public boolean isSearchBarVisible() {
        return searchBar.isVisible();
    }

    private final FluentAction TOGGLE_SEARCHBAR = fluentAction("ToggleSearchBar",
                                                               a -> searchBar.setVisible(!searchBar.isVisible()))
            .accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                KeyEvent.CTRL_DOWN_MASK))
            .shortDescription("View or Hide the Search Bar.")
            .largeIcon(FontAwesome.SEARCH.createIcon(32))
            .smallIcon(FontAwesome.SEARCH.createIcon(16));

    public DocumentView(@NonNull Document document) {
        setLayout(new BorderLayout());
        /////////////////////
        // Create Components
        /////////////////////
        this.hStringViewer = initHStringViewer(document);
        this.tblAnnotations = with(new EntityAnnotationTable(document), $ -> {
            $.addMouseListener(SwingListeners.mouseDoubleClicked(e -> onAnnotationTableDoubleClick($)));
        });
        this.tblSentences = with(new SentenceAnnotationTable(document), $ -> {
            $.addMouseListener(SwingListeners.mouseDoubleClicked(e -> onAnnotationTableDoubleClick($)));
        });
        this.tblTokens = with(new TokenAnnotationTable(document), $ -> {
            $.addMouseListener(SwingListeners.mouseDoubleClicked(e -> onAnnotationTableDoubleClick($)));
        });
        this.tblSearchResults = with(new MangoTable($("Start", Integer.class),
                                                    $("End", Integer.class),
                                                    $("Result", String.class)), $ -> {
            $.addMouseListener(SwingListeners.mouseDoubleClicked(e -> onAnnotationTableDoubleClick($)));
            $.setFillsViewportHeight(true);
            $.setAutoCreateRowSorter(true);
            $.setRowHeightPadding(3);
            $.setShowGrid(true);
            $.setReorderingAllowed(false);
            $.withColumn(0, c -> {
                c.setPreferredWidth(75);
                c.setMaxWidth(75);
                c.setResizable(false);
            });
            $.withColumn(1, c -> {
                c.setPreferredWidth(75);
                c.setMaxWidth(75);
                c.setResizable(false);
            });
        });
        this.tblDocumentAttributes = initDocumentAttributes(document);
        this.tbPaneTools = with(new JTabbedPane(), $ -> {
            $.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            $.setTabPlacement(JTabbedPane.BOTTOM);
            $.addTab("Document Attributes", new JScrollPane(tblDocumentAttributes));
            $.addTab("Entity Table", new JScrollPane(tblAnnotations));
            $.addTab("Sentence Table", new JScrollPane(tblSentences));
            $.addTab("Tokens Table", new JScrollPane(tblTokens));
            $.addTab("Search Results", new JScrollPane(tblSearchResults));
            $.setSelectedIndex(1);
        });
        var splitPane = with(new JSplitPane(JSplitPane.VERTICAL_SPLIT, hStringViewer, tbPaneTools), $ -> {
            $.setDividerLocation(400);
            $.setDividerSize(5);
            $.setResizeWeight(1);
        });
        this.searchBar = new SearchBar(document);
        this.searchBar.addSearchNavigationListener(h -> {
            if (h != null) {
                hStringViewer.setSelectionRange(h.start(), h.end());
            }
        });
        this.searchBar.addSearchResultListListener(results -> {
            tbPaneTools.setSelectedIndex(4);
            tblSearchResults.clear();
            results.forEach(r -> tblSearchResults.addRow(r.start(), r.end(), r.toString()));
        });
        this.searchBar.setVisible(false);

        /////////////////////
        // Add Components
        /////////////////////
        add(searchBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        SwingHelpers.visitComponents(this,
                                     component -> {
                                         component.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                                                  .put(TOGGLE_SEARCHBAR.getAccelerator(), TOGGLE_SEARCHBAR);
                                         component.getActionMap()
                                                  .put(TOGGLE_SEARCHBAR, TOGGLE_SEARCHBAR);
                                     });


    }

    public void onAnnotationTableDoubleClick(MangoTable table) {
        if (table.getSelectedRow() >= 0) {
            int start = Cast.as(table.getValueAt(table.getSelectedRow(), 0));
            int end = Cast.as(table.getValueAt(table.getSelectedRow(), 1));
            hStringViewer.setSelectionRange(start, end);
        }
    }

    private HStringViewer initHStringViewer(Document document) {
        HStringViewer hStringViewer = new HStringViewer(document);
        EntityColors colors = new EntityColors();
        colors.forEach((entityType, color) -> {
            hStringViewer.addStyle(entityType.toString(), fluentStyle -> fluentStyle.background(color)
                                                                                    .foreground(Colors.calculateBestFontColor(color))
                                                                                    .bold(true));
        });
        document.entities().forEach(entity -> {
            hStringViewer.highlightAnnotation(entity,
                                              entity.attribute(Types.ENTITY_TYPE).toString(),
                                              entity.attribute(Types.ENTITY_TYPE).label());
        });
        InputMap inputMap = hStringViewer.getEditorInputMap();
        inputMap.put(TOGGLE_SEARCHBAR.getAccelerator(), TOGGLE_SEARCHBAR);
        return hStringViewer;
    }


    private MangoTable initDocumentAttributes(Document document) {
        MangoTable tbl = with(new MangoTable("Attribute", "Value"), $ -> {
            $.setRowHeight($.getRowHeight() + 3);
            $.setShowGrid(true);
            $.setFillsViewportHeight(true);
            $.setAutoCreateRowSorter(true);
            $.getTableHeader().setReorderingAllowed(false);
            $.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        });
        document.attributeMap().forEach((a, v) -> tbl.addRow(a.name(), v));
        return tbl;
    }
}
