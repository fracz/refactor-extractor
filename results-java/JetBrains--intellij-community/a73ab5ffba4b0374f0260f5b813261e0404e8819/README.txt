commit a73ab5ffba4b0374f0260f5b813261e0404e8819
Author: Alexandr Evstigneev <Alexandr.Evstigneev@jetbrains.com>
Date:   Mon Jan 9 17:44:17 2017 +0300

    RunContentExecutor refactoring: removed redundant project parameter and used Application#invokeLater instead of SwingUtilities#invokeLater