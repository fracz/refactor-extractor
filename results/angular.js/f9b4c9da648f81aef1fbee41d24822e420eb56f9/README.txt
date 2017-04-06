commit f9b4c9da648f81aef1fbee41d24822e420eb56f9
Author: Di Peng <pengdi@google.com>
Date:   Thu Jul 14 18:28:15 2011 -0700

    refactor(docs): run e2e tests with and without jquery

    - e2e tests will run index.html (without jquery) and with
    index-jq.html(with jquery).
    - many small changes to make e2e tests work withough JQuery as we
    discover problems that were previously hidden by using real JQuery.