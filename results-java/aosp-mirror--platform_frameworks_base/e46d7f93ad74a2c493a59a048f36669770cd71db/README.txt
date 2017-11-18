commit e46d7f93ad74a2c493a59a048f36669770cd71db
Author: Tomasz Mikolajewski <mtomasz@google.com>
Date:   Tue Mar 15 17:41:31 2016 +0900

    Add a performance test for launching DocumentsUI.

    The test launches the DocumentsUI as picker, then waits until the
    main thread idles, which guarantees that roots are loaded and UI
    rendered.

    It confirms, that the recent system cache improves cold start
    performance by around 24% on my configuration (from 1685ms to 1357ms).

    Bug: 27370274
    Change-Id: I738202ea434a7bfe7080fc0994f636ef0e7847cd