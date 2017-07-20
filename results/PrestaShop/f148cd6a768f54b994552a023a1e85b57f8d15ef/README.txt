commit f148cd6a768f54b994552a023a1e85b57f8d15ef
Author: thomas-aw <thomas@amazing-web.fr>
Date:   Sat Aug 16 12:52:42 2014 +0200

    MultiShop erase available_for_order

    With the condition on multishop_check that return false, update_fields was null so system take all fields.

    11 days with this error in production, maybe tests have to be improve no ?