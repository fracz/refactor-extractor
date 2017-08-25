commit e6b3d7a10045297b4b5fb8f2518a0984c52a9b35
Author: raveren <raveren@gmail.com>
Date:   Fri Nov 8 21:26:06 2013 +0200

    TWO NEW MAJOR FEATURES! And a few smaller ones.
     1) Cli output with colors and no HTML escaping!
     2) Calls to Kint in background ajax calls are presented straight to the screen! Currently only works if you use jQuery, but other adapters will be added in the future (contributions welcome!).
     3) Both of the above are detected automatically, so it all works out of the box. You can avoid automatic detection via the new `~` modifier (the `@` does it too now) (`~Kint::dump($var)` will dump html+css+js no matter the request type).
     4) Plain output is enhanced a *little* by HTML now and another - pure plain text mode added. The two serve different purposes: plain is for extremely-minimal-yet-readable output to the browser, whitespace-only is for storing in logs.
     5) Reorganized theme location and introduced theming for the ajax window. I know the default one looks horrible, I'm working on it! :)

    All of the new features are **stable and very usable**, but are a little more than at a proof of concept level. They all need refactoring and improving the display - and I've got it all planned out. I just want to push it out to receive as much feedback as possible - especially about annoyances I might have caused for typical use cases - as much as I worked to avoid them.