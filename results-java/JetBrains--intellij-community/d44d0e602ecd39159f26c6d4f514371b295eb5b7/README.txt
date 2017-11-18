commit d44d0e602ecd39159f26c6d4f514371b295eb5b7
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Mon Jun 24 15:22:48 2013 +0400

    a number of inspection tools refactorings:
    to reduce confusion, InspectionToolWrapper no longer extends InspectionTool;
    InspectionToolWrapper and InspectionProfileManager moved to analysis;
    InspectionNode decoupled from InspectionTool - the former stored in the InspectionToolPresentation and the mapping InspectionToolWrapper->InspectionToolPresentation is stored in GlobalInspectionContext