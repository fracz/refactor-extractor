commit 66cd7b8ecca8123c41c1a8eee96b0409985b40f4
Author: Juan Leyva <juanleyvadelgado@gmail.com>
Date:   Wed Jan 18 16:34:28 2017 +0100

    MDL-57693 mod_lesson: Refactor to move code to lesson methods

    This is a big refactor to avoid code duplication in the new WS.
    Basically I’ve moved code from the page and renderer to the lesson
    class.
    New code is into methods, there are no code functionality changes (just
    minor fixes in comments).