commit d9d68d916fbc507db0ab507919b1ca802924f73a
Author: Chris Beams <chris@gradle.com>
Date:   Fri Oct 16 10:46:40 2015 +0200

    Create API jar according to API specification

    This change ensures that a corresponding API jar is automatically
    created alongside every normal "runtime" jar, and ensures that the
    contents of the API jar include only classes declared in packages
    exported by the build script's API spec.

    If no API spec is present in the build script, an API jar is still
    created, but contains all packages present in the runtime jar. That is,
    the API and runtime jar contents will be identical. A later story will
    introduce so-called "API stripping", which will ensure that non-public
    members are removed from API jars in a safe and effective manner.

    The related design doc has been updated and implemented test cases have
    been marked complete. The test cases that remain deal with avoiding
    triggering recompilation when it is not necessary, and these will be
    addressed together in a subsequent commit.

    Implementation notes:

     - The changes made in `JvmComponentPlugin#createTasks` are non-ideal, a
       workaround to compensate for the fact that we do not yet have
       first-class "variants" at the `JvmComponentSpec` level. That is, we
       do not yet have the capability to express that a single logical JVM
       library is manifested physically in two or more binary variants (in
       this case, the API and runtime jar). This modeling and refactoring
       work is planned for and addressed by other stories intended to be
       started soon; as such, this implementation in this commit should be
       considered stopgap.

     - The use of `JvmBinaryTasks#getJar`, along with its assumptions about
       any given binary being associated with one jar and one jar only are
       now no longer valid as of this change. This constraint has been
       relaxed by replacing `#getJar` calls with `DomainObjectSet#withType`
       which allows for returning zero or more matches. The `#getJar` method
       is now used in only one place in production code
       (`ScalaLanguagePlugin`), and as such it may be best to refactor away
       this lone usage and remove remove the `#getJar` method altogether.

     - The recently-introduced `ApiSpec` and `PackageName` supporting types
       remain package-private within `org.gradle.jvm.internal` in this
       change, in favor of propagating the set of exported packages as a
       simple `Set<String>` where necessary. It may yet be the case that
       these types should be exposed, e.g. for external use by plugin
       authors, but no story has yet indicated this necessity.