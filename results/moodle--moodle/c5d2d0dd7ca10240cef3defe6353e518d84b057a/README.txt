commit c5d2d0dd7ca10240cef3defe6353e518d84b057a
Author: jerome <jerome>
Date:   Thu Jul 31 06:01:12 2008 +0000

    MDL-15402 implementation of repositories administration. Administrator can hide/show/sort/setup repositories. This code has been commited in order to be used by FilePicker.
    Because it's doing the same thing than the filter manager, the code is similar. Some generic code need to be done (filter and repository code should use the same function, and the current generic filter lib should be refactor, at least their name). Optimization of original code should also be done if necessary.