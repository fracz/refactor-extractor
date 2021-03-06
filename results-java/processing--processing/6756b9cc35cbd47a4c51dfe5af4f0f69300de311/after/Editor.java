/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;

import processing.app.contrib.ToolContribution;
import processing.app.syntax.*;
import processing.app.tools.*;
import processing.core.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

/**
 * Main editor panel for the Processing Development Environment.
 */
public abstract class Editor extends JFrame implements RunnerListener {
  protected Base base;
  protected EditorState state;
  protected Mode mode;

  // otherwise, if the window is resized with the message label
  // set to blank, it's preferredSize() will be fukered
  static protected final String EMPTY =
    "                                                                     " +
    "                                                                     " +
    "                                                                     ";

  /**
   * true if this file has not yet been given a name by the user
   */
//  private boolean untitled;

  private PageFormat pageFormat;
  private PrinterJob printerJob;

  // file and sketch menus for re-inserting items
  private JMenu fileMenu;
//  private JMenuItem saveMenuItem;
//  private JMenuItem saveAsMenuItem;

  private JMenu sketchMenu;

  protected EditorHeader header;
  protected EditorToolbar toolbar;
  protected JEditTextArea textarea;
  protected EditorStatus status;
  protected JSplitPane splitPane;
  protected JPanel consolePanel;
  protected EditorConsole console;
  protected EditorLineStatus lineStatus;

  // currently opened program
  protected Sketch sketch;

  // runtime information and window placement
  private Point sketchWindowLocation;

  // undo fellers
  private JMenuItem undoItem, redoItem;
  protected UndoAction undoAction;
  protected RedoAction redoAction;
  /** the currently selected tab's undo manager */
  private UndoManager undo;
  // used internally for every edit. Groups hotkey-event text manipulations and
  // groups  multi-character inputs into a single undos.
  private CompoundEdit compoundEdit;
  // timer to decide when to group characters into an undo
  private Timer timer;
  private TimerTask endUndoEvent;
  // true if inserting text, false if removing text
  private boolean isInserting;
  // maintain caret position during undo operations
  private final Stack<Integer> caretUndoStack = new Stack<Integer>();
  private final Stack<Integer> caretRedoStack = new Stack<Integer>();

  private FindReplace find;
  JMenu toolsMenu;
  JMenu modeMenu;

  ArrayList<ToolContribution> coreTools;
  public ArrayList<ToolContribution> contribTools;


//  protected Editor(final Base base, String path, int[] location, final Mode mode) {
  protected Editor(final Base base, String path, EditorState state, final Mode mode) {
    super("Processing", state.checkConfig());
    this.base = base;
    this.state = state;
    this.mode = mode;

    Toolkit.setIcon(this);  // TODO should this be per-mode?

    // Install default actions for Run, Present, etc.
//    resetHandlers();

    // add listener to handle window close box hit event
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          base.handleClose(Editor.this, false);
        }
      });
    // don't close the window when clicked, the app will take care
    // of that via the handleQuitInternal() methods
    // http://dev.processing.org/bugs/show_bug.cgi?id=440
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    // When bringing a window to front, let the Base know
    addWindowListener(new WindowAdapter() {
        public void windowActivated(WindowEvent e) {
//          EditorConsole.systemOut.println("editor window activated");
          base.handleActivated(Editor.this);
//          mode.handleActivated(Editor.this);
          fileMenu.insert(base.getSketchbookMenu(), 2);
          fileMenu.insert(base.getRecentMenu(), 3);
//          fileMenu.insert(mode.getExamplesMenu(), 3);
          sketchMenu.insert(mode.getImportMenu(), 4);
          mode.insertToolbarRecentMenu();
        }

        // added for 1.0.5
        // http://dev.processing.org/bugs/show_bug.cgi?id=1260
        public void windowDeactivated(WindowEvent e) {
//          EditorConsole.systemErr.println("editor window deactivated");
//          mode.handleDeactivated(Editor.this);
          fileMenu.remove(base.getSketchbookMenu());
          fileMenu.remove(base.getRecentMenu());
//          fileMenu.remove(mode.getExamplesMenu());
          sketchMenu.remove(mode.getImportMenu());
          mode.removeToolbarRecentMenu();
        }
      });

    timer = new Timer();

    buildMenuBar();

    Container contentPain = getContentPane();
    contentPain.setLayout(new BorderLayout());
    JPanel pain = new JPanel();
    pain.setLayout(new BorderLayout());
    contentPain.add(pain, BorderLayout.CENTER);

    Box box = Box.createVerticalBox();
    Box upper = Box.createVerticalBox();

    initModeMenu();
    toolbar = createToolbar();
    upper.add(toolbar);

    header = new EditorHeader(this);
    upper.add(header);

    textarea = createTextArea();
    textarea.setRightClickPopup(new TextAreaPopup());
    textarea.setHorizontalOffset(JEditTextArea.leftHandGutter);

    // assemble console panel, consisting of status area and the console itself
    consolePanel = new JPanel();
    consolePanel.setLayout(new BorderLayout());

    status = new EditorStatus(this);
    consolePanel.add(status, BorderLayout.NORTH);

    console = new EditorConsole(this);
    // windows puts an ugly border on this guy
    console.setBorder(null);
    consolePanel.add(console, BorderLayout.CENTER);

    lineStatus = new EditorLineStatus(this);
    consolePanel.add(lineStatus, BorderLayout.SOUTH);

    upper.add(textarea);

    // alternate spot for status, but ugly
//    status = new EditorStatus(this);
//    upper.add(status);

    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, consolePanel);

    // disable this because it hides the message area, which is essential (issue #745)
    splitPane.setOneTouchExpandable(false);
    // repaint child panes while resizing
    splitPane.setContinuousLayout(true);
    // if window increases in size, give all of increase to
    // the textarea in the upper pane
    splitPane.setResizeWeight(1D);

    // to fix ugliness.. normally macosx java 1.3 puts an
    // ugly white border around this object, so turn it off.
    splitPane.setBorder(null);

    // the default size on windows is too small and kinda ugly
    int dividerSize = Preferences.getInteger("editor.divider.size");
    if (dividerSize != 0) {
      splitPane.setDividerSize(dividerSize);
    }

    box.add(splitPane);

    pain.add(box);

    // get shift down/up events so we can show the alt version of toolbar buttons
    textarea.addKeyListener(toolbar);

    // end an undo-chunk any time the caret moves unless it's when text is edited
    textarea.addCaretListener(new CaretListener() {
      String lastText = textarea.getText();
      public void caretUpdate(CaretEvent e) {
        String newText = textarea.getText();
        if (lastText.equals(newText) && isDirectEdit()) {
          endTextEditHistory();
        }
        lastText = newText;
      }
    });

    pain.setTransferHandler(new FileDropHandler());

    // Finish preparing Editor (formerly found in Base)
    pack();

    // Set the window bounds and the divider location before setting it visible
    state.apply(this);

    // Set the minimum size for the editor window
    setMinimumSize(new Dimension(Preferences.getInteger("editor.window.width.min"),
                                 Preferences.getInteger("editor.window.height.min")));

    // Bring back the general options for the editor
    applyPreferences();

    // Make textField get the focus whenever frame is activated.
    // http://download.oracle.com/javase/tutorial/uiswing/misc/focus.html
    // May not be necessary, but helps avoid random situations with
    // the editor not being able to request its own focus.
    addWindowFocusListener(new WindowAdapter() {
      public void windowGainedFocus(WindowEvent e) {
        textarea.requestFocusInWindow();
      }

//      public void windowLostFocus(WindowEvent e) {
//        System.out.println("lost focus, should we tell the text area?");
//      }
    });

    // Open the document that was passed in
    boolean loaded = handleOpenInternal(path);
    if (!loaded) {
      sketch = null;
    }
  }


  /**
   * Broken out to get modes working for GSOC, but this needs a longer-term
   * solution where the listeners are handled properly.
   */
  protected JEditTextArea createTextArea() {
    return new JEditTextArea(new PdeTextAreaDefaults(mode));
  }


  public EditorState getEditorState() {
    return state;
  }


  public void removeRecent() {
    base.removeRecent(this);
  }


  public void addRecent() {
    base.handleRecent(this);
  }


  /**
   * Handles files dragged & dropped from the desktop and into the editor
   * window. Dragging files into the editor window is the same as using
   * "Sketch &rarr; Add File" for each file.
   */
  class FileDropHandler extends TransferHandler {
    public boolean canImport(JComponent dest, DataFlavor[] flavors) {
      return true;
    }

    @SuppressWarnings("unchecked")
    public boolean importData(JComponent src, Transferable transferable) {
      int successful = 0;

      try {
        DataFlavor uriListFlavor =
          new DataFlavor("text/uri-list;class=java.lang.String");

        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          java.util.List list = (java.util.List)
            transferable.getTransferData(DataFlavor.javaFileListFlavor);
          for (int i = 0; i < list.size(); i++) {
            File file = (File) list.get(i);
            if (sketch.addFile(file)) {
              successful++;
            }
          }
        } else if (transferable.isDataFlavorSupported(uriListFlavor)) {
          // Some platforms (Mac OS X and Linux, when this began) preferred
          // this method of moving files.
          String data = (String)transferable.getTransferData(uriListFlavor);
          String[] pieces = PApplet.splitTokens(data, "\r\n");
          for (int i = 0; i < pieces.length; i++) {
            if (pieces[i].startsWith("#")) continue;

            String path = null;
            if (pieces[i].startsWith("file:///")) {
              path = pieces[i].substring(7);
            } else if (pieces[i].startsWith("file:/")) {
              path = pieces[i].substring(5);
            }
            if (sketch.addFile(new File(path))) {
              successful++;
            }
          }
        }
      } catch (Exception e) {
        Base.showWarning("Drag & Drop Problem",
                         "An error occurred while trying to add files to the sketch.", e);
        return false;
      }

      if (successful == 0) {
        statusError("No files were added to the sketch.");

      } else if (successful == 1) {
        statusNotice("One file added to the sketch.");

      } else {
        statusNotice(successful + " files added to the sketch.");
      }
      return true;
    }
  }


  public Base getBase() {
    return base;
  }


  public Mode getMode() {
    return mode;
  }


  protected void initModeMenu() {
    modeMenu = new JMenu();
    for (final Mode m : base.getModeList()) {
      if (mode == m) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(m.getTitle());
        // doesn't need a listener, since it doesn't do anything
        item.setSelected(true);
        modeMenu.add(item);
      } else {
        JMenuItem item = new JMenuItem(m.getTitle());
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            base.changeMode(m);
          }
        });
        modeMenu.add(item);
      }
    }

    modeMenu.addSeparator();
    JMenuItem addLib = new JMenuItem("Add Mode...");
    addLib.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        base.handleOpenModeManager();
      }
    });
    modeMenu.add(addLib);
  }


  public JMenu getModeMenu() {
    return modeMenu;
  }



//  public Settings getTheme() {
//    return mode.getTheme();
//  }


  abstract public EditorToolbar createToolbar();


  abstract public Formatter createFormatter();


//  protected void setPlacement(int[] location) {
//    setBounds(location[0], location[1], location[2], location[3]);
//    if (location[4] != 0) {
//      splitPane.setDividerLocation(location[4]);
//    }
//  }
//
//
//  protected int[] getPlacement() {
//    int[] location = new int[5];
//
//    // Get the dimensions of the Frame
//    Rectangle bounds = getBounds();
//    location[0] = bounds.x;
//    location[1] = bounds.y;
//    location[2] = bounds.width;
//    location[3] = bounds.height;
//
//    // Get the current placement of the divider
//    location[4] = splitPane.getDividerLocation();
//
//    return location;
//  }


  protected void setDividerLocation(int pos) {
    splitPane.setDividerLocation(pos);
  }


  protected int getDividerLocation() {
    return splitPane.getDividerLocation();
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Read and apply new values from the preferences, either because
   * the app is just starting up, or the user just finished messing
   * with things in the Preferences window.
   */
  protected void applyPreferences() {
//    // apply the setting for 'use external editor'
//    boolean external = Preferences.getBoolean("editor.external");
//    textarea.setEditable(!external);
//    saveMenuItem.setEnabled(!external);
//    saveAsMenuItem.setEnabled(!external);

    TextAreaPainter painter = textarea.getPainter();
//    if (external) {
//      // disable line highlight and turn off the caret when disabling
//      Color color = mode.getColor("editor.external.bgcolor");
//      painter.setBackground(color);
//      painter.setLineHighlightEnabled(false);
//      textarea.setCaretVisible(false);
//    } else {
    Color color = mode.getColor("editor.bgcolor");
    painter.setBackground(color);
    boolean highlight = Preferences.getBoolean("editor.linehighlight");
    painter.setLineHighlightEnabled(highlight);
    textarea.setCaretVisible(true);
//    }

    // apply changes to the font size for the editor
    painter.setFont(Preferences.getFont("editor.font"));

    // in case tab expansion stuff has changed
    // removing this, just checking prefs directly instead
//    listener.applyPreferences();

    // in case moved to a new location
    // For 0125, changing to async version (to be implemented later)
    //sketchbook.rebuildMenus();
    // For 0126, moved into Base, which will notify all editors.
    //base.rebuildMenusAsync();
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  protected void buildMenuBar() {
    JMenuBar menubar = new JMenuBar();
    menubar = new JMenuBar();
    fileMenu = buildFileMenu();
    menubar.add(fileMenu);
    menubar.add(buildEditMenu());
    menubar.add(buildSketchMenu());
//    rebuildToolList();
    rebuildToolMenu();
    menubar.add(getToolMenu());

    JMenu modeMenu = buildModeMenu();
    if (modeMenu != null) {
      menubar.add(modeMenu);
    }

//    // These are temporary entries while Android mode is being worked out.
//    // The mode will not be in the tools menu, and won't involve a cmd-key
//    if (!Base.RELEASE) {
//      try {
//        Class clazz = Class.forName("processing.app.tools.android.AndroidMode");
//        Object mode = clazz.newInstance();
//        Method m = clazz.getMethod("init", new Class[] { Editor.class, JMenuBar.class });
//        //String libraryPath = (String) m.invoke(null, new Object[] { });
//        m.invoke(mode, new Object[] { this, menubar });
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }

    menubar.add(buildHelpMenu());
    setJMenuBar(menubar);
  }


  abstract public JMenu buildFileMenu();


//  public JMenu buildFileMenu(Editor editor) {
//    return buildFileMenu(editor, null);
//  }
//
//
//  // most of these items are per-mode
//  protected JMenu buildFileMenu(Editor editor, JMenuItem[] exportItems) {


  protected JMenu buildFileMenu(JMenuItem[] exportItems) {
    JMenuItem item;
    JMenu fileMenu = new JMenu("File");

    item = Toolkit.newJMenuItem("New", 'N');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          base.handleNew();
        }
      });
    fileMenu.add(item);

    item = Toolkit.newJMenuItem("Open...", 'O');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          base.handleOpenPrompt();
        }
      });
    fileMenu.add(item);

    fileMenu.add(base.getSketchbookMenu());

//    fileMenu.add(mode.getExamplesMenu());
    item = Toolkit.newJMenuItemShift("Examples...", 'O');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mode.showExamplesFrame();
      }
    });
    fileMenu.add(item);

    item = Toolkit.newJMenuItem("Close", 'W');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        base.handleClose(Editor.this, false);
      }
    });
    fileMenu.add(item);

    item = Toolkit.newJMenuItem("Save", 'S');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleSave(false);
      }
    });
//    saveMenuItem = item;
    fileMenu.add(item);

    item = Toolkit.newJMenuItemShift("Save As...", 'S');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleSaveAs();
      }
    });
//    saveAsMenuItem = item;
    fileMenu.add(item);

    if (exportItems != null) {
      for (JMenuItem ei : exportItems) {
        fileMenu.add(ei);
      }
    }
    fileMenu.addSeparator();

    item = Toolkit.newJMenuItemShift("Page Setup", 'P');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handlePageSetup();
      }
    });
    fileMenu.add(item);

    item = Toolkit.newJMenuItem("Print", 'P');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handlePrint();
      }
    });
    fileMenu.add(item);

    // Mac OS X already has its own preferences and quit menu.
    // That's right! Think different, b*tches!
    if (!Base.isMacOS()) {
      fileMenu.addSeparator();

      item = Toolkit.newJMenuItem("Preferences", ',');
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          base.handlePrefs();
        }
      });
      fileMenu.add(item);

      fileMenu.addSeparator();

      item = Toolkit.newJMenuItem("Quit", 'Q');
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          base.handleQuit();
        }
      });
      fileMenu.add(item);
    }
    return fileMenu;
  }


//  public void setSaveItem(JMenuItem item) {
//    saveMenuItem = item;
//  }


//  public void setSaveAsItem(JMenuItem item) {
//    saveAsMenuItem = item;
//  }


  protected JMenu buildEditMenu() {
    JMenu menu = new JMenu("Edit");
    JMenuItem item;

    undoItem = Toolkit.newJMenuItem("Undo", 'Z');
    undoItem.addActionListener(undoAction = new UndoAction());
    menu.add(undoItem);

    // Gotta follow them interface guidelines
    // http://code.google.com/p/processing/issues/detail?id=363
    if (Base.isWindows()) {
      redoItem = Toolkit.newJMenuItem("Redo", 'Y');
    } else {  // Linux and OS X
      redoItem = Toolkit.newJMenuItemShift("Redo", 'Z');
    }
    redoItem.addActionListener(redoAction = new RedoAction());
    menu.add(redoItem);

    menu.addSeparator();

    // TODO "cut" and "copy" should really only be enabled
    // if some text is currently selected
    item = Toolkit.newJMenuItem("Cut", 'X');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleCut();
        }
      });
    menu.add(item);

    item = Toolkit.newJMenuItem("Copy", 'C');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          textarea.copy();
        }
      });
    menu.add(item);

    item = Toolkit.newJMenuItemShift("Copy as HTML", 'C');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleCopyAsHTML();
        }
      });
    menu.add(item);

    item = Toolkit.newJMenuItem("Paste", 'V');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          textarea.paste();
          sketch.setModified(true);
        }
      });
    menu.add(item);

    item = Toolkit.newJMenuItem("Select All", 'A');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          textarea.selectAll();
        }
      });
    menu.add(item);

    /*
    menu.addSeparator();

    item = Toolkit.newJMenuItem("Delete Selected Lines", 'D');
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleDeleteLines();
      }
    });
    menu.add(item);

    item = new JMenuItem("Move Selected Lines Up");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.ALT_MASK));
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleMoveLines(true);
        }
      });
    menu.add(item);

    item = new JMenuItem("Move Selected Lines Down");
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.ALT_MASK));
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleMoveLines(false);
        }
      });
    menu.add(item);
     */

    menu.addSeparator();

    item = Toolkit.newJMenuItem("Auto Format", 'T');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleAutoFormat();
        }
    });
    menu.add(item);

    item = Toolkit.newJMenuItem("Comment/Uncomment", '/');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleCommentUncomment();
        }
    });
    menu.add(item);

    item = Toolkit.newJMenuItem("Increase Indent", ']');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleIndentOutdent(true);
        }
    });
    menu.add(item);

    item = Toolkit.newJMenuItem("Decrease Indent", '[');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleIndentOutdent(false);
        }
    });
    menu.add(item);

    menu.addSeparator();

    item = Toolkit.newJMenuItem("Find...", 'F');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (find == null) {
            find = new FindReplace(Editor.this);
          }
          //new FindReplace(Editor.this).show();
          find.setVisible(true);
        }
      });
    menu.add(item);

    // TODO find next should only be enabled after a
    // search has actually taken place
    item = Toolkit.newJMenuItem("Find Next", 'G');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (find != null) {
            find.findNext();
          }
        }
      });
    menu.add(item);

    item = Toolkit.newJMenuItemShift("Find Previous", 'G');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (find != null) {
            find.findPrevious();
          }
        }
      });
    menu.add(item);

    // For Arduino and Mac, this should be command-E, but that currently conflicts with Export Applet
    item = Toolkit.newJMenuItemAlt("Use Selection for Find", 'F');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (find == null) {
            find = new FindReplace(Editor.this);
          }
          find.setFindText(getSelectedText());
        }
      });
    menu.add(item);

    return menu;
  }


  abstract public JMenu buildSketchMenu();


  protected JMenu buildSketchMenu(JMenuItem[] runItems) {
    JMenuItem item;
    sketchMenu = new JMenu("Sketch");

    for (JMenuItem mi : runItems) {
      sketchMenu.add(mi);
    }

    sketchMenu.addSeparator();

    sketchMenu.add(mode.getImportMenu());

    item = Toolkit.newJMenuItem("Show Sketch Folder", 'K');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Base.openFolder(sketch.getFolder());
        }
      });
    sketchMenu.add(item);
    item.setEnabled(Base.openFolderAvailable());

    item = new JMenuItem("Add File...");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sketch.handleAddFile();
        }
      });
    sketchMenu.add(item);

    return sketchMenu;
  }


  abstract public void handleImportLibrary(String jarPath);


  public JMenu getToolMenu() {
    if (toolsMenu == null) {
      rebuildToolMenu();
    }
    return toolsMenu;
  }


//  protected void rebuildToolList() {
//    coreTools = ToolContribution.list(Base.getToolsFolder(), true);
//    contribTools = ToolContribution.list(Base.getSketchbookToolsFolder(), true);
//  }


  public void rebuildToolMenu() {
    if (toolsMenu == null) {
      toolsMenu = new JMenu("Tools");
    } else {
      toolsMenu.removeAll();
    }

//    rebuildToolList();
    coreTools = ToolContribution.loadAll(Base.getToolsFolder());
    contribTools = ToolContribution.loadAll(Base.getSketchbookToolsFolder());

    addInternalTools(toolsMenu);
    addTools(toolsMenu, coreTools);
    addTools(toolsMenu, contribTools);

    toolsMenu.addSeparator();
    JMenuItem item = new JMenuItem("Add Tool...");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        base.handleOpenToolManager();
      }
    });
    toolsMenu.add(item);
  }


  protected void addTools(JMenu menu, ArrayList<ToolContribution> tools) {
    HashMap<String, JMenuItem> toolItems = new HashMap<String, JMenuItem>();

    for (final ToolContribution tool : tools) {
      String title = tool.getMenuTitle();
      JMenuItem item = new JMenuItem(title);
      item.addActionListener(new ActionListener() {
        boolean inited;

        public void actionPerformed(ActionEvent e) {
          if (!inited) {
            tool.init(Editor.this);
            inited = true;
          }
          EventQueue.invokeLater(tool);
        }
      });
      //menu.add(item);
      toolItems.put(title, item);
    }

    ArrayList<String> toolList = new ArrayList<String>(toolItems.keySet());
    if (toolList.size() > 0) {
      menu.addSeparator();
      Collections.sort(toolList);
      for (String title : toolList) {
        menu.add(toolItems.get(title));
      }
    }
  }


  /**
   * Override this if you want a special menu for your particular 'mode'.
   */
  public JMenu buildModeMenu() {
    return null;
  }


  protected void addToolMenuItem(JMenu menu, String className) {
    try {
      Class<?> toolClass = Class.forName(className);
      final Tool tool = (Tool) toolClass.newInstance();

      JMenuItem item = new JMenuItem(tool.getMenuTitle());

      tool.init(Editor.this);

      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SwingUtilities.invokeLater(tool);
        }
      });
      menu.add(item);
//      return item;

    } catch (Exception e) {
      e.printStackTrace();
//      return null;
    }
  }


  protected JMenu addInternalTools(JMenu menu) {
//    JMenuItem item;
//    item = createToolMenuItem("processing.app.tools.AutoFormatTool");
//    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
//    item.setAccelerator(KeyStroke.getKeyStroke('T', modifiers));
//    menu.add(item);

    addToolMenuItem(menu, "processing.app.tools.CreateFont");
    addToolMenuItem(menu, "processing.app.tools.ColorSelector");
    addToolMenuItem(menu, "processing.app.tools.Archiver");

    if (Base.isMacOS()) {
      if (SerialFixer.isNeeded()) {
        addToolMenuItem(menu, "processing.app.tools.SerialFixer");
      }
      addToolMenuItem(menu, "processing.app.tools.InstallCommander");
    }

    // I think this is no longer needed... It was Mac OS X specific,
    // and they gave up on MacRoman a long time ago.
//    addToolMenuItem(menu, "processing.app.tools.FixEncoding");

    // currently commented out
//    if (Base.DEBUG) {
//      addToolMenuItem(menu, "processing.app.tools.ExportExamples");
//    }

//    // These are temporary entries while Android mode is being worked out.
//    // The mode will not be in the tools menu, and won't involve a cmd-key
//    if (!Base.RELEASE) {
//      item = createToolMenuItem("processing.app.tools.android.AndroidTool");
//      item.setAccelerator(KeyStroke.getKeyStroke('D', modifiers));
//      menu.add(item);
//      menu.add(createToolMenuItem("processing.app.tools.android.Permissions"));
//      menu.add(createToolMenuItem("processing.app.tools.android.Reset"));
//    }

    return menu;
  }


  /*
  // testing internal web server to serve up docs from a zip file
  item = new JMenuItem("Web Server Test");
  item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //WebServer ws = new WebServer();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              int port = WebServer.launch("/Users/fry/coconut/processing/build/shared/reference.zip");
              Base.openURL("http://127.0.0.1:" + port + "/reference/setup_.html");

            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }
        });
      }
    });
  menu.add(item);
  */

  /*
  item = new JMenuItem("Browser Test");
  item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Base.openURL("http://processing.org/learning/gettingstarted/");
        //JFrame browserFrame = new JFrame("Browser");
        BrowserStartup bs = new BrowserStartup("jar:file:/Users/fry/coconut/processing/build/shared/reference.zip!/reference/setup_.html");
        bs.initUI();
        bs.launch();
      }
    });
  menu.add(item);
  */


  abstract public JMenu buildHelpMenu();


  public void showReference(String filename) {
    File file = new File(mode.getReferenceFolder(), filename);
    // Prepend with file:// and also encode spaces & other characters
    Base.openURL(file.toURI().toString());
  }


  static public void showChanges() {
    Base.openURL("http://wiki.processing.org/w/Changes");
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  class UndoAction extends AbstractAction {
    public UndoAction() {
      super("Undo");
      this.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      stopCompoundEdit();

      try {
        final Integer caret = caretUndoStack.pop();
        caretRedoStack.push(caret);
        textarea.setCaretPosition(caret);
        textarea.scrollToCaret();
      } catch (Exception ignore) {
      }
      try {
        undo.undo();
      } catch (CannotUndoException ex) {
        //System.out.println("Unable to undo: " + ex);
        //ex.printStackTrace();
      }
      updateUndoState();
      redoAction.updateRedoState();
      if (sketch != null) {
        sketch.setModified(!getText().equals(sketch.getCurrentCode().getSavedProgram()));
      }
    }

    protected void updateUndoState() {
      if (undo.canUndo() || compoundEdit != null && compoundEdit.isInProgress()) {
        this.setEnabled(true);
        undoItem.setEnabled(true);
        undoItem.setText(undo.getUndoPresentationName());
        putValue(Action.NAME, undo.getUndoPresentationName());
//        if (sketch != null) {
//          sketch.setModified(true);  // 0107, removed for 0196
//        }
      } else {
        this.setEnabled(false);
        undoItem.setEnabled(false);
        undoItem.setText("Undo");
        putValue(Action.NAME, "Undo");
//        if (sketch != null) {
//          sketch.setModified(false);  // 0107
//        }
      }
    }
  }


  class RedoAction extends AbstractAction {
    public RedoAction() {
      super("Redo");
      this.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      stopCompoundEdit();

      try {
        undo.redo();
      } catch (CannotRedoException ex) {
        //System.out.println("Unable to redo: " + ex);
        //ex.printStackTrace();
      }
      try {
        final Integer caret = caretRedoStack.pop();
        caretUndoStack.push(caret);
        textarea.setCaretPosition(caret);
      } catch (Exception ignore) {
      }
      updateRedoState();
      undoAction.updateUndoState();
      if (sketch != null) {
        sketch.setModified(!getText().equals(sketch.getCurrentCode().getSavedProgram()));
      }
    }

    protected void updateRedoState() {
      if (undo.canRedo()) {
        redoItem.setEnabled(true);
        redoItem.setText(undo.getRedoPresentationName());
        putValue(Action.NAME, undo.getRedoPresentationName());
      } else {
        this.setEnabled(false);
        redoItem.setEnabled(false);
        redoItem.setText("Redo");
        putValue(Action.NAME, "Redo");
      }
    }
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  // these will be done in a more generic way soon, more like:
  // setHandler("action name", Runnable);
  // but for the time being, working out the kinks of how many things to
  // abstract from the editor in this fashion.


//  public void setHandlers(Runnable runHandler, Runnable presentHandler,
//                          Runnable stopHandler,
//                          Runnable exportHandler, Runnable exportAppHandler) {
//    this.runHandler = runHandler;
//    this.presentHandler = presentHandler;
//    this.stopHandler = stopHandler;
//    this.exportHandler = exportHandler;
//    this.exportAppHandler = exportAppHandler;
//  }


//  public void resetHandlers() {
//    runHandler = new DefaultRunHandler();
//    presentHandler = new DefaultPresentHandler();
//    stopHandler = new DefaultStopHandler();
//    exportHandler = new DefaultExportHandler();
//    exportAppHandler = new DefaultExportAppHandler();
//  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Gets the current sketch object.
   */
  public Sketch getSketch() {
    return sketch;
  }


  /**
   * Get the JEditTextArea object for use (not recommended). This should only
   * be used in obscure cases that really need to hack the internals of the
   * JEditTextArea. Most tools should only interface via the get/set functions
   * found in this class. This will maintain compatibility with future releases,
   * which will not use JEditTextArea.
   */
  public JEditTextArea getTextArea() {
    return textarea;
  }


  /**
   * Get the contents of the current buffer. Used by the Sketch class.
   */
  public String getText() {
    return textarea.getText();
  }


  /**
   * Get a range of text from the current buffer.
   */
  public String getText(int start, int stop) {
    return textarea.getText(start, stop - start);
  }


  /**
   * Replace the entire contents of the front-most tab.
   */
  public void setText(String what) {
    startCompoundEdit();
    textarea.setText(what);
    stopCompoundEdit();
  }


  public void insertText(String what) {
    startCompoundEdit();
    int caret = getCaretOffset();
    setSelection(caret, caret);
    textarea.setSelectedText(what);
    stopCompoundEdit();
  }


  /**
   * Called to update the text but not switch to a different set of code
   * (which would affect the undo manager).
   */
//  public void setText2(String what, int start, int stop) {
//    beginCompoundEdit();
//    textarea.setText(what);
//    endCompoundEdit();
//
//    // make sure that a tool isn't asking for a bad location
//    start = Math.max(0, Math.min(start, textarea.getDocumentLength()));
//    stop = Math.max(0, Math.min(start, textarea.getDocumentLength()));
//    textarea.select(start, stop);
//
//    textarea.requestFocus();  // get the caret blinking
//  }


  public String getSelectedText() {
    return textarea.getSelectedText();
  }


  public void setSelectedText(String what) {
    textarea.setSelectedText(what);
  }


  public void setSelection(int start, int stop) {
    // make sure that a tool isn't asking for a bad location
    start = PApplet.constrain(start, 0, textarea.getDocumentLength());
    stop = PApplet.constrain(stop, 0, textarea.getDocumentLength());

    textarea.select(start, stop);
  }


  /**
   * Get the position (character offset) of the caret. With text selected,
   * this will be the last character actually selected, no matter the direction
   * of the selection. That is, if the user clicks and drags to select lines
   * 7 up to 4, then the caret position will be somewhere on line four.
   */
  public int getCaretOffset() {
    return textarea.getCaretPosition();
  }


  /**
   * True if some text is currently selected.
   */
  public boolean isSelectionActive() {
    return textarea.isSelectionActive();
  }


  /**
   * Get the beginning point of the current selection.
   */
  public int getSelectionStart() {
    return textarea.getSelectionStart();
  }


  /**
   * Get the end point of the current selection.
   */
  public int getSelectionStop() {
    return textarea.getSelectionStop();
  }


  /**
   * Get text for a specified line.
   */
  public String getLineText(int line) {
    return textarea.getLineText(line);
  }


  /**
   * Replace the text on a specified line.
   */
  public void setLineText(int line, String what) {
    startCompoundEdit();
    textarea.select(getLineStartOffset(line), getLineStopOffset(line));
    textarea.setSelectedText(what);
    stopCompoundEdit();
  }


  /**
   * Get character offset for the start of a given line of text.
   */
  public int getLineStartOffset(int line) {
    return textarea.getLineStartOffset(line);
  }


  /**
   * Get character offset for end of a given line of text.
   */
  public int getLineStopOffset(int line) {
    return textarea.getLineStopOffset(line);
  }


  /**
   * Get the number of lines in the currently displayed buffer.
   */
  public int getLineCount() {
    return textarea.getLineCount();
  }


  /**
   * Use before a manipulating text to group editing operations together as a
   * single undo. Use stopCompoundEdit() once finished.
   */
  public void startCompoundEdit() {
    stopCompoundEdit();
    compoundEdit = new CompoundEdit();
  }


  /**
   * Use with startCompoundEdit() to group edit operations in a single undo.
   */
  public void stopCompoundEdit() {
    if (compoundEdit != null) {
      compoundEdit.end();
      undo.addEdit(compoundEdit);
      undoAction.updateUndoState();
      redoAction.updateRedoState();
      compoundEdit = null;
    }
  }


  public int getScrollPosition() {
    return textarea.getScrollPosition();
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Switch between tabs, this swaps out the Document object
   * that's currently being manipulated.
   */
  protected void setCode(SketchCode code) {
    SyntaxDocument document = (SyntaxDocument) code.getDocument();

    if (document == null) {  // this document not yet inited
      document = new SyntaxDocument();
      code.setDocument(document);

      // turn on syntax highlighting
      document.setTokenMarker(mode.getTokenMarker());

      // insert the program text into the document object
      try {
        document.insertString(0, code.getProgram(), null);
      } catch (BadLocationException bl) {
        bl.printStackTrace();
      }

      // set up this guy's own undo manager
//      code.undo = new UndoManager();

      document.addDocumentListener(new DocumentListener() {

        public void removeUpdate(DocumentEvent e) {
          if (isInserting && isDirectEdit()) {
            endTextEditHistory();
          }
          isInserting = false;
        }

        public void insertUpdate(DocumentEvent e) {
          if (!isInserting && isDirectEdit()) {
            endTextEditHistory();
          }
          isInserting = true;
        }

        public void changedUpdate(DocumentEvent e) {
          endTextEditHistory();
        }
      });

      // connect the undo listener to the editor
      document.addUndoableEditListener(new UndoableEditListener() {

          public void undoableEditHappened(UndoableEditEvent e) {
            // if an edit is in progress, reset the timer
            if (endUndoEvent != null) {
              endUndoEvent.cancel();
              endUndoEvent = null;
              startTimerEvent();
            }

            // if this edit is just getting started, create a compound edit
            if (compoundEdit == null) {
              startCompoundEdit();
              startTimerEvent();
            }

            compoundEdit.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
          }
        });
    }

    // update the document object that's in use
    textarea.setDocument(document,
                         code.getSelectionStart(), code.getSelectionStop(),
                         code.getScrollPosition());

//    textarea.requestFocus();  // get the caret blinking
    textarea.requestFocusInWindow();  // required for caret blinking

    this.undo = code.getUndo();
    undoAction.updateUndoState();
    redoAction.updateRedoState();
  }

  /**
   * @return true if the text is being edited from direct input from typing and
   *         not shortcuts that manipulate text
   */
  boolean isDirectEdit() {
    return endUndoEvent != null;
  }

  void startTimerEvent() {
    endUndoEvent = new TimerTask() {
      public void run() {
        endTextEditHistory();
      }
    };
    timer.schedule(endUndoEvent, 3000);
    // let the gc eat the cancelled events
    timer.purge();
  }

  void endTextEditHistory() {
    if (endUndoEvent != null) {
      endUndoEvent.cancel();
      endUndoEvent = null;
    }
    stopCompoundEdit();
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Implements Edit &rarr; Cut.
   */
  public void handleCut() {
    textarea.cut();
    sketch.setModified(true);
  }


  /**
   * Implements Edit &rarr; Copy.
   */
  public void handleCopy() {
    textarea.copy();
  }


  public void handleCopyAsHTML() {
    textarea.copyAsHTML();
    statusNotice("Code formatted as HTML has been copied to the clipboard.");
  }


  /**
   * Implements Edit &rarr; Paste.
   */
  public void handlePaste() {
    textarea.paste();
    sketch.setModified(true);
  }


  /**
   * Implements Edit &rarr; Select All.
   */
  public void handleSelectAll() {
    textarea.selectAll();
  }

//  /**
//   * @param moveUp
//   *          true to swap the selected lines with the line above, false to swap
//   *          with the line beneath
//   */
  /*
  public void handleMoveLines(boolean moveUp) {
    startCompoundEdit();

    int startLine = textarea.getSelectionStartLine();
    int stopLine = textarea.getSelectionStopLine();

    // if more than one line is selected and none of the characters of the end
    // line are selected, don't move that line
    if (startLine != stopLine
        && textarea.getSelectionStop() == textarea.getLineStartOffset(stopLine))
      stopLine--;

    int replacedLine = moveUp ? startLine - 1 : stopLine + 1;
    if (replacedLine < 0 || replacedLine >= textarea.getLineCount())
      return;

    final String source = getText();

    int replaceStart = textarea.getLineStartOffset(replacedLine);
    int replaceEnd = textarea.getLineStopOffset(replacedLine);
    if (replaceEnd == source.length() + 1)
      replaceEnd--;

    int selectionStart = textarea.getLineStartOffset(startLine);
    int selectionEnd = textarea.getLineStopOffset(stopLine);
    if (selectionEnd == source.length() + 1)
      selectionEnd--;

    String replacedText = source.substring(replaceStart, replaceEnd);
    String selectedText = source.substring(selectionStart, selectionEnd);
    if (replacedLine == textarea.getLineCount() - 1) {
      replacedText += "\n";
      selectedText = selectedText.substring(0, selectedText.length() - 1);
    } else if (stopLine == textarea.getLineCount() - 1) {
      selectedText += "\n";
      replacedText = replacedText.substring(0, replacedText.length() - 1);
    }

    int newSelectionStart, newSelectionEnd;
    if (moveUp) {
      // Change the selection, then change the line above
      textarea.select(selectionStart, selectionEnd);
      textarea.setSelectedText(replacedText);

      textarea.select(replaceStart, replaceEnd);
      textarea.setSelectedText(selectedText);

      newSelectionStart = textarea.getLineStartOffset(startLine - 1);
      newSelectionEnd = textarea.getLineStopOffset(stopLine - 1) -  1;
    } else {
      // Change the line beneath, then change the selection
      textarea.select(replaceStart, replaceEnd);
      textarea.setSelectedText(selectedText);

      textarea.select(selectionStart, selectionEnd);
      textarea.setSelectedText(replacedText);

      newSelectionStart = textarea.getLineStartOffset(startLine + 1);
      newSelectionEnd = textarea.getLineStopOffset(stopLine + 1) - 1;
    }

    textarea.select(newSelectionStart, newSelectionEnd);
    stopCompoundEdit();
  }
  */


  /*
  public void handleDeleteLines() {
    int startLine = textarea.getSelectionStartLine();
    int stopLine = textarea.getSelectionStopLine();

    int start = textarea.getLineStartOffset(startLine);
    int end = textarea.getLineStopOffset(stopLine);
    if (end == getText().length() + 1)
      end--;

    textarea.select(start, end);
    textarea.setSelectedText("");
  }
  */


  public void handleAutoFormat() {
    final String source = getText();

    try {
      final String formattedText = createFormatter().format(source);
      // save current (rough) selection point
      int selectionEnd = getSelectionStop();

      // make sure the caret would be past the end of the text
      if (formattedText.length() < selectionEnd - 1) {
        selectionEnd = formattedText.length() - 1;
      }

      if (formattedText.equals(source)) {
        statusNotice("No changes necessary for Auto Format.");
      } else {
        // replace with new bootiful text
        // selectionEnd hopefully at least in the neighborhood
        setText(formattedText);
        setSelection(selectionEnd, selectionEnd);
        getSketch().setModified(true);
        // mark as finished
        statusNotice("Auto Format finished.");
      }

    } catch (final Exception e) {
      statusError(e);
    }
  }


  abstract public String getCommentPrefix();


  protected void handleCommentUncomment() {
    startCompoundEdit();

    String prefix = getCommentPrefix();
    int prefixLen = prefix.length();

    int startLine = textarea.getSelectionStartLine();
    int stopLine = textarea.getSelectionStopLine();

    int lastLineStart = textarea.getLineStartOffset(stopLine);
    int selectionStop = textarea.getSelectionStop();
    // If the selection ends at the beginning of the last line,
    // then don't (un)comment that line.
    if (selectionStop == lastLineStart) {
      // Though if there's no selection, don't do that
      if (textarea.isSelectionActive()) {
        stopLine--;
      }
    }

    // If the text is empty, ignore the user.
    // Also ensure that all lines are commented (not just the first)
    // when determining whether to comment or uncomment.
    int length = textarea.getDocumentLength();
    boolean commented = true;
    for (int i = startLine; commented && (i <= stopLine); i++) {
      int pos = textarea.getLineStartOffset(i);
      if (pos + prefixLen > length) {
        commented = false;
      } else {
        // Check the first characters to see if it's already a comment.
        String begin = textarea.getText(pos, prefixLen);
        //System.out.println("begin is '" + begin + "'");
        commented = begin.equals(prefix);
      }
    }

    for (int line = startLine; line <= stopLine; line++) {
      int location = textarea.getLineStartOffset(line);
      if (commented) {
        // remove a comment
        textarea.select(location, location + prefixLen);
        if (textarea.getSelectedText().equals(prefix)) {
          textarea.setSelectedText("");
        }
      } else {
        // add a comment
        textarea.select(location, location);
        textarea.setSelectedText(prefix);
      }
    }
    // Subtract one from the end, otherwise selects past the current line.
    // (Which causes subsequent calls to keep expanding the selection)
    textarea.select(textarea.getLineStartOffset(startLine),
                    textarea.getLineStopOffset(stopLine) - 1);
    stopCompoundEdit();
    sketch.setModified(true);
  }


  public void handleIndent() {
    handleIndentOutdent(true);
  }


  public void handleOutdent() {
    handleIndentOutdent(false);
  }


  public void handleIndentOutdent(boolean indent) {
    int tabSize = Preferences.getInteger("editor.tabs.size");
    String tabString = Editor.EMPTY.substring(0, tabSize);

    startCompoundEdit();

    int startLine = textarea.getSelectionStartLine();
    int stopLine = textarea.getSelectionStopLine();

    // If the selection ends at the beginning of the last line,
    // then don't (un)comment that line.
    int lastLineStart = textarea.getLineStartOffset(stopLine);
    int selectionStop = textarea.getSelectionStop();
    if (selectionStop == lastLineStart) {
      // Though if there's no selection, don't do that
      if (textarea.isSelectionActive()) {
        stopLine--;
      }
    }

    for (int line = startLine; line <= stopLine; line++) {
      int location = textarea.getLineStartOffset(line);

      if (indent) {
        textarea.select(location, location);
        textarea.setSelectedText(tabString);

      } else {  // outdent
        textarea.select(location, location + tabSize);
        // Don't eat code if it's not indented
        if (textarea.getSelectedText().equals(tabString)) {
          textarea.setSelectedText("");
        }
      }
    }
    // Subtract one from the end, otherwise selects past the current line.
    // (Which causes subsequent calls to keep expanding the selection)
    textarea.select(textarea.getLineStartOffset(startLine),
                    textarea.getLineStopOffset(stopLine) - 1);
    stopCompoundEdit();
    sketch.setModified(true);
  }


  static public boolean checkParen(char[] array, int index, int stop) {
//  boolean paren = false;
//  int stepper = i + 1;
//  while (stepper < mlength) {
//    if (array[stepper] == '(') {
//      paren = true;
//      break;
//    }
//    stepper++;
//  }
    while (index < stop) {
//    if (array[index] == '(') {
//      return true;
//    } else if (!Character.isWhitespace(array[index])) {
//      return false;
//    }
      switch (array[index]) {
      case '(':
        return true;

      case ' ':
      case '\t':
      case '\n':
      case '\r':
        index++;
        break;

      default:
//      System.out.println("defaulting because " + array[index] + " " + PApplet.hex(array[index]));
        return false;
      }
    }
//  System.out.println("exiting " + new String(array, index, stop - index));
    return false;
  }


  protected boolean functionable(char c) {
    return (c == '_') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }


  /**
   * Check the current selection for reference. If no selection is active,
   * expand the current selection.
   * @return
   */
  protected String referenceCheck(boolean selectIfFound) {
    int start = textarea.getSelectionStart();
    int stop = textarea.getSelectionStop();
    if (stop < start) {
      int temp = stop;
      stop = start;
      start = temp;
    }
    char[] c = textarea.getText().toCharArray();

//    System.out.println("checking reference");
    if (start == stop) {
      while (start > 0 && functionable(c[start - 1])) {
        start--;
      }
      while (stop < c.length && functionable(c[stop])) {
        stop++;
      }
//      System.out.println("start is stop");
    }
    String text = new String(c, start, stop - start).trim();
//    System.out.println("  reference piece is '" + text + "'");
    if (checkParen(c, stop, c.length)) {
      text += "_";
    }
    String ref = mode.lookupReference(text);
    if (selectIfFound) {
      textarea.select(start, stop);
    }
    return ref;
  }


  protected void handleFindReference() {
    String ref = referenceCheck(true);
    if (ref != null) {
      showReference(ref + ".html");
    } else {
      String text = textarea.getSelectedText().trim();
      if (text.length() == 0) {
        statusNotice("First select a word to find in the reference.");
      } else {
        statusNotice("No reference available for \"" + text + "\"");
      }
    }
  }


  /*
  protected void handleFindReference() {
    String text = textarea.getSelectedText().trim();

    if (text.length() == 0) {
      statusNotice("First select a word to find in the reference.");

    } else {
      char[] c = textarea.getText().toCharArray();
      int after = Math.max(textarea.getSelectionStart(), textarea.getSelectionStop());
      if (checkParen(c, after, c.length)) {
        text += "_";
        System.out.println("looking up ref for " + text);
      }
      String referenceFile = mode.lookupReference(text);
      System.out.println("reference file is " + referenceFile);
      if (referenceFile == null) {
        statusNotice("No reference available for \"" + text + "\"");
      } else {
        showReference(referenceFile + ".html");
      }
    }
  }


  protected void handleFindReference() {
    String text = textarea.getSelectedText().trim();

    if (text.length() == 0) {
      statusNotice("First select a word to find in the reference.");

    } else {
      String referenceFile = mode.lookupReference(text);
      //System.out.println("reference file is " + referenceFile);
      if (referenceFile == null) {
        statusNotice("No reference available for \"" + text + "\"");
      } else {
        showReference(referenceFile + ".html");
      }
    }
  }
  */


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Set the location of the sketch run window. Used by Runner to update the
   * Editor about window drag events while the sketch is running.
   */
  public void setSketchLocation(Point p) {
    sketchWindowLocation = p;
  }


  /**
   * Get the last location of the sketch's run window. Used by Runner to make
   * the window show up in the same location as when it was last closed.
   */
  public Point getSketchLocation() {
    return sketchWindowLocation;
  }


//  public void internalCloseRunner() {
//    mode.internalCloseRunner(this);
//  }


  /**
   * Check if the sketch is modified and ask user to save changes.
   * @return false if canceling the close/quit operation
   */
  protected boolean checkModified() {
    if (!sketch.isModified()) return true;

    // As of Processing 1.0.10, this always happens immediately.
    // http://dev.processing.org/bugs/show_bug.cgi?id=1456

    String prompt = "Save changes to " + sketch.getName() + "?  ";

    if (!Base.isMacOS()) {
      int result =
        JOptionPane.showConfirmDialog(this, prompt, "Close",
                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);

      if (result == JOptionPane.YES_OPTION) {
        return handleSave(true);

      } else if (result == JOptionPane.NO_OPTION) {
        return true;  // ok to continue

      } else if (result == JOptionPane.CANCEL_OPTION ||
                 result == JOptionPane.CLOSED_OPTION) {
        return false;

      } else {
        throw new IllegalStateException();
      }

    } else {
      // This code is disabled unless Java 1.5 is being used on Mac OS X
      // because of a Java bug that prevents the initial value of the
      // dialog from being set properly (at least on my MacBook Pro).
      // The bug causes the "Don't Save" option to be the highlighted,
      // blinking, default. This sucks. But I'll tell you what doesn't
      // suck--workarounds for the Mac and Apple's snobby attitude about it!
      // I think it's nifty that they treat their developers like dirt.

      // Pane formatting adapted from the quaqua guide
      // http://www.randelshofer.ch/quaqua/guide/joptionpane.html
      JOptionPane pane =
        new JOptionPane("<html> " +
                        "<head> <style type=\"text/css\">"+
                        "b { font: 13pt \"Lucida Grande\" }"+
                        "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"+
                        "</style> </head>" +
                        "<b>Do you want to save changes to this sketch<BR>" +
                        " before closing?</b>" +
                        "<p>If you don't save, your changes will be lost.",
                        JOptionPane.QUESTION_MESSAGE);

      String[] options = new String[] {
        "Save", "Cancel", "Don't Save"
      };
      pane.setOptions(options);

      // highlight the safest option ala apple hig
      pane.setInitialValue(options[0]);

      // on macosx, setting the destructive property places this option
      // away from the others at the lefthand side
      pane.putClientProperty("Quaqua.OptionPane.destructiveOption",
                             new Integer(2));

      JDialog dialog = pane.createDialog(this, null);
      dialog.setVisible(true);

      Object result = pane.getValue();
      if (result == options[0]) {  // save (and close/quit)
        return handleSave(true);

      } else if (result == options[2]) {  // don't save (still close/quit)
        return true;

      } else {  // cancel?
        return false;
      }
    }
  }


  /**
   * Open a sketch from a particular path, but don't check to save changes.
   * Used by Sketch.saveAs() to re-open a sketch after the "Save As"
   */
//  protected void handleOpenUnchecked(String path, int codeIndex,
//                                     int selStart, int selStop, int scrollPos) {
//    internalCloseRunner();
//    handleOpenInternal(path);
//    // Replacing a document that may be untitled. If this is an actual
//    // untitled document, then editor.untitled will be set by Base.
//    untitled = false;
//
//    sketch.setCurrentCode(codeIndex);
//    textarea.select(selStart, selStop);
//    textarea.setScrollPosition(scrollPos);
//  }


  /**
   * Second stage of open, occurs after having checked to see if the
   * modifications (if any) to the previous sketch need to be saved.
   */
  protected boolean handleOpenInternal(String path) {
    // check to make sure that this .pde file is
    // in a folder of the same name
    File file = new File(path);
    File parentFile = new File(file.getParent());
    String parentName = parentFile.getName();
    String pdeName = parentName + ".pde";
    File altFile = new File(file.getParent(), pdeName);

    if (pdeName.equals(file.getName())) {
      // no beef with this guy

    } else if (altFile.exists()) {
      // user selected a .java from the same sketch,
      // but open the .pde instead
      path = altFile.getAbsolutePath();
      //System.out.println("found alt file in same folder");

    } else if (!path.endsWith(".pde")) {
      Base.showWarning("Bad file selected",
                       "Processing can only open its own sketches\n" +
                       "and other files ending in .pde", null);
      return false;

    } else {
      String properParent =
        file.getName().substring(0, file.getName().length() - 4);

      Object[] options = { "OK", "Cancel" };
      String prompt =
        "The file \"" + file.getName() + "\" needs to be inside\n" +
        "a sketch folder named \"" + properParent + "\".\n" +
        "Create this folder, move the file, and continue?";

      int result = JOptionPane.showOptionDialog(this,
                                                prompt,
                                                "Moving",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                options,
                                                options[0]);

      if (result == JOptionPane.YES_OPTION) {
        // create properly named folder
        File properFolder = new File(file.getParent(), properParent);
        if (properFolder.exists()) {
          Base.showWarning("Error",
                           "A folder named \"" + properParent + "\" " +
                           "already exists. Can't open sketch.", null);
          return false;
        }
        if (!properFolder.mkdirs()) {
          //throw new IOException("Couldn't create sketch folder");
          Base.showWarning("Error",
                           "Could not create the sketch folder.", null);
          return false;
        }
        // copy the sketch inside
        File properPdeFile = new File(properFolder, file.getName());
        File origPdeFile = new File(path);
        try {
          Base.copyFile(origPdeFile, properPdeFile);
        } catch (IOException e) {
          Base.showWarning("Error", "Could not copy to a proper location.", e);
          return false;
        }

        // remove the original file, so user doesn't get confused
        origPdeFile.delete();

        // update with the new path
        path = properPdeFile.getAbsolutePath();

      } else if (result == JOptionPane.NO_OPTION) {
        return false;
      }
    }

    try {
      sketch = new Sketch(path, this);
    } catch (IOException e) {
      Base.showWarning("Error", "Could not create the sketch.", e);
      return false;
    }
    header.rebuild();
    updateTitle();
    // Disable untitled setting from previous document, if any
//    untitled = false;

    // Store information on who's open and running
    // (in case there's a crash or something that can't be recovered)
//    base.storeSketches();
    Preferences.save();

    // opening was successful
    return true;

//    } catch (Exception e) {
//      e.printStackTrace();
//      statusError(e);
//      return false;
//    }
  }


  /**
   * Set the title of the PDE window based on the current sketch, i.e.
   * something like "sketch_070752a - Processing 0126"
   */
  public void updateTitle() {
    setTitle(sketch.getName() + " | Processing " + Base.VERSION_NAME);

    if (!sketch.isUntitled()) {
      // set current file for OS X so that cmd-click in title bar works
      File sketchFile = sketch.getMainFile();
      getRootPane().putClientProperty("Window.documentFile", sketchFile);
    } else {
      // per other applications, don't set this until the file has been saved
      getRootPane().putClientProperty("Window.documentFile", null);
    }
  }


  /**
   * Actually handle the save command. If 'immediately' is set to false,
   * this will happen in another thread so that the message area
   * will update and the save button will stay highlighted while the
   * save is happening. If 'immediately' is true, then it will happen
   * immediately. This is used during a quit, because invokeLater()
   * won't run properly while a quit is happening. This fixes
   * <A HREF="http://dev.processing.org/bugs/show_bug.cgi?id=276">Bug 276</A>.
   */
  public boolean handleSave(boolean immediately) {
//    handleStop();  // 0136

    if (sketch.isUntitled()) {
      return handleSaveAs();
      // need to get the name, user might also cancel here

    } else if (immediately) {
      handleSaveImpl();

    } else {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            handleSaveImpl();
          }
        });
    }
    return true;
  }


  protected void handleSaveImpl() {
    statusNotice("Saving...");
    try {
      if (sketch.save()) {
        statusNotice("Done Saving.");
      } else {
        statusEmpty();
      }

    } catch (Exception e) {
      // show the error as a message in the window
      statusError(e);

      // zero out the current action,
      // so that checkModified2 will just do nothing
      //checkModifiedMode = 0;
      // this is used when another operation calls a save
    }
  }


  public boolean handleSaveAs() {
    statusNotice("Saving...");
    try {
      if (sketch.saveAs()) {
        statusNotice("Done Saving.");
        // Disabling this for 0125, instead rebuild the menu inside
        // the Save As method of the Sketch object, since that's the
        // only one who knows whether something was renamed.
        //sketchbook.rebuildMenusAsync();
      } else {
        statusNotice("Save Canceled.");
        return false;
      }
    } catch (Exception e) {
      // show the error as a message in the window
      statusError(e);
    }
    return true;
  }


  /**
   * Handler for File &rarr; Page Setup.
   */
  public void handlePageSetup() {
    //printerJob = null;
    if (printerJob == null) {
      printerJob = PrinterJob.getPrinterJob();
    }
    if (pageFormat == null) {
      pageFormat = printerJob.defaultPage();
    }
    pageFormat = printerJob.pageDialog(pageFormat);
    //System.out.println("page format is " + pageFormat);
  }


  /**
   * Handler for File &rarr; Print.
   */
  public void handlePrint() {
    statusNotice("Printing...");
    //printerJob = null;
    if (printerJob == null) {
      printerJob = PrinterJob.getPrinterJob();
    }
    if (pageFormat != null) {
      //System.out.println("setting page format " + pageFormat);
      printerJob.setPrintable(textarea.getPainter(), pageFormat);
    } else {
      printerJob.setPrintable(textarea.getPainter());
    }
    // set the name of the job to the code name
    printerJob.setJobName(sketch.getCurrentCode().getPrettyName());

    if (printerJob.printDialog()) {
      try {
        printerJob.print();
        statusNotice("Done printing.");

      } catch (PrinterException pe) {
        statusError("Error while printing.");
        pe.printStackTrace();
      }
    } else {
      statusNotice("Printing canceled.");
    }
    //printerJob = null;  // clear this out?
  }


  /**
   * Grab current contents of the sketch window, advance the console,
   * stop any other running sketches... not in that order.
   */
  public void prepareRun() {
    internalCloseRunner();
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++) System.out.println();

    // clear the console on each run, unless the user doesn't want to
    if (Preferences.getBoolean("console.auto_clear")) {
      console.clear();
    }

    // make sure the user didn't hide the sketch folder
    sketch.ensureExistence();

    // make sure any edits have been stored
    //current.setProgram(editor.getText());
    sketch.getCurrentCode().setProgram(getText());

//    // if an external editor is being used, need to grab the
//    // latest version of the code from the file.
//    if (Preferences.getBoolean("editor.external")) {
//      sketch.reload();
//    }
  }


  /**
   * Halt the current runner for whatever reason. Might be the VM dying,
   * the window closing, an error...
   */
  abstract public void internalCloseRunner();


  abstract public void deactivateRun();


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Show an error in the status bar.
   */
  public void statusError(String what) {
    status.error(what);
    //new Exception("deactivating RUN").printStackTrace();
//    toolbar.deactivate(EditorToolbar.RUN);
  }


  /**
   * Show an exception in the editor status bar.
   */
  public void statusError(Exception e) {
    e.printStackTrace();
//    if (e == null) {
//      System.err.println("Editor.statusError() was passed a null exception.");
//      return;
//    }

    if (e instanceof SketchException) {
      SketchException re = (SketchException) e;
      if (re.hasCodeIndex()) {
        sketch.setCurrentCode(re.getCodeIndex());
      }
      if (re.hasCodeLine()) {
        int line = re.getCodeLine();
        // subtract one from the end so that the \n ain't included
        if (line >= textarea.getLineCount()) {
          // The error is at the end of this current chunk of code,
          // so the last line needs to be selected.
          line = textarea.getLineCount() - 1;
          if (textarea.getLineText(line).length() == 0) {
            // The last line may be zero length, meaning nothing to select.
            // If so, back up one more line.
            line--;
          }
        }
        if (line < 0 || line >= textarea.getLineCount()) {
          System.err.println("Bad error line: " + line);
        } else {
          textarea.select(textarea.getLineStartOffset(line),
                          textarea.getLineStopOffset(line) - 1);
        }
      }
    }

    // Since this will catch all Exception types, spend some time figuring
    // out which kind and try to give a better error message to the user.
    String mess = e.getMessage();
    if (mess != null) {
      String javaLang = "java.lang.";
      if (mess.indexOf(javaLang) == 0) {
        mess = mess.substring(javaLang.length());
      }
      String rxString = "RuntimeException: ";
      if (mess.startsWith(rxString)) {
        mess = mess.substring(rxString.length());
      }
      statusError(mess);
    }
//    e.printStackTrace();
  }


  /**
   * Show a notice message in the editor status bar.
   */
  public void statusNotice(String msg) {
    status.notice(msg);
  }


  public void clearNotice(String msg) {
    if (status.message.equals(msg)) {
      statusEmpty();
    }
  }


  /**
   * Clear the status area.
   */
  public void statusEmpty() {
    statusNotice(EMPTY);
  }


  public void startIndeterminate() {
    status.startIndeterminate();
  }


  public void stopIndeterminate() {
    status.stopIndeterminate();
  }


  public void statusHalt() {
    // stop called by someone else
  }


  public boolean isHalted() {
    return false;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Returns the edit popup menu.
   */
  class TextAreaPopup extends JPopupMenu {
    JMenuItem cutItem;
    JMenuItem copyItem;
    JMenuItem discourseItem;
    JMenuItem referenceItem;


    public TextAreaPopup() {
      JMenuItem item;

      cutItem = new JMenuItem("Cut");
      cutItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleCut();
          }
      });
      this.add(cutItem);

      copyItem = new JMenuItem("Copy");
      copyItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleCopy();
          }
        });
      this.add(copyItem);

      discourseItem = new JMenuItem("Copy as HTML");
      discourseItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleCopyAsHTML();
          }
        });
      this.add(discourseItem);

      item = new JMenuItem("Paste");
      item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handlePaste();
          }
        });
      this.add(item);

      item = new JMenuItem("Select All");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          handleSelectAll();
        }
      });
      this.add(item);

      this.addSeparator();

      item = new JMenuItem("Comment/Uncomment");
      item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleCommentUncomment();
          }
      });
      this.add(item);

      item = new JMenuItem("Increase Indent");
      item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleIndentOutdent(true);
          }
      });
      this.add(item);

      item = new JMenuItem("Decrease Indent");
      item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleIndentOutdent(false);
          }
      });
      this.add(item);

      this.addSeparator();

      referenceItem = new JMenuItem("Find in Reference");
      referenceItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            handleFindReference();
          }
        });
      this.add(referenceItem);
    }

    // if no text is selected, disable copy and cut menu items
    public void show(Component component, int x, int y) {
//      if (textarea.isSelectionActive()) {
//        cutItem.setEnabled(true);
//        copyItem.setEnabled(true);
//        discourseItem.setEnabled(true);
//
////        String sel = textarea.getSelectedText().trim();
////        String referenceFile = mode.lookupReference(sel);
////        referenceItem.setEnabled(referenceFile != null);
//
//      } else {
//        cutItem.setEnabled(false);
//        copyItem.setEnabled(false);
//        discourseItem.setEnabled(false);
////        referenceItem.setEnabled(false);
//      }
      boolean active = textarea.isSelectionActive();
      cutItem.setEnabled(active);
      copyItem.setEnabled(active);
      discourseItem.setEnabled(active);

      referenceItem.setEnabled(referenceCheck(false) != null);
      super.show(component, x, y);
    }
  }
}