package com.intellij.application.options.colors;

import com.intellij.application.options.editor.EditorOptionsProvider;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.ide.DataManager;
import com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.diff.impl.settings.DiffOptionsPanel;
import com.intellij.openapi.diff.impl.settings.DiffPreviewPanel;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.ex.DefaultColorSchemesManager;
import com.intellij.openapi.editor.colors.impl.DefaultColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.*;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.options.colors.ColorSettingsPages;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusFactory;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.util.containers.HashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class ColorAndFontOptions extends SearchableConfigurable.Parent.Abstract implements EditorOptionsProvider {
  private HashMap<String,MyColorScheme> mySchemes;
  private MyColorScheme mySelectedScheme;
  public static final String DIFF_GROUP = ApplicationBundle.message("title.diff");
  public static final String FILE_STATUS_GROUP = ApplicationBundle.message("title.file.status");
  public static final String SCOPES_GROUP = ApplicationBundle.message("title.scope.based");

  private boolean mySomeSchemesDeleted = false;
  private ArrayList<NewColorAndFontPanel> mySubPanels;
  private boolean myResetCompleted = false;
  private boolean myApplyCompleted = false;

  public boolean isModified() {
    boolean result = isModifiedImpl();
    if (result) {
      myApplyCompleted = false;
    }
    return result;
  }

  private boolean isModifiedImpl() {
    if (mySomeSchemesDeleted) return true;

    if (!mySelectedScheme.getName().equals(EditorColorsManager.getInstance().getGlobalScheme().getName())) return true;

    for (MyColorScheme scheme : mySchemes.values()) {
      if (scheme.isModified()) return true;
    }

    return false;
  }

  public EditorColorsScheme selectScheme(@NotNull String name) {
    mySelectedScheme = getScheme(name);
    return mySelectedScheme;
  }

  private MyColorScheme getScheme(String name) {
    return mySchemes.get(name);
  }

  public EditorColorsScheme getSelectedScheme() {
    return mySelectedScheme;
  }

  public EditorColorsScheme getOriginalSelectedScheme() {
    return mySelectedScheme == null ? null : mySelectedScheme.getOriginalScheme();
  }

  public EditorSchemeAttributeDescriptor[] getCurrentDescriptions() {
    return mySelectedScheme.getDescriptors();
  }

  public static boolean isDefault(EditorColorsScheme scheme) {
    return ((MyColorScheme)scheme).isDefault();
  }

  public String[] getSchemeNames() {
    ArrayList<MyColorScheme> schemes = new ArrayList<MyColorScheme>(mySchemes.values());
    Collections.sort(schemes, new Comparator<MyColorScheme>() {
      public int compare(MyColorScheme o1, MyColorScheme o2) {
        if (isDefault(o1) && !isDefault(o2)) return -1;
        if (!isDefault(o1) && isDefault(o2)) return 1;

        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });

    ArrayList<String> names = new ArrayList<String>(schemes.size());
    for (MyColorScheme scheme : schemes) {
      names.add(scheme.getName());
    }

    return names.toArray(new String[names.size()]);
  }

  public Collection<EditorColorsScheme> getSchemes() {
    return new ArrayList<EditorColorsScheme>(mySchemes.values());
  }

  public void saveSchemeAs(String name) {
    MyColorScheme scheme = mySelectedScheme;
    if (scheme == null) return;

    EditorColorsScheme clone = (EditorColorsScheme)scheme.getOriginalScheme().clone();

    scheme.apply(clone);

    clone.setName(name);
    MyColorScheme newScheme = new MyColorScheme(clone);
    initScheme(newScheme);

    mySchemes.put(name, newScheme);
    selectScheme(newScheme.getName());
    resetSchemesCombo();
  }

  public void addImportedScheme(final EditorColorsScheme imported) {
    MyColorScheme newScheme = new MyColorScheme(imported);
    initScheme(newScheme);

    mySchemes.put(imported.getName(), newScheme);
    selectScheme(newScheme.getName());
    resetSchemesCombo();
  }

  public void removeScheme(String name) {
    if (mySelectedScheme.getName().equals(name)) {
      //noinspection HardCodedStringLiteral
      selectScheme("Default");
    }

    mySchemes.remove(name);
    resetSchemesCombo();
    mySomeSchemesDeleted = true;
  }

  public void apply() throws ConfigurationException {
    if (!myApplyCompleted) {
      try {
        EditorColorsManager myColorsManager = EditorColorsManager.getInstance();

        myColorsManager.removeAllSchemes();
        for (MyColorScheme scheme : mySchemes.values()) {
            if (!scheme.isDefault()) {
              scheme.apply();
              myColorsManager.addColorsScheme(scheme.getOriginalScheme());
            }
          }

        EditorColorsScheme originalScheme = mySelectedScheme.getOriginalScheme();
        myColorsManager.setGlobalScheme(originalScheme);

        applyChangesToEditors();

        myResetCompleted = false;
        reset();
      }
      finally {
        myApplyCompleted = true;


      }


    }


//    initAll();
//    resetSchemesCombo();


  }

  private void applyChangesToEditors() {
    EditorFactory.getInstance().refreshAllEditors();

    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    for (Project openProject : openProjects) {
      FileStatusManager.getInstance(openProject).fileStatusesChanged();
      DaemonCodeAnalyzer.getInstance(openProject).restart();
    }
  }

  private boolean myIsReset = false;

  private void resetSchemesCombo() {
    myIsReset = true;
    try {
      for (NewColorAndFontPanel subPanel : mySubPanels) {
        subPanel.resetSchemesCombo();
      }
    }
    finally {
      myIsReset = false;
    }
  }

  protected Configurable[] buildConfigurables() {
    initAll();

    mySubPanels = createSubPanels();

    for (NewColorAndFontPanel subPanel : mySubPanels) {
      subPanel.addListener(new ColorAndFontSettingsListener.Abstract(){
        public void schemeChanged(final EditorColorsScheme scheme) {
          if (!myIsReset) {
            resetSchemesCombo();
          }
        }
      });
    }

    List<Configurable> result = new ArrayList<Configurable>();

    for (final NewColorAndFontPanel subPanel : mySubPanels) {
      result.add(new Configurable(){
        @Nls
        public String getDisplayName() {
          return subPanel.getDisplayName();
        }

        public Icon getIcon() {
          return null;
        }

        public String getHelpTopic() {
          return null;
        }

        public JComponent createComponent() {
          return subPanel;
        }

        public boolean isModified() {
          return ColorAndFontOptions.this.isModified();
        }

        public void apply() throws ConfigurationException {
          ColorAndFontOptions.this.apply();
        }

        public void reset() {
          ColorAndFontOptions.this.reset();
        }

        public void disposeUIResources() {
          subPanel.dispose();
        }
      });
    }

    return result.toArray(new Configurable[result.size()]);
  }

  private ArrayList<NewColorAndFontPanel> createSubPanels() {

    ArrayList<NewColorAndFontPanel> result = new ArrayList<NewColorAndFontPanel>();

    ColorSettingsPage[] pages = ColorSettingsPages.getInstance().getRegisteredPages();
    for (ColorSettingsPage page : pages) {
      final SimpleEditorPreview preview = new SimpleEditorPreview(this, page);
      NewColorAndFontPanel panel = NewColorAndFontPanel.create(preview,
                                                            page.getDisplayName(),
                                                            this);

      panel.addOptionListListener(new ColorAndFontSettingsListener.Abstract(){
        public void selectedOptionChanged(final Object selected) {
          preview.blinkSelectedHighlightType((EditorSchemeAttributeDescriptor)selected);
        }
      });
      result.add(panel);
    }

    result.add(createDiffPanel());

    result.add(NewColorAndFontPanel.create(new PreviewPanel.Empty(), ColorAndFontOptions.FILE_STATUS_GROUP, this));

    final JPanel scopePanel = createChooseScopePanel();
    result.add(NewColorAndFontPanel.create(new PreviewPanel.Empty(){
      public Component getPanel() {

        return scopePanel;
      }

    }, ColorAndFontOptions.SCOPES_GROUP, this));


    return result;
  }

  private NewColorAndFontPanel createDiffPanel() {
    final DiffPreviewPanel diffPreviewPanel = new DiffPreviewPanel();
    diffPreviewPanel.setMergeRequest(null);
    final DiffOptionsPanel optionsPanel = new DiffOptionsPanel(this);

    optionsPanel.addListener(new ColorAndFontSettingsListener.Abstract(){
      public void actionPerformed(final ActionEvent e) {
        optionsPanel.applyChangesToScheme();
        diffPreviewPanel.updateView();
      }
    });


    SchemesPanel schemesPanel = new SchemesPanel(this);

    schemesPanel.addListener(new ColorAndFontSettingsListener.Abstract(){
      public void schemeChanged(final EditorColorsScheme scheme) {
        diffPreviewPanel.setColorScheme(getSelectedScheme());
        optionsPanel.updateOptionsList();
        diffPreviewPanel.updateView();
      }
    } );

    return new NewColorAndFontPanel(schemesPanel, optionsPanel, diffPreviewPanel,ColorAndFontOptions.DIFF_GROUP);

  }

  private JPanel createChooseScopePanel() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    JPanel panel = new JPanel(new GridBagLayout());
    //panel.setBorder(new LineBorder(Color.red));
    if (projects.length == 0) return panel;
    GridBagConstraints gc = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                                   new Insets(0, 0, 0, 0), 0, 0);
    final Project contextProject = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
    final Project project = contextProject != null ? contextProject : projects[0];

    JButton button = new JButton(ApplicationBundle.message("button.edit.scopes"));
    button.setPreferredSize(new Dimension(230, button.getPreferredSize().height));
    panel.add(button, gc);
    gc.gridx = GridBagConstraints.REMAINDER;
    gc.weightx = 1;
    panel.add(new JPanel(), gc);

    gc.gridy++;
    gc.gridx=0;
    gc.weighty = 1;
    panel.add(new JPanel(), gc);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final ScopeChooserConfigurable configurable = ScopeChooserConfigurable.getInstance(project);
        final boolean isOk = ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
        if (isOk) {
          //TODO lesya
          reset();
        }
      }
    });
    return panel;
  }


  private void initAll() {
    EditorColorsManager colorsManager = EditorColorsManager.getInstance();
    EditorColorsScheme[] allSchemes = colorsManager.getAllSchemes();

    mySchemes = new HashMap<String, MyColorScheme>();
    for (EditorColorsScheme allScheme : allSchemes) {
      MyColorScheme schemeDelegate = new MyColorScheme(allScheme);
      initScheme(schemeDelegate);
      mySchemes.put(schemeDelegate.getName(), schemeDelegate);
    }

    mySelectedScheme = mySchemes.get(EditorColorsManager.getInstance().getGlobalScheme().getName());
    assert mySelectedScheme != null : EditorColorsManager.getInstance().getGlobalScheme().getName() + "; myschemes=" + mySchemes;
  }

  private static void initScheme(MyColorScheme scheme) {
    ArrayList<EditorSchemeAttributeDescriptor> descriptions = new ArrayList<EditorSchemeAttributeDescriptor>();
    initPluggedDescriptions(descriptions, scheme);
    initDiffDescriptors(descriptions, scheme);
    initFileStatusDescriptors(descriptions, scheme);
    initScopesDescriptors(descriptions, scheme);

    scheme.setDescriptors(descriptions.toArray(new EditorSchemeAttributeDescriptor[descriptions.size()]));
  }

  private static void initPluggedDescriptions(ArrayList<EditorSchemeAttributeDescriptor> descriptions, MyColorScheme scheme) {
    ColorSettingsPage[] pages = ColorSettingsPages.getInstance().getRegisteredPages();
    for (ColorSettingsPage page : pages) {
      initDescriptions(page, descriptions, scheme);
    }
  }

  private static void initDescriptions(ColorSettingsPage page,
                                       ArrayList<EditorSchemeAttributeDescriptor> descriptions,
                                       MyColorScheme scheme) {
    String group = page.getDisplayName();
    AttributesDescriptor[] attributeDescriptors = page.getAttributeDescriptors();
    for (AttributesDescriptor descriptor : attributeDescriptors) {
      addSchemedDescription(descriptions, descriptor.getDisplayName(), group, descriptor.getKey(), scheme, null, null);
    }

    ColorDescriptor[] colorDescriptors = page.getColorDescriptors();
    for (ColorDescriptor descriptor : colorDescriptors) {
      ColorKey back = descriptor.getKind() == ColorDescriptor.Kind.BACKGROUND ? descriptor.getKey() : null;
      ColorKey fore = descriptor.getKind() == ColorDescriptor.Kind.FOREGROUND ? descriptor.getKey() : null;
      addEditorSettingDescription(descriptions, descriptor.getDisplayName(), group, back, fore, scheme);
    }
  }

  private static void initDiffDescriptors(ArrayList<EditorSchemeAttributeDescriptor> descriptions, MyColorScheme scheme) {
    DiffOptionsPanel.addSchemeDescriptions(descriptions, scheme);
  }

  private static void initFileStatusDescriptors(ArrayList<EditorSchemeAttributeDescriptor> descriptions, MyColorScheme scheme) {

    FileStatus[] statuses = FileStatusFactory.SERVICE.getInstance().getAllFileStatuses();

    for (FileStatus fileStatus : statuses) {
      addEditorSettingDescription(descriptions,
                                  fileStatus.getText(),
                                  FILE_STATUS_GROUP,
                                  null,
                                  fileStatus.getColorKey(),
                                  scheme);

    }
  }
  private static void initScopesDescriptors(ArrayList<EditorSchemeAttributeDescriptor> descriptions, MyColorScheme scheme) {
    Set<Pair<NamedScope,NamedScopesHolder>> namedScopes = new THashSet<Pair<NamedScope,NamedScopesHolder>>(new TObjectHashingStrategy<Pair<NamedScope,NamedScopesHolder>>() {
      public int computeHashCode(final Pair<NamedScope, NamedScopesHolder> object) {
        return object.getFirst().getName().hashCode();
      }

      public boolean equals(final Pair<NamedScope, NamedScopesHolder> o1, final Pair<NamedScope, NamedScopesHolder> o2) {
        return o1.getFirst().getName().equals(o2.getFirst().getName());
      }
    });
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    for (Project project : projects) {
      DaemonCodeAnalyzerImpl codeAnalyzer = (DaemonCodeAnalyzerImpl)DaemonCodeAnalyzer.getInstance(project);
      List<Pair<NamedScope,NamedScopesHolder>> cachedScopes = codeAnalyzer.getScopeBasedHighlightingCachedScopes();
      namedScopes.addAll(cachedScopes);
    }

    List<Pair<NamedScope, NamedScopesHolder>> list = new ArrayList<Pair<NamedScope, NamedScopesHolder>>(namedScopes);

    Collections.sort(list, new Comparator<Pair<NamedScope,NamedScopesHolder>>() {
      public int compare(final Pair<NamedScope,NamedScopesHolder> o1, final Pair<NamedScope,NamedScopesHolder> o2) {
        return o1.getFirst().getName().compareToIgnoreCase(o2.getFirst().getName());
      }
    });
    for (Pair<NamedScope,NamedScopesHolder> pair : list) {
      NamedScope namedScope = pair.getFirst();
      String name = namedScope.getName();
      TextAttributesKey textAttributesKey = getScopeTextAttributeKey(name);
      if (scheme.getAttributes(textAttributesKey) == null) {
        scheme.setAttributes(textAttributesKey, new TextAttributes());
      }
      NamedScopesHolder holder = pair.getSecond();

      PackageSet value = namedScope.getValue();
      String toolTip = holder.getDisplayName() + (value==null ? "" : ": "+ value.getText());
      addSchemedDescription(descriptions,
                            name,
                            SCOPES_GROUP,
                            textAttributesKey,
                            scheme, holder.getIcon(), toolTip);
    }
  }

  public static TextAttributesKey getScopeTextAttributeKey(final String scope) {
    return TextAttributesKey.find("SCOPE_KEY_" + scope);
  }

  private static void addEditorSettingDescription(ArrayList<EditorSchemeAttributeDescriptor> array,
                                                                     String name,
                                                                     String group,
                                                                     ColorKey backgroundKey,
                                                                     ColorKey foregroundKey,
                                                                     EditorColorsScheme scheme) {
    String type = null;
    if (foregroundKey != null) {
      type = foregroundKey.getExternalName();
    }
    else {
      if (backgroundKey != null) {
        type = backgroundKey.getExternalName();
      }
    }
    ColorAndFontDescription descr = new EditorSettingColorDescription(name, group, backgroundKey, foregroundKey, type, scheme);
    array.add(descr);
  }

  private static void addSchemedDescription(ArrayList<EditorSchemeAttributeDescriptor> array, String name, String group, TextAttributesKey key,
                                            EditorColorsScheme scheme,
                                            Icon icon,
                                            String toolTip) {
    ColorAndFontDescription descr = new SchemeTextAttributesDescription(name, group, key, scheme, icon, toolTip);
    array.add(descr);
  }

  public String getDisplayName() {
    return ApplicationBundle.message("title.colors.and.fonts");
  }

  public Icon getIcon() {
    return IconLoader.getIcon("/general/configurableColorsAndFonts.png");
  }

  public void reset() {
    if (!myResetCompleted) {
      try {
        mySomeSchemesDeleted = false;
        initAll();
        resetSchemesCombo();
      }
      finally {
        myResetCompleted = true;
      }
    }

  }

  public void disposeUIResources() {
    mySubPanels = null;
    myResetCompleted = false;
    myApplyCompleted = false;
  }

  public boolean currentSchemeIsDefault() {
    return mySelectedScheme.isDefault();
  }

  public boolean currentSchemeIsShared() {
    return ColorSettingsUtil.isSharedScheme(mySelectedScheme);
  }


  private static class SchemeTextAttributesDescription extends TextAttributesDescription {
    private TextAttributes myAttributesToApply;
    private TextAttributesKey key;

    public SchemeTextAttributesDescription(String name, String group, TextAttributesKey key, EditorColorsScheme scheme, Icon icon,
                                           String toolTip) {
      super(name, group,
            scheme.getAttributes(key) == null
            ? new TextAttributes()
            : scheme.getAttributes(key).clone(),
            key, scheme, icon, toolTip);
      this.key = key;
      myAttributesToApply = scheme.getAttributes(key);
      initCheckedStatus();
    }

    public void apply(EditorColorsScheme scheme) {
      if (scheme == null) scheme = getScheme();
      if (myAttributesToApply != null) {
        scheme.setAttributes(key, getTextAttributes());
      }
    }

    public boolean isModified() {
      return !Comparing.equal(myAttributesToApply, getTextAttributes());
    }

    public boolean isErrorStripeEnabled() {
      return true;
    }
  }

  private static class GetSetColor {
    private final ColorKey myKey;
    private EditorColorsScheme myScheme;
    private boolean isModified = false;
    private Color myColor;

    public GetSetColor(ColorKey key, EditorColorsScheme scheme) {
      myKey = key;
      myScheme = scheme;
      myColor = myScheme.getColor(myKey);
    }

    public Color getColor() {
      return myColor;
    }

    public void setColor(Color col) {
      if (getColor() == null || !getColor().equals(col)) {
        isModified = true;
        myColor = col;
      }
    }

    public void apply(EditorColorsScheme scheme) {
      if (scheme == null) scheme = myScheme;
      scheme.setColor(myKey, myColor);
    }

    public boolean isModified() {
      return isModified;
    }
  }

  private static class EditorSettingColorDescription extends ColorAndFontDescription {
    private GetSetColor myGetSetForeground;
    private GetSetColor myGetSetBackground;

    public EditorSettingColorDescription(String name,
                                         String group,
                                         ColorKey backgroundKey,
                                         ColorKey foregroundKey,
                                         String type,
                                         EditorColorsScheme scheme) {
      super(name, group, type, scheme, null, null);
      if (backgroundKey != null) {
        myGetSetBackground = new GetSetColor(backgroundKey, scheme);
      }
      if (foregroundKey != null) {
        myGetSetForeground = new GetSetColor(foregroundKey, scheme);
      }
      initCheckedStatus();
    }

    public int getFontType() {
      return 0;
    }

    public void setFontType(int type) {
    }

    public Color getExternalEffectColor() {
      return null;
    }

    public void setExternalEffectColor(Color color) {
    }

    public void setExternalEffectType(EffectType type) {
    }

    public EffectType getExternalEffectType() {
      return EffectType.LINE_UNDERSCORE;
    }

    public Color getExternalForeground() {
      if (myGetSetForeground == null) {
        return null;
      }
      return myGetSetForeground.getColor();
    }

    public void setExternalForeground(Color col) {
      if (myGetSetForeground == null) {
        return;
      }
      myGetSetForeground.setColor(col);
    }

    public Color getExternalBackground() {
      if (myGetSetBackground == null) {
        return null;
      }
      return myGetSetBackground.getColor();
    }

    public void setExternalBackground(Color col) {
      if (myGetSetBackground == null) {
        return;
      }
      myGetSetBackground.setColor(col);
    }

    public Color getExternalErrorStripe() {
      return null;
    }

    public void setExternalErrorStripe(Color col) {
    }

    public boolean isFontEnabled() {
      return false;
    }

    public boolean isForegroundEnabled() {
      return myGetSetForeground != null;
    }

    public boolean isBackgroundEnabled() {
      return myGetSetBackground != null;
    }

    public boolean isEffectsColorEnabled() {
      return false;
    }

    public boolean isModified() {
      return myGetSetBackground != null && myGetSetBackground.isModified()
             || myGetSetForeground != null && myGetSetForeground.isModified();
    }

    public void apply(EditorColorsScheme scheme) {
      if (myGetSetBackground != null) {
        myGetSetBackground.apply(scheme);
      }
      if (myGetSetForeground != null) {
        myGetSetForeground.apply(scheme);
      }
    }
  }

  public String getHelpTopic() {
    return "preferences.colorsFonts";
  }

  private static class MyColorScheme extends EditorColorsSchemeImpl {
    private int myFontSize;
    private float myLineSpacing;
    private String myFontName;
    private EditorSchemeAttributeDescriptor[] myDescriptors;
    private String myName;

    public MyColorScheme(EditorColorsScheme parenScheme) {
      super(parenScheme, DefaultColorSchemesManager.getInstance());
      myFontSize = parenScheme.getEditorFontSize();
      myLineSpacing = parenScheme.getLineSpacing();
      myFontName = parenScheme.getEditorFontName();
      myName = parenScheme.getName();
      if (parenScheme instanceof ExternalizableScheme) {
        getExternalInfo().copy(((ExternalizableScheme)parenScheme).getExternalInfo());
      }
      initFonts();
    }

    public String getName() {
      return myName;
    }

    public void setName(String name) {
      myName = name;
    }

    public void setDescriptors(EditorSchemeAttributeDescriptor[] descriptors) {
      myDescriptors = descriptors;
    }

    public EditorSchemeAttributeDescriptor[] getDescriptors() {
      return myDescriptors;
    }

    public boolean isDefault() {
      return myParentScheme instanceof DefaultColorsScheme;
    }

    public boolean isModified() {
      if (myFontSize != myParentScheme.getEditorFontSize()) return true;
      if (myLineSpacing != myParentScheme.getLineSpacing()) return true;
      if (!myFontName.equals(myParentScheme.getEditorFontName())) return true;

      for (EditorSchemeAttributeDescriptor descriptor : myDescriptors) {
        if (descriptor.isModified()) {
          return true;
        }
      }

      return false;
    }

    public void apply() {
      apply(myParentScheme);
    }

    public void apply(EditorColorsScheme scheme) {
      scheme.setEditorFontSize(myFontSize);
      scheme.setEditorFontName(myFontName);
      scheme.setLineSpacing(myLineSpacing);

      for (EditorSchemeAttributeDescriptor descriptor : myDescriptors) {
        descriptor.apply(scheme);
      }
    }

    public String getEditorFontName() {
      return myFontName;
    }

    public int getEditorFontSize() {
      return myFontSize;
    }

    public float getLineSpacing() {
      return myLineSpacing;
    }

    public void setEditorFontSize(int fontSize) {
      myFontSize = fontSize;
      initFonts();
    }

    public void setLineSpacing(float lineSpacing) {
      myLineSpacing = lineSpacing;
    }

    public void setEditorFontName(String fontName) {
      myFontName = fontName;
      initFonts();
    }

    public Object clone() {
      return null;
    }

    public EditorColorsScheme getOriginalScheme() {
      return myParentScheme;
    }
  }

  public String getId() {
    return getHelpTopic();
  }

  @Nullable
  public Runnable enableSearch(final String option) {
    return showOption(option, true);
  }

  private Runnable showOption(final String option, final boolean highlight) {
    return new Runnable(){
      public void run() {
        if (mySubPanels != null) {
          for (NewColorAndFontPanel subPanel : mySubPanels) {
            subPanel.showOption(ColorAndFontOptions.this, option, highlight).run();
          }
        }
      }
    };
  }

  @Nullable
  public Runnable selectOption(String option) {
    return showOption(option, false);
  }

  public Map<String, String> processListOptions(){
    HashMap<String, String> result = new HashMap<String, String>();
    for (NewColorAndFontPanel subPanel : mySubPanels) {
      result.putAll(subPanel.processListOptions());
    }
    return result;
  }
}