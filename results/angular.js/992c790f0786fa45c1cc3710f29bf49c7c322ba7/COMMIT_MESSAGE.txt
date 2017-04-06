commit 992c790f0786fa45c1cc3710f29bf49c7c322ba7
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Tue Nov 29 21:51:59 2011 -0800

    refactor(scope): separate controller from scope

    Controller is standalone object, created using "new" operator, not messed up with scope anymore.
    Instead, related scope is injected as $scope.

    See design proposal: https://docs.google.com/document/pub?id=1SsgVj17ec6tnZEX3ugsvg0rVVR11wTso5Md-RdEmC0k

    Closes #321
    Closes #425

    Breaks controller methods are not exported to scope automatically
    Breaks Scope#$new() does not take controller as argument anymore