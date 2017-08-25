    public function doDigitalGoodsCapture()
    {
        //@testo:source

        $capture = new CaptureRequest(array(

            // ...

            'NOSHIPPING' => Api::NOSHIPPING_NOT_DISPLAY_ADDRESS,
            'REQCONFIRMSHIPPING' => Api::REQCONFIRMSHIPPING_NOT_REQUIRED,
            'L_PAYMENTREQUEST_0_ITEMCATEGORY0' => Api::PAYMENTREQUEST_ITERMCATEGORY_DIGITAL,
            'L_PAYMENTREQUEST_0_NAME0' => 'Awesome e-book',
            'L_PAYMENTREQUEST_0_DESC0' => 'Great stories of America.',
            'L_PAYMENTREQUEST_0_AMT0' => 10,
            'L_PAYMENTREQUEST_0_QTY0' => 1,
            'L_PAYMENTREQUEST_0_TAXAMT0' => 2,
        ));
    }

    /**
     * @test
     *
     * @depends doCapture
     */
||||||||||||||||HAS_DOC_COMMENTNO_RETURN_TYPE
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW
            (
                (AST_ARRAY
                    (AST_ARRAY_ELEM
                        (AST_CLASS_CONST
                            (SCALAR))
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (AST_CLASS_CONST
                            (SCALAR))
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (AST_CLASS_CONST
                            (SCALAR))
                        (SCALAR))
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
                        (SCALAR))
                    (AST_ARRAY_ELEM
                        (SCALAR)
                        (SCALAR)))))))||||||||