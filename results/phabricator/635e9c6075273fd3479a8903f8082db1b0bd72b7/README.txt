commit 635e9c6075273fd3479a8903f8082db1b0bd72b7
Author: epriestley <git@epriestley.com>
Date:   Mon Sep 21 04:41:52 2015 -0700

    Provide a generic "Datasource" StandardCustomField

    Summary:
    Ref T9253. See discussion in D13843.

    I want to let Drydock blueprints for Almanac services choose those services from a typeahead, but only list appropriate services in the typeahead. To do this:

      - Provide a StandardCustomField for an arbitrary datasource.
      - Adjust the AlmanacServiceDatasource to allow filtering by service class.

    This implementation is substantially the same as the one in D13843, with some adjustments:

      - I lifted most of the code in the `Users` standard custom field into a new `Tokenizer` standard custom field.
      - The `Users` and `Datasource` custom fields now extend the `Tokenizer` custom field and can share most of the code it uses.
      - I exposed this field fully as a configurable field. I don't think anyone will ever use it, but this generality costs us nearly nothing and improves consistency.
      - The code in D13843 didn't actually pass the parameters over the wire, since the object that responds to the request is not the same object that renders the field. Use the "parameters" mechanism in datasources to get things passed over the wire.

    Test Plan:
      - Created a custom "users" field in Maniphest and made sure it still wokred.
      - Created a custom "almanc services" field in Maniphest and selected some services for a task.
      - With additional changes from D13843, selected an appropriate Almanac service in a new Drydock blueprint.

    Reviewers: hach-que, chad

    Reviewed By: hach-que, chad

    Maniphest Tasks: T9253

    Differential Revision: https://secure.phabricator.com/D14111