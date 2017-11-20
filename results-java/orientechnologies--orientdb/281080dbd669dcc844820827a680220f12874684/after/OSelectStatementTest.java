package com.orientechnologies.orient.core.sql.parser;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.testng.annotations.Test;

@Test
public class OSelectStatementTest {

  protected SimpleNode checkRightSyntax(String query) {
    return checkSyntax(query, true);
  }

  protected SimpleNode checkWrongSyntax(String query) {
    return checkSyntax(query, false);
  }

  protected SimpleNode checkSyntax(String query, boolean isCorrect) {
    OrientSql osql = getParserFor(query);
    try {
      SimpleNode result = osql.OrientGrammar();
      if (!isCorrect) {
        fail();
      }
      return result;
    } catch (Exception e) {
      if (isCorrect) {
        e.printStackTrace();
        fail();
      }
    }
    return null;
  }

  public void testSimpleSelect() {
    checkRightSyntax("select from Foo");
    checkRightSyntax("select * from Foo");

    checkWrongSyntax("select from Foo bar");
    checkWrongSyntax("select * from Foo bar");
    checkWrongSyntax("select Foo");
    checkWrongSyntax("select * Foo");

  }

  public void testSubSelect() {
    checkRightSyntax("select from (select from Foo)");

    checkWrongSyntax("select from select from foo");
  }

  public void testSimpleSelectWhere() {
    checkRightSyntax("select from Foo where name = 'foo'");
    checkRightSyntax("select * from Foo where name = 'foo'");

    checkRightSyntax("select from Foo where name = 'foo' and surname = \"bar\"");
    checkRightSyntax("select * from Foo where name = 'foo' and surname = \"bar\"");

    checkWrongSyntax("select * from Foo name = 'foo'");
    checkWrongSyntax("select from Foo bar where name = 'foo'");
    checkWrongSyntax("select * from Foo bar where name = 'foo'");
    checkWrongSyntax("select Foo where name = 'foo'");
    checkWrongSyntax("select * Foo where name = 'foo'");

  }

  public void testIn() {
    SimpleNode result = checkRightSyntax("select count(*) from OFunction where name in [\"a\"]");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }

  public void testNotIn() {
    SimpleNode result = checkRightSyntax("select count(*) from OFunction where name not in [\"a\"]");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }

  public void testNamedParam() {
    SimpleNode result = checkRightSyntax("select from JavaComplexTestClass where enumField = :enumItem");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }

  public void testDottedAtField() {
    SimpleNode result = checkRightSyntax("select from City where country.@class = 'Country'");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }

  public void testLongDotted() {
    SimpleNode result = checkRightSyntax("select from Profile where location.city.country.name = 'Spain'");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }

  public void testInIsNotAReservedWord() {
    SimpleNode result = checkRightSyntax(
"select count(*) from TRVertex where in.type() not in [\"LINKSET\"] ");
    // result.dump("    ");
    assertTrue(result.children[0] instanceof OStatement);
    OStatement stm = (OStatement) result.children[0];
    assertTrue(stm.children[0] instanceof OSelectStatement);
    OSelectStatement select = (OSelectStatement) stm.children[0];
  }



  private void printTree(String s) {
    OrientSql osql = getParserFor(s);
    try {
      SimpleNode n = osql.OrientGrammar();
      n.dump(" ");
    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

  protected OrientSql getParserFor(String string) {
    InputStream is = new ByteArrayInputStream(string.getBytes());
    OrientSql osql = new OrientSql(is);
    return osql;
  }
}