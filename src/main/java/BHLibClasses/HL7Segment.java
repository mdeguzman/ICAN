package BHLibClasses;

/**
 * HL7Segment class provides the methods to manipulate the segment
 */
public class HL7Segment extends HL7Data {
    //This class contains segment specific methods for HL7 segment
    //  string manipulation

    //Authors: Ray Fillingham and Norman Soh
    //Organisation: The Alfred
    //Year: 2005

    /**
     * This constructor creates a segment of the specified input segment text string
     * @param pSegment Segment text string
     */
    public HL7Segment(String pSegment) {
        //Initialises HL7Message with segment separator pre-defined

        mSeparatorGet = k.PIPE_GET;
        mSeparatorSet = k.PIPE_SET;
        mData = pSegment;
        if (mData.length()==3) {
            mData = mData.concat(mSeparatorSet);
        }
        mTrimFlag = 0;
        mItemNum = 2;
    }

    /**
     * Gets the whole segment
     * @return Returns a segment text string
     */
    public String getSegment() {

        return getData();
    }

    /**
     * Sets the segment to the specified value
     * @param pSegment Segment text string
     */
    public void setSegment(String pSegment) {

        setData(pSegment);
    }

    /**
     * Gets a field at the current location
     * @return Returns a field text string
     */
    public String getField() {

        return getItem();
    }

    /**
     * Gets a field at a specified location
     * @return Returns a field text string
     * @param pFieldNum Field position number
     */
    public String getField(int pFieldNum) {
        if (getSegmentID().equalsIgnoreCase("MSH")) {
            return getItem(pFieldNum);
        } else {
            return getItem(pFieldNum + 1);
        }
    }

    /**
     * Gets a field at a specified location
     * @return Returns a field text string
     * @param pFieldID HL7 field identifier
     */
    public String getField(String pFieldID) {
        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        if (getSegmentID().equalsIgnoreCase("MSH")) {
            return getItem(aFD.fieldNum);
        } else {
            return getItem(aFD.fieldNum + 1);
        }
    }

    /**
     * Gets a field at the next location
     * @return Returns a field text string
     */
    public String getNextField() {

        return getNextItem();
    }

    /**
     * Sets a value to field at the current location
     * @param pField Field text string
     */
    public void setField(String pField) {

        setItem(pField);
    }

    /**
     * Sets a value to field at a specified location
     * @param pField Field text string
     * @param pFieldNum Field position number
     */
    public void setField(String pField, int pFieldNum) {

        if (getSegmentID().equalsIgnoreCase("MSH")) {
            setItem(pField, pFieldNum);
        } else {
            setItem(pField, pFieldNum + 1);
        }
    }

    /**
     * Sets a value to field at a specified location
     * @param pField Field text string
     * @param pFieldID HL7 field identifier
     */
    public void setField(String pField, String pFieldID) {
        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        if (getSegmentID().equalsIgnoreCase("MSH")) {
            setItem(pField, aFD.fieldNum);
        } else {
            setItem(pField, aFD.fieldNum + 1);
        }
    }

    /**
     * Counts the number of fields in a segment
     * @return Returns the total number of fields
     */
    public int countFields() {

        return countItems() - 1;
    }

    /**
     * Copies all fields from a linked segment
     */
    public void copyFields() {

        copyItems();
    }

    /**
     * Copies specified fields from a linked segment
     * @param pFieldNum Array of field position numbers
     */
    public void copyFields(int pFieldNum[]) {
        //copyFields(int[]) will copy fields within one segment to another

        //Increment all numeric IDs by one
        int aArrayLength = pFieldNum.length;
        int aCount;

        for (aCount = 0; aCount < aArrayLength; aCount++) {
            pFieldNum[aCount]++;
        }
        copyItems(pFieldNum);
    }

    /**
     * Copies specified fields from a linked segment
     * @param pFieldID Array of HL7 field identifiers
     */
    public void copyFields(String pFieldID[]) {
        //copyItems(int) copies only the specified item name from the 2nd HL7Data
        //instance to the 1st in the same location

        int aCounter = 0;
        String aFieldStr;
        HL7FieldDescriptor aFD;
        int aItemNum = 0;

        for (aCounter = 0;aCounter < pFieldID.length; aCounter++) {
            aFieldStr = pFieldID[aCounter];
            aFD = new HL7FieldDescriptor(aFieldStr);
            if (aFD.subSubFieldNum > 0) {
                aItemNum = aFD.subSubFieldNum;
            } else if (aFD.subFieldNum > 0) {
                aItemNum = aFD.subFieldNum;
            } else if (aFD.fieldNum > 0) {
                aItemNum = aFD.fieldNum;
            }
            if (aItemNum > 0) {
                if (mLinkTo.getData().startsWith("MSH")) {
                    setField(mLinkTo.getItem(aItemNum), aItemNum);
                } else {
                    setField(mLinkTo.getItem(aItemNum + 1), aItemNum);
                }
            }
        }
    }

    /**
     * Clears a field at the current location in the segment
     */
    public void clearFields() {

        clearItems();
    }

    /**
     * Clears specified fields in the segment
     * @param pFieldNum Array of field position numbers
     */
    public void clearFields(int pFieldNum[]) {
        //clearFields(int[]) will clear the specified fields within one segment

        //Increment all numeric IDs by one
        int aArrayLength = pFieldNum.length;
        int aCount;
        String aDataOrig = mData;

        for (aCount = 0; aCount < aArrayLength; aCount++) {
            pFieldNum[aCount]++;
        }
        clearItems(pFieldNum);
    }

    /**
     * Clears specified fields in the segment
     * @param pFieldID Array of HL7 field identifiers
     */
    public void clearFields(String pFieldID[]) {
        //clearItems(string) will clear an item specified by a item name
        //as opposed to a item number

        int aCounter = 0;
        int aItemNum = 0;
        String aFieldStr;
        HL7FieldDescriptor aFD;
        String aDataOrig = mData;

        for (aCounter = 0;aCounter < pFieldID.length; aCounter++) {
            aFieldStr = pFieldID[aCounter];
            aFD = new HL7FieldDescriptor(aFieldStr);
            if (aFD.subSubFieldNum > 0) {
                aItemNum = aFD.subSubFieldNum;
            } else if (aFD.subFieldNum > 0) {
                aItemNum = aFD.subFieldNum;
            } else if (aFD.fieldNum > 0) {
                aItemNum = aFD.fieldNum;
            }
            if (aItemNum > 0 && aItemNum <= countItems()) {
                setField(k.NULL, aItemNum);
            }
        }
    }

    /**
     * Gets a field at a specified HL7 field identifier
     * @return Returns a field text string
     * @param pFieldID HL7 field identifier
     */
    public String get(String pFieldID) {
        //get(string) returns the data element at the specified named location

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        int aFieldIDCount = aFD.numOfLevels;
        String aResult = k.NULL;

        //Get field
        if (aFieldIDCount == 2) {
            aResult = getField(aFD.fieldNum);
        }
        if (aFieldIDCount == 3) {
            HL7Field aField = new HL7Field(getField(aFD.fieldNum), k.CARROT_GET, k.CARROT_SET);
            aResult = aField.getSubField(aFD.subFieldNum);
        }
        if (aFieldIDCount == 4) {
            HL7Field aField = new HL7Field(getField(aFD.fieldNum), k.CARROT_GET, k.CARROT_SET);
            HL7Field aSubField = new HL7Field(aField.getSubField(aFD.subFieldNum), k.AMPERSAND_GET, k.AMPERSAND_SET);
            aResult = aSubField.getSubField(aFD.subSubFieldNum);
        }
        return aResult;
    }

    /**
     * Returns the value of a field in a repeating field definition
     * @return Returns the field contents
     * @param pFieldID HL7 field identifier
     * @param pRepeatNum Repeat number
     */
    public String get(String pFieldID, int pRepeatNum) {
        String aResult = k.NULL;
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            aResult = aRepeatFieldArray[pRepeatNum - 1];
        }
        return aResult;
    }

    /**
     *  Gets a field at a specified HL7 field identifier and type
     * @return Returns a sub-field text string
     * @param pFieldID HL7 field identifier
     * @param pFieldType HL7 field type
     */
    public String get(String pFieldID, String pFieldType) {
        //get(string, string) returns the data element at the specified named location

        HL7FieldDescriptor aFDID = new HL7FieldDescriptor(pFieldID);
        HL7FieldDescriptor aFDType = new HL7FieldDescriptor(pFieldType);
        int aFieldIDCount = aFDID.numOfLevels;
        int aFieldTypeCount = aFDType.numOfLevels;
        String aResult = k.NULL;

        //Get field
        if (aFieldIDCount == 2 && aFieldTypeCount == 2) {
            HL7Field aField = new HL7Field(getField(aFDID.fieldNum), k.CARROT_GET, k.CARROT_SET);
            aResult = aField.getSubField(aFDType.fieldNum);
        }
        if (aFieldIDCount == 2 && aFieldTypeCount == 3) {
            HL7Field aField = new HL7Field(getField(aFDID.fieldNum), k.CARROT_GET, k.CARROT_SET);
            HL7Field aSubField = new HL7Field(aField.getSubField(aFDType.fieldNum), k.AMPERSAND_GET, k.AMPERSAND_SET);
            aResult = aSubField.getSubField(aFDType.subFieldNum);
        }
        return aResult;
    }

    /**
     * Returns the value of a field data type in a repeating field definition
     * @param pFieldID HL7 field identifier
     * @param pFieldType HL7 field data type
     * @param pRepeatNum Repeat number
     * @return Returns the value of the field data type
     */
    public String get(String pFieldID, String pFieldType, int pRepeatNum) {
        String aResult = k.NULL;
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum - 1]);
            aResult = aRepeatField.getSubField(pFieldType);
        }
        return aResult;
    }

    /**
     * Sets a value at a specified HL7 field identifier location
     * @param pFieldID HL7 field identifier
     * @param pField Field text string
     */
    public void set(String pFieldID, String pField) {
        // set(string, string) sets or replaces a data element at the specified named location

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        int aFieldIDCount = aFD.numOfLevels;
        String aResult = k.NULL;

        //Set field
        if (aFieldIDCount == 2) {
            setField(pField, aFD.fieldNum);
        }
        if (aFieldIDCount == 3) {
            HL7Field aField = new HL7Field(getField(aFD.fieldNum), k.CARROT_GET, k.CARROT_SET);
            aField.setSubField(pField, aFD.subFieldNum);
            setField(aField.getField(), aFD.fieldNum);
        }
        if (aFieldIDCount == 4) {
            HL7Field aField = new HL7Field(getField(aFD.fieldNum), k.CARROT_GET, k.CARROT_SET);
            HL7Field aSubField = new HL7Field(aField.getSubField(aFD.subFieldNum), k.AMPERSAND_GET, k.AMPERSAND_SET);
            aSubField.setSubField(pField, aFD.subSubFieldNum);
            aField.setSubField(aSubField.getField(), aFD.subFieldNum);
            setField(aField.getField(), aFD.fieldNum);
        }
    }

    /**
     * Sets a field value into a repeating field definition
     * @param pFieldID HL7 field identifier
     * @param pField Field value to set
     * @param pRepeatNum Repeat number
     */
    public void set(String pFieldID, String pField, int pRepeatNum) {
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            aRepeatFieldArray[pRepeatNum - 1] = pField;
            setRepeatFields(pFieldID, aRepeatFieldArray);
        } else {
            //expand repeat fields
            String aTempFieldArray[] = new String[pRepeatNum];
            for (int i = 0; i < pRepeatNum; i++) {
                aTempFieldArray[i] = "";
            }
            for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                aTempFieldArray[i] = aRepeatFieldArray[i];
            }
            aTempFieldArray[pRepeatNum - 1] = pField;
            setRepeatFields(pFieldID, aTempFieldArray);
        }
    }

    /**
     * Sets a value at a specified HL7 field identifier and type location
     * @param pFieldID HL7 field identifier
     * @param pFieldType HL7 field type
     * @param pField Field text string
     */
    public void set(String pFieldID, String pFieldType, String pField) {
        // set(string, string) sets or replaces a data element at the specified named location

        HL7FieldDescriptor aFDID = new HL7FieldDescriptor(pFieldID);
        HL7FieldDescriptor aFDType = new HL7FieldDescriptor(pFieldType);
        int aFieldIDCount = aFDID.numOfLevels;
        int aFieldTypeCount = aFDType.numOfLevels;
        String aResult = k.NULL;

        //Set field
        if (aFieldIDCount == 2 && aFieldTypeCount == 2) {
            HL7Field aField = new HL7Field(getField(aFDID.fieldNum), k.CARROT_GET, k.CARROT_SET);
            aField.setSubField(pField, aFDType.fieldNum);
            setField(aField.getField(), aFDID.fieldNum);
        }
        if (aFieldIDCount == 2 && aFieldTypeCount == 3) {
            HL7Field aField = new HL7Field(getField(aFDID.fieldNum), k.CARROT_GET, k.CARROT_SET);
            HL7Field aSubField = new HL7Field(aField.getSubField(aFDType.fieldNum), k.AMPERSAND_GET, k.AMPERSAND_SET);
            aSubField.setSubField(pField, aFDType.subFieldNum);
            aField.setSubField(aSubField.getField(), aFDType.fieldNum);
            setField(aField.getField(), aFDID.fieldNum);
        }
    }

    /**
     * Sets a field value data type into a repeating field definition
     * @param pFieldID HL7 field identifier
     * @param pField Field value to set
     * @param pRepeatNum Repeat number
     * @param pFieldType HL7 field data type
     */
    public void set(String pFieldID, String pFieldType, String pField, int pRepeatNum) {
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum - 1]);
            aRepeatField.setSubField(pField, pFieldType);
            aRepeatFieldArray[pRepeatNum - 1] = aRepeatField.getField();
            setRepeatFields(pFieldID, aRepeatFieldArray);
        } else {
            //expand repeat fields
            String aTempFieldArray[] = new String[pRepeatNum];
            for (int i = 0; i < pRepeatNum; i++) {
                aTempFieldArray[i] = "";
            }
            for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                aTempFieldArray[i] = aRepeatFieldArray[i];
            }
            HL7Field aTempField = new HL7Field(aTempFieldArray[pRepeatNum - 1]);
            aTempField.setSubField(pField, pFieldType);
            aTempFieldArray[pRepeatNum - 1] = aTempField.getField();
            setRepeatFields(pFieldID, aTempFieldArray);
        }
    }

    /**
     * Counts the number of repeating fields
     * @param pFieldID HL7 field identifier
     * @return Returns an integer
     */
    public int countRepeatFields(String pFieldID) {
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aResult = aRepeatFieldArray.length;
        if (aResult == 1 && aRepeatFieldArray[0].equalsIgnoreCase(k.NULL)) {
            aResult = 0;
        }
        return aResult;
    }

    /**
     * Returns a string array of repeating fields at a specified HL7 field identifier
     * @param pFieldID HL7 field identifier
     * @return Repeating field text string
     */
    public String[] getRepeatFields(String pFieldID) {
        //getRepeatFields(string) returns a string array of repeating fields only

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        int aFieldIDCount = aFD.numOfLevels;
        String aResult[] = {};
        int aCounter;

        //Get fields
        if (aFieldIDCount == 2) {
            String aRepeatField[] = getField(aFD.fieldNum).split(k.REPEAT_GET);
            aResult = new String[aRepeatField.length];
            for (aCounter = 0; aCounter < aRepeatField.length; aCounter++) {
                aResult[aCounter] = aRepeatField[aCounter];
            }
        }
        return aResult;
    }

    /**
     * Sets a repeating field at a specified HL7 field identifier
     * @param pFieldID HL7 field identifier
     * @param pField Array of repeating field text string
     */
    public void setRepeatFields(String pFieldID, String[] pField) {
        //setRepeatFields(string, string) sets / replaces a repeating set of fields within a segment only

        HL7FieldDescriptor aFD = new HL7FieldDescriptor(pFieldID);
        int aFieldIDCount = aFD.numOfLevels;
        String aResult = k.NULL;
        int aCounter = 0;

        //Get fields
        if (aFieldIDCount == 2 && pField.length > 0) {
            aResult = pField[aCounter];
            for (aCounter = 1; aCounter < pField.length; aCounter++) {
                try {
                    aResult = aResult.concat(k.REPEAT_SET).concat(pField[aCounter]);
                } catch (NullPointerException e) {
                    aCounter = pField.length;
                }
            }
            setField(aResult, aFD.fieldNum);
        }
    }

    /**
     * Retrieves the segment identifier of the segment
     * @return Returns the segment identifier
     */
    public String getSegmentID() {
        String aResult = k.NULL;

        String aArray[] = mData.split(k.PIPE_GET);
        aResult = aArray[0];

        return aResult;
    }

    /**
     * Trims trailing "|" characters from the segment
     * @param pSegment Segment text string
     * @return Returns a segment text string
     */
    public String trimSegment(String pSegment) {
        //trimSegment(string) returns a re-formated string with or without trailing pipe characters

        String aResult = k.NULL;
        String aTempSeg = k.NULL;
        int aSegLength;
        int aSegCharCount;
        String aChar;
        boolean aSegDelimiter;

        aSegLength = pSegment.length();
        //Trim all trailing pipe characters at end of segment
        aSegDelimiter = false;
        for (aSegCharCount = aSegLength; aSegCharCount > 0; aSegCharCount--) {
            aChar = pSegment.substring(aSegCharCount - 1, aSegCharCount);
            if (aChar.equalsIgnoreCase(k.PIPE_SET) && aSegDelimiter == true) {
                //Do nothing - removing this last pipe
            } else if (aChar.equalsIgnoreCase(k.PIPE_SET) && aSegCharCount == aSegLength) {
                //Do nothing - removing this last pipe
                aSegDelimiter = true;
            } else {
                aTempSeg = aChar.concat(aTempSeg);
                aSegDelimiter = false;
            }
        }
        aResult = aTempSeg;
        return aResult;
    }

    /**
     * Copies fields at a specified HL7 field identifier and type from a
     * linked segment
     * @param pFieldID HL7 field identifier
     * @param pFieldType HL7 field type identifier
     */
    public void copy(String pFieldID, String pFieldType) {
        // Copies a field and type from a linked segment

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aField = aLinkToSeg.get(pFieldID, pFieldType);
        set(pFieldID, pFieldType, aField);
    }

    /**
     * Copies fields at a specified HL7 field identifier from a
     * linked segment
     * @param pFieldID HL7 field identifier
     */
    public void copy(String pFieldID) {
        // Copies a field from a linked segment

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aField = aLinkToSeg.get(pFieldID);
        set(pFieldID, aField);
    }

    /**
     * Copies fields at a specified repeating HL7 field identifier from a
     * linked segment
     */
    public void copy(String pFieldID, int pRepeatNum) {
        // Copies a field in a repeating field location
        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum <= aLinkToRepeatFieldArrayCount) {
            if (aRepeatFieldArrayCount < aLinkToRepeatFieldArrayCount) {
                //target field array is less than source
                String aTempRepeatFieldArray[] = new String[aLinkToRepeatFieldArrayCount];
                for (int i = 0; i < pRepeatNum; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }

                aTempRepeatFieldArray[pRepeatNum - 1] = aLinkToRepeatFieldArray[pRepeatNum - 1];
                setRepeatFields(pFieldID, aTempRepeatFieldArray);
            } else {
                //target field array is greater or equal to source
                aRepeatFieldArray[pRepeatNum - 1] = aLinkToRepeatFieldArray[pRepeatNum - 1];
                setRepeatFields(pFieldID, aRepeatFieldArray);
            }
        }
    }

    /**
     * Copies fields at a specified repeating HL7 field identifier from a
     * linked segment
     */
    public void copy(String pFieldID, String pFieldType, int pRepeatNum) {
        // Copies a subfield in a repeating field location
        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum <= aLinkToRepeatFieldArrayCount) {
            if (aRepeatFieldArrayCount < aLinkToRepeatFieldArrayCount) {
                //target field array is less than source
                String aTempRepeatFieldArray[] = new String[aLinkToRepeatFieldArrayCount];
                for (int i = 0; i < pRepeatNum; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }
                HL7Field aLinkToField = new HL7Field(aLinkToRepeatFieldArray[pRepeatNum - 1]);
                String aSubFieldValue = aLinkToField.getSubField(pFieldType);
                HL7Field aRepeatField = new HL7Field(aTempRepeatFieldArray[pRepeatNum - 1]);
                aRepeatField.setSubField(aSubFieldValue, pFieldType);
                aTempRepeatFieldArray[pRepeatNum - 1] = aRepeatField.getField();
                setRepeatFields(pFieldID, aTempRepeatFieldArray);
            } else {
                //target field array is greater or equal to source
                HL7Field aLinkToField = new HL7Field(aLinkToRepeatFieldArray[pRepeatNum - 1]);
                String aSubFieldValue = aLinkToField.getSubField(pFieldType);
                HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum - 1]);
                aRepeatField.setSubField(aSubFieldValue, pFieldType);
                aRepeatFieldArray[pRepeatNum - 1] = aRepeatField.getField();
                setRepeatFields(pFieldID, aRepeatFieldArray);
            }
        }
    }

    /**
     * Moves a field from a specified HL7 field identifier and type from a linked
     * segment to a destination HL7 field identifier and type
     * @param pDestFieldID HL7 field identifier
     * @param pDestFieldType HL7 field type identifier
     * @param pSourceFieldID HL7 field identifier
     * @param pSourceFieldType HL7 field type identifier
     */
    public void move(String pDestFieldID, String pDestFieldType, String pSourceFieldID, String pSourceFieldType) {
        // Copies a field and type from a linked to segment to another field and type location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aField = aLinkToSeg.get(pSourceFieldID, pSourceFieldType);
        set(pDestFieldID, pDestFieldType, aField);
    }

    /**
     * Moves a field from a specified HL7 field identifier from a linked
     * segment to a destination HL7 field identifier
     * @param pDestFieldID HL7 field identifier
     * @param pSourceFieldID HL7 field type identifier
     */
    public void move(String pDestFieldID, String pSourceFieldID) {
        // Copies a field from a linked to segment to another field location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aField = aLinkToSeg.get(pSourceFieldID);
        set(pDestFieldID, aField);
    }

    /**
     * Moves a field from a specified repeating HL7 field identifier from a linked
     * segment to a destination HL7 field identifier
     */
    public void move(String pDestFieldID, int pRepeatNum, String pSourceFieldID) {
        // Copies a repeating field from a linked to segment to another repeating field location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pSourceFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pDestFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum <= aLinkToRepeatFieldArrayCount) {
            if (aRepeatFieldArrayCount < aLinkToRepeatFieldArrayCount) {
                //target field array is less than source
                String aTempRepeatFieldArray[] = new String[aLinkToRepeatFieldArrayCount];
                for (int i = 0; i < pRepeatNum; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }

                aTempRepeatFieldArray[pRepeatNum - 1] = aLinkToRepeatFieldArray[pRepeatNum - 1];
                setRepeatFields(pDestFieldID, aTempRepeatFieldArray);
            } else {
                //target field array is greater or equal to source
                aRepeatFieldArray[pRepeatNum - 1] = aLinkToRepeatFieldArray[pRepeatNum - 1];
                setRepeatFields(pDestFieldID, aRepeatFieldArray);
            }
        }
    }

    /**
     * Moves a field from a specified repeating HL7 field identifier from a linked
     * segment to a destination HL7 field identifier
     */
    public void move(String pDestFieldID, String pDestFieldType, int pRepeatNum, String pSourceFieldID, String pSourceFieldType) {
        // Copies a repeating field from a linked to segment to another repeating field location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pSourceFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pDestFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum <= aLinkToRepeatFieldArrayCount) {
            if (aRepeatFieldArrayCount < aLinkToRepeatFieldArrayCount) {
                //target field array is less than source
                String aTempRepeatFieldArray[] = new String[aLinkToRepeatFieldArrayCount];
                for (int i = 0; i < pRepeatNum; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }
                HL7Field aLinkToField = new HL7Field(aLinkToRepeatFieldArray[pRepeatNum - 1]);
                String aSubFieldValue = aLinkToField.getSubField(pSourceFieldType);
                HL7Field aRepeatField = new HL7Field(aTempRepeatFieldArray[pRepeatNum - 1]);
                aRepeatField.setSubField(aSubFieldValue, pDestFieldType);
                aTempRepeatFieldArray[pRepeatNum - 1] = aRepeatField.getField();
                setRepeatFields(pDestFieldID, aTempRepeatFieldArray);
            } else {
                //target field array is greater or equal to source
                HL7Field aLinkToField = new HL7Field(aLinkToRepeatFieldArray[pRepeatNum - 1]);
                String aSubFieldValue = aLinkToField.getSubField(pSourceFieldType);
                HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum - 1]);
                aRepeatField.setSubField(aSubFieldValue, pDestFieldType);
                aRepeatFieldArray[pRepeatNum - 1] = aRepeatField.getField();
                setRepeatFields(pDestFieldID, aRepeatFieldArray);
            }
        }
    }

    /**
     * Moves a field from a specified repeating HL7 field identifier from a linked
     * segment to a destination HL7 field identifier
     */
    public void move(String pDestFieldID, int pRepeatNum1, String pSourceFieldID, int pRepeatNum2) {
        // Copies a repeating field from a linked to segment to another repeating field location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pSourceFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pDestFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum2 <= aLinkToRepeatFieldArrayCount) {
            String aSourceField = aLinkToRepeatFieldArray[pRepeatNum2 - 1];
            if (pRepeatNum1 <= aRepeatFieldArrayCount) {
                aRepeatFieldArray[pRepeatNum1 - 1] = aSourceField;
                setRepeatFields(pDestFieldID, aRepeatFieldArray);
            } else {
                String aTempRepeatFieldArray[] = new String[pRepeatNum1];
                for (int i = 0; i < pRepeatNum1; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }
                aTempRepeatFieldArray[pRepeatNum1 - 1] = aSourceField;
                setRepeatFields(pDestFieldID, aTempRepeatFieldArray);
            }
        }
    }

    /**
     * Moves a field from a specified repeating HL7 field identifier from a linked
     * segment to a destination HL7 field identifier
     */
    public void move(String pDestFieldID, String pDestFieldType, int pRepeatNum1, String pSourceFieldID, String pSourceFieldType, int pRepeatNum2) {
        // Copies a repeating field from a linked to segment to another repeating field location

        HL7Segment aLinkToSeg = new HL7Segment(mLinkTo.getData());
        String aLinkToRepeatFieldArray[] = aLinkToSeg.getRepeatFields(pSourceFieldID);
        String aRepeatFieldArray[] = getRepeatFields(pDestFieldID);
        int aLinkToRepeatFieldArrayCount = aLinkToRepeatFieldArray.length;
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;

        if (pRepeatNum2 <= aLinkToRepeatFieldArrayCount) {
            HL7Field aSourceField = new HL7Field(aLinkToRepeatFieldArray[pRepeatNum2 - 1]);
            String aSourceFieldValue = aSourceField.getSubField(pSourceFieldType);
            if (pRepeatNum1 <= aRepeatFieldArrayCount) {
                HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum1 - 1]);
                aRepeatField.setSubField(aSourceFieldValue, pDestFieldType);
                aRepeatFieldArray[pRepeatNum1 - 1] = aRepeatField.getField();
                setRepeatFields(pDestFieldID, aRepeatFieldArray);
            } else {
                String aTempRepeatFieldArray[] = new String[pRepeatNum1];
                for (int i = 0; i < pRepeatNum1; i++) {
                    aTempRepeatFieldArray[i] = "";
                }
                for (int i = 0; i < aRepeatFieldArrayCount; i++) {
                    aTempRepeatFieldArray[i] = aRepeatFieldArray[i];
                }
                HL7Field aTempRepeatField = new HL7Field(aTempRepeatFieldArray[pRepeatNum1 - 1]);
                aTempRepeatField.setSubField(aSourceFieldValue, pDestFieldType);
                aTempRepeatFieldArray[pRepeatNum1 - 1] = aTempRepeatField.getField();
                setRepeatFields(pDestFieldID, aTempRepeatFieldArray);
            }
        }
    }

    /**
     * Determines a field is empty at a specified HL7 field identifier
     * @param pFieldID HL7 field identifier
     * @return Returns true or false
     */
    public boolean isEmpty(String pFieldID) {
        boolean aResult = false;
        String aTemp = get(pFieldID);
        if (aTemp.equalsIgnoreCase(k.NULL)) {
            aResult = true;
        }
        return aResult;
    }

    /**
     * Determines a field is empty at a specified HL7 field and type identifier
     * @param pFieldID HL7 field identifier
     * @param pFieldType HL7 file type identifier
     * @return Returns true or false
     */
    public boolean isEmpty(String pFieldID, String pFieldType) {
        boolean aResult = false;
        String aTemp = get(pFieldID, pFieldType);
        if (aTemp.equalsIgnoreCase(k.NULL)) {
            aResult = true;
        }
        return aResult;
    }

    /**
     * Determines if a value matches a specified value
     */
    public boolean hasValue(String pFieldID, String pCompareValue) {
        boolean aResult = false;

        String aCompareValue = pCompareValue;
        String aFieldValue = get(pFieldID);

        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

    /**
     * Determines if a value matches a specified value
     */
    public boolean hasValue(String pFieldID, String pFieldType, String pCompareValue) {

        boolean aResult = false;
        String aFieldValue = get(pFieldID, pFieldType);
        String aCompareValue = pCompareValue;

        if (aFieldValue.matches(aCompareValue)) {
            aResult = true;
        }

        return aResult;
    }

    /**
     * Determines if a value matches a specified value
     */
    public boolean hasValue(String pFieldID, String pCompareValue, int pRepeatNum) {
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        boolean aResult = false;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            if (aRepeatFieldArray[pRepeatNum - 1] == null) {
                //null found
            } else if (aRepeatFieldArray[pRepeatNum - 1].length() > 0) {
                if (aRepeatFieldArray[pRepeatNum - 1].equalsIgnoreCase(pCompareValue)) {
                    aResult = true;
                }
            }
        }
        return aResult;
    }

    /**
     * Determines if a value matches a specified value
     */
    public boolean hasValue(String pFieldID, String pFieldType, String pCompareValue, int pRepeatNum) {
        String aRepeatFieldArray[] = getRepeatFields(pFieldID);
        int aRepeatFieldArrayCount = aRepeatFieldArray.length;
        boolean aResult = false;
        if (pRepeatNum <= aRepeatFieldArrayCount) {
            if (aRepeatFieldArray[pRepeatNum - 1] == null) {
                //null found
            } else if (aRepeatFieldArray[pRepeatNum - 1].length() > 0) {
                HL7Field aRepeatField = new HL7Field(aRepeatFieldArray[pRepeatNum - 1]);
                String aSubField = aRepeatField.getSubField(pFieldType);
                if (aSubField.equalsIgnoreCase(pCompareValue)) {
                    aResult = true;
                }
            }
        }
        return aResult;
    }

}