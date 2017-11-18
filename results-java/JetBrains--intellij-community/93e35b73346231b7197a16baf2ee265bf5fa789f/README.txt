commit 93e35b73346231b7197a16baf2ee265bf5fa789f
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Mon Mar 27 12:35:02 2017 +0700

    ExtractSetFromComparisonChainAction improvements (IDEA-CR-19606)

    1. Fixed modifiers for interfaces
    2. Replacement wrapped with Collections.unmodifiableSet
    3. Supported Java 1.4 and lower
    4. Supported Guava ImmutableSet
    5. Supported comparisons like s.equals("xyz"), Objects.equals(s, "xyz")