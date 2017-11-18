commit 0f86dc815c46f5bde00e42fc875ed0502a1fac44
Author: Googler <noreply@google.com>
Date:   Tue Apr 5 17:52:42 2016 +0000

    4.25 of 5: Writing of UnwrittenMergedAndroidData

    Introduces the AndroidDataWriter and AndroidDataWritingVisitor to abstract the io operations from the data classes.
    Necessary refactoring to the stubbed write method on DataValue in DataAsset.writeAsset and DataResource.writeResource.
    New interface for the AttributeValues to reflect the simplifications of writing Resource Attributes.

    Of special note is the fact all xml is written into a single file, values.xml. This is following the Gradle convention and aapt has demonstrated a preference of only reading a values.xml and ignoring all other xml files in the values directory. Unless profiling demonstrates an advantage to writing multiple files (which I doubt), this merger carries on this convention.

    --
    MOS_MIGRATED_REVID=119066611