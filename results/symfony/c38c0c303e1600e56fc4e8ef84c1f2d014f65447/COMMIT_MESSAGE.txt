commit c38c0c303e1600e56fc4e8ef84c1f2d014f65447
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jan 13 11:16:45 2011 +0100

    refactored Templating

     * made the renderer argument of Storage ctor mandatory
     * refactored the Engine class to avoid code duplication
     * simplified the check for a template that extends another one but with a different renderer