package com.gengoai.hermes.tools.swing.conll;

import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.json.Json;
import com.gengoai.string.Strings;
import com.gengoai.swing.component.HBox;
import com.gengoai.swing.component.MangoTable;
import com.gengoai.swing.component.model.MangoTableModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gengoai.function.Functional.with;
import static com.gengoai.swing.component.Components.dim;
import static com.gengoai.tuple.Tuples.$;

public class NewProjectDialog extends JDialog {

    private static class ColumnTableModel extends MangoTableModel {
        public ColumnTableModel() {
            super($("Name", String.class, false),
                  $("Type", String.class, true),
                  $("Tags", String.class, true));
        }
    }

    public NewProjectDialog(JFrame owner) {
        setMinimumSize(dim(800, 500));
        setMaximumSize(dim(800, 500));
        setResizable(false);
        setLocationRelativeTo(owner);
        setModal(true);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        MangoTable table = new MangoTable(new ColumnTableModel());
        add(new JScrollPane(table));
        with(table, $ -> {
            $.setFillsViewportHeight(true);
            $.setRowHeightPadding(3);
            $.setShowGrid(true);
            $.setReorderingAllowed(false);
            $.withColumn(1, c -> {
                c.setCellEditor(new DefaultCellEditor(createColumnTypeComboBox()));
            });
        });
        var hBox = new HBox(5, 5);
        var addRow = new JButton("+");
        addRow.addActionListener(l -> {
            String columnName = (String) JOptionPane.showInputDialog(
                    this,
                    "Column Name",
                    "New Column",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            if (Strings.isNotNullOrBlank(columnName)) {
                table.addRow(columnName, "FIXED", "");
            }
        });
        var deleteRow = new JButton("-");
        hBox.add(addRow);
        hBox.add(deleteRow);
        add(hBox);
        deleteRow.addActionListener(l -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                table.remove(row);
            }
        });

        var chooseFolderDialog = new JFileChooser();
        chooseFolderDialog.setDialogTitle("Select Folder");
        chooseFolderDialog.setMultiSelectionEnabled(false);
        chooseFolderDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        var locationBox = new JPanel(new BorderLayout());
        var locationField = new JTextField(500);
        var chooseLocation = new JButton("...");
        chooseLocation.addActionListener(l -> {
            if (chooseFolderDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                locationField.setText(chooseFolderDialog.getSelectedFile().getAbsolutePath());
            }
        });
        locationBox.add(new JLabel("Location:"), BorderLayout.WEST);
        locationBox.add(locationField, BorderLayout.CENTER);
        locationBox.add(chooseLocation, BorderLayout.EAST);
        add(locationBox);

        var buttonBox = new HBox(5, 5);
        var ok = new JButton("OK");
        ok.addActionListener(l -> {
            if (Strings.isNullOrBlank(locationField.getText())) {
                JOptionPane.showMessageDialog(this, "Please enter a location for the project.");
                return;
            }
            Resource projectFolder = Resources.from(locationField.getText());
            projectFolder.mkdirs();
            projectFolder.getChild("documents").mkdirs();
            List<Map<String, Object>> data = new ArrayList<>();
            for (int row = 0; row < table.getRowCount(); row++) {
                String name = (String) table.getValueAt(row, 0);
                String type = (String) table.getValueAt(row, 1);
                String tags = (String) table.getValueAt(row, 2);
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("name", name);
                if (Strings.isNotNullOrBlank(type)) {
                    rowMap.put("tagType", type);
                }
                if (Strings.isNotNullOrBlank(tags)) {
                    if (tags.contains(" ")) {
                        rowMap.put("tags", tags.split("\\s+"));
                    } else {
                        rowMap.put("tags", tags);
                    }
                }
                data.add(rowMap);
            }
            try {
                Json.dumpPretty(data, projectFolder.getChild("annotation.json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dispose();
        });
        var cancel = new JButton("Cancel");
        cancel.addActionListener(l -> {
            dispose();
        });
        buttonBox.add(ok);
        buttonBox.add(cancel);
        add(buttonBox);

    }

    private JComboBox<String> createColumnTypeComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("");
        comboBox.addItem("FIXED");
        comboBox.addItem("IOB-HIERARCHICAL_ENUM");
        comboBox.addItem("IOB-FIXED");
        comboBox.addItem("IOB-FIXED-HIERARCHY");
        comboBox.addItem("IOB-FREEFORM");
        return comboBox;
    }

}
