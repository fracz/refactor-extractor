package com.jetbrains.python.documentation;

import java.util.List;

/**
 * @author yole
 */
public class SphinxDocString extends StructuredDocString {
  public static String[] KEYWORD_ARGUMENT_TAGS = new String[] { "keyword", "key" };

  public SphinxDocString(String docstringText) {
    super(docstringText, ":");
  }

  @Override
  public List<String> getParameters() {
    return getTagArguments(EpydocString.PARAM_TAGS);
  }

  @Override
  public List<String> getKeywordArguments() {
    return getTagArguments(KEYWORD_ARGUMENT_TAGS);
  }

  @Override
  public String getKeywordArgumentDescription(String paramName) {
    return getTagValue(KEYWORD_ARGUMENT_TAGS, paramName);
  }

  @Override
  public String getReturnType() {
    return getTagValue("rtype");
  }

  @Override
  public String getParamType(String paramName) {
    return getTagValue("type", paramName);
  }

  @Override
  public String getParamDescription(String paramName) {
    return getTagValue("param", paramName);
  }

  @Override
  public String getReturnDescription() {
    return getTagValue("return");
  }

  @Override
  public List<String> getRaisedExceptions() {
    return getTagArguments(EpydocString.RAISES_TAGS);
  }

  @Override
  public String getRaisedExceptionDescription(String exceptionName) {
    return getTagValue(EpydocString.RAISES_TAGS, exceptionName);
  }
}