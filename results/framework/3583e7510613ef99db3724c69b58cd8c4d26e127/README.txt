commit 3583e7510613ef99db3724c69b58cd8c4d26e127
Author: Jeroen Noten <jeroennoten@me.com>
Date:   Fri Oct 16 12:13:24 2015 +0200

    Make formatErrors() adhere to contracts

    This change makes the implementation of `FormRequest::formatErrors()` method adhere to the `Validator` and `MessageBag` contracts. Note that the behavior is exactly the same, because `getMessageBag()` is just an alias for `errors()` and the same holds for `toArray()` and `getMessages()`. However, `errors()` and `getMessages()` are not in the contracts, while the method typehints the `Validator` interface. So without this change, if you would want to swap the implementation of `Validator` or `MessageBag` to a custom one, `FormRequest` breaks.

    This change also improves IDE navigation.