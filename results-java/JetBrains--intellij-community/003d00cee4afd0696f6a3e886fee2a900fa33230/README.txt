commit 003d00cee4afd0696f6a3e886fee2a900fa33230
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Wed Jan 29 18:53:13 2014 +0400

    PY-11968 "Pull member up" refactoring should not allow user to move member up if parent already has one
    PY-11290 Target classes of "Pull Members Up" refactoring should be sorted in ancestry order
    PY-11289 "Pull Members Up" should not hoist code into python stubs/skeletons.
    PY-11288 Better default for target of "Pull Members Up" refactoring