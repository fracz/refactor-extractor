commit 9be6016c963a45f890711f90bf353b7a644285a2
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Mar 8 15:05:14 2016 +0100

    Collapses ComposableRecordStore into CommonAbstractStore

    because CommonAbstractStore already had knowledge about RecordFormat
    at this point and so could implement the (previously) abstract methods
    on its own. This results in shallower inheritance in store classes
    and may make JITs job easier.

    As a side effect MetaDataStore had to be changed to act as a real
    record store in that it now has a proper MetaDataRecordFormat.
    It still have the static record accessors, but that can be improved later.