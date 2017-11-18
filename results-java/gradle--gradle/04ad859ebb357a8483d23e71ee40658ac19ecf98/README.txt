commit 04ad859ebb357a8483d23e71ee40658ac19ecf98
Author: daz <darrell.deboer@gradleware.com>
Date:   Thu Sep 12 07:13:49 2013 -0600

    Some tool chain refactoring to assist with targeting for architecture

    - Can only configure path on GccToolChain, for Visual C++ use installDir
    - Only checkAvailable once in ToolChain.target(Platform), rather than in every call to PlatformToolChain
    - In VisualCppToolChain, configure the paths/environment when targeting a platform, rather than when setting the installer