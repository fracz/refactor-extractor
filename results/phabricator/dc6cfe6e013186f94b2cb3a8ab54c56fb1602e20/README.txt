commit dc6cfe6e013186f94b2cb3a8ab54c56fb1602e20
Author: Afaque Hussain <Afaque.Hussain@outlook.com>
Date:   Fri Apr 19 11:40:13 2013 -0700

    Created PhabricatorExternalAccount Class and Sql patch to create an external_account table.

    Summary: Created PhabricatorExternalAccount class with only data members. Will discuss with you regarding the necessary functions to be implemented in this class. Sql Patch to create a new table for external_accounts. Will I have to write unit tests the new storage object? Sending you this diff so that you can comment on this to further improve :).

    Test Plan: {F40977}

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Korvin, AnhNhan

    Maniphest Tasks: T1536, T1205

    Differential Revision: https://secure.phabricator.com/D5724