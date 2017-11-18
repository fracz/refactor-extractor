package org.jetbrains.idea.maven.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListScrollingUtil;
import com.intellij.util.Alarm;
import org.jetbrains.idea.maven.core.util.MavenId;
import org.jetbrains.idea.maven.repo.RepositoryIndex;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class RepositorySearchDialog extends DialogWrapper {
  private RepositoryIndex myIndex = new RepositoryIndex("C:\\temp\\maven-central-index", null);

  private MavenId myResult;

  private JPanel myMainPanel;
  private JTextField mySearchField;
  private JList myResultsList;

  private Alarm myAlarm = new Alarm();

  public RepositorySearchDialog(Project project) {
    super(project, true);

    configComponents();

    setTitle("Maven Repository Search");
    setOKActionEnabled(false);
    init();
  }

  private void configComponents() {
    mySearchField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        scheduleSearch();
      }
    });

    myResultsList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (myAlarm.getActiveRequestCount() > 0) return;

        boolean hasSelection = !myResultsList.isSelectionEmpty();
        setOKActionEnabled(hasSelection);
      }
    });

    mySearchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
          case KeyEvent.VK_DOWN:
            ListScrollingUtil.moveDown(myResultsList, e.getModifiersEx());
            break;
          case KeyEvent.VK_UP:
            ListScrollingUtil.moveUp(myResultsList, e.getModifiersEx());
            break;
          case KeyEvent.VK_PAGE_UP:
            ListScrollingUtil.movePageUp(myResultsList);
            break;
          case KeyEvent.VK_PAGE_DOWN:
            ListScrollingUtil.movePageDown(myResultsList);
            break;
        }
      }
    });

    myResultsList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (!mySearchField.hasFocus()) {
          mySearchField.requestFocus();
        }
      }
    });
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public MavenId getResult() {
    return myResult;
  }

  private void scheduleSearch() {
    setOKActionEnabled(false);

    myAlarm.cancelAllRequests();
    myAlarm.addRequest(new Runnable() {
      public void run() {
        doSearch();
      }
    }, 500);
  }

  private void doSearch() {
    try {
      final List<MavenId> result = myIndex.search(mySearchField.getText());
      final AbstractListModel model = new AbstractListModel() {
        public int getSize() {
          return result.size();
        }

        public Object getElementAt(int index) {
          return result.get(index);
        }
      };

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          myResultsList.setModel(model);
        }
      });
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doOKAction() {
    myResult = (MavenId)myResultsList.getSelectedValue();
    super.doOKAction();
  }
}