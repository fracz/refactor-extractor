commit 12b4abb3ce34b3281b5b18ad48e2a86403b8138b
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Wed Mar 18 15:28:43 2015 +0300

    PY-15336 Allow to preselect several module members before bulk move

    Note that because of the limitation of MoveHandlerDelegate extension
    point, caret *should be placed* on one of the desired module members.
    E.g. if it begins either on whitespace before the first element or on
    some non-movable element refactoring dialog won't be shown at all.