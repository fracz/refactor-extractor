commit 2ffafac3450cb322d064895ceb207c2b90fa755c
Author: hansd <mail@dockter.biz>
Date:   Thu Oct 29 10:00:32 2009 +0100

    GRADLE-718 Fixed a resolve bug when project have identical group/version/name values. Big refactoring of the ivy conversion layer.
    Clearly separated converters for resolving, publishing artifacts and generating the published ivy.xml.
    Improved modularization of the ivy conversion layer.