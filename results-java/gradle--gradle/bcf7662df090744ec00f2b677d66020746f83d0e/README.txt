commit bcf7662df090744ec00f2b677d66020746f83d0e
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Oct 4 22:39:38 2011 +0200

    GRADLE-646. Working on a feature that allows forcing certain transitive dependencies versions mixed with strict version conflict strategy. Without this feature, one must create 'fake' first level dependencies to force certain versions of transitive dependencies. The DSL for it is just a first pass - we should pair on it really. I tried to incrementally grow this feature + keep the DSL consistent with other parts of Gradle. This check-in is just refactoring for now and preparing the system for the feature.