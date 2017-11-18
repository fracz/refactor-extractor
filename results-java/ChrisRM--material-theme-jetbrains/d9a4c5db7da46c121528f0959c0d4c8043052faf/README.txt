commit d9a4c5db7da46c121528f0959c0d4c8043052faf
Author: tamas.kiss <kisstkondoros@gmail.com>
Date:   Sat Oct 10 13:30:59 2015 +0200

    File icon lookup mechanism refactored

    Motivation:
    The old implementation was very hard to extend
    and to be understood.
    Because of that, a configuration file has
    been introduced, which can be changed extended
    with new file associations easily.
    The lookup mechanism goes linearily, thus a simple
    ranking system has been introduced implicitly.

    The associations can be given in two ways currently:
    - RegexAssociation (regular expression pattern)
    - TypeAssociation (file type)