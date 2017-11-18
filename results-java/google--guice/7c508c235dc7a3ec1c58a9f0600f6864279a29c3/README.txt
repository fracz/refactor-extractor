commit 7c508c235dc7a3ec1c58a9f0600f6864279a29c3
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Thu Feb 19 02:56:27 2009 +0000

    Initial support for building an AOP-free copy of the Guice source tree from the standard source tree.

    The new build.xml creates a complete copy of the entire source tree in build/no_aop, stripping the lines between these tags:
      /*if[AOP]*/
      /*end[AOP]*/
    Currently I'm only removing Binder#removeInterceptor(), but in a follow-up CL I'll fix it so that all bytecode-generation code is stripped out. I'll probably refactor the standard code so this can be done with minimal impact.

    With this strategy we produce a full Guice source tree, which we can then use to run testcases on, do builds, etc. The entire approach is similar to the one we used with Glazed Lists to erase generics.

    The stripping builds on Tom Ball's Munge.java program, with a few alterations:
     - we're a bit more careful about handling /* in end-of-line comments and Strings
     - I wrote an Ant task called MungeTask that supports munging an entire fileset
    http://weblogs.java.net/blog/tball/archive/2006/09/munge_swings_se.html

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@850 d779f126-a31b-0410-b53b-1d3aecad763e