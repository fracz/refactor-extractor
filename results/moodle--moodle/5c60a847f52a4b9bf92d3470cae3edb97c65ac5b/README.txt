commit 5c60a847f52a4b9bf92d3470cae3edb97c65ac5b
Author: sam marshall <s.marshall@open.ac.uk>
Date:   Wed Apr 13 14:28:23 2011 +0100

    MDL-26647 (1) 'extrauserselectorfields' -> 'showuseridentity', add capability

    This change:
    - Renames the existing setting 'extrauserselectorfields' to 'showuseridentity'
      in preparation for using it in more places. (Upgrade change, new version.)
    - Adds a new capability moodle/site:viewuseridentity, now required in order
      to see the extra fields; if you don't have the capability, you don't see them
    - Slightly improves the display of extra fields in user selector list; it used
      to be like 'sam marshall, 01234567, email@address' and is now
      'sam marshall [01234567, email@address]' ie the fields are in square
      brackets
    - Turns feature on for the group selector - the feature was enabled for other
      user selectors but not for the group selector. Tim did the disable code, he
      thinks this may be to do with more people having access to group selector -
      probably not a problem now it is controlled by capability.