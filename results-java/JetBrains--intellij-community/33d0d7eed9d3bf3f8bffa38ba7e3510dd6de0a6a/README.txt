commit 33d0d7eed9d3bf3f8bffa38ba7e3510dd6de0a6a
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Wed Dec 26 19:34:28 2012 +0400

    [git] Better detect Git executable on Windows, refactor, test

    * Introduce GitExecutableDetector to detect executable path.
     - Collect all potential msysgit distributives under Program Files and Program Files (x86).
     - Select the best candidate: this is either "Git" without version, or the one with the highest possible version (if we could parse it out from the directory name).
     - If nothing found, try cygwin.
     - If nothing found, return the sole executable "git.exe" or "git.cmd". The latter is preferable, but doesn't exist in 1.8.0.2 (and maybe won't exist in the future), so we need to check if git.cmd can be started.
     - git.exe is returned as the default. It still can be not functional if it is not in the PATH, but we can't do more to find it.

    * GitVcs#activate: if the path to Git is not set, detect it. If it is set to git.exe/cmd (sole executable), and it is not valid, try to detect something better. Then pass to the ExecutableValidator.

    * Write tests for the Windows detector.