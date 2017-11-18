commit 3e89f97a14c8824261e3101018c16222eb063557
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Mon Sep 12 22:16:12 2016 +0300

    PY-20733: Give user ability to choose remote mapping when she creates remote project

     * It makes no sense to have remote project with out of remote mapping, so we ask user for mappings when project is created
     * Since we need project to be created before running mapping tool, we show mapping window at next step (huge refactoring is required to display mapping window before project is created)
     * Platform calls ``ProjectGenerator#generateProject``, but we have a lot of generators, so we moved code to parent which delegates remote project configuration to ``PyRemoteInterpreterManager`` and then calls ``configureProject``