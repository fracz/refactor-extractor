commit ada655a1d9f1cca1175b621f3daa0ad234d05668
Author: Sergey Simonchik <sergey.simonchik@jetbrains.com>
Date:   Fri Sep 22 23:55:42 2017 +0300

    sm test runner: support multi test sessions without process restart

    To improve performance for JavaScript test runners (jest, mocha, karma) started in watch mode.
    Introduce "testingStarted"/"testingFinished" events to separate test runs from each other.