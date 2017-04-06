commit c5995149b369a1e78573a4ed0da9e55f9870e5c9
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Oct 29 20:53:36 2015 -0400

    Further improve detecttion of custom CNVR

    Refine the approach of having <mvc:view-resolvers> detect and use the
    ContentNegotiationManager instance registered with
    <mvc:annotation-driven> introduced in the last commit.

    Issue: SPR-13559