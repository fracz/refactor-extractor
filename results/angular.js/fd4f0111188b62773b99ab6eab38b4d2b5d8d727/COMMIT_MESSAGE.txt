commit fd4f0111188b62773b99ab6eab38b4d2b5d8d727
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Fri Jan 27 00:52:42 2017 -0800

    perf($compile): do not use deepWatch in literal one-way bindings

    Avoiding deep watchers for array/object literals will improve watcher
    performance of all literals passed as one-way bindings, especially those
    containing references to large/complex objects.

    BREAKING CHANGE:
    Previously when a literal value was passed into a directive/component via
    one-way binding it would be watched with a deep watcher.

    For example, for `<my-component input="[a]">`, a new instance of the array
    would be passed into the directive/component (and trigger $onChanges) not
    only if `a` changed but also if any sub property of `a` changed such as
    `a.b` or `a.b.c.d.e` etc.

    This also means a new but equal value for `a` would NOT trigger such a
    change.

    Now literal values use an input-based watch similar to other directive/component
    one-way bindings. In this context inputs are the non-constant parts of the
    literal. In the example above the input would be `a`. Changes are only
    triggered when the inputs to the literal change.

    Closes #15301