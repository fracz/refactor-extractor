  public function render() {
    $readme_path = $this->getPath();
    $readme_name = basename($readme_path);
    $interpreter = $this->getReadmeLanguage($readme_name);

    $content = $this->getContent();

    $class = null;
    switch ($interpreter) {
      case 'remarkup':
        // TODO: This is sketchy, but make sure we hit the markup cache.
        $markup_object = id(new PhabricatorMarkupOneOff())
          ->setEngineRuleset('diffusion-readme')
          ->setContent($content);
        $markup_field = 'default';

        $content = id(new PhabricatorMarkupEngine())
          ->setViewer($this->getUser())
          ->addObject($markup_object, $markup_field)
          ->process()
          ->getOutput($markup_object, $markup_field);

        $engine = $markup_object->newMarkupEngine($markup_field);
        $toc = PhutilRemarkupHeaderBlockRule::renderTableOfContents($engine);
        if ($toc) {
          $toc = phutil_tag_div(
            'phabricator-remarkup-toc',
            array(
              phutil_tag_div(
                'phabricator-remarkup-toc-header',
                pht('Table of Contents')),
              $toc,
            ));
          $content = array($toc, $content);
        }

        $readme_content = $content;
        $class = 'phabricator-remarkup';
        break;
      case 'rainbow':
        $content = id(new PhutilRainbowSyntaxHighlighter())
          ->getHighlightFuture($content)
          ->resolve();
        $readme_content = phutil_escape_html_newlines($content);

        require_celerity_resource('syntax-highlighting-css');
        $class = 'remarkup-code';
        break;
      default:
      case 'text':
        $readme_content = phutil_escape_html_newlines($content);
        break;
    }

    $readme_content = phutil_tag(
      'div',
      array(
        'class' => $class,
      ),
      $readme_content);

    $box = new PHUIBoxView();
    $box->appendChild($readme_content);
    $box->addPadding(PHUI::PADDING_LARGE);

    $object_box = id(new PHUIObjectBoxView())
      ->setHeaderText($readme_name)
      ->appendChild($box);

    return $object_box;
  }

}||||||||  public function render() {
    $readme_path = $this->getPath();
    $readme_name = basename($readme_path);
    $interpreter = $this->getReadmeLanguage($readme_name);
    require_celerity_resource('diffusion-readme-css');

    $content = $this->getContent();

    $class = null;
    switch ($interpreter) {
      case 'remarkup':
        // TODO: This is sketchy, but make sure we hit the markup cache.
        $markup_object = id(new PhabricatorMarkupOneOff())
          ->setEngineRuleset('diffusion-readme')
          ->setContent($content);
        $markup_field = 'default';

        $content = id(new PhabricatorMarkupEngine())
          ->setViewer($this->getUser())
          ->addObject($markup_object, $markup_field)
          ->process()
          ->getOutput($markup_object, $markup_field);

        $engine = $markup_object->newMarkupEngine($markup_field);
        $toc = PhutilRemarkupHeaderBlockRule::renderTableOfContents($engine);
        if ($toc) {
          $toc = phutil_tag_div(
            'phabricator-remarkup-toc',
            array(
              phutil_tag_div(
                'phabricator-remarkup-toc-header',
                pht('Table of Contents')),
              $toc,
            ));
          $content = array($toc, $content);
        }

        $readme_content = $content;
        $class = null;
        break;
      case 'rainbow':
        $content = id(new PhutilRainbowSyntaxHighlighter())
          ->getHighlightFuture($content)
          ->resolve();
        $readme_content = phutil_escape_html_newlines($content);

        require_celerity_resource('syntax-highlighting-css');
        $class = 'remarkup-code ml';
        break;
      default:
      case 'text':
        $readme_content = phutil_escape_html_newlines($content);
        $class = 'ml';
        break;
    }

    $readme_content = phutil_tag_div($class, $readme_content);
    $header = id(new PHUIHeaderView())
      ->setHeader($readme_name);

    return id(new PHUIDocumentView())
      ->setFluid(true)
      ->appendChild($readme_content)
      ->addClass('diffusion-readme-view')
      ->setHeader($header)
      ->setFontKit(PHUIDocumentView::FONT_SOURCE_SANS);
  }

}||||||||NO_DOC_COMMENTNO_RETURN_TYPE
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CALL
            (
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)
            (
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CONST))
    (AST_SWITCH
        (AST_VAR)
        (AST_SWITCH_LIST
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_CALL
                                    (
                                        (AST_NEW)))
                                (
                                    (SCALAR)))
                            (
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_METHOD_CALL
                                    (AST_METHOD_CALL
                                        (AST_CALL
                                            (
                                                (AST_NEW)))
                                        (
                                            (AST_METHOD_CALL
                                                (AST_VAR))))
                                    (
                                        (AST_VAR)
                                        (AST_VAR))))
                            (
                                (AST_VAR)
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_VAR)
                            (
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_STATIC_CALL
                            (
                                (AST_VAR))))
                    (AST_IF
                        (AST_IF_ELEM
                            (AST_VAR)
                            (
                                (AST_ASSIGN
                                    (AST_VAR)
                                    (AST_CALL
                                        (
                                            (SCALAR)
                                            (AST_ARRAY
                                                (AST_ARRAY_ELEM
                                                    (AST_CALL
                                                        (
                                                            (SCALAR)
                                                            (AST_CALL
                                                                (
                                                                    (SCALAR)))))
                                                    (NULL))
                                                (AST_ARRAY_ELEM
                                                    (AST_VAR)
                                                    (NULL))))))
                                (AST_ASSIGN
                                    (AST_VAR)
                                    (AST_ARRAY
                                        (AST_ARRAY_ELEM
                                            (AST_VAR)
                                            (NULL))
                                        (AST_ARRAY_ELEM
                                            (AST_VAR)
                                            (NULL)))))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_VAR))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_BREAK
                        (NULL))))
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_CALL
                                    (
                                        (AST_NEW)))
                                (
                                    (AST_VAR)))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_CALL
                            (
                                (AST_VAR))))
                    (AST_CALL
                        (
                            (SCALAR)))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_BREAK
                        (NULL))))
            (AST_SWITCH_CASE
                (NULL))
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_CALL
                            (
                                (AST_VAR))))
                    (AST_BREAK
                        (NULL))))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CALL
            (
                (SCALAR)
                (AST_ARRAY
                    (AST_ARRAY_ELEM
                        (AST_VAR)
                        (SCALAR)))
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_NEW))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_VAR)))
    (AST_METHOD_CALL
        (AST_VAR)
        (
            (AST_CLASS_CONST
                (SCALAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_METHOD_CALL
                (AST_CALL
                    (
                        (AST_NEW)))
                (
                    (AST_VAR)))
            (
                (AST_VAR))))
    (AST_RETURN
        (AST_VAR)))||||||||NO_DOC_COMMENTNO_RETURN_TYPE
(
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CALL
            (
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)
            (
                (AST_VAR))))
    (AST_CALL
        (
            (SCALAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_VAR)))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CONST))
    (AST_SWITCH
        (AST_VAR)
        (AST_SWITCH_LIST
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_CALL
                                    (
                                        (AST_NEW)))
                                (
                                    (SCALAR)))
                            (
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_METHOD_CALL
                                    (AST_METHOD_CALL
                                        (AST_CALL
                                            (
                                                (AST_NEW)))
                                        (
                                            (AST_METHOD_CALL
                                                (AST_VAR))))
                                    (
                                        (AST_VAR)
                                        (AST_VAR))))
                            (
                                (AST_VAR)
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_VAR)
                            (
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_STATIC_CALL
                            (
                                (AST_VAR))))
                    (AST_IF
                        (AST_IF_ELEM
                            (AST_VAR)
                            (
                                (AST_ASSIGN
                                    (AST_VAR)
                                    (AST_CALL
                                        (
                                            (SCALAR)
                                            (AST_ARRAY
                                                (AST_ARRAY_ELEM
                                                    (AST_CALL
                                                        (
                                                            (SCALAR)
                                                            (AST_CALL
                                                                (
                                                                    (SCALAR)))))
                                                    (NULL))
                                                (AST_ARRAY_ELEM
                                                    (AST_VAR)
                                                    (NULL))))))
                                (AST_ASSIGN
                                    (AST_VAR)
                                    (AST_ARRAY
                                        (AST_ARRAY_ELEM
                                            (AST_VAR)
                                            (NULL))
                                        (AST_ARRAY_ELEM
                                            (AST_VAR)
                                            (NULL)))))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_VAR))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_CONST))
                    (AST_BREAK
                        (NULL))))
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_METHOD_CALL
                            (AST_METHOD_CALL
                                (AST_CALL
                                    (
                                        (AST_NEW)))
                                (
                                    (AST_VAR)))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_CALL
                            (
                                (AST_VAR))))
                    (AST_CALL
                        (
                            (SCALAR)))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_BREAK
                        (NULL))))
            (AST_SWITCH_CASE
                (NULL))
            (AST_SWITCH_CASE
                (SCALAR)
                (
                    (AST_ASSIGN
                        (AST_VAR)
                        (AST_CALL
                            (
                                (AST_VAR))))
                    (AST_ASSIGN
                        (AST_VAR)
                        (SCALAR))
                    (AST_BREAK
                        (NULL))))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_CALL
            (
                (AST_VAR)
                (AST_VAR))))
    (AST_ASSIGN
        (AST_VAR)
        (AST_METHOD_CALL
            (AST_CALL
                (
                    (AST_NEW)))
            (
                (AST_VAR))))
    (AST_RETURN
        (AST_METHOD_CALL
            (AST_METHOD_CALL
                (AST_METHOD_CALL
                    (AST_METHOD_CALL
                        (AST_METHOD_CALL
                            (AST_CALL
                                (
                                    (AST_NEW)))
                            (
                                (AST_CONST)))
                        (
                            (AST_VAR)))
                    (
                        (SCALAR)))
                (
                    (AST_VAR)))
            (
                (AST_CLASS_CONST
                    (SCALAR))))))