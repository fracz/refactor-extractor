commit c660475aafe91269be306c802823cf543005ce36
Author: Calin Juravle <calin@google.com>
Date:   Wed Jan 25 17:08:03 2017 -0800

    A bit more refactoring in BackgroundDexOptService

    Extract convenient methods to help with:
    - adding a shell command to run the background dexopt job
    - extending idleOptimizations to cover secondary dex files

    Test: device boots, background job run.
    Bug: 32871170
    Change-Id: I18fde62a1cb05c030a620722d5f217589beaefb1