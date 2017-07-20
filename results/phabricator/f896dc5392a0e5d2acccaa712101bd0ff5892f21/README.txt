commit f896dc5392a0e5d2acccaa712101bd0ff5892f21
Author: epriestley <git@epriestley.com>
Date:   Thu May 22 10:47:00 2014 -0700

    Put a cache in front of Celerity transforms, and update packages

    Summary:
    Fixes T5094. In some cases we do slightly expensive transformations to resources (inlining images, replacing URIs, building packages). We can throw cache in front of them easily since URIs are already permanently associated with a single resource.

    Also browse around and move some CSS/JS into packages.

    Test Plan:
    Added logging to verify the caches are working, saw moderately improved performance.

    Browsed around looking at resources tab in developer console, saw fewer total requests.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T5094

    Differential Revision: https://secure.phabricator.com/D9175