commit ace40d552603d2d44582bf794a76fc44f515713f
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Tue Sep 23 18:43:08 2014 +0100

    chore(docs): refactor the docs app search for better bootup time

    This commit refactors how the search index is built. The docsSearch service
    is now defined by a provider, which returns a different implementation of
    the service depending upon whether the current browser supports WebWorkers
    or now.

    * **WebWorker supported**: The index is then built and stored in a new worker.
    The service posts and receives messages to and from this worker to make
    queries on the search index.

    * **WebWorker no supported**: The index is built locally but with a 500ms
    delay so that the initial page can render before the browser is blocked as
    the index is built.

    Also the way that the current app is identified has been modified so we can
    slim down the js data files (pages-data.js) to again improve startup time.

    Closes #9204
    Closes #9203