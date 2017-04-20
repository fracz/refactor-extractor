commit 5a7bd964b4265a0e0e87e453fa0d1a4ccb604fe1
Author: Ben Alpert <balpert@fb.com>
Date:   Fri Aug 7 19:06:44 2015 -0700

    Minimal implementation of stateless components

    Stateless pure-function components give us more opportunity to make performance optimizations. For now, we'll do a minimal implementation which has similar performance characteristics to other components in the interests of shipping 0.14 and allowing people to begin writing code using this pattern; in the future we can refactor to allocate less and avoid other unnecessary work.