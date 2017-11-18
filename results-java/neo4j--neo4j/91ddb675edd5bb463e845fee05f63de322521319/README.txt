commit 91ddb675edd5bb463e845fee05f63de322521319
Author: Chris Gioran <chris@neotechnology.com>
Date:   Wed Apr 20 15:36:57 2016 +0300

    Adds secondary unit information in serialized NodeCommands

    Adds boolean flags related to secondary unit usage in the first
     byte serialized as part of NodeCommands, in a fashion similar to
     RelationshipCommand.
    The NodeCommandTest is improved with additional assertions.