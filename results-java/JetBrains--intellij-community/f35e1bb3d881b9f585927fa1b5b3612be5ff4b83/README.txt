commit f35e1bb3d881b9f585927fa1b5b3612be5ff4b83
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Mon Jun 24 15:23:00 2013 +0400

    a number of inspection tools refactorings:
    to reduce confusion, InspectionToolWrapper no longer extends InspectionTool;
    InspectionToolWrapper and InspectionProfileManager moved to analysis;
    InspectionNode decoupled from InspectionTool - the former stored in the InspectionToolPresentation and the mapping InspectionToolWrapper->InspectionToolPresentation is stored in GlobalInspectionContext