commit e31e06d92a7e2903c5d34adb019bd401117b6f79
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Aug 2 10:26:01 2013 +0200

    changes related to REVIEW-2792

    - renamed ComponentMetaDataDetails to ComponentMetadataDetails
    - replace ComponentMetadataDetails.getGroup()/getName()/getVersion() with getId()
    - renamed project.modules to project.componentMetadata
    - renamed some members that still had `moduleHandler` in their name
    - Javadoc improvements