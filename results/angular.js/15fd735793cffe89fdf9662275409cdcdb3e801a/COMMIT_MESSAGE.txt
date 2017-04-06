commit 15fd735793cffe89fdf9662275409cdcdb3e801a
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Thu Jan 12 03:00:34 2012 -0800

    refactor($autoScroll): rename to $anchorScroll and allow disabling auto scrolling (links)

    Now, that we have autoscroll attribute on ng:include, there is no reason to disable the service completely, so $anchorScrollProvider.disableAutoScrolling() means it won't be scrolling when $location.hash() changes.

    And then, it's not $autoScroll at all, it actually scrolls to anchor when it's called, so I renamed
    it to $anchorScroll.