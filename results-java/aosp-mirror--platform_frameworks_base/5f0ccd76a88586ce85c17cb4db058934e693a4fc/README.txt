commit 5f0ccd76a88586ce85c17cb4db058934e693a4fc
Author: Maksymilian Osowski <maxosowski@google.com>
Date:   Fri Jul 23 17:15:26 2010 +0100

    Moved practically all of the prerefactoring functionality to the new design.

    Renamed LayoutTestsRunner activity to TestsListActivity. It will be responsible for restrating the Executer after the crash. Now it only starts
    it.
    Renamed LayoutTestsRunnerThread to TestsListPreloaderThread. It only preloads tests now.
    LayoutTest class is no longer needed, its functionality is in LayoutTestsExecuter.
    Most of the functionality from LayoutTestsRunnerThread is now in ManagerService.

    Change-Id: I08924d949ceb9f8816888bc8e795256d0542fa99