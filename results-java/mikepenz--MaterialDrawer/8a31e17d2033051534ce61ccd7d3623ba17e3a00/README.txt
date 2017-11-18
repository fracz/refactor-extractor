commit 8a31e17d2033051534ce61ccd7d3623ba17e3a00
Author: Mike Penz <mikepenz@gmail.com>
Date:   Tue Jul 14 00:19:02 2015 +0200

    * add new StringHolder, ColorHolder, ImageHolder classes to simplify multiple possibilities for the same type
    ** remove setters and getters and just refer to those holders as it will simplify new types and make it easier to maintain lots of possibilities
    * continue refactoring
    * move some functionality into new classes
    * move some functions into the BaseDrawerItem