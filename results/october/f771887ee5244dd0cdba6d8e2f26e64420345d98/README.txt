commit f771887ee5244dd0cdba6d8e2f26e64420345d98
Author: Samuel Georges <sam@daftspunk.com>
Date:   Wed Nov 30 07:08:12 2016 +1100

    When refreshing fields, forceFill is a bit too flaky

    - Using a proxy field would populate a relation with an array of attributes, this is not good or consistent. Instead the FormModelSaver trait is used to bring the behavior in line with FormController and others. This should improve consistency in the behavior and developer expectations.

    - The noticeable difference will be that relations and their values are now set by the postback data where possible. In cases where this is undesirable -- eg: updating a relation triggers proxy fields to update with existing values, when they should seed from the relation instead (desire to lose prior changes) -- the formExtendRefreshData controller override can be used to prune the existing values from the dataset, where they will then be seeded from the model as normal.

    - Also the $data property is only rebuilt if it differs from the model. Previously it would corrupt the model-based dataset by converting to an array then back to an object. If the two objects are the same, they will be passed by reference and values will replicate this way.