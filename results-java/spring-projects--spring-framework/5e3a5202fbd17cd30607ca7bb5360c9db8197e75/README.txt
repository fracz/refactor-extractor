commit 5e3a5202fbd17cd30607ca7bb5360c9db8197e75
Author: Keith Donald <kdonald@vmware.com>
Date:   Tue Jun 7 02:51:44 2011 +0000

    restored TypeDescriptor getElementType, getMapKeyType, and getMapValueType compatibility; StringToCollection and Array Converters are now conditional and check targetElementType if present; TypeDesciptor#isAssignable no longer bothers with element type and map key/value types in checking assignability for consistency elsewhere; improved javadoc