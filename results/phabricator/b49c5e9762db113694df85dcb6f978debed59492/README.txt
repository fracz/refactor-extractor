commit b49c5e9762db113694df85dcb6f978debed59492
Author: epriestley <git@epriestley.com>
Date:   Tue Jun 14 07:55:04 2011 -0700

    "Merge Duplicates" in Maniphest

    Summary:
    Allow duplicate tasks to be selected and merged in Maniphest.

    I didn't create a separate transaction type for this because that implies a
    bunch of really complicated rules which I don't want to sort out right now
    (e.g., do we need to do cycle detection for merges? If so, what do we do when we
    detect a cycle?) since I think it's unnecessary to get right for the initial
    implementation (my Tasks merge implementation was similar to this and worked
    quite well) and if/when we eventually need the metadata to be available in a
    computer-readable form that need should inform the implementation.

    Plenty of room for improvement here, of course.

    Test Plan:
    Merged duplicate tasks, tried to perform invalid merge operations (e.g., merge a
    task into itself).
    Tested existing attach workflows (task -> revision, revision -> task).

    Reviewed By: aran
    Reviewers: tuomaspelkonen, jungejason, aran
    CC: anjali, aran
    Differential Revision: 459