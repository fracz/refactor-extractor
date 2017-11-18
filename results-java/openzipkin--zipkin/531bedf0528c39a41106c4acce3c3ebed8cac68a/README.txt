commit 531bedf0528c39a41106c4acce3c3ebed8cac68a
Author: Adrian Cole <acole@pivotal.io>
Date:   Fri Jan 8 09:12:42 2016 +0800

    Fixes imports per style and removes unnecessary "this" qualification

    Spring Framework style guide requires folks to always qualify fields
    with `this.`. This project isn't a part of the spring framework, nor
    derives it style from that.

    Square and Google style (from which it derives) does not require such
    qualification. I've contributed to projects from many orgs, and Spring
    is the only place I've seen this. I don't think it is a stretch to call
    this unconventional.

    Unconventional is fine, when it improves the experience of development.
    Writing much of the code in this codebase, I've found the unnecessary
    qualification to cause quite the opposite. It is a cognitive break,
    where I wonder what is special which requires this qualification? Other
    times, it intrudes on readability, for example chaining with parameters
    ends up with `this.` boilerplated throughout the calls. In other words,
    the fluency is reduced. Minor, but this can make long chains worse, or
    cause wrapping.

    While I feel it wouldn't be in my place to ask for Spring to change its
    style, including use of tabs and `this.` qualifications, I do think it
    is reasonable to not bring this (no pun intended) here.

    The result of reverting this change, if convention isn't valuable, will
    at least be a more joyful experience for the primary contributor.