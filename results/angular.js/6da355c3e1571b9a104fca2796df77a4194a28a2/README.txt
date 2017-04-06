commit 6da355c3e1571b9a104fca2796df77a4194a28a2
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Wed Mar 28 16:03:59 2012 -0700

    refactor($compile): move methods of attr object into prototype

    We have many instances of this object and we clone them as well (e.g. ng-repeat).
    This should save some memory and performance as well.

    Double prefixed private properties of attr object:
    attr.$element -> attr.$$element
    attr.$observers -> attr.$$observers

    Update shallowCopy to not copy $$ properties and allow passing optional destination object.