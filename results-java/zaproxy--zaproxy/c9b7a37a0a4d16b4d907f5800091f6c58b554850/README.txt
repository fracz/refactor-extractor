commit c9b7a37a0a4d16b4d907f5800091f6c58b554850
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Wed Jul 11 13:43:16 2012 +0000

    refactored 'filter WebSocket tab messages'-dialog to use WebSocketUiHelper
    fixed bug with long values in various channel selector
     - do not show selected value in two lines
     - show wider dropdown if values are wider than their JComboBox (added WiderDropdownJComboBox)