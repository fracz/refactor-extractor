commit dbffbefb7cd7af2ac063c95378a035aa9fbbd2ff
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Fri Jan 20 14:04:53 2012 -0800

    refactor($controller): Add $controller service for instantiating controllers

    So that we can allow user to override this service and use BC hack:
    https://gist.github.com/1649788