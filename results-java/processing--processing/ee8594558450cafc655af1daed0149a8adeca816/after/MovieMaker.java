package processing.app.tools;
/*
 * Nearly all of this code is
 * Copyright © 2010-2011 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 * (However, he should not be held responsible for the current mess of a hack
 * that it has become.)
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */

import ch.randelshofer.gui.datatransfer.FileTextFieldTransferHandler;
import ch.randelshofer.media.mp3.MP3AudioInputStream;
import ch.randelshofer.media.quicktime.QuickTimeWriter;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import processing.app.Editor;


// TODO [fry 2011-09-06]
// + The dialog box is super ugly. It's a hacked up version of the previous
//   interface, but it'll take a bit of time to clean it up.
//   http://code.google.com/p/processing/issues/detail?id=836
// + the None compressor seems to have bugs, so just disabled it instead.
// + the 'pass through' option seems to be broken, it's been removed, and in
//   its place is an option to use the same width and height as the originals.
// + when this new 'pass through' is set, there's some nastiness with how
//   the 'final' width/height variables are passed to the movie maker.
//   this is an easy fix but needs a couple minutes.

/**
 * Hacked from Werner Randelshofer's QuickTimeWriter demo. The original version
 * can be found <a href="http://www.randelshofer.ch/blog/2010/10/writing-quicktime-movies-in-pure-java/">here</a>.
 */
public class MovieMaker extends JFrame implements Tool {
  private JFileChooser imageFolderChooser;
  private JFileChooser soundFileChooser;
  private JFileChooser movieFileChooser;
  private Preferences prefs;

//  private Editor editor;

//MovieMaker m = new MovieMaker();
//m.setVisible(true);
//m.pack();

  public String getMenuTitle() {
    return "Movie Maker";
  }


  public void run() {
//    System.out.println("calling run() for MovieMaker " + EventQueue.isDispatchThread());
    setVisible(true);
  }


//  public void run() {
//    String classPath =
//      getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
//    System.out.println("cp is " + classPath);
//    try {
//      String[] cmd = new String[] {
//        "java", "-cp", classPath, "processing.app.tools.MovieMaker"
//      };
//      Runtime.getRuntime().exec(cmd);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }


  public void init(Editor editor) {
//    System.out.println("calling init for MovieMaker " + EventQueue.isDispatchThread());
//    this.editor = editor;
    initComponents();

//    String version = getClass().getPackage().getImplementationVersion();
//    if (version != null) {
//      setTitle(getTitle() + " " + version);
//    }

    ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 18, 18, 18));
    imageFolderField.setTransferHandler(new FileTextFieldTransferHandler(JFileChooser.DIRECTORIES_ONLY));
    soundFileField.setTransferHandler(new FileTextFieldTransferHandler());

    JComponent[] smallComponents = {
      compressionBox,
      compressionLabel,
      fpsField,
      fpsLabel,
      widthField,
      widthLabel,
      heightField,
      heightLabel,
      originalSizeCheckBox,
//      noPreparationRadio,
//      fastStartCompressedRadio,
//      fastStartRadio
    };
    for (JComponent c : smallComponents) {
      c.putClientProperty("JComponent.sizeVariant", "small");
    }

    // Get Preferences
    prefs = Preferences.userNodeForPackage(MovieMaker.class);
    imageFolderField.setText(prefs.get("movie.imageFolder", ""));
    soundFileField.setText(prefs.get("movie.soundFile", ""));
    widthField.setText("" + prefs.getInt("movie.width", 640));
    heightField.setText("" + prefs.getInt("movie.height", 480));
    boolean original = prefs.getBoolean("movie.originalSize", false);
    originalSizeCheckBox.setSelected(original);
    widthField.setEnabled(!original);
    heightField.setEnabled(!original);
    String fps = "" + prefs.getDouble("movie.fps", 30);
    if (fps.endsWith(".0")) {
      fps = fps.substring(0, fps.length() - 2);
    }
    fpsField.setText(fps);
    compressionBox.setSelectedIndex(Math.max(0, Math.min(compressionBox.getItemCount() - 1, prefs.getInt("movie.compression", 0))));

    originalSizeCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled = !originalSizeCheckBox.isSelected();
        widthField.setEnabled(enabled);
        heightField.setEnabled(enabled);
      }
    });

//    String streaming = prefs.get("movie.streaming", "fastStartCompressed");
//    for (Enumeration<AbstractButton> i = streamingGroup.getElements(); i.hasMoreElements();) {
//      AbstractButton btn = i.nextElement();
//      if (btn.getActionCommand().equals(streaming)) {
//        btn.setSelected(true);
//        break;
//      }
//    }

    // scoot everybody around
    pack();
    // center the frame on screen
    setLocationRelativeTo(null);
  }


  /**
   * Registers key events for a Ctrl-W and ESC with an ActionListener
   * that will take care of disposing the window.
   */
  static public void registerWindowCloseKeys(JRootPane root,
                                             ActionListener disposer) {
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    root.registerKeyboardAction(disposer, stroke,
                                JComponent.WHEN_IN_FOCUSED_WINDOW);

    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    stroke = KeyStroke.getKeyStroke('W', modifiers);
    root.registerKeyboardAction(disposer, stroke,
                                JComponent.WHEN_IN_FOCUSED_WINDOW);
  }


  private void initComponents() {
    imageFolderHelpLabel = new JLabel();
    imageFolderField = new JTextField();
    chooseImageFolderButton = new JButton();
    soundFileHelpLabel = new JLabel();
    soundFileField = new JTextField();
    chooseSoundFileButton = new JButton();
    createMovieButton = new JButton();
    widthLabel = new JLabel();
    widthField = new JTextField();
    heightLabel = new JLabel();
    heightField = new JTextField();
    compressionLabel = new JLabel();
    compressionBox = new JComboBox();
    fpsLabel = new JLabel();
    fpsField = new JTextField();
    originalSizeCheckBox = new JCheckBox();
//    streamingLabel = new JLabel();
//    streamingGroup = new ButtonGroup();
//    noPreparationRadio = new JRadioButton();
//    fastStartRadio = new JRadioButton();
//    fastStartCompressedRadio = new JRadioButton();

    FormListener formListener = new FormListener();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
//        System.exit(0);
      }
    });
    registerWindowCloseKeys(getRootPane(), new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        setVisible(false);
//        System.exit(0);
      }
    });
    setTitle("QuickTime Movie Maker");

    aboutLabel =
      new JLabel("<html>" +
                 "<b>This tool creates a QuickTime movie from a sequence of images.</b><br> " +
                 "<br>" +
                 "To avoid artifacts caused by re-compressing images as video,<br> " +
                 "use TIFF, TGA (from Processing), or PNG images as the source.<br>" +
                 "<br>" +
                 "TIFF and TGA images will write more quickly, but require more disk:<br>" +
                 "<tt>saveFrame(\"frames/####.tif\");</tt><br>" +
                 "<tt>saveFrame(\"frames/####.tga\");</tt><br>" +
                 "<br>" +
                 "PNG images are smaller, but your sketch will run more slowly:<br>" +
                 "<tt>saveFrame(\"frames/####.png\");</tt><br>" +
                 "<br>" +
                 "<font color=#808080>This code is based on QuickTime Movie Maker 1.5.1 2011-01-17.<br>" +
                 "Copyright \u00A9 2010-2011 Werner Randelshofer. All rights reserved.<br>" +
                 "This software is licensed under Creative Commons Atribution 3.0.");

    imageFolderHelpLabel.setText("Drag a folder with image files into the field below:");
    chooseImageFolderButton.setText("Choose...");
    chooseImageFolderButton.addActionListener(formListener);

    soundFileHelpLabel.setText("Drag a sound file into the field below (.au, .aiff, .wav, .mp3):");
    chooseSoundFileButton.setText("Choose...");
    chooseSoundFileButton.addActionListener(formListener);

    createMovieButton.setText("Create Movie...");
    createMovieButton.addActionListener(formListener);

    Font font = new Font("Dialog", Font.PLAIN, 11);

    widthLabel.setFont(font);
    widthLabel.setText("Width:");
    widthField.setColumns(4);
    widthField.setFont(font);
    widthField.setText("320");

    heightLabel.setFont(font);
    heightLabel.setText("Height:");
    heightField.setColumns(4);
    heightField.setFont(font);
    heightField.setText("240");

    compressionLabel.setFont(font);
    compressionLabel.setText("Compression:");
    compressionBox.setFont(font);
    //compressionBox.setModel(new DefaultComboBoxModel(new String[] { "None", "Animation", "JPEG", "PNG" }));
    compressionBox.setModel(new DefaultComboBoxModel(new String[] { "Animation", "JPEG", "PNG" }));

    fpsLabel.setFont(font);
    fpsLabel.setText("Frame Rate:");
    fpsField.setColumns(4);
    fpsField.setFont(font);
    fpsField.setText("30");

    originalSizeCheckBox.setFont(font);
    originalSizeCheckBox.setText("Same size as originals");
    originalSizeCheckBox.setToolTipText("Check this box if the folder contains already encoded video frames in the desired size.");

//        streamingLabel.setText("Prepare for Internet Streaming");
//
//        streamingGroup.add(noPreparationRadio);
//        noPreparationRadio.setFont(font);
//        noPreparationRadio.setSelected(true);
//        noPreparationRadio.setText("No preparation");
//        noPreparationRadio.setActionCommand("none");
//        noPreparationRadio.addActionListener(formListener);
//
//        streamingGroup.add(fastStartRadio);
//        fastStartRadio.setFont(font);
//        fastStartRadio.setText("Fast Start");
//        fastStartRadio.setActionCommand("fastStart");
//        fastStartRadio.addActionListener(formListener);
//
//        streamingGroup.add(fastStartCompressedRadio);
//        fastStartCompressedRadio.setFont(font);
//        fastStartCompressedRadio.setText("Fast Start - Compressed Header");
//        fastStartCompressedRadio.setActionCommand("fastStartCompressed");
//        fastStartCompressedRadio.addActionListener(formListener);

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                              .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                  .addGroup(layout.createSequentialGroup()
                                                            .addGap(61, 61, 61)
                                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                      .addComponent(widthLabel)
                                                                      .addComponent(fpsLabel))
                                                                      .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                          .addComponent(fpsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                          .addComponent(compressionLabel)
                                                                                          .addGap(1, 1, 1)
                                                                                          .addComponent(compressionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                          .addComponent(originalSizeCheckBox))
                                                                                          .addGroup(layout.createSequentialGroup()
                                                                                                    .addComponent(widthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                    .addComponent(heightLabel)
                                                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                                    .addComponent(heightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                                                                    .addGap(41, 41, 41))
                                                                                                    .addGroup(layout.createSequentialGroup()
                                                                                                              .addContainerGap()
                                                                                                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                                                        .addComponent(aboutLabel, GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                                                                                                                        .addComponent(imageFolderHelpLabel)
                                                                                                                        .addComponent(soundFileHelpLabel)
                                                                                                                        .addGroup(layout.createSequentialGroup()
                                                                                                                                  .addComponent(soundFileField, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                                                                                                                                  .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                  .addComponent(chooseSoundFileButton))
                                                                                                                                  .addComponent(createMovieButton, GroupLayout.Alignment.TRAILING)
                                                                                                                                  .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                                                                                            .addComponent(imageFolderField, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                                                                                                                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                            .addComponent(chooseImageFolderButton))))
                                                                                                                                            .addGroup(layout.createSequentialGroup()
                                                                                                                                                      .addContainerGap())))
    );
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                      .addContainerGap()
                                      .addComponent(aboutLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                      .addGap(18, 18, 18)
                                      .addComponent(imageFolderHelpLabel)
                                      .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(imageFolderField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(chooseImageFolderButton))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                          .addComponent(widthLabel)
                                                          .addComponent(widthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                          .addComponent(heightLabel)
                                                          .addComponent(heightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(compressionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(fpsLabel)
                                                                    .addComponent(fpsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(compressionLabel)
                                                                    .addComponent(originalSizeCheckBox))
                                                                    .addGap(18, 18, 18)
                                                                    .addComponent(soundFileHelpLabel)
                                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                              .addComponent(soundFileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                              .addComponent(chooseSoundFileButton))
                                                                              .addGap(18, 18, 18)
                                                                              .addComponent(createMovieButton)
                                                                              .addContainerGap())
    );

    pack();
  }

  // Code for dispatching events from components to event handlers.

  private class FormListener implements java.awt.event.ActionListener {
    FormListener() {}
    public void actionPerformed(java.awt.event.ActionEvent evt) {
      if (evt.getSource() == chooseImageFolderButton) {
        MovieMaker.this.chooseImageFolder(evt);
      }
      else if (evt.getSource() == chooseSoundFileButton) {
        MovieMaker.this.chooseSoundFile(evt);
      }
      else if (evt.getSource() == createMovieButton) {
        MovieMaker.this.createMovie(evt);
      }
//      else if (evt.getSource() == fastStartCompressedRadio) {
//        MovieMaker.this.streamingRadioPerformed(evt);
//      }
//      else if (evt.getSource() == fastStartRadio) {
//        MovieMaker.this.streamingRadioPerformed(evt);
//      }
//      else if (evt.getSource() == noPreparationRadio) {
//        MovieMaker.this.streamingRadioPerformed(evt);
//      }
    }
  }

  private void chooseImageFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseImageFolder
    if (imageFolderChooser == null) {
      imageFolderChooser = new JFileChooser();
      imageFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (imageFolderField.getText().length() > 0) {
        imageFolderChooser.setSelectedFile(new File(imageFolderField.getText()));
      } else if (soundFileField.getText().length() > 0) {
        imageFolderChooser.setCurrentDirectory(new File(soundFileField.getText()).getParentFile());
      }
    }
    if (JFileChooser.APPROVE_OPTION == imageFolderChooser.showOpenDialog(this)) {
      imageFolderField.setText(imageFolderChooser.getSelectedFile().getPath());
    }
  }

  private void chooseSoundFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseSoundFile
    if (soundFileChooser == null) {
      soundFileChooser = new JFileChooser();
      if (soundFileField.getText().length() > 0) {
        soundFileChooser.setSelectedFile(new File(soundFileField.getText()));
      } else if (imageFolderField.getText().length() > 0) {
        soundFileChooser.setCurrentDirectory(new File(imageFolderField.getText()));
      }
    }
    if (JFileChooser.APPROVE_OPTION == soundFileChooser.showOpenDialog(this)) {
      soundFileField.setText(soundFileChooser.getSelectedFile().getPath());
    }
  }


  // this is super naughty, and shouldn't be out here. it's a hack to get the
  // ImageIcon width/height setting to work. there are better ways to do this
  // given a bit of time. you know, time? the infinite but non-renewable resource?
  int width, height;

  private void createMovie(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMovie
    // ---------------------------------
    // Check input
    // ---------------------------------
    final File soundFile = soundFileField.getText().trim().length() == 0 ? null : new File(soundFileField.getText().trim());
    final File imageFolder = imageFolderField.getText().trim().length() == 0 ? null : new File(imageFolderField.getText().trim());
    //final String streaming = prefs.get("movie.streaming", "fastStartCompressed");
    final String streaming = "fastStartCompressed";
    if (soundFile == null && imageFolder == null) {
      JOptionPane.showMessageDialog(this, "<html>You need to specify a folder with<br>image files and/or a sound file.");
      return;
    }

    final double fps;
    try {
      width = Integer.parseInt(widthField.getText());
      height = Integer.parseInt(heightField.getText());
      fps = Double.parseDouble(fpsField.getText());
    } catch (Throwable t) {
      JOptionPane.showMessageDialog(this, "<html>Width, Height and FPS must be numeric.");
      return;
    }
    if (width < 1 || height < 1 || fps < 1) {
      JOptionPane.showMessageDialog(this, "<html>Width, Height and FPS must be greater than zero.");
      return;
    }

    final QuickTimeWriter.VideoFormat videoFormat;
    switch (compressionBox.getSelectedIndex()) {
//    case 0:
//      videoFormat = QuickTimeWriter.VideoFormat.RAW;
//      break;
    case 0://1:
      videoFormat = QuickTimeWriter.VideoFormat.RLE;
      break;
    case 1://2:
      videoFormat = QuickTimeWriter.VideoFormat.JPG;
      break;
    case 2://3:
    default:
      videoFormat = QuickTimeWriter.VideoFormat.PNG;
      break;
    }

    // ---------------------------------
    // Update Preferences
    // ---------------------------------
    prefs.put("movie.imageFolder", imageFolderField.getText());
    prefs.put("movie.soundFile", soundFileField.getText());
    prefs.putInt("movie.width", width);
    prefs.putInt("movie.height", height);
    prefs.putDouble("movie.fps", fps);
    prefs.putInt("movie.compression", compressionBox.getSelectedIndex());
    prefs.putBoolean("movie.originalSize", originalSizeCheckBox.isSelected());


    // ---------------------------------
    // Choose an output file
    // ---------------------------------
    if (movieFileChooser == null) {
      movieFileChooser = new JFileChooser();
      if (prefs.get("movie.outputFile", null) != null) {
        movieFileChooser.setSelectedFile(new File(prefs.get("movie.outputFile", null)));
      } else {
        if (imageFolderField.getText().length() > 0) {
          movieFileChooser.setCurrentDirectory(new File(imageFolderField.getText()).getParentFile());
        } else if (soundFileField.getText().length() > 0) {
          movieFileChooser.setCurrentDirectory(new File(soundFileField.getText()).getParentFile());
        }
      }
    }
    if (JFileChooser.APPROVE_OPTION != movieFileChooser.showSaveDialog(this)) {
      return;
    }

    final File movieFile = movieFileChooser.getSelectedFile().getPath().toLowerCase().endsWith(".mov")//
    ? movieFileChooser.getSelectedFile()
        : new File(movieFileChooser.getSelectedFile().getPath() + ".mov");
    prefs.put("movie.outputFile", movieFile.getPath());
    createMovieButton.setEnabled(false);

    final boolean originalSize = originalSizeCheckBox.isSelected();

    // ---------------------------------
    // Create the QuickTime movie
    // ---------------------------------
    SwingWorker w = new SwingWorker() {

      @Override
      protected Object doInBackground() {
        try {

          // Read image files
          File[] imgFiles = null;
          if (imageFolder != null) {
            imgFiles = imageFolder.listFiles(new FileFilter() {
              FileSystemView fsv = FileSystemView.getFileSystemView();

              public boolean accept(File f) {
                return f.isFile() && !fsv.isHiddenFile(f) && !f.getName().equals("Thumbs.db");
              }
            });
            if (imgFiles == null || imgFiles.length == 0) {
              return new RuntimeException("No image files found.");
            }
            Arrays.sort(imgFiles);
          }

          // Check on first image, if we can actually do pass through
          if (originalSize) {
            // This was using ImageIcon, which can't handle some file types.
            // For 2.1, switching to ImageIO (which is used for movie
            // generation anyway) [fry 131008]
            BufferedImage temp = readImage(imgFiles[0]);
            if (temp == null) {
              return new RuntimeException("Coult not read " + imgFiles[0].getAbsolutePath());
            }
            width = temp.getWidth();
            height = temp.getHeight();
            if (width <= 0 || height <= 0) {
              return new RuntimeException("Could not read " + imgFiles[0].getName() + ", it may be bad.");
            }
          }

          // Delete movie file if it already exists.
          if (movieFile.exists()) {
            movieFile.delete();
          }

          if (imageFolder != null && soundFile != null) {
            writeVideoAndAudio(movieFile, imgFiles, soundFile, width, height, fps, videoFormat, /*passThrough,*/ streaming);
          } else if (imageFolder != null) {
            writeVideoOnlyVFR(movieFile, imgFiles, width, height, fps, videoFormat, /*passThrough,*/ streaming);
          } else {
            writeAudioOnly(movieFile, soundFile, streaming);
          }
          return null;

        } catch (Throwable t) {
          return t;
        }
      }

      @Override
      protected void done() {
        Object o;
        try {
          o = get();
        } catch (Exception ex) {
          o = ex;
        }
        if (o instanceof Throwable) {
          Throwable t = (Throwable) o;
          t.printStackTrace();
          JOptionPane.showMessageDialog(MovieMaker.this, "<html>Creating the QuickTime Movie failed.<br>" + (t.getMessage() == null ? t.toString() : t.getMessage()), "Sorry", JOptionPane.ERROR_MESSAGE);
        }
        createMovieButton.setEnabled(true);
      }
    };
    w.execute();


  }//GEN-LAST:event_createMovie


  private BufferedImage readImage(File file) throws IOException {
    BufferedImage image = ImageIO.read(file);

    /*
    String[] loadImageFormats = ImageIO.getReaderFormatNames();
    if (loadImageFormats != null) {
      for (String format : loadImageFormats) {
        System.out.println(format);
      }
    }
    */

    if (image == null) {
      String path = file.getAbsolutePath();
      // Might be an incompatible TGA or TIFF created by Processing
      if (path.toLowerCase().endsWith(".tga")) {
        return loadImageTGA(file);

      } else if (path.toLowerCase().endsWith(".tif")) {
        throw new IOException("Try TGA or PNG images instead of TIFF.");
      }
    }
    return image;
  }


  /**
   * Targa image loader for RLE-compressed TGA files.
   * <p>
   * Rewritten for 0115 to read/write RLE-encoded targa images.
   * For 0125, non-RLE encoded images are now supported, along with
   * images whose y-order is reversed (which is standard for TGA files).
   */
  static protected BufferedImage loadImageTGA(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    if (is == null) return null;

    byte header[] = new byte[18];
    int offset = 0;
    do {
      int count = is.read(header, offset, header.length - offset);
      if (count == -1) return null;
      offset += count;
    } while (offset < 18);

    /*
      header[2] image type code
      2  (0x02) - Uncompressed, RGB images.
      3  (0x03) - Uncompressed, black and white images.
      10 (0x0A) - Runlength encoded RGB images.
      11 (0x0B) - Compressed, black and white images. (grayscale?)

      header[16] is the bit depth (8, 24, 32)

      header[17] image descriptor (packed bits)
      0x20 is 32 = origin upper-left
      0x28 is 32 + 8 = origin upper-left + 32 bits

        7  6  5  4  3  2  1  0
      128 64 32 16  8  4  2  1
    */

    int format = 0;
    final int RGB = 1;
    final int ARGB = 2;
    final int ALPHA = 4;

    if (((header[2] == 3) || (header[2] == 11)) &&  // B&W, plus RLE or not
        (header[16] == 8) &&  // 8 bits
        ((header[17] == 0x8) || (header[17] == 0x28))) {  // origin, 32 bit
      format = ALPHA;

    } else if (((header[2] == 2) || (header[2] == 10)) &&  // RGB, RLE or not
               (header[16] == 24) &&  // 24 bits
               ((header[17] == 0x20) || (header[17] == 0))) {  // origin
      format = RGB;

    } else if (((header[2] == 2) || (header[2] == 10)) &&
               (header[16] == 32) &&
               ((header[17] == 0x8) || (header[17] == 0x28))) {  // origin, 32
      format = ARGB;
    }

    if (format == 0) {
      throw new IOException("Unknown .tga file format for " + file.getName());
    }

    int w = ((header[13] & 0xff) << 8) + (header[12] & 0xff);
    int h = ((header[15] & 0xff) << 8) + (header[14] & 0xff);
    //PImage outgoing = createImage(w, h, format);
    int[] pixels = new int[w * h];

    // where "reversed" means upper-left corner (normal for most of
    // the modernized world, but "reversed" for the tga spec)
    //boolean reversed = (header[17] & 0x20) != 0;
    // https://github.com/processing/processing/issues/1682
    boolean reversed = (header[17] & 0x20) == 0;

    if ((header[2] == 2) || (header[2] == 3)) {  // not RLE encoded
      if (reversed) {
        int index = (h-1) * w;
        switch (format) {
        case ALPHA:
          for (int y = h-1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
              pixels[index + x] = is.read();
            }
            index -= w;
          }
          break;
        case RGB:
          for (int y = h-1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
              pixels[index + x] =
                is.read() | (is.read() << 8) | (is.read() << 16) |
                0xff000000;
            }
            index -= w;
          }
          break;
        case ARGB:
          for (int y = h-1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
              pixels[index + x] =
                is.read() | (is.read() << 8) | (is.read() << 16) |
                (is.read() << 24);
            }
            index -= w;
          }
        }
      } else {  // not reversed
        int count = w * h;
        switch (format) {
        case ALPHA:
          for (int i = 0; i < count; i++) {
            pixels[i] = is.read();
          }
          break;
        case RGB:
          for (int i = 0; i < count; i++) {
            pixels[i] =
              is.read() | (is.read() << 8) | (is.read() << 16) |
              0xff000000;
          }
          break;
        case ARGB:
          for (int i = 0; i < count; i++) {
            pixels[i] =
              is.read() | (is.read() << 8) | (is.read() << 16) |
              (is.read() << 24);
          }
          break;
        }
      }

    } else {  // header[2] is 10 or 11
      int index = 0;

      while (index < pixels.length) {
        int num = is.read();
        boolean isRLE = (num & 0x80) != 0;
        if (isRLE) {
          num -= 127;  // (num & 0x7F) + 1
          int pixel = 0;
          switch (format) {
          case ALPHA:
            pixel = is.read();
            break;
          case RGB:
            pixel = 0xFF000000 |
              is.read() | (is.read() << 8) | (is.read() << 16);
            //(is.read() << 16) | (is.read() << 8) | is.read();
            break;
          case ARGB:
            pixel = is.read() |
              (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
            break;
          }
          for (int i = 0; i < num; i++) {
            pixels[index++] = pixel;
            if (index == pixels.length) break;
          }
        } else {  // write up to 127 bytes as uncompressed
          num += 1;
          switch (format) {
          case ALPHA:
            for (int i = 0; i < num; i++) {
              pixels[index++] = is.read();
            }
            break;
          case RGB:
            for (int i = 0; i < num; i++) {
              pixels[index++] = 0xFF000000 |
                is.read() | (is.read() << 8) | (is.read() << 16);
            }
            break;
          case ARGB:
            for (int i = 0; i < num; i++) {
              pixels[index++] = is.read() |
                (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
            }
            break;
          }
        }
      }

      if (!reversed) {
        int[] temp = new int[w];
        for (int y = 0; y < h/2; y++) {
          int z = (h-1) - y;
          System.arraycopy(pixels, y*w, temp, 0, w);
          System.arraycopy(pixels, z*w, pixels, y*w, w);
          System.arraycopy(temp, 0, pixels, z*w, w);
        }
      }
    }
    int type = (format == RGB) ?
        BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
    BufferedImage image = new BufferedImage(w, h, type);
    WritableRaster wr = image.getRaster();
    wr.setDataElements(0, 0, w, h, pixels);
    return image;
  }


//  private void streamingRadioPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streamingRadioPerformed
//    prefs.put("movie.streaming", evt.getActionCommand());
//  }//GEN-LAST:event_streamingRadioPerformed

  /** variable frame rate. */
  private void writeVideoOnlyVFR(File movieFile, File[] imgFiles, int width, int height, double fps, QuickTimeWriter.VideoFormat videoFormat, /*boolean passThrough,*/ String streaming) throws IOException {
    File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
    ProgressMonitor p = new ProgressMonitor(MovieMaker.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
    Graphics2D g = null;
    BufferedImage img = null;
    BufferedImage prevImg = null;
    int[] data = null;
    int[] prevData = null;
    QuickTimeWriter qtOut = null;
    try {
      int timeScale = (int) (fps * 100.0);
      int duration = 100;

      qtOut = new QuickTimeWriter(videoFormat == QuickTimeWriter.VideoFormat.RAW ? movieFile : tmpFile);
      qtOut.addVideoTrack(videoFormat, timeScale, width, height);
      qtOut.setSyncInterval(0, 30);

      //if (!passThrough) {
      if (true) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        prevImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        prevData = ((DataBufferInt) prevImg.getRaster().getDataBuffer()).getData();
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }
      int prevImgDuration = 0;
      for (int i = 0; i < imgFiles.length && !p.isCanceled(); i++) {
        File f = imgFiles[i];
        p.setNote("Processing " + f.getName());
        p.setProgress(i);

        //if (passThrough) {
        if (false) {
          qtOut.writeSample(0, f, duration);
        } else {
          BufferedImage fImg = ImageIO.read(f);
          g.drawImage(fImg, 0, 0, width, height, null);
          if (i != 0 && Arrays.equals(data, prevData)) {
            prevImgDuration += duration;
          } else {
            if (prevImgDuration != 0) {
              qtOut.writeFrame(0, prevImg, prevImgDuration);
            }
            prevImgDuration = duration;
            System.arraycopy(data, 0, prevData, 0, data.length);
          }
        }
      }
      if (prevImgDuration != 0) {
        qtOut.writeFrame(0, prevImg, prevImgDuration);
      }
      if (streaming.equals("fastStart")) {
        qtOut.toWebOptimizedMovie(movieFile, false);
        tmpFile.delete();
      } else if (streaming.equals("fastStartCompressed")) {
        qtOut.toWebOptimizedMovie(movieFile, true);
        tmpFile.delete();
      }
      qtOut.close();
      qtOut = null;
    } finally {
      p.close();
      if (g != null) {
        g.dispose();
      }
      if (img != null) {
        img.flush();
      }
      if (qtOut != null) {
        qtOut.close();
      }
    }
  }

  /** fixed framerate. */
  /*
    private void writeVideoOnlyFFR(File movieFile, File[] imgFiles, int width, int height, double fps, QuickTimeWriter.VideoFormat videoFormat, boolean passThrough, String streaming) throws IOException {
        File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
        ProgressMonitor p = new ProgressMonitor(MovieMaker.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
        Graphics2D g = null;
        BufferedImage imgBuffer = null;
        QuickTimeWriter qtOut = null;

        try {
            int timeScale = (int) (fps * 100.0);
            int duration = 100;
            qtOut = new QuickTimeWriter(videoFormat == QuickTimeWriter.VideoFormat.RAW ? movieFile : tmpFile);
            qtOut.addVideoTrack(videoFormat, timeScale, width, height);
            //qtOut.setSyncInterval(0,0);
            if (!passThrough) {
                imgBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                g = imgBuffer.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            }
            for (int i = 0; i < imgFiles.length && !p.isCanceled(); i++) {
                File f = imgFiles[i];
                p.setNote("Processing " + f.getName());
                p.setProgress(i);

                if (passThrough) {
                    qtOut.writeSample(0, f, duration);
                } else {
                    BufferedImage fImg = ImageIO.read(f);
                    if (fImg == null) {
                        continue;
                    }
                    g.drawImage(fImg, 0, 0, width, height, null);
                    qtOut.writeFrame(0, imgBuffer, duration);
                }
            }
            if (streaming.equals("fastStart")) {
                qtOut.toWebOptimizedMovie(movieFile, false);
                tmpFile.delete();
            } else if (streaming.equals("fastStartCompressed")) {
                qtOut.toWebOptimizedMovie(movieFile, true);
                tmpFile.delete();
            }
                qtOut.close();
                qtOut = null;
        } finally {
            p.close();
            if (g != null) {
                g.dispose();
            }
            if (imgBuffer != null) {
                imgBuffer.flush();
            }
            if (qtOut != null) {
                qtOut.close();
            }
        }
    }
   */

  private void writeAudioOnly(File movieFile, File audioFile, String streaming) throws IOException {
    File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");

    int length = (int) Math.min(Integer.MAX_VALUE, audioFile.length()); // file length is used for a rough progress estimate. This will only work for uncompressed audio.
    ProgressMonitor p = new ProgressMonitor(MovieMaker.this, "Creating " + movieFile.getName(), "Initializing...", 0, length);
    AudioInputStream audioIn = null;
    QuickTimeWriter qtOut = null;

    try {
      qtOut = new QuickTimeWriter(tmpFile);
      if (audioFile.getName().toLowerCase().endsWith(".mp3")) {
        audioIn = new MP3AudioInputStream(audioFile);
      } else {
        audioIn = AudioSystem.getAudioInputStream(audioFile);
      }
      AudioFormat audioFormat = audioIn.getFormat();
      //System.out.println("QuickTimeMovieMakerMain " + audioFormat);
      qtOut.addAudioTrack(audioFormat);
      boolean isVBR = audioFormat.getProperty("vbr") != null && ((Boolean) audioFormat.getProperty("vbr")).booleanValue();
      int asSize = audioFormat.getFrameSize();
      int nbOfFramesInBuffer = isVBR ? 1 : Math.max(1, 1024 / asSize);
      int asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
      //System.out.println("  frameDuration=" + asDuration);
      long count = 0;
      byte[] audioBuffer = new byte[asSize * nbOfFramesInBuffer];
      for (int bytesRead = audioIn.read(audioBuffer); bytesRead
      != -1; bytesRead = audioIn.read(audioBuffer)) {
        if (bytesRead != 0) {
          int framesRead = bytesRead / asSize;
          qtOut.writeSamples(0, framesRead, audioBuffer, 0, bytesRead, asDuration);
          count += bytesRead;
          p.setProgress((int) count);
        }
        if (isVBR) {
          audioFormat = audioIn.getFormat();
          if (audioFormat == null) {
            break;
          }
          asSize = audioFormat.getFrameSize();
          asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
          if (audioBuffer.length < asSize) {
            audioBuffer = new byte[asSize];
          }
        }
      }
      audioIn.close();
      audioIn = null;
      if (streaming.equals("fastStart")) {
        qtOut.toWebOptimizedMovie(movieFile, false);
        tmpFile.delete();
      } else if (streaming.equals("fastStartCompressed")) {
        qtOut.toWebOptimizedMovie(movieFile, true);
        tmpFile.delete();
      }
      qtOut.close();
      qtOut = null;
    } catch (UnsupportedAudioFileException e) {
      IOException ioe = new IOException(e.getMessage());
      ioe.initCause(e);
      throw ioe;
    } finally {
      p.close();
      if (audioIn != null) {
        audioIn.close();
      }
      if (qtOut != null) {
        qtOut.close();
      }
    }
  }

  private void writeVideoAndAudio(File movieFile, File[] imgFiles, File audioFile, int width, int height, double fps, QuickTimeWriter.VideoFormat videoFormat, /*boolean passThrough,*/ String streaming) throws IOException {
    File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
    ProgressMonitor p = new ProgressMonitor(MovieMaker.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
    AudioInputStream audioIn = null;
    QuickTimeWriter qtOut = null;
    BufferedImage imgBuffer = null;
    Graphics2D g = null;

    try {
      // Determine audio format
      if (audioFile.getName().toLowerCase().endsWith(".mp3")) {
        audioIn = new MP3AudioInputStream(audioFile);
      } else {
        audioIn = AudioSystem.getAudioInputStream(audioFile);
      }
      AudioFormat audioFormat = audioIn.getFormat();
      boolean isVBR = audioFormat.getProperty("vbr") != null && ((Boolean) audioFormat.getProperty("vbr")).booleanValue();

      // Determine duration of a single sample
      int asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
      int vsDuration = 100;
      // Create writer
      qtOut = new QuickTimeWriter(videoFormat == QuickTimeWriter.VideoFormat.RAW ? movieFile : tmpFile);
      qtOut.addAudioTrack(audioFormat); // audio in track 0
      qtOut.addVideoTrack(videoFormat, (int) (fps * vsDuration), width, height);  // video in track 1

      // Create audio buffer
      int asSize;
      byte[] audioBuffer;
      if (isVBR) {
        // => variable bit rate: create audio buffer for a single frame
        asSize = audioFormat.getFrameSize();
        audioBuffer = new byte[asSize];
      } else {
        // => fixed bit rate: create audio buffer for half a second
        asSize = audioFormat.getChannels() * audioFormat.getSampleSizeInBits() / 8;
        audioBuffer = new byte[(int) (qtOut.getMediaTimeScale(0) / 2 * asSize)];
      }

      // Create video buffer
      //if (!passThrough) {
      if (true) {
        imgBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g = imgBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      } // Main loop
      int movieTime = 0;
      int imgIndex = 0;
      boolean isAudioDone = false;
      while ((imgIndex < imgFiles.length || !isAudioDone) && !p.isCanceled()) {
        // Advance movie time by half a second (we interleave twice per second)
        movieTime += qtOut.getMovieTimeScale() / 2;

        // Advance audio to movie time + 1 second (audio must be ahead of video by 1 second)
        while (!isAudioDone && qtOut.getTrackDuration(0) < movieTime + qtOut.getMovieTimeScale()) {
          int len = audioIn.read(audioBuffer);
          if (len == -1) {
            isAudioDone = true;
          } else {
            qtOut.writeSamples(0, len / asSize, audioBuffer, 0, len, asDuration);
          }
          if (isVBR) {
            // => variable bit rate: format can change at any time
            audioFormat = audioIn.getFormat();
            if (audioFormat == null) {
              break;
            }
            asSize = audioFormat.getFrameSize();
            asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
            if (audioBuffer.length < asSize) {
              audioBuffer = new byte[asSize];
            }
          }
        }

        // Advance video to movie time
        while (imgIndex < imgFiles.length && qtOut.getTrackDuration(1) < movieTime) {
          // catch up with video time
          p.setProgress(imgIndex);
          p.setNote("Processing " + imgFiles[imgIndex].getName());
          //if (passThrough) {
          if (false) {
            qtOut.writeSample(1, imgFiles[imgIndex], vsDuration);
          } else {
            BufferedImage fImg = ImageIO.read(imgFiles[imgIndex]);
            if (fImg == null) {
              continue;
            }
            g.drawImage(fImg, 0, 0, width, height, null);
            fImg.flush();
            qtOut.writeFrame(1, imgBuffer, vsDuration);
          }
          ++imgIndex;
        }
      }
      if (streaming.equals("fastStart")) {
        qtOut.toWebOptimizedMovie(movieFile, false);
        tmpFile.delete();
      } else if (streaming.equals("fastStartCompressed")) {
        qtOut.toWebOptimizedMovie(movieFile, true);
        tmpFile.delete();
      }
      qtOut.close();
      qtOut = null;
    } catch (UnsupportedAudioFileException e) {
      IOException ioe = new IOException(e.getMessage());
      ioe.initCause(e);
      throw ioe;
    } finally {
      p.close();
      if (qtOut != null) {
        qtOut.close();
      }
      if (audioIn != null) {
        audioIn.close();
      }
      if (g != null) {
        g.dispose();
      }
      if (imgBuffer != null) {
        imgBuffer.flush();
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        MovieMaker m = new MovieMaker();
        m.init(null);
//        m.init();
        m.setVisible(true);
//        m.pack();
      }
    });
  }

  private JLabel aboutLabel;
  private JButton chooseImageFolderButton;
  private JButton chooseSoundFileButton;
  private JComboBox compressionBox;
  private JLabel compressionLabel;
//  private JRadioButton fastStartCompressedRadio;
//  private JRadioButton fastStartRadio;
  private JTextField fpsField;
  private JLabel fpsLabel;
  private JTextField heightField;
  private JLabel heightLabel;
  private JTextField imageFolderField;
  private JLabel imageFolderHelpLabel;
//  private JRadioButton noPreparationRadio;
  private JCheckBox originalSizeCheckBox;
  private JTextField soundFileField;
  private JLabel soundFileHelpLabel;
//  private ButtonGroup streamingGroup;
//  private JLabel streamingLabel;
  private JTextField widthField;
  private JLabel widthLabel;
//  private JLabel copyrightLabel;
  private JButton createMovieButton;
}