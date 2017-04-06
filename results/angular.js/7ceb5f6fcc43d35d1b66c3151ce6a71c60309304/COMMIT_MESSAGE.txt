commit 7ceb5f6fcc43d35d1b66c3151ce6a71c60309304
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Wed Sep 21 14:38:01 2016 +0200

    refactor(jqLite): Don't get/set props when getting/setting bool attrs

    This is done automatically by browsers in cases where it's needed; the
    workaround was only needed for IE<9. The new behavior means boolean attributes
    will not be reflected on elements where browsers don't reflect them.

    This change aligns jqLite with jQuery 3.

    Fixes #14126

    BREAKING CHANGE: Previously, all boolean attributes were reflected into
    properties in a setter and from a property in a getter, even on elements that
    don't treat those attributes in a special way. Now Angular doesn't do it
    by itself but relies on browsers to know when to reflect the property. Note that
    this browser-level conversions differs between browsers; if you need to change
    dynamic state of an element you should modify the property, not the attribute.

    See https://jquery.com/upgrade-guide/1.9/#attr-versus-prop- for a more detailed
    description about a related change in jQuery 1.9.

    To migrate the code follow the example below:

    Before:

    CSS:
    input[checked="checked"] { ... }

    JS:
    elem1.attr('checked', 'checked');
    elem2.attr('checked', false);

    After:

    CSS:
    input:checked { ... }

    JS:
    elem1.prop('checked', true);
    elem2.prop('checked', false);