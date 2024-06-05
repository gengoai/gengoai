package com.gengoai.swing;

import lombok.NonNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class Dialogs {
   public static File openFile(@NonNull Component parent, @NonNull String title, File startingLocation, @NonNull String... extensions) {
      JFileChooser chooser = new JFileChooser();
      if (startingLocation != null) {
         chooser.setCurrentDirectory(startingLocation);
      }
      chooser.setDialogTitle(title);
      if (extensions.length > 0) {
         chooser.setFileFilter(new FileNameExtensionFilter("Files", extensions));
      }
      int returnVal = chooser.showOpenDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         return chooser.getSelectedFile();
      }
      return null;
   }

   public static File selectFolder(@NonNull Component parent, @NonNull String title, File startingLocation) {
      JFileChooser chooser = new JFileChooser();
      if (startingLocation != null) {
         chooser.setCurrentDirectory(startingLocation);
      }
      chooser.setDialogTitle(title);
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = chooser.showOpenDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         return chooser.getSelectedFile();
      }
      return null;
   }

   public static File saveFile(@NonNull Component parent, @NonNull String title, File startingLocation, @NonNull String... extensions) {
      JFileChooser chooser = new JFileChooser();
      if (startingLocation != null) {
         chooser.setCurrentDirectory(startingLocation);
      }
      chooser.setDialogTitle(title);
      if (extensions.length > 0) {
         chooser.setFileFilter(new FileNameExtensionFilter("Files", extensions));
      }
      int returnVal = chooser.showSaveDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         return chooser.getSelectedFile();
      }
      return null;
   }

}
