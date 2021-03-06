/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.wmi.model;

import org.jkiss.dbeaver.model.DBPIdentifierCase;
import org.jkiss.dbeaver.model.DBPKeywordType;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.sql.SQLStateType;
import org.jkiss.utils.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Info
 */
public class WMIDialect implements SQLDialect {

    public WMIDialect()
    {
    }

    @Override
    public String getDialectName() {
        return "WMI";
    }

    @Override
    public String getIdentifierQuoteString()
    {
        return "'";
    }

    @Override
    public Collection<String> getExecuteKeywords()
    {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getReservedWords() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getFunctions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getTypes() {
        return Collections.emptySet();
    }

    @Override
    public DBPKeywordType getKeywordType(String word) {
        return null;
    }

    @Override
    public List<String> getMatchedKeywords(String word) {
        return Collections.emptyList();
    }

    @Override
    public boolean isKeywordStart(String word) {
        return false;
    }

    @Override
    public boolean isEntityQueryWord(String word) {
        return false;
    }

    @Override
    public boolean isAttributeQueryWord(String word) {
        return false;
    }

    @Override
    public String getSearchStringEscape()
    {
        return "%";
    }

    @Override
    public int getCatalogUsage()
    {
        return 0;
    }

    @Override
    public int getSchemaUsage()
    {
        return 0;
    }

    @Override
    public String getCatalogSeparator()
    {
        return ".";
    }

    @Override
    public char getStructSeparator()
    {
        return '.';
    }

    @Override
    public boolean isCatalogAtStart()
    {
        return true;
    }

    @Override
    public SQLStateType getSQLStateType()
    {
        return SQLStateType.UNKNOWN;
    }

    @Override
    public String getScriptDelimiter()
    {
        return ";";
    }

    @Override
    public boolean validUnquotedCharacter(char c)
    {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }

    @Override
    public boolean supportsUnquotedMixedCase()
    {
        return true;
    }

    @Override
    public boolean supportsQuotedMixedCase()
    {
        return true;
    }

    @Override
    public boolean supportsSubqueries()
    {
        return false;
    }

    @Override
    public DBPIdentifierCase storesUnquotedCase()
    {
        return DBPIdentifierCase.MIXED;
    }

    @Override
    public DBPIdentifierCase storesQuotedCase()
    {
        return DBPIdentifierCase.MIXED;
    }

    @Override
    public Pair<String, String> getMultiLineComments() {
        return null;
    }

    @Override
    public String[] getSingleLineComments() {
        return new String[] { "--" };
    }
}