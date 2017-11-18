commit fdf07bb9c3d39226f9e15023a64acff3e5e87c49
Author: Irina.Chernushina <irina.chernushina@jetbrains.com>
Date:   Mon Oct 9 18:34:18 2017 +0200

    json schema annotation: add a check for annotated element type

    - significantly improves performance for big files, because less matching
    schema searches are done