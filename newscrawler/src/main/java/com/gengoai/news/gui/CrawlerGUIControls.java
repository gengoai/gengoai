package com.gengoai.news.gui;

import com.gengoai.swing.component.VBox;

import javax.swing.*;
import java.awt.*;

public class CrawlerGUIControls {
   public JFileChooser fileChooser = new JFileChooser();
   public JTextField txtIndexLocation = new JTextField();
   public JButton btnSelectIndex = new JButton("...");
   public JPanel pnlIndexLocation = new JPanel();
   public JPanel pnlControls = new JPanel();
   public VBox pnlProgress = new VBox();
   public JProgressBar pbFeed = new JProgressBar();
   public JProgressBar pbPage = new JProgressBar();
   public JPanel pnlButtons = new JPanel();
   public JButton btnStart = new JButton("Start");
   public JButton btnStop = new JButton("Stop");

   public CrawlerGUIControls(JFrame frame) {
      pnlControls.setLayout(new BorderLayout());

      pnlIndexLocation.setLayout(new BoxLayout(pnlIndexLocation, BoxLayout.X_AXIS));
      pnlIndexLocation.setVisible(false);
      pnlIndexLocation.add(new JLabel(" Index: "));
      txtIndexLocation.setEnabled(false);
      pnlIndexLocation.add(txtIndexLocation);
      pnlIndexLocation.add(btnSelectIndex);
      pnlIndexLocation.setPreferredSize(new Dimension(400, 20));
      pnlControls.add(pnlIndexLocation, BorderLayout.NORTH);

      pnlProgress.setVisible(false);
      pnlProgress.add(new JLabel("Feeds:"));
      pbFeed.setPreferredSize(new Dimension(400, 20));
      pbFeed.setStringPainted(true);
      pnlProgress.add(pbFeed);
      pnlProgress.add(new JLabel("Pages:"));
      pbPage.setPreferredSize(new Dimension(400, 20));
      pbPage.setStringPainted(true);
      pnlProgress.add(pbPage);
      pnlControls.add(pnlProgress, BorderLayout.CENTER);


      pnlButtons.setVisible(false);
      pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
      pnlButtons.add(btnStart);
      pnlButtons.add(btnStop);
      btnStop.setEnabled(false);
      pnlControls.add(pnlButtons, BorderLayout.SOUTH);

      pnlControls.setPreferredSize(new Dimension(400, 120));
      frame.add(pnlControls, BorderLayout.CENTER);
   }

}
