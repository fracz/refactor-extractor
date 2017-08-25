    public function createRecurringPaymnt($captureBillingAgreement)
    {
        $payment = PaymentFactory::create(new Api(new Curl, array(
            'username' => 'a_username',
            'password' => 'a_pasword',
            'signature' => 'a_signature',
            'sandbox' => true
        )));

        //@testo:start
        //@testo:source
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Api;
        //@testo:uncomment:use Payum\Paypal\ExpressCheckout\Nvp\Request\Api\CreateRecurringPaymentProfileRequest;
        //@testo:uncomment:use Payum\Core\Request\SyncRequest;

        $billingAgreementDetails = $captureBillingAgreement->getModel();

        $recurringPaymentDetails = new \ArrayObject(array(
            'TOKEN' => $billingAgreementDetails['TOKEN'],
            'PROFILESTARTDATE' => date(DATE_ATOM),
            'DESC' => $billingAgreementDetails['L_BILLINGAGREEMENTDESCRIPTION0'],
            'AMT' => 1.45,
            'CURRENCYCODE' => 'USD',
            'BILLINGPERIOD' => Api::BILLINGPERIOD_DAY,
            'BILLINGFREQUENCY' => 2,
        ));
        //@testo:end
        $recurringPaymentDetails['PROFILEID'] = 'aProfileid';
        //@testo:start

        $payment->execute(
            new CreateRecurringPaymentProfileRequest($recurringPaymentDetails)
        );
        $payment->execute(new SyncRequest($recurringPaymentDetails));

        $recurringPaymentStatus = new BinaryMaskStatusRequest($recurringPaymentDetails);
        $payment->execute($recurringPaymentStatus);

        if ($recurringPaymentStatus->isSuccess()) {
            //@testo:uncomment:echo 'We are done';
        }

        //@testo:uncomment:echo "Hmm. We are not. Let's check other possible statuses!";
        //@testo:end

        $this->assertTrue($recurringPaymentStatus->isFailed());
    }

    /**
     * @test
     *
     * @depends doCapture
     */
||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(NO_PARAM_TYPENO_PARAM_DEFAULT)
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
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_ARRAY
                    (AST_ARRAY_ELEM
                        (AST_DIM
                            (AST_VAR)
                            (SCALAR))
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (AST_CALL
                            (
                                (AST_CONST)))
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (AST_DIM
                            (AST_VAR)
                            (SCALAR))
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
        (AST_DIM
            (AST_VAR)
            (SCALAR))
        (SCALAR))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_NEW
                (
                    (AST_VAR)))))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_NEW
                (
                    (AST_VAR)))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_VAR))))
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
                (AST_VAR)))))||||||||