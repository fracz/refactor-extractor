commit 807a9b236963ff863573050d5aba146a9bbe23db
Author: mstaib <mstaib@google.com>
Date:   Tue Sep 19 17:06:32 2017 +0200

    LateBoundDefault: enforce access to a single fragment (or none).

    Currently, there is no way to enforce that LateBoundDefaults only access
    the fragments that they declare. This means that LateBoundDefaults can
    fail to declare fragments at all, or declare the wrong ones, and still
    have no troubles.

    But when trimming, these fragments must be declared, because otherwise
    they will not necessarily be available.

    This change refactors LateBoundDefault to declare a single fragment type,
    not a set. All existing LateBoundDefaults use sets with a single element
    or no elements at all for their set of fragment classes, so this does not
    limit anything being done currently.

    To account for LateBoundDefaults which do not use configuration at all,
    typically those which only want to access the configured attribute map,
    it is possible for Void to be the fragment class which is requested.

    To account for LateBoundDefaults which need to access methods of the
    BuildConfiguration instance itself, it is possible for BuildConfiguration
    to be the fragment class which is requested; however, this is unsafe, so
    it is only a temporary state until a way to do this without also giving
    access to all of the fragments can be added.

    Drive-by refactoring: LateBoundDefaults' values are now typed. All actual
    production LateBoundDefaults were Label or List<Label> typed, through the
    LateBoundLabel and LateBoundLabelList subclasses. These subclasses have
    been removed, and LateBoundDefault has two type parameters, one for the
    type of its input, and one for the type of its output.

    RELNOTES: None.
    PiperOrigin-RevId: 169242278