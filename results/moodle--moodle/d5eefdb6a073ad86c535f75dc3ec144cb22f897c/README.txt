commit d5eefdb6a073ad86c535f75dc3ec144cb22f897c
Author: Juan Leyva <juanleyvadelgado@gmail.com>
Date:   Fri Apr 21 11:26:43 2017 +0200

    MDL-58668 mod_lesson: Fix how multi answers are processed

    The module was choosing as incorrect the first possible incorrect
    answer instead the first student incorrect answer.
    In the patch I also refactored the foreach loop to avoid code
    duplication.