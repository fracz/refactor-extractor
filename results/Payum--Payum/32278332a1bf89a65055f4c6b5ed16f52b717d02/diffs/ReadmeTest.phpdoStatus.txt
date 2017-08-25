    public function doStatus(array $arguments)
    {
        $payment = $arguments[0];
        $capture = $arguments[1];

        $model = $capture->getModel();

        unset($model['L_ERRORCODE0']);
        $model['CHECKOUTSTATUS'] = Api::CHECKOUTSTATUS_PAYMENT_COMPLETED;
        $model['PAYMENTREQUEST_0_PAYMENTSTATUS'] = Api::PAYMENTSTATUS_COMPLETED;

        //@testo:start
        //@testo:source
        //@testo:uncomment:use Payum\Core\Request\BinaryMaskStatusRequest;

        $status = new BinaryMaskStatusRequest($capture->getModel());
        $payment->execute($status);

        if ($status->isSuccess()) {
            //@testo:uncomment:echo 'We are done';
        }

        //@testo:uncomment:echo "Hmm. We are not. Let's check other possible statuses!";
        //@testo:end
        $this->assertTrue($status->isSuccess());
    }

    /**
     * @test
     */
||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(PARAM_TYPENO_PARAM_DEFAULT)
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_DIM
            (AST_VAR)
            (SCALAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_DIM
            (AST_VAR)
            (SCALAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_UNSET
        (AST_DIM
            (AST_VAR)
            (SCALAR)))
    (AST_ASSIGN
        (AST_DIM
            (AST_VAR)
            (SCALAR))
        (AST_CLASS_CONST
            (SCALAR)))
    (AST_ASSIGN
        (AST_DIM
            (AST_VAR)
            (SCALAR))
        (AST_CLASS_CONST
            (SCALAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_METHOD_CALL
                    (AST_VAR)))))
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