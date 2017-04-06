commit 1cad16c6f9144113e2b458ce91af8fcb0211c173
Author: Vojta Jina <vojta@gemin-i.org>
Date:   Sat Oct 16 15:22:36 2010 +0100

    Update $location API Close #62

    update(objOrString)
    updateHash(objOrString [, objOrString])
    toString()
    cancel()

    Examples:
    $location.update('http://www.angularjs.org/path#path?a=b');
    $location.update({port: 443, protocol: 'https'});
    $location.updateHash('hashPath');
    $location.updateHash({a: 'b'});
    $location.updateHash('hashPath', {a: 'b'});

    This commit was produced by squash of more commits, here are the old messages:

    - Change tests to use update() instead of parse().
    - First implementation of update() method
    - Test for update() with object parameter
    - Add new tests for location, refactor location code
    - Add tests for updateHash()
    - Implement updateHash()
    - Take one or two arguments, could be string - update hashPath, or hash object - update hashSearch...
    - Fixed other service tests, to use new $location.update()
    Added $location.cancel() method (with test)
    Added $location.parse() for back compatability
    Remove parse() method