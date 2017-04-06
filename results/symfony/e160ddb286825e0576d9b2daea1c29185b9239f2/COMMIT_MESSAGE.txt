commit e160ddb286825e0576d9b2daea1c29185b9239f2
Merge: 430c6d7 0270136
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon May 27 16:49:42 2013 +0200

    Merge branch '2.3'

    * 2.3: (37 commits)
      [Console] renamed ConsoleForExceptionEvent into ConsoleExceptionEvent
      Fix several instances of doubled words
      [Security] Fixed the check if an interface exists.
      Added missing slovak translations
      [FrameworkBundle] removed HttpFoundation classes from HttpKernel cache
      [Finder] Fix iteration fails with non-rewindable streams
      [Finder] Fix unexpected duplicate sub path related AppendIterator issue
      [Security] Added tests for the DefaultLogoutSuccessHandler.
      [Security] Added tests for the DefaultAuthenticationSuccessHandler.
      [ClassLoader] tiny refactoring
      [Security] Added tests for the DefaultAuthenticationFailureHandler.
      [Security] Added tests for the remember me ReponseListener.
      [Security] Added tests for the SessionAuthenticationStrategy.
      [Security] Added tests for the AccessMap.
      [FrameworkBundle] removed deprecated method from cache:clear command
      [WebProfiler] remove deprecated verbose option
      fix logger in regards to DebugLoggerInterface
      [Form] [2.3] removed old option
      Added type of return value in VoterInterface.
      [Console] Add namespace support back in to list command
      ...