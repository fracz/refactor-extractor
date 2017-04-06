commit 15d3d74444ede63651f5ef10dd172da5293529ad
Merge: dcd2642 d3d57da
Author: Jason Tedor <jason@tedor.me>
Date:   Sat May 14 17:33:52 2016 -0400

    Merge branch 'master' into feature/seq_no

    * master: (904 commits)
      Removes unused methods in the o/e/common/Strings class.
      Add note regarding thread stack size on Windows
      painless: restore accidentally removed test
      Documented fuzzy_transpositions in match query
      Add not-null precondition check in BulkRequest
      Build: Make run task you full zip distribution
      Build: More pom generation improvements
      Add test for wrong array index
      Take return type from "after" field.
      painless: build descriptor of array and field load/store in code; fix array index to adapt type not DEF
      Build: Add developer info to generated pom files
      painless: improve exception stacktraces
      painless: Rename the dynamic call site factory to DefBootstrap and make the inner class very short (PIC = Polymorphic Inline Cache)
      Remove dead code.
      Avoid race while retiring executors
      Allow only a single extension for a scripting engine
      Adding REST tests to ensure key_as_string behavior stays consistent
      [test] Set logging to 11 on reindex test
      [TEST] increase logger level until we know what is going on
      Don't allow `fuzziness` for `multi_match` types cross_fields, phrase and phrase_prefix
      ...