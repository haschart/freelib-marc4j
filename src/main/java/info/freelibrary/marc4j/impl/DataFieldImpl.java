/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package info.freelibrary.marc4j.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.marc4j.marc.DataField;
import org.marc4j.marc.IllegalAddException;
import org.marc4j.marc.InvalidMARCException;
import org.marc4j.marc.Subfield;

/**
 * DataField defines behavior for a data field (tag 010-999).
 * <p>
 * Data fields are variable fields identified by tags beginning with ASCII numeric values other than two zero's. Data
 * fields contain indicators, subfield codes, data and a field terminator.
 *
 * @author Bas Peters
 * @author Kevin S. Clarke <ksclarke@gmail.com>
 * @author Tod Olson, University of Chicago
 */
public class DataFieldImpl extends VariableFieldImpl implements DataField {

    private static final long serialVersionUID = -1804044736102463060L;

    private char myFirstInd;

    private char mySecondInd;

    private final List<Subfield> mySubfields = new ArrayList<Subfield>();

    /**
     * Creates a new <code>DataField</code>.
     */
    DataFieldImpl() {
    }

    /**
     * Creates a new <code>DataField</code> and sets the tag name and the first and second indicator.
     *
     * @param aTag The tag name
     * @param aFirstInd The first indicator
     * @param aSecondInd The second indicator
     */
    DataFieldImpl(final String aTag, final char aFirstInd, final char aSecondInd) {
        setTag(aTag);
        setIndicator1(aFirstInd);
        setIndicator2(aSecondInd);
    }

    /**
     * Sets the tag of a <code>DataField</code>.
     *
     * @param aTag The tag of a <code>DataField</code>
     */
    @Override
    public void setTag(final String aTag) {
        super.setTag(aTag);

        if (aTag.length() == 3) {
            try {
                if (Integer.parseInt(aTag) < 10) {
                    throw new InvalidMARCException(aTag + " is not a valid DataField tag");
                }
            } catch (final NumberFormatException details) {
                throw new InvalidMARCException(aTag + " is not a number");
            }
        } else {
            throw new InvalidMARCException(aTag + " is not a three digit tag");
        }
    }

    /**
     * Sets the field's first indicator.
     *
     * @param aFirstInd The first indicator
     */
    @Override
    public void setIndicator1(final char aFirstInd) {
        myFirstInd = aFirstInd;
    }

    /**
     * Returns the field's first indicator.
     *
     * @return The field's first indicator
     */
    @Override
    public char getIndicator1() {
        return myFirstInd;
    }

    /**
     * Sets the field's second indicator.
     *
     * @param aSecondInd The field's second indicator
     */
    @Override
    public void setIndicator2(final char aSecondInd) {
        mySecondInd = aSecondInd;
    }

    /**
     * Returns the field's second indicator
     *
     * @return The field's second indicator
     */
    @Override
    public char getIndicator2() {
        return mySecondInd;
    }

    /**
     * Adds a <code>Subfield</code>.
     *
     * @param aSubfield The <code>Subfield</code> of a <code>DataField</code>
     * @throws IllegalAddException when the parameter is not an instance of <code>SubfieldImpl</code>
     */
    @Override
    public void addSubfield(final Subfield aSubfield) {
        if (aSubfield instanceof SubfieldImpl) {
            mySubfields.add(aSubfield);
        } else {
            throw new IllegalAddException("Supplied Subfield isn't an instance of SubfieldImpl");
        }
    }

    /**
     * Inserts a <code>Subfield</code> at the specified position.
     *
     * @param aIndex The subfield's position within the list
     * @param aSubfield The <code>Subfield</code> object
     * @throws IllegalAddException when supplied Subfield isn't an instance of <code>SubfieldImpl</code>
     */
    @Override
    public void addSubfield(final int aIndex, final Subfield aSubfield) {
        mySubfields.add(aIndex, aSubfield);
    }

    /**
     * Removes a <code>Subfield</code> from the field.
     *
     * @param aSubfield The subfield to remove from the field.
     */
    @Override
    public void removeSubfield(final Subfield aSubfield) {
        mySubfields.remove(aSubfield);
    }

    /**
     * Returns the list of <code>Subfield</code> objects.
     *
     * @return The list of <code>Subfield</code> objects
     */
    @Override
    public List<Subfield> getSubfields() {
        // TODO: consistent result/expectation as getSubfields(char)?
        return mySubfields;
    }

    /**
     * Returns the {@link Subfield}s with the supplied <code>char</code> code.
     *
     * @param aCode A subfield code
     * @return A {@link List} of {@link Subfield}s
     */
    @Override
    public List<Subfield> getSubfields(final char aCode) {
        final List<Subfield> subfields = new ArrayList<Subfield>();

        for (final Subfield subfield : mySubfields) {
            if (subfield.getCode() == aCode) {
                subfields.add(subfield);
            }
        }

        return subfields;
    }

    /**
     * Returns the number of subfields in this <code>DataField</code>.
     *
     * @return The number of subfields in this <code>DataField</code>
     */
    @Override
    public int countSubfields() {
        return mySubfields != null ? mySubfields.size() : 0;
    }

    /**
     * Returns a list of subfields from a supplied pattern. The pattern can either be a string of subfield codes or a
     * regular expression to compare subfield codes against. The inclusion of brackets indicates the pattern should be
     * parsed as a regular expression.
     */
    @Override
    public List<Subfield> getSubfields(final String aPattern) {
        final List<Subfield> sfData = new ArrayList<Subfield>();

        if (aPattern == null || aPattern.length() == 0) {
            for (final Subfield sf : this.getSubfields()) {
                sfData.add(sf);
            }
        } else if (aPattern.contains("[")) {
            // Brackets indicate a pattern
            try {
                final Pattern sfPattern = Pattern.compile(aPattern);

                for (final Subfield sf : this.getSubfields()) {
                    final Matcher m = sfPattern.matcher("" + sf.getCode());

                    if (m.matches()) {
                        sfData.add(sf);
                    }
                }
            } catch (final PatternSyntaxException details) {
                throw new PatternSyntaxException(details.getDescription() + " in subfield pattern " + aPattern,
                        details.getPattern(), details.getIndex());
            }
        } else {
            // Otherwise string is a list of values
            for (final Subfield sf : this.getSubfields()) {
                if (aPattern.contains(String.valueOf(sf.getCode()))) {
                    sfData.add(sf);
                }
            }
        }

        return sfData;
    }

    @Override
    public String getSubfieldsAsString(final String aPattern) {
        return getSubfieldsAsString(aPattern, '\u0000');
    }

    @Override
    public String getSubfieldsAsString(final String aPattern, final char aPaddingChar) {
        final List<Subfield> sfList = this.getSubfields(aPattern);

        if (sfList.isEmpty()) {
            return null;
        }

        final StringBuilder buf = new StringBuilder();

        for (final Subfield sf : sfList) {
            buf.append(sf.getData());

            if (aPaddingChar != '\u0000') {
                buf.append(sf.getData());
            }
        }

        return buf.toString();
    }

    /**
     * Returns the first {@link Subfield} matching the supplied <code>char</code> code.
     *
     * @param aCode A code for the subfield to be returned
     */
    @Override
    public Subfield getSubfield(final char aCode) {
        for (final Subfield subfield : mySubfields) {
            if (subfield.getCode() == aCode) {
                return subfield;
            }
        }

        return null;
    }

    /**
     * Returns <code>true</code> if a match is found for the supplied regular expression pattern; else,
     * <code>false</code>.
     *
     * @param aPattern A regular expression pattern to find in the subfields
     */
    @Override
    public boolean find(final String aPattern) {
        for (final Subfield subfield : mySubfields) {
            if (subfield.find(aPattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a string representation of this data field.
     * <p>
     * Example:
     *
     * <pre>
     *    245 10$aSummerland /$cMichael Chabon.
     * </pre>
     *
     * @return A string representation of this data field
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(' ');
        sb.append(getIndicator1());
        sb.append(getIndicator2());

        for (final Subfield subfield : mySubfields) {
            sb.append(subfield.toString());
        }

        return sb.toString();
    }

}
