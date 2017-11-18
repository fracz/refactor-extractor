commit 5ea4ab2bca518a3ac24dc5c0713326e8699d3fad
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Tue Nov 25 08:43:49 2008 +0000

    Adding newPrivateBinder() to the SPI. We're promoting private bindings from an extension to being a part of core Guice!

    This code isn't wired up yet, so creating injectors whose modules use this API will fail. In a follow-up CL, I'll add the implementation code and refactor the existing PrivateModule class to build on this.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@700 d779f126-a31b-0410-b53b-1d3aecad763e