    public function createBillingAgrement()
    {
        //@testo:start
        //@testo:source
        //@testo:uncomment:use Payum\Core\Request\CaptureRequest;
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Api;

        $captureBillingAgreement = new CaptureRequest(array(
            'PAYMENTREQUEST_0_AMT' => 0,
            'RETURNURL' => 'http://foo.com/finishPayment',
            'CANCELURL' => 'http://foo.com/finishPayment',
            'L_BILLINGTYPE0' => Api::BILLINGTYPE_RECURRING_PAYMENTS,
            'L_BILLINGAGREEMENTDESCRIPTION0' => 'Subsribe for weather forecast',
        ));

        // ...
        //@testo:end

        $billingAgreementDetails = $captureBillingAgreement->getModel();
        $billingAgreementDetails['TOKEN'] = 'aToken';
        $billingAgreementDetails['EMAIL'] = 'foo@example.com';

        $captureBillingAgreement->setModel($billingAgreementDetails);

        return $captureBillingAgreement;
    }

    /**
     * @test
     *
     * @depends createBillingAgrement
     */
||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(
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
                        (AST_CLASS_CONST
                            (SCALAR))
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR))))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_DIM
            (AST_VAR)
            (SCALAR))
        (SCALAR))
    (AST_ASSIGN
        (AST_DIM
            (AST_VAR)
            (SCALAR))
        (SCALAR))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_VAR)))
    (AST_RETURN
        (AST_VAR)))||||||||