package com.gengoai.hermes.tools.swing.gui;

import com.gengoai.ParameterDef;
import com.gengoai.hermes.corpus.Corpus;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.hermes.format.DocFormatParameters;
import com.gengoai.hermes.format.DocFormatService;
import com.gengoai.io.Resources;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.string.Strings;
import com.gengoai.string.TableFormatter;
import com.gengoai.swing.component.listener.FluentAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gengoai.swing.component.Components.button;

public class NewCorpusDialog extends JDialog {
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

    private final Consumer<Resource> onCreated;

    public NewCorpusDialog(Consumer<Resource> onCreated){
        this.onCreated = onCreated;
        setLayout(new MigLayout());
        setModal(true);
        setSize(450, 275);
        setResizable(false);
        setTitle("New Corpus");
        setLocationRelativeTo(null);
        List<String> items = DocFormatService.getProviders().stream()
                                             .map(p -> p.getName().toLowerCase())
                                             .collect(Collectors.toList());
        Collections.sort(items);
        items.forEach(formats::addItem);

        add(new JLabel("Corpus Location:"), "align right");
        add(corpusLocationTxt, "growx");
        add(chooseCorpusLocationBtn, "wrap");
        add(new JLabel("Doc. Format:"), "align right");
        add(formats, "growx, span 2, wrap");
        add(new JLabel("OPL:"), "align right");
        add(isOPL, "grow, wrap");

        add(new JLabel("Import Location:"), "align right");
        add(importLocationTxt, "growx");
        add(chooseDFLocationBtn, "wrap");

        add(new JLabel("Format Options:"), "align right");
        add(formatOptionsText, "growx");

        var formatParamsBtn = new JButton("?");
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

        add(formatParamsBtn, "wrap");




        var hBox = new JPanel();
        var btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(l -> dispose());
        hBox.add(btnCancel, "align right");
        hBox.add(createCorpusBtn, "align right");
        add(hBox, "span 3, align right");
        pack();
    }

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
        onCreated.accept(Resources.from(corpusLocationTxt.getText()));
        dispose();
    }

}//end class NewCorpusDialog
