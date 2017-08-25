    public function doCaptureAwesomeCart(array $arguments)
    {
        $payment = $arguments[0];

        //@testo:start
        //@testo:source
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Examples\Model\AwesomeCart;
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Examples\Action\CaptureAwesomeCartAction;

        //...

        $cart = new AwesomeCart;

        $payment->addAction(new CaptureAwesomeCartAction);

        $capture = new CaptureRequest($cart);
        if ($interactiveRequest = $payment->execute($capture, $expectsInteractive = true)) {
            if ($interactiveRequest instanceof RedirectUrlInteractiveRequest) {
                header('Location: '.$interactiveRequest->getUrl());
                exit;
            }

            throw $interactiveRequest; //unexpected request
        }

        $status = new BinaryMaskStatusRequest($capture->getModel()->getPaymentDetails());
        $payment->execute($status);

        if ($status->isSuccess()) {
            //@testo:uncomment:echo 'We are done';
        }

        //@testo:uncomment:echo "Hmm. We are not. Let's check other possible statuses!";
        //@testo:end
        $this->assertTrue($status->isFailed());

        $paymentDetails = $status->getModel();
        $this->assertEquals('Security header is not valid', $paymentDetails['L_LONGMESSAGE0']);
    }
}||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(PARAM_TYPENO_PARAM_DEFAULT)
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_DIM
            (AST_VAR)
            (SCALAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_NEW)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_VAR))))
    (AST_IF
        (AST_IF_ELEM
            (AST_ASSIGN
                (AST_VAR)
                (AST_METHOD_CALL
                    (AST_VAR)
                    (
                        (AST_VAR)
                        (AST_ASSIGN
                            (AST_VAR)
                            (AST_CONST)))))
            (
                (AST_IF
                    (AST_IF_ELEM
                        (AST_INSTANCEOF
                            (AST_VAR))
                        (
                            (AST_CALL
                                (
                                    (AST_BINARY_OP
                                        (SCALAR)
                                        (AST_METHOD_CALL
                                            (AST_VAR)))))
                            (AST_EXIT
                                (NULL)))))
                (AST_THROW
                    (AST_VAR)))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_METHOD_CALL
                    (AST_METHOD_CALL
                        (AST_VAR))))))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_VAR)))
    (AST_IF
        (AST_IF_ELEM
            (AST_METHOD_CALL
                (AST_VAR))))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_METHOD_CALL
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (SCALAR)
            (AST_DIM
                (AST_VAR)
                (SCALAR)))))||||||||