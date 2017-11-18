commit a9379d0b44ca1f68a0036d2b65218e17fa348514
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Tue May 9 17:40:24 2017 -0700

    Allow null fill values to support authentication case

    If a dataset needs to be authenticated the fill service
    may not have the values but needs to tell the system for
    which fields to show the fill UI. We now allow passing
    a null value to mean the view is a part of the dataset
    semantically but its value should remain unchanged.

    If a dataset has no values, i.e. the related autofill ids
    are mapped to null, we cannot properly filter. In this case
    we always match such items regardless what the user typed.

    While at this improved accessibility support for filtering
    to announce when the number of items being filtered changes.

    Also while at this allowed a dataset authentication to return
    a response which replaces the current response and refreshes
    the UI. Matching datasets with null values to any text plus
    allowing a response to be returned from a dataset auth enables
    the use case where there is always "Import" item at the
    end of the list which when clicked can show arbitrarily more
    data entries associated to other apps.

    Another change is that we now provide the client state
    bundle on both request and dataset auth.

    Finally, this change gets rid of dataset waiting auth and
    response waiting auth concepts since the reference to the
    response and the dataset is piped with the auth request.

    Fixed a bug where the width of the autofill UI was not
    properly measured by going over all items in the adapter.
    Now we measure enough height to fit the first three and the
    width id the width of the widest item in the adapter.

    Test: Added LoginActivityTest#testDatasetAuthTwoFieldsReplaceResponse
          Added LoginActivityTest#testDatasetAuthTwoFieldsNoValues
          Added LiginActivityTest#filterTextNullValuesAlwaysMatched
          All autofill CTS tests pass

    bug:37724701
    bug:37424539

    Change-Id: Ic19e5d7cbdbb7d110c9e7da0ad60b540cbf1aecf