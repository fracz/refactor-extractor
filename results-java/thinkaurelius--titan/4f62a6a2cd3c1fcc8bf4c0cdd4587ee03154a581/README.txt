commit 4f62a6a2cd3c1fcc8bf4c0cdd4587ee03154a581
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Thu Oct 9 06:03:55 2014 -0400

    Assembly & sample conf file templates refactoring

    This is for #743 and #762.

    This commit does a couple of related things.  I could probably pull
    them into separate commits with some effort and additional testing to
    make sure the split was clean, but they've been gradually maturing in
    my git-stash for a while as a single changeset anyway.

    1. The "filesystem" assembly descriptor, which copies files to the
       root of the repo (like bin/gremlin.sh) for testing during
       development, now just includes a component called "common" and
       nothing else.  The "archive" assembly descriptor, which builds the
       zipfiles, also includes this component, plus a second for
       html/javadocs and a third for the readme-style text files in the
       root of the archive.  This change eliminated the copy-paste
       duplication between the zipfile and root-of-dev-repo assemblies.
       The clean goal has also been updated to delete up rexhome and
       examples in the root of the repo, since the filesystem descriptor
       now includes those via the common component.  This fixes #762.

    2. The sample Titan conf files now pass through an additional
       preprocessor called ConfigurationFileFilter.  This replaces lines
       of the form #TITANCFG{key=value} with a word-wrapped multi-line
       comment including information from the ConfigOption associated with
       key, including the description, default value, data type, and
       mutability.  For mutabilities FIXED, GLOBAL_OFFLINE, and GLOBAL,
       there's also an tip to use the Management System API and reminder
       that changes to the value in the file have no effect after Titan is
       first started.  After this comment appears the actual key=value
       pair on a line by itself.

       After ConfigurationFileFilter has completely processed the input
       file and written to a separate output file, it opens and reads the
       separate output file with ConfigurationLint.  ConfigurationLint
       checks that the file parses as a PropertiesConfiguration and that
       every key is associated with a Titan ConfigOption.  Furthermore,
       this commit extends ConfigurationLint to execute the verification
       predicate defined in each ConfigOption on its associated value in
       the separate output file.  This serves as a sanity check on
       ConfigurationFileFilter.  If ConfigurationFileFilter or
       ConfigurationLint find an error and exit with nonzero status, the
       build fails.

       This addresses #743.

       I could have probably figured out how to do all this with Velocity,
       but my aims were modest and I couldn't quickly figure out how to
       extend Velocity quite this way, so I just wrote this little utility
       program.
    (cherry picked from commit a8bad29d01dd4ab1c0e25b0c25738e72f9b55e05)