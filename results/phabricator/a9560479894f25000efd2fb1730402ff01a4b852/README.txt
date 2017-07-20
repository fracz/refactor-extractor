commit a9560479894f25000efd2fb1730402ff01a4b852
Author: epriestley <git@epriestley.com>
Date:   Thu Nov 24 08:13:38 2016 -0800

    Use PhutilQueryCompiler in Phabricator fulltext search

    Summary:
    Ref T11741. Fixes T10642. Parse and compile user queries with a consistent ruleset, then submit queries to the backend using whatever ruleset MySQL is configured with.

    This means that `ft_boolean_syntax` no longer needs to be configured (we'll just do the right thing in all cases).

    This should improve behavior with RDS immediately (T10642), and allow us to improve behavior with InnoDB in the future (T11741).

    Test Plan:
      - Ran various queries in the UI, saw the expected results.
      - Ran bad queries, got useful errors.
      - Searched threads in Conpherence.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10642, T11741

    Differential Revision: https://secure.phabricator.com/D16939