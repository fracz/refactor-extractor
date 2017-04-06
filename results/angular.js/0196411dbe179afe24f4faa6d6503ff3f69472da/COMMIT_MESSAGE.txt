commit 0196411dbe179afe24f4faa6d6503ff3f69472da
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Wed Nov 30 12:23:58 2011 -0800

    refactor(scope.$watch): rearrange arguments passed into watcher (newValue, oldValue, scope)

    As scopes are injected into controllers now, you have the reference anyway, so having scope as first argument makes no sense…

    Breaks $watcher gets arguments in different order (newValue, oldValue, scope)