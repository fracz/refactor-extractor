commit ff9041dc486adf0a8dec41f80bbfbdd49f97016a
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Jun 9 12:57:35 2015 +0200

    Transport: allow to de-serialize arbitrary objects given their name

    As part of the query refactoring, we want to be able to serialize queries by having them extend Writeable, rather than serializing their json. When reading them though, we need to be able to identify which query we have to create, based on its name.

    For this purpose we introduce a new abstraction called NamedWriteable, which is supported by StreamOutput and StreamInput through writeNamedWriteable and readNamedWriteable methods. A new NamedWriteableRegistry is introduced also where named writeable prototypes need to be registered so that we are able to retrieve the proper instance of query given its name and then de-serialize it calling readFrom against it.

    Closes #11553