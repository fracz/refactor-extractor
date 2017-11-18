commit 8d90dbb2f57d6b07c25c3a731ccbe4d2235be11f
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Jun 26 07:32:40 2014 -0400

    Major Hadoop module refactoring

    This commit reintroduces the titan-hadoop-core module last seen in
    3a16ea2f48fbcd8bbe74884d982ef8a39e9535fd.  The core module compiles
    against the same Hadoop 2.x version as the titan-hadoop-2 module, so
    in the event of an oversight failure on our part that leaks a
    bytecode-level Hadoop1/2 incompatibility into titan-hadoop-core, only
    the titan-hadoop-1 module will break.

    This is technically inferior to the strategy used prior to this
    commit.  That approach had no titan-hadoop-core module.  The
    titan-hadoop-1 and titan-hadoop-2 modules shared each used the build
    helper plugin to add a common source directory to the build path, so
    that they had one directory containing hadoop1/2-specific sources and
    also had the common source directory.  The titan-hadoop-1 and -2
    modules compiled every source file using their target Hadoop library
    version.  This is nice because it precludes classfile linkage errors
    at runtime, but the downside is that neither Eclipse m2e nor IDEA are
    capable of comprehending this Maven configuration, rendering them
    little more than red-colored text editors on the modules in question.

    Contrast this with the approach in this commit, where
    titan-hadoop-core is compiled against a Hadoop 2 jar, and it's on us
    to keep out IncompatibleClassChange-inducing ABI incompatibilites.
    This raises the possibility of runtime classfile linkage errors, but
    the upshot is that both Eclipse and IDEA can understand it.

    This commit makes a few other changes:

    * deleted BlueprintsGraphOutputMapReduce and replaced calls to its
      static methods with equivalent static calls on
      TitanGraphOutputMapReduce

    * deleted BlueprintsGraphOutputMapReduceTest; there is some test logic
      in here that would be nice to port to Titan, but replacing
      TinkerGraph with Titan without breaking/rewriting the test seemed
      nontrivial

    * Shortened some config keys:
      titan.hadoop.graph.input.titan  -> titan.hadoop.input
      titan.hadoop.graph.output.titan -> titan.hadoop.output

    The config changes are barely tested.  The config keys are probably
    going to get tweaked again in the future to fit them into a
    ConfigNamespace and/or to avoid potential collisions with other keys
    in the titan.hadoop.graph.input prefix (e.g. "location").