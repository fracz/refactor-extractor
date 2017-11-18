commit 9675a71a0cfebd5641a986fdeeea100478bb7ffa
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Wed Feb 22 18:05:30 2017 -0500

    Improve webviews for updates (#61)

    * initial external url tracking; add test

    * refactor to add external link delegate method to client

    * add external link logic to update activity

    * fix up back nav on webviews

    * Add context to external link event

    * Add back logic to WebViewActivity

    The base for a few of our activities

    * Add request handler for another update requests

    Pings the VM on another index requests to properly display sequence title and share data

    * Add tests for new request logic

    Also includes a new default intent because hey sometimes we just need a default intent

    * Handle additional update url requests

    * Make shareIntent output a PublishSubject to stay hidden on rotation

    * Add additional index url logic to ProjectUpdates

    * Rename initialProject -> Project since the project will never change

    * Add new delegate method to config

    * Remove unused uri helpers, add unit test for KSUri

    * remove redundant null filters