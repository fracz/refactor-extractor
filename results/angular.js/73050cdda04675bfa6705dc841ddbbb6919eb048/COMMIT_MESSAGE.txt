commit 73050cdda04675bfa6705dc841ddbbb6919eb048
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Wed Oct 12 14:23:14 2016 +0200

    fix(jqLite): align jqLite camelCasing logic with JQuery

    jqLite needs camelCase for it's css method; it should only convert one dash
    followed by a lowercase letter to an uppercase one; it shouldn't touch
    underscores, colons or collapse multiple dashes into one. This is behavior
    of jQuery 3 as well.

    Also, jqLite's css camelCasing logic was put in a separate function and
    refactored: now the properties starting from an uppercase letter are used by
    default (i.e. Webkit, not webkit) and the only exception is for the -ms- prefix
    that is converted to ms, not Ms. This makes the logic clearer as we're just
    always changing a dash followed by a lowercase letter by an uppercase one; this
    is also how it works in jQuery.

    The camelCasing for the $compile and $sce services retains the previous behaviour.

    Ref #15126
    Fix #7744

    BREAKING CHANGE: before, when Angular was used without jQuery, the key passed
    to the css method was more heavily camelCased; now only a single (!) hyphen
    followed by a lowercase letter is getting transformed. This also affects APIs
    that rely on the css method, like ngStyle.

    If you use Angular with jQuery, it already behaved in this way so no changes
    are needed on your part.

    To migrate the code follow the example below:

    Before:

    HTML:

    // All five versions used to be equivalent.
    <div ng-style={background_color: 'blue'}></div>
    <div ng-style={'background:color': 'blue'}></div>
    <div ng-style={'background-color': 'blue'}></div>
    <div ng-style={'background--color': 'blue'}></div>
    <div ng-style={backgroundColor: 'blue'}></div>

    JS:

    // All five versions used to be equivalent.
    elem.css('background_color', 'blue');
    elem.css('background:color', 'blue');
    elem.css('background-color', 'blue');
    elem.css('background--color', 'blue');
    elem.css('backgroundColor', 'blue');

    // All five versions used to be equivalent.
    var bgColor = elem.css('background_color');
    var bgColor = elem.css('background:color');
    var bgColor = elem.css('background-color');
    var bgColor = elem.css('background--color');
    var bgColor = elem.css('backgroundColor');

    After:

    HTML:

    // Previous five versions are no longer equivalent but these two still are.
    <div ng-style={'background-color': 'blue'}></div>
    <div ng-style={backgroundColor: 'blue'}></div>

    JS:

    // Previous five versions are no longer equivalent but these two still are.
    elem.css('background-color', 'blue');
    elem.css('backgroundColor', 'blue');

    // Previous five versions are no longer equivalent but these two still are.
    var bgColor = elem.css('background-color');
    var bgColor = elem.css('backgroundColor');