commit fc9337f9efc9f52e2f54aec1af864843f4897bcf
Author: crazyboblee <crazyboblee@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Fri Jan 26 00:51:34 2007 +0000

    Added code to automatically convert between primitive types and their wrapper types. Modified SingletonScope to use double checked locking which resulted in a noticable performance improvement.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@24 d779f126-a31b-0410-b53b-1d3aecad763e