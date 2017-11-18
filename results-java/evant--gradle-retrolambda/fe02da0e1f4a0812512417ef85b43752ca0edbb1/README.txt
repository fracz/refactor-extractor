commit fe02da0e1f4a0812512417ef85b43752ca0edbb1
Author: Evan Tatarka <evan@tatarka.me>
Date:   Thu Apr 30 21:59:11 2015 -0400

    Major refactoring of android plugin.

    The method for modifying the javaCompile task is now *way* less hackey. In fact,
    it is much closer to the original way that it was done. I had originally
    abandoned this approach because it was breaking increment compilation is some
    weird ways. However, I think I have solved it.

    What this means for you: It is now less fickle of plugin application order and
    way less likely to break other plugins.

    Note: I also threw in the fix for #101