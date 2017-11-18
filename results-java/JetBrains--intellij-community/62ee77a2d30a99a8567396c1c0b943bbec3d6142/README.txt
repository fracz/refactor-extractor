commit 62ee77a2d30a99a8567396c1c0b943bbec3d6142
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sat Jun 9 18:15:15 2012 +0400

    [merge tool] Make possible to hide/show line numbers, soft wraps and use other editor settings.

    * Introduce an action in the merge tool toolbar to change settings of all 3 editors.
    * Save the settings in MergeToolSettings, workspace-level.
    * Since all 4 settings are similar, hold them in a separate enum MergeToolEditorSetting with the #apply() method which would apply the setting to the passed editor.
    * Get rid of EditorPlace.ViewProperty and all related methods and classes. Instead use more simple way to initialize settings (MergePanel2#initEditorSettings) and to change them (#setHighlighterSettings and other - for DiffPreviewPanel
    * Remove EditorPlace.ComponentState - no need in the additional level of abstraction since the DiffEditorState is the only usage.
    * Make some small cleanups and refactorings.