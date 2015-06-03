package com.dataagility.ICAN.BHLibClasses;

/**
 * The HL7Field class contains methods specific to manipulating  HL7 subfields within
 * an HL7 Field (an HL7 Field is broken into sub fields via the "^" seperator).
 *
 * Methods are provided to copy subfields between HL7Field Objects and to clear,
 * set and get subfield contents.
 */
public class HL7Field extends HL7Data {

    //Authors: Ray Fillingham and Norman Soh
    //Organisation: The Alfred
    //Year: 2005

    /**
     * This constructor creates an empty field
     */
    public HL7Field() {
        mSeparatorGet = k.CARROT_GET;
        mSeparatorSet = k.CARROT_SET;
        mData = k.NULL;
        mTrimFlag = 0;
        mItemNum = 1;
    }

    /**
     * This constructor creates the specified field passed to it
     * @param pField The field text string
     */
    public HL7Field(String pField) {
        mSeparatorGet = k.CARROT_GET;
        mSeparatorSet = k.CARROT_SET;
        mData = pField;
        mTrimFlag = 0;
        mItemNum = 1;
    }

    /**
     * This constructor creates the specified field with the specified field separators
     * @param pField The field text string
     * @param pSeparatorGet The separator string to perform the split / get
     * @param pSeparatorSet The separator used in joining subfields together
     */
    public HL7Field(String pField, String pSeparatorGet, String pSeparatorSet) {
        //Initialises HL7Message

        mSeparatorGet = pSeparatorGet;
        mSeparatorSet = pSeparatorSet;
        mData = pField;
        mTrimFlag = 0;
        mItemNum = 1;
    }

    /**
     * Returns the current Field.
     * @return Returns the full field text string
     */
    public String getField() {
        return getData();
    }
    /**
     * Sets the current Field.
     * @param pField The string to replace the whole field text string
     */
    public void setField(String pField) {

        setData(pField);
    }
//    /**
//     * Returns the current Sub Field.
//     * @return Returns the subfield within the field
//     */
    public String getSubField() {

        return getItem();
    }
    /**
     * Returns the [pFieldNum] Sub Field.
     * @param pFieldNum The position number within the field
     * @return Returns the specified subfield within the field
     */
    public String getSubField(int pFieldNum) {

        return getItem(pFieldNum);
    }
    /**
     * Returns the  Sub Field identified by [pSubFieldID].
     * NOTE: pSubFieldID MUST BE an HL7 Data Type.
     * @param pSubFieldID The HL7 data type identifier
     * @return Returns the specified subfield
     */
    public String getSubField(String pSubFieldID) {
        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pSubFieldID);
        return getItem(aFD.fieldNum);
    }

    /**
     * Returns the Next Sub Field.
     * @return Returns a subfield within a field
     */
    public String getNextSubField() {
        return getNextItem();
    }
//    /**
//     * Sets the Next Sub Field.
//     * @param pField The string to be placed in the field's current position
//     */
    public void setSubField(String pField) {
        setItem(pField);
    }
    /**
     * Sets the [pFieldNum] Sub Field.
     * @param pField The subfield text string
     * @param pFieldNum The position number of the subfield
     */
    public void setSubField(String pField, int pFieldNum) {
        setItem(pField, pFieldNum);
    }

    /**
     * Sets the sub field identified by [pFieldTypeID] to the value [pField] Sub Field.
     * NOTE: [pFieldTypeID] MUST BE an HL7 Data Type!!!
     * @param pField The subfield text string
     * @param pFieldTypeID The HL7 Data type identifier
     */
    public void setSubField(String pField, String pFieldTypeID) {
        HL7FieldDescriptor aFDType = new HL7FieldDescriptor(pFieldTypeID);
        setItem(pField, aFDType.fieldNum);
    }
    /**
     * Returns a count of the number of Sub Fields.
     * @return Returns the number of subfields within a field
     */
    public int countSubFields() {
        return countItems();
    }
//
//    /** Copys all sub fields. */
    public void copySubFields() {
        copyItems();
    }

//    /**
//     * Copys all sub fields identified by their field number in [pFieldNum] array.
//     * e.g ..... copySubFields({1, 3, 5}]; ... Copys the 1st, 3rd and 5th sub fields.
//     * @param pFieldNum An array of subfield position numbers
//     */
    public void copySubFields(int pFieldNum[]) {
        copyItems(pFieldNum);
    }

    /**
     * Copys all sub fields identified by their field identifier in [pFieldID] array.
     * e.g ..... copySubFields({"CX_1","CX_3", "CX_4"}); ... Copys the 1st, 3rd and 4th sub fields.
     * @param pFieldID A string array of HL7 Data types
     */
    public void copySubFields(String pFieldID[]) {

        copyItems(pFieldID);
    }

//    /** Clears all sub fields.
//     */
    public void clearSubFields() {

        clearItems();
    }
//
//    /**
//     * Clears all sub fields identified by their field number in [pFieldNum] array.
//     * e.g ..... clearSubFields({1, 3, 5}); ... Clears the 1st, 3rd and 5th sub fields.
//     * @param pFieldNum An array of field position numbers
//     */
    public void clearSubFields(int pFieldNum[]) {

        clearItems(pFieldNum);
    }
//
    /**
     * Clears all sub fields identified by their field identifier in [pFieldID] array.
     * e.g ..... clearSubFields({"CX_1","CX_3", "CX_4"}]; ... Clears the 1st, 3rd and 4th sub fields.
     * @param pFieldID A string array of HL7 Data types
     */
    public void clearSubFields(String pFieldID[]) {

        clearItems(pFieldID);
    }

}