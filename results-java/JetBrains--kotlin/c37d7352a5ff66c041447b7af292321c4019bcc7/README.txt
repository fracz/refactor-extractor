commit c37d7352a5ff66c041447b7af292321c4019bcc7
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Thu Dec 20 18:50:32 2012 +0400

    improved reporting TYPE_MISMATCH error for function literals

    (introduced EXPECTED_PARAMETER_TYPE_MISMATCH, EXPECTED_RETURN_TYPE_MISMATCH, EXPECTED_PARAMETERS_NUMBER_MISMATCH
    instead of reporting TYPE_MISMATCH on the whole function literal)