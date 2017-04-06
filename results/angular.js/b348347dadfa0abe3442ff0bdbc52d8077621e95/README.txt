commit b348347dadfa0abe3442ff0bdbc52d8077621e95
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Wed Mar 7 12:17:48 2012 -0800

    refactor(fromJson): Remove error() and just throw

    It's more likely you are using angular.fromJson() inside Angular world, which means you get proper
    exception handling by $exceptionHandler.

    There is no point to explicitly push it to console and it causes memory leaks on most browsers
    (tried Chrome stable/canary, Safari, FF).