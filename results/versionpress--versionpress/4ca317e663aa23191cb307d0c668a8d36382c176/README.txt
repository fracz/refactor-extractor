commit 4ca317e663aa23191cb307d0c668a8d36382c176
Author: Borek Bernard <borekb@gmail.com>
Date:   Wed Nov 19 00:07:53 2014 +0100

    [#175] Change listeners in storages replaced with return values of `save()` and `delete()` methods

    Details:

     * `Storage::addChangeListener()` and `callOnChangeListeners()` removed
     * `Storage::save()` and `delete()` methods now return ChangeInfo|null
     * Storages no longer use the helper `saveEntity()` method - the work was just moved to the save() method (there was no value added in saveEntity() after the refactoring)
     * `createChangeInfo()` method which used to be internal method in various subclasses extracted to the Storage base class
     * DirectoryStorage and its subclasses no longer have a dedicated `getEditAction()` - the detection (if at all necessary) is simply done in `createChangeInfo()`
     * Inline diff computation replaced with the new `EntityUtils::getDiff()` method