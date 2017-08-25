    public function doCapture()
    {
        //@testo:start
        //@testo:source
        //@testo:uncomment:use Buzz\Client\Curl;
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Api;
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\PaymentFactory;
        //@testo:uncomment:use Payum\Core\Request\CaptureRequest;
        //@testo:uncomment:use Payum\Core\Request\RedirectUrlInteractiveRequest;

        $payment = PaymentFactory::create(new Api(new Curl, array(
            'username' => 'a_username',
            'password' => 'a_pasword',
            'signature' => 'a_signature',
            'sandbox' => true
        )));

        $capture = new CaptureRequest(array(
            'PAYMENTREQUEST_0_AMT' => 10,
            'PAYMENTREQUEST_0_CURRENCY' => 'USD',
            'RETURNURL' => 'http://foo.com/finishPayment',
            'CANCELURL' => 'http://foo.com/finishPayment',
        ));

        if ($interactiveRequest = $payment->execute($capture, $expectsInteractive = true)) {
            //save your models somewhere.
            if ($interactiveRequest instanceof RedirectUrlInteractiveRequest) {
                header('Location: '.$interactiveRequest->getUrl());
                exit;
            }

            throw $interactiveRequest;
        }
        //@testo:end

        $model = $capture->getModel();

        $this->assertArrayHasKey('L_LONGMESSAGE0', $model);
        $this->assertEquals('Security header is not valid', $model['L_LONGMESSAGE0']);

        return array(
            $payment,
            $capture
        );
    }

    /**
     * @test
     */
||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_STATIC_CALL
            (
                (AST_NEW
                    (
                        (AST_NEW)
                        (AST_ARRAY
                            (AST_ARRAY_ELEM
                                (SCALAR)
                                (SCALAR))
                            (AST_ARRAY_ELEM
                                (SCALAR)
                                (SCALAR))
                            (AST_ARRAY_ELEM
                                (SCALAR)
                                (SCALAR))
                            (AST_ARRAY_ELEM
                                (AST_CONST)
                                (SCALAR))))))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_ARRAY
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR))))))
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
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (SCALAR)
            (AST_VAR)))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (SCALAR)
            (AST_DIM
                (AST_VAR)
                (SCALAR))))
    (AST_RETURN
        (AST_ARRAY
            (AST_ARRAY_ELEM
                (AST_VAR)
                (NULL))
            (AST_ARRAY_ELEM
                (AST_VAR)
                (NULL)))))||||||||