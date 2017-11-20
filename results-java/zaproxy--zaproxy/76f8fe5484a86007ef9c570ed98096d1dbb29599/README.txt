commit 76f8fe5484a86007ef9c570ed98096d1dbb29599
Author: robert.koch@loggia.at <robert.koch@loggia.at>
Date:   Thu Jun 28 12:07:14 2012 +0000

    switched from JList to JTable in WebSockets tab
    renamed & refactored WebSocketUiModel to WebSocketTableModel (no more cell renderer needed)
    fixed filtering regarding joined model
    refactored channel selector (JComboBox) - auto width, normal text plus label