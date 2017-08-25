commit 0d06b6fda771f5369509361ec46dce67a1b9c398
Author: mjollnir_ <mjollnir_>
Date:   Thu Sep 11 13:42:58 2008 +0000

    MDL-16423 - big refactor of the way callers interact with the portfolio code

    added two new contract methods to the caller class, load_data and expected_callbackargs (static)
    this means that the base class is the only place that needs a constructor
    and that no data loading happens in the constructor
    this in turn means we can check callback argument validity much more lightly

    also completely remoted portfolio_add_button function and replaced with a class
    as the argument list was getting out of control.  it's now much more readable.