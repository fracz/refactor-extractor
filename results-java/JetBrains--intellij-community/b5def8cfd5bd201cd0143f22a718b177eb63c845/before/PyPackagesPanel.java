package com.jetbrains.python.packaging.ui;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.packaging.*;
import com.jetbrains.python.sdk.flavors.IronPythonSdkFlavor;
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor;
import com.jetbrains.python.sdk.PythonSdkType;
import org.apache.xmlrpc.AsyncCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class PyPackagesPanel extends JPanel {
  public static final String INSTALL_DISTRIBUTE = "installDistribute";
  public static final String INSTALL_PIP = "installPip";
  public static final String CREATE_VENV = "createVEnv";

  private final JButton myInstallButton;
  private final JButton myUninstallButton;
  private final JButton myUpgradeButton;

  private final JBTable myPackagesTable;
  private DefaultTableModel myPackagesTableModel;
  private Sdk mySelectedSdk;
  private final Project myProject;
  private final PyPackagesNotificationPanel myNotificationArea;
  private boolean myHasDistribute;
  private boolean myHasPip = true;
  private final List<Consumer<Sdk>> myPathChangedListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private Set<String> currentlyInstalling = new HashSet<String>();

  public PyPackagesPanel(Project project, PyPackagesNotificationPanel area) {
    super(new GridBagLayout());
    myProject = project;
    myNotificationArea = area;
    myInstallButton = new JButton("Install");
    myUninstallButton = new JButton("Uninstall");
    myUpgradeButton = new JButton("Upgrade");
    myInstallButton.setMnemonic('I');
    myUninstallButton.setMnemonic('U');
    myUpgradeButton.setMnemonic('p');

    myInstallButton.setEnabled(false);
    myUninstallButton.setEnabled(false);
    myUpgradeButton.setEnabled(false);

    myPackagesTableModel = new DefaultTableModel(new String[]{"Package", "Version", "Latest"}, 0) {
      @Override
      public boolean isCellEditable(int i, int i1) {
        return false;
      }
    };
    final TableCellRenderer tableCellRenderer = new MyTableCellRenderer();
    myPackagesTable = new JBTable(myPackagesTableModel) {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column) {
        return tableCellRenderer;
      }
    };

    Insets anInsets = new Insets(2, 2, 2, 2);
    JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myPackagesTable,
                                                                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(500, 500));
    add(scrollPane, new GridBagConstraints(0, 0, 1, 8, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                           anInsets, 0, 0));

    addUninstallAction();
    addUpgradeAction();

    add(myInstallButton,
        new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                               anInsets, 0, 0));
    add(myUninstallButton,
        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                               anInsets, 0, 0));
    add(myUpgradeButton,
        new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, anInsets, 0, 0));

    myPackagesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent event) {
        updateUninstallUpgrade();
      }
    });

    myInstallButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mySelectedSdk != null) {
          ManagePackagesDialog dialog = new ManagePackagesDialog(myProject, mySelectedSdk, PyPackagesPanel.this);
          dialog.show();
        }
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
        if (mySelectedSdk != null && myInstallButton.isEnabled()) {
          ManagePackagesDialog dialog = new ManagePackagesDialog(myProject, mySelectedSdk, PyPackagesPanel.this);
          Point p = e.getPoint();
          int row = myPackagesTable.rowAtPoint(p);
          int column = myPackagesTable.columnAtPoint(p);
          if (row >= 0 && column >= 0) {
            Object pyPackage = myPackagesTable.getValueAt(row, 0);
            if (pyPackage instanceof PyPackage) {
              dialog.setSelected(((PyPackage)pyPackage).getName());
            }
          }
          dialog.show();
          return true;
        }
        return false;
      }
    }.installOn(myPackagesTable);

    myNotificationArea.addLinkHandler(INSTALL_DISTRIBUTE, new Runnable() {
      @Override
      public void run() {
        final Sdk sdk = mySelectedSdk;
        if (sdk != null) {
          installManagementTool(sdk, PyPackageManagerImpl.DISTRIBUTE);
        }
      }
    });
    myNotificationArea.addLinkHandler(INSTALL_PIP, new Runnable() {
      @Override
      public void run() {
        final Sdk sdk = mySelectedSdk;
        if (sdk != null) {
          installManagementTool(sdk, PyPackageManagerImpl.PIP);
        }
      }
    });
  }

  public void addPathChangedListener(Consumer<Sdk> consumer) {
    myPathChangedListeners.add(consumer);
  }

  public JBTable getPackagesTable() {
    return myPackagesTable;
  }

  public PyPackagesNotificationPanel getNotificationsArea() {
    return myNotificationArea;
  }

  private void addUpgradeAction() {
    myUpgradeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final int[] rows = myPackagesTable.getSelectedRows();
        final Sdk selectedSdk = mySelectedSdk;
        if (selectedSdk != null) {
          for (int row : rows) {
            final Object pyPackage = myPackagesTableModel.getValueAt(row, 0);
            if (pyPackage instanceof PyPackage) {
              final String packageName = ((PyPackage)pyPackage).getName();
              final Object currentVersion = myPackagesTableModel.getValueAt(row, 1);

              PyPIPackageUtil.INSTANCE.usePackageReleases(packageName, new AsyncCallback() {
                @Override
                public void handleResult(Object result, URL url, String method) {
                  final List<String> releases = (List<String>)result;
                  PyPIPackageUtil.INSTANCE.addPackageReleases(packageName, releases);
                  if (!releases.isEmpty() &&
                      PyRequirement.VERSION_COMPARATOR.compare((String)currentVersion, releases.get(0)) >= 0)
                    return;

                  ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      PyPackageManagerImpl.UI ui =
                        new PyPackageManagerImpl.UI(myProject, selectedSdk, new PyPackageManagerImpl.UI.Listener() {
                          @Override
                          public void started() {
                            myPackagesTable.setPaintBusy(true);
                            currentlyInstalling.add(packageName);
                          }

                          @Override
                          public void finished(final List<PyExternalProcessException> exceptions) {
                            myPackagesTable.clearSelection();
                            updatePackages(selectedSdk);
                            myPackagesTable.setPaintBusy(false);
                            currentlyInstalling.remove(packageName);
                            if (exceptions.isEmpty()) {
                              myNotificationArea.showSuccess("Package successfully upgraded");
                            }
                            else {
                              myNotificationArea.showError("Upgrade packages failed. <a href=\"xxx\">Details...</a>",
                                                           "Upgrade Packages Failed",
                                                           PyPackageManagerImpl.UI
                                                             .createDescription(exceptions, "Upgrade packages failed."));
                            }
                          }
                        });
                      ui.install(Collections.singletonList(new PyRequirement(packageName)), Collections.singletonList("-U"));
                      myUpgradeButton.setEnabled(false);
                    }
                  }, ModalityState.any());
                }

                @Override
                public void handleError(Exception exception, URL url, String method) {
                  ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      Messages.showErrorDialog("Error occurred. Please, check your internet connection.",
                                               "Upgrade Package Failed.");
                    }
                  }, ModalityState.any());
                }
              });
            }
          }
        }
      }
    });
  }

  private void updateUninstallUpgrade() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final int[] selected = myPackagesTable.getSelectedRows();
        boolean isAvailabe = mySelectedSdk != null && selected.length != 0 && myHasPip;
        boolean upgradeAvailable = false;
        boolean isPipOrDistribute = false;
        boolean isInUserSite = true;
        final String userSite = PyPackageManagerImpl.getUserSite();
        if (isAvailabe) {
          for (int i = 0; i != selected.length; ++i) {
            final int index = selected[i];
            if (index >= myPackagesTable.getRowCount()) continue;
            final Object value = myPackagesTable.getValueAt(index, 0);
            if (value instanceof PyPackage) {
              final PyPackage pyPackage = (PyPackage)value;
              final String pyPackageName = pyPackage.getName();
              final String availableVersion = (String)myPackagesTable.getValueAt(index, 2);
              upgradeAvailable = PyRequirement.VERSION_COMPARATOR.compare(pyPackage.getVersion(), availableVersion) < 0 &&
                                 !currentlyInstalling.contains(pyPackageName);
              isPipOrDistribute = "pip".equals(pyPackageName) || "distribute".equals(pyPackageName);
              isAvailabe = !isPipOrDistribute;

              final String location = pyPackage.getLocation();
              if (isInUserSite && location != null) {
                isInUserSite = location.startsWith(userSite);
              }
              if (!isAvailabe) break;
            }
          }
        }
        final boolean isVEnv = PythonSdkType.isVirtualEnv(mySelectedSdk);
        myUninstallButton.setEnabled(isAvailabe && (!isVEnv || !isInUserSite));
        myUpgradeButton.setEnabled(upgradeAvailable && isAvailabe || isPipOrDistribute);
      }
    }, ModalityState.any());
  }

  private void addUninstallAction() {
    myUninstallButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final List<PyPackage> packages = getSelectedPackages();
        final Sdk sdk = mySelectedSdk;
        if (sdk != null) {
          PyPackageManagerImpl.UI ui = new PyPackageManagerImpl.UI(myProject, sdk, new PyPackageManagerImpl.UI.Listener() {
            @Override
            public void started() {
              myPackagesTable.setPaintBusy(true);
            }

            @Override
            public void finished(final List<PyExternalProcessException> exceptions) {
              myPackagesTable.clearSelection();
              updatePackages(sdk);
              myPackagesTable.setPaintBusy(false);
              if (exceptions.isEmpty()) {
                myNotificationArea.showSuccess("Packages successfully uninstalled");
              }
              else {
                myNotificationArea.showError("Uninstall packages failed. <a href=\"xxx\">Details...</a>",
                                             "Uninstall Packages Failed",
                                             PyPackageManagerImpl.UI.createDescription(exceptions, "Uninstall packages failed."));
              }
            }
          });
          ui.uninstall(packages);
        }
      }
    });
  }

  @NotNull
  private List<PyPackage> getSelectedPackages() {
    final List<PyPackage> results = new ArrayList<PyPackage>();
    final int[] rows = myPackagesTable.getSelectedRows();
    for (int row : rows) {
      final Object packageName = myPackagesTableModel.getValueAt(row, 0);
      if (packageName instanceof PyPackage) {
        results.add((PyPackage)packageName);
      }
    }
    return results;
  }

  public void updatePackages(Sdk selectedSdk) {
    mySelectedSdk = selectedSdk;
    myPackagesTable.clearSelection();
    myPackagesTableModel.getDataVector().clear();
    updatePackages(selectedSdk, null);
  }

  public void updatePackages(final Sdk selectedSdk, @Nullable final Set<String> installed) {
    myPackagesTable.setPaintBusy(true);
    final Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        List<PyPackage> packages = Lists.newArrayList();
        if (selectedSdk != null) {
          try {
            packages = ((PyPackageManagerImpl)PyPackageManager.getInstance(selectedSdk)).getPackages();
          }
          catch (PyExternalProcessException e) {
            // do nothing, we already have an empty list
          }
          finally {
            final List<PyPackage> finalPackages = packages;
            final Map<String, String> cache = PyPIPackageUtil.getPyPIPackages();
            if (cache.isEmpty()) {
              updateCache(application);
            }
            application.invokeLater(new Runnable() {
              @Override
              public void run() {

                if (selectedSdk == mySelectedSdk) {
                  myPackagesTableModel.getDataVector().clear();
                  for (PyPackage pyPackage : finalPackages) {
                    if (installed != null) {
                      installed.add(pyPackage.getName());
                    }
                    final String version = cache.get(pyPackage.getName());
                    myPackagesTableModel
                      .addRow(new Object[]{pyPackage, pyPackage.getVersion(), version == null ? "" : version});
                  }
                  if (!cache.isEmpty()) {
                    myPackagesTable.setPaintBusy(false);
                  }
                }
              }
            }, ModalityState.any());
          }
        }
      }
    });
  }

  private void updateCache(final Application application) {
    application.executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          PyPIPackageUtil.INSTANCE.updatePyPICache(PyPackageService.getInstance());
          application.invokeLater(new Runnable() {
            @Override
            public void run() {
              final Map<String, String> cache = PyPIPackageUtil.getPyPIPackages();
              for (int i = 0; i != myPackagesTableModel.getRowCount(); ++i) {
                final PyPackage pyPackage = (PyPackage)myPackagesTableModel.getValueAt(i, 0);
                final String version = cache.get(pyPackage.getName());
                myPackagesTableModel.setValueAt(version, i, 2);
              }
              myPackagesTable.setPaintBusy(false);
            }
          }, ModalityState.stateForComponent(myPackagesTable));
        }
        catch (IOException ignored) {
          myPackagesTable.setPaintBusy(false);
        }
      }
    });
  }

  public void updateNotifications(@NotNull final Sdk selectedSdk) {
    final Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        PyExternalProcessException exc = null;
        try {
          PyPackageManagerImpl packageManager = (PyPackageManagerImpl)PyPackageManager.getInstance(selectedSdk);
          myHasDistribute = packageManager.findPackage(PyPackageManagerImpl.PACKAGE_DISTRIBUTE) != null;
          if (!myHasDistribute) {
            myHasDistribute = packageManager.findPackage(PyPackageManagerImpl.PACKAGE_SETUPTOOLS) != null;
          }
          myHasPip = packageManager.findPackage(PyPackageManagerImpl.PACKAGE_PIP) != null;
        }
        catch (PyExternalProcessException e) {
          exc = e;
        }
        final PyExternalProcessException externalProcessException = exc;
        application.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (selectedSdk == mySelectedSdk) {
              final PythonSdkFlavor flavor = PythonSdkFlavor.getFlavor(selectedSdk);
              final boolean invalid = PythonSdkType.isInvalid(selectedSdk);
              boolean allowCreateVirtualEnv =
                !(PythonSdkType.isRemote(selectedSdk) || flavor instanceof IronPythonSdkFlavor) &&
                !PythonSdkType.isVirtualEnv(selectedSdk) &&
                myNotificationArea.hasLinkHandler(CREATE_VENV);
              final String createVirtualEnvLink = "<a href=\"" + CREATE_VENV + "\">create new VirtualEnv</a>";
              myNotificationArea.hide();
              if (!invalid) {
                String text = null;
                if (externalProcessException != null) {
                  final int retCode = externalProcessException.getRetcode();
                  if (retCode == PyPackageManagerImpl.ERROR_NO_PIP) {
                    myHasPip = false;
                  }
                  else if (retCode == PyPackageManagerImpl.ERROR_NO_DISTRIBUTE) {
                    myHasDistribute = false;
                  }
                  else {
                    text = externalProcessException.getMessage();
                  }
                  final boolean hasPackagingTools = myHasPip && myHasDistribute;
                  allowCreateVirtualEnv &= !hasPackagingTools;
                }
                if (text == null) {
                  if (!myHasDistribute) {
                    text = "Python package management tools not found. <a href=\"" + INSTALL_DISTRIBUTE + "\">Install 'distribute'</a>";
                  }
                  else if (!myHasPip) {
                    text = "Python packaging tool 'pip' not found. <a href=\"" + INSTALL_PIP + "\">Install 'pip'</a>";
                  }
                }
                if (text != null) {
                  if (allowCreateVirtualEnv) {
                    text += " or " + createVirtualEnvLink;
                  }
                  myNotificationArea.showWarning(text);
                }
              }

              myInstallButton.setEnabled(!invalid && externalProcessException == null && myHasPip);
            }
          }
        }, ModalityState.any());
      }
    });
  }

  private void installManagementTool(@NotNull final Sdk sdk, final String name) {
    final PyPackageManagerImpl.UI ui = new PyPackageManagerImpl.UI(myProject, sdk, new PyPackageManagerImpl.UI.Listener() {
      @Override
      public void started() {
        myPackagesTable.setPaintBusy(true);
      }

      @Override
      public void finished(List<PyExternalProcessException> exceptions) {
        myPackagesTable.setPaintBusy(false);
        PyPackageManagerImpl packageManager = (PyPackageManagerImpl)PyPackageManager.getInstance(sdk);
        if (!exceptions.isEmpty()) {
          final String firstLine = "Install package failed. ";
          final String description = PyPackageManagerImpl.UI.createDescription(exceptions, firstLine);
          packageManager.showInstallationError(myProject, "Failed to install " + name, description);
        }
        packageManager.refresh();
        updatePackages(sdk);
        for (Consumer<Sdk> listener : myPathChangedListeners) {
          listener.consume(sdk);
        }
        updateNotifications(sdk);
      }
    });
    ui.installManagement(name);
  }


  private static class MyTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
      final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      final String version = (String)table.getValueAt(row, 1);
      final String availableVersion = (String)table.getValueAt(row, 2);
      cell.setIcon(PyRequirement.VERSION_COMPARATOR.compare(version, availableVersion) < 0 && column == 2 ?
                   AllIcons.Vcs.Arrow_right : null);
      final Object pyPackage = table.getValueAt(row, 0);
      if (pyPackage instanceof PyPackage) {
        cell.setToolTipText(FileUtil.getLocationRelativeToUserHome(((PyPackage)pyPackage).getLocation()));
      }
      return cell;
    }
  }
}