package com.dataagility.ICAN.BHLibClasses;
//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

/**
 * The HL7Message class contains methods specific to manipulating  HL7 Segments within
 * an HL7 Message (an HL7 Message is broken into Segments by CR - Carriage Return
 * seperators).
 *
 * Methods are provided to copy Segments between HL7Message Objects and to clear,
 * set and get Segment contents.
 */
public class HL7Message extends HL7Data {
    /**
     * Create a blank HL7 Message object.
     */
    public String[] mMessageSegArray;
    public String[] mMessageSegIDArray;
    public HL7Message() {
        //Initialises HL7Message with segment separator pre-defined

        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = k.NO_TRIM;
        mItemNum = 1;
        mData = "MSH|\r";
    }

    /**
     * Creates an HL7 message object that contains [pMessage] and with trim set to NO-TRIM.
     * @param pMessage The HL7 message to be operated on by this objects methods.
     */
    public HL7Message(String pMessage) {
        //Initialises HL7Message with segment separator pre-defined

        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = k.NO_TRIM;
        mItemNum = 1;
        mData = trimMessage(pMessage);
        if (!mData.endsWith(k.CARRIAGE_RETURN) && mData.length() > 0) {
            mData = mData.concat(mSeparatorSet);
        }
        mMessageSegArray = mData.split(k.CARRIAGE_RETURN) ;
        mMessageSegIDArray = new String[mMessageSegArray.length];
        int i;
        for (i = 0; i < mMessageSegArray.length; i++) {
            if (mMessageSegArray[i] == null) {
                mMessageSegArray[i] = k.NULL;
                mMessageSegIDArray[i] = k.NULL;
            } else {
                if (!mMessageSegArray[i].equals(k.NULL)) {
                    mMessageSegIDArray[i] = mMessageSegArray[i].substring(0, 3);
                } else {
                    mMessageSegArray[i] = k.NULL;
                    mMessageSegIDArray[i] = k.NULL;
                }
            }
        }
    }
    /**
     * Creates an HL7 message object that contains [pMessage] and with trim set
     * according to [pTrimFlag]
     * @param pMessage The HL7 message to be operated on by this objects methods.
     * @param pTrimFlag This flag indicates if triming of trailing Fields is to occur or not.<p>
     * Values are:-<p>
     * NO_TRIM ... Do not trim at all,<p>
     * TRIM_LAST ... remove only the last seperator (Required when handling CSC messages)<p>
     * TRIM_ALL ... remove all trailing seperators.
     */
    public HL7Message(String pMessage, int pTrimFlag) {
        //Initialises HL7Message with segment separator pre-defined

        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = pTrimFlag;
        mItemNum = 1;
        mData = trimMessage(pMessage);
        if (!mData.endsWith(k.CARRIAGE_RETURN)) {
            mData = mData.concat(mSeparatorSet);
        }
        mMessageSegArray = mData.split(k.CARRIAGE_RETURN) ;
        mMessageSegIDArray = new String[mMessageSegArray.length];
        int i;
        for (i = 0; i < mMessageSegArray.length; i++) {
            if (mMessageSegArray[i] == null) {
                mMessageSegArray[i] = k.NULL;
                mMessageSegIDArray[i] = k.NULL;
            } else {
                if (!mMessageSegArray[i].equals(k.NULL)) {
                    mMessageSegIDArray[i] = mMessageSegArray[i].substring(0, 3);
                } else {
                    mMessageSegArray[i] = k.NULL;
                    mMessageSegIDArray[i] = k.NULL;
                }
            }
        }
    }

    /**
     * Returns the current Message
     * @return Returns the complete HL7 Message.
     */
    public String getMessage() {

        if (mTrimFlag == k.TRIM_ALL) {
            return trimMessage(getData());
        } else {
            return getData();
        }
    }


    /**
     * Sets the current Message
     * @param pMessage HL7 message text string
     */
    public void setMessage(String pMessage) {

        setData(pMessage);
    }

    /**
     * Returns the current Segment.
     * @return Segment text string
     */
    public String getSegment() {

        return getItem();
    }

    /**
     * Returns the (pSegmentNum) Segment.
     * @param pSegmentNum Segment position number
     * @return Segment text string
     */
    public String getSegment(int pSegmentNum) {

        return getItem(pSegmentNum);
    }

    /**
     * Returns the Segment identified by (pSegmentID).
     * @return Segment text string
     * @param pSegmentID Segment identifier
     */
    public String getSegment(String pSegmentID) {
        //getSegment(string) returns the first occurrence of the specified
        //  segment id

        String aArray[] = mData.split(mSeparatorGet);
        String aResult = k.NULL;
        int aArrayCount;

        for (aArrayCount = 0; aArrayCount < aArray.length; aArrayCount++) {
            if (aArray[aArrayCount].indexOf(pSegmentID) == 0) {
                aResult = aArray[aArrayCount];
                break;
            }
        }
        return aResult;
    }

    /**
     * Returns the (pSegmentNum) instance of the Segment identified by (pSegmentID).
     * @return Segment text string
     * @param pSegmentID Segment identifier
     * @param pSegmentNum Segment position number
     */
    public String getSegment(String pSegmentID, int pSegmentNum) {
        //getSegment(string, int) returns the nth occurrence of the specified
        //  segment id

        String aArray[] = mData.split(mSeparatorGet);
        String aResult = k.NULL;
        int aArrayCount;
        int aSegCount = 0;

        for (aArrayCount = 0; aArrayCount < aArray.length; aArrayCount++) {
            if (aArray[aArrayCount].indexOf(pSegmentID) == 0) {
                aSegCount++;
                if (aSegCount == pSegmentNum) {
                    aResult = aArray[aArrayCount];
                }
            }
        }
        return aResult;
    }

    /**
     * Returns the Next Segment.
     * @return Segment text string
     */
    public String getNextSegment() {

        return getNextItem();
    }

    /**
     * Sets the Next Segment.
     * @param pSegment Segment text string
     */
    public void setSegment(String pSegment) {

        setItem(pSegment);
    }


    /**
     * Sets the (pSegmentNum) instance of the Segment to (pSegment>.
     * @param pSegment Segment text string
     * @param pSegmentNum Segment position number
     */
    public void setSegment(String pSegment, int pSegmentNum) {

        setItem(pSegment, pSegmentNum);
        //Check to see if last segment of the message contains a CARRIAGE_RETURN.
        //If not, add one in to complete the message

        if (!mData.endsWith(mSeparatorSet)) {
            mData = mData.concat(mSeparatorSet);
        }
    }

    /**
     * Returns a count of the total number of Segments in the Message.
     * @return Total number of segments
     */
    public int countSegments() {

        return countItems();
    }

    /**
     * Returns a count of the total number of (pSegmentI> Segments in the Message.
     * @return Total number of segments
     * @param pSegmentID Segment identifier
     */
    public int countSegments(String pSegmentID) {
        //countSegments(string) returns the number of occurrence of HL7 segment as specified by strSegmentID

        String aArray[] = mData.split(mSeparatorGet);
        String aResult = k.NULL;
        int aArrayCount;
        int aSegCount = 0;

        for (aArrayCount = 0; aArrayCount < aArray.length; aArrayCount++) {
            if (aArray[aArrayCount].indexOf(pSegmentID) == 0) {
                aSegCount++;
            }
        }
        return aSegCount;
    }
//
//    /** Copys all Segments between the linked HL7Message Object and this Object. */
    public void copySegments() {

        copyItems();
    }

//    /**
//     * Copys all Segments identified by their Segment number in (pSegmentNum) array
//     * between the linked HL7Message Object and this Object.
//     * e.g ..... copySegments({1, 3, 5}]; ... Copys the 1st, 3rd and 5th Segments.
//     * @param pSegmentNum Array of segment position numbers
//     */
    public void copySegments(int pSegmentNum[]) {

        copyItems(pSegmentNum);
        //Check to see if last segment of the message contains a CARRIAGE_RETURN.
        //If not, add one in to complete the message

        if (!mData.endsWith(mSeparatorSet)) {
            mData = mData.concat(mSeparatorSet);
        }
    }
//
    /**
     * Copys all Segments identified by their Segment identifier in (pSegmentID) array
     * between the linked HL7Message Object and this Object.
     * e.g ..... copySegments({"MSH", "EVN", "PID"}]; ... Copys the MSH , EVN
     * and PID Segments.
     * @param pSegmentID Array of segment HL7 field identifiers
     */
    public void copySegments(String pSegmentID[]) {

        copyItems(pSegmentID);
        //Check to see if last segment of the message contains a CARRIAGE_RETURN.
        //If not, add one in to complete the message

        if (!mData.endsWith(mSeparatorSet)) {
            mData = mData.concat(mSeparatorSet);
        }
    }
//    /** Clears all segments out of the Message */
    public void clearSegments() {

        clearItems();
    }
//    /**
//     * Clears each of the Segments indetified by their SegmentNum in the (pSegmentNum) array.
//     * @param pSegmentNum Array of segment position numbers
//     */
    public void clearSegments(int pSegmentNum[]) {

        clearItems(pSegmentNum);
    }
//
    /**
     * Clears each of the Segments indentified by their SegmentID in the (pSegmentID) array.
     * @param pSegmentID Array of segment HL7 field identifiers
     */
    public void clearSegments(String pSegmentID[]) {

        clearItems(pSegmentID);
    }

    /**
     * Returns a count of the number of Groups identified by (pSegmentGroupID[]> <p>
     * A Group is defined by a list of Segments that comprises the group ( ie.e "ORC","OBR","OBX").
     * @param pSegmentGroupID Segment group identifier
     * @return Total number of groupings
     */
    public int countGroups(String pSegmentGroupID[]) {
        //countGroups(string) counts the number of groups within a HL7 message
        //  containing a list of specified segment IDs
        return countSegments(pSegmentGroupID[0]);
    }

    /**
     * Returns a Group of Segmentsidentified by (pSegmentGroupID[]> <p>
     * A Group is defined by a list of Segments that comprises the group ( ie.e "ORC","OBR","OBX").
     * @param pSegmentGroupID Array of segment HL7 field identifiers
     * @param pSegmentGroupNum Segment group position number
     * @return Segment group text string
     */
    public String getGroup(String pSegmentGroupID[], int pSegmentGroupNum) {
        String aResult = k.NULL;
        HL7Group aOutGroup = new HL7Group(k.NULL);
        int aGroupCount = countSegments(pSegmentGroupID[0]);
        int aSegmentCount = mMessageSegArray.length;
        int aSegmentIDCount = pSegmentGroupID.length;
        String aOutGroupArray[] = new String[aGroupCount];
        boolean aAppend = false;
        boolean aNewGroup = false;
        boolean aInGroup = false;
        int aOutGroupArrayCount = 0;

        if (aGroupCount > 0) {
            for (int i = 0 ; i < aSegmentCount; i++) {
                aAppend = false;
                aNewGroup = false;
                for (int x = 0; x < aSegmentIDCount; x++) {
                    if (mMessageSegArray[i].startsWith(pSegmentGroupID[x])) {
                        if (x == 0) {
                            aInGroup = true;
                            aNewGroup = true;
                            aAppend = true;
                        } else {
                            if (aInGroup == true) {
                                aAppend = true;
                            }
                        }
                    }
                }
                if (aAppend == true && aNewGroup == false) {
                    aOutGroup.append(mMessageSegArray[i]);

                } else if (aAppend == true && aNewGroup == true) {
                    if (aOutGroup.getGroup().length() > 0) {
                        aOutGroupArray[aOutGroupArrayCount++] = aOutGroup.getGroup();
                    }
                    aOutGroup = new HL7Group();
                    aOutGroup.append(mMessageSegArray[i]);
                } else if (aAppend == false) {
                    aInGroup = false;
                }
            }
        }
        if (aOutGroup.getGroup().length() > 0) {
            aOutGroupArray[aOutGroupArrayCount++] = aOutGroup.getGroup();
        }
        if (pSegmentGroupNum > 0 && pSegmentGroupNum <= aGroupCount) {
            aResult = aOutGroupArray[pSegmentGroupNum - 1];
        }
        return aResult;
    }

    /**
     * Sets or Replaces a Group of Segments identified by (pSegmentGroupID[]> <p>
     * A Group is defined by a list of Segments that comprises the group ( ie.e "ORC","OBR","OBX").
     * @param pSegmentGroupID Array of segment HL7 field identifiers
     * @param pSegmentGroupNum Segment group position number
     * @param pSegmentGroup Segment group text string
     */
    public void setGroup(String pSegmentGroupID[], int pSegmentGroupNum, String pSegmentGroup) {
        //setGroup(string, int, string) sets or replaces a segment in the message at
        //  a specified location

        String aTemp;
        int aCounter = 0;
        int aSegmentCounter = 0;
        int aArrayCounter= 0;
        int aFoundCounter = 0;
        int aGroupCounter = 0;
        int aStartofString = 0;
        int aEndofString = 0;
        int aAccumLength = 0;
        boolean aMatch;

        //Count number of elements in pSegmentGroupID array
        aArrayCounter = pSegmentGroupID.length;

        //Check each element for match
        int aCnt = countSegments() + 1;
        for (aSegmentCounter = 1; aSegmentCounter <= aCnt; aSegmentCounter++) {
            aTemp = getSegment(aSegmentCounter);
            aAccumLength = aAccumLength + aTemp.length() + 1;
            aMatch = false;
            for (aCounter = 0; aCounter < aArrayCounter; aCounter++) {
                if (aTemp.indexOf(pSegmentGroupID[aCounter]) == 0) {
                    aMatch = true;
                    if (aFoundCounter == 0 && aCounter == 0) {
                        //Matches first segment group id - start of new group
                        aFoundCounter = 1;
                        aGroupCounter++;
                        if (aGroupCounter == pSegmentGroupNum) {
                            aStartofString = aAccumLength - aTemp.length() - 1;
                        }
                    } else if (aFoundCounter > 0 && aCounter == 0) {
                        //Matches first segment group id - thus new group found
                        aFoundCounter = 1;
                        aGroupCounter++;

                        if (aGroupCounter == pSegmentGroupNum + 1) {
                            aEndofString = aAccumLength - aTemp.length() - 2;
                        } else if (aGroupCounter == pSegmentGroupNum && aStartofString == 0) {
                            aStartofString = aAccumLength - aTemp.length() - 1;
                        }
                    } else if (aFoundCounter > 0 && aCounter > 0) {
                        //Matches second, third, ... segment group id
                        aFoundCounter++;
                    }
                }
            }
            //No matches for segment group id
            if (aMatch == false) {
                if (aFoundCounter > 0) {
                    aFoundCounter = 0;
                    if (aGroupCounter == pSegmentGroupNum) {
                        aEndofString = aAccumLength - aTemp.length() - 2;
                    }
                }
            }
        }
        if (aEndofString > aStartofString) {
            //Add pSegmentGroup text into message
            mData = mData.substring(0, aStartofString).concat(pSegmentGroup).concat(mData.substring(aEndofString));
        }
    }
    /**
     * Returns the HL7 Message Trigger Event (i.e. "A01", "A34", "P03" ....).
     * @return Returns the trigger event as a text string
     */
    public String getTriggerEvent() {
        //Returns the Trigger event for the message

        String MSH_segment_id = "MSH";
        int MSH_9_message_type = 9;
        int CM_EVENT = 2;

        String aResult = k.NULL;
        String aTemp = k.NULL;
        HL7Segment aSegment;
        HL7Field aField;

        aTemp = getSegment(MSH_segment_id);
        if (aTemp.indexOf(MSH_segment_id) == 0) {
            aSegment = new HL7Segment(aTemp);
            aField = new HL7Field(aSegment.getField(MSH_9_message_type), k.CARROT_GET, k.CARROT_SET);
            aResult = aField.getSubField(CM_EVENT);
        }
        return aResult;
    }

    /**
     * Returns True/False depending on if the current HL7 messages type is contained within [pEventList]<p>
     * @return true if Event Type is contained in pEventList else false.
     * @param pEventList List of one or more HL7 Event Types that are acceptable.<p>
     * e.g. "A01, A02, A03, A08, A11, A12, A13".
     */
    public boolean isEvent(String pEventList) {
        //Returns the Trigger event for the message
        boolean aResult = false;

        String aEvent = getTriggerEvent();
        if (aEvent.length() > 0) {
            if (pEventList.indexOf(aEvent) >= 0) {
                aResult = true;
            } else {
                aResult = false;
            }
        }
        return aResult;
    }

    /**
     * Trims trailing "|" characters from all segments in a message
     * @param pMessage HL7 message text string
     * @return Returns a null or the original input string
     */
    public String trimMessage(String pMessage) {
        //trimMessage(string) returns a re-formated string with or without trailing pipe characters

        String aResult = k.NULL;
        int aMsgLength;
        int aMsgCharCount;
        String aTempMessage = k.NULL;
        String aChar;
        boolean aSegmentDelimiter;
        int aFieldDelimiterCount;

        if (mTrimFlag != k.NO_TRIM) {
            aMsgLength = pMessage.length();
            if (mTrimFlag == k.TRIM_LAST) {
                //Trim the last pipe character at end of each segment
                aSegmentDelimiter = false;
                aFieldDelimiterCount = 0;
                for (aMsgCharCount = aMsgLength; aMsgCharCount > 0; aMsgCharCount--) {
                    aChar = pMessage.substring(aMsgCharCount - 1, aMsgCharCount);
                    if (aChar.equalsIgnoreCase(mSeparatorSet)) {
                        aSegmentDelimiter = true;
                    }
                    if (aChar.equalsIgnoreCase(k.PIPE_SET) && aSegmentDelimiter == true) {
                        //Do nothing - removing this last pipe
                        aSegmentDelimiter = false;
                    } else if (aChar.equalsIgnoreCase(k.PIPE_SET) && aMsgCharCount == aMsgLength) {
                        //Do nothing - removing this last pipe
                    } else {
                        aTempMessage = aChar.concat(aTempMessage);
                        if (!aChar.equalsIgnoreCase(mSeparatorSet)) {
                            aSegmentDelimiter = false;
                        }
                    }
                }
                aResult = aTempMessage;
            } else if (mTrimFlag == k.TRIM_ALL) {
                //Trim all trailing pipe characters at end of each segment
                aSegmentDelimiter = false;
                aFieldDelimiterCount = 0;
                for (aMsgCharCount = aMsgLength; aMsgCharCount > 0; aMsgCharCount--) {
                    aChar = pMessage.substring(aMsgCharCount - 1, aMsgCharCount);
                    if (aChar.equalsIgnoreCase(mSeparatorSet)) {
                        aSegmentDelimiter = true;
                    }
                    if (aChar.equalsIgnoreCase(k.PIPE_SET) && aSegmentDelimiter == true) {
                        //Do nothing - removing this last pipe
                    } else if (aChar.equalsIgnoreCase(k.PIPE_SET) && aMsgCharCount == aMsgLength) {
                        //Do nothing - removing this last pipe
                        aSegmentDelimiter = true;
                    } else {
                        aTempMessage = aChar.concat(aTempMessage);
                        if (!aChar.equalsIgnoreCase(mSeparatorSet)) {
                            aSegmentDelimiter = false;
                        }
                    }
                }
                aResult = aTempMessage;
            }
        } else {
            //No trimming required
            aResult = pMessage;
        }
        return aResult;
    }

    /**
     * append(pItem) appends pItem to the end of the item message separated by a
     * delimiter
     * @param pItem The item to be appended
     */
    public void append(String pItem) {
        if (!pItem.equalsIgnoreCase(k.NULL)) {
            if (mData.endsWith(mSeparatorSet) || mData.equalsIgnoreCase(k.NULL)) {
                if (!pItem.endsWith(mSeparatorSet)) {
                    mData = mData.concat(pItem).concat(mSeparatorSet);
                } else {
                    mData = mData.concat(pItem);
                }
            } else {
                if (!pItem.endsWith(mSeparatorSet)) {
                    mData = mData.concat(mSeparatorSet).concat(pItem).concat(mSeparatorSet);
                } else {
                    mData = mData.concat(mSeparatorSet).concat(pItem);
                }
            }
        }
    }

    /**
     * Appends a HL7Segment to the end of the message
     * @param aSegment HL7Segment class
     */
    public void append(HL7Segment aSegment) {
        append(aSegment.getSegment());
    }

    /**
     * Appends a HL7Group to the end of the message
     * @param aGroup HL7Group class
     */
    public void append(HL7Group aGroup) {
        append(aGroup.getGroup());
    }

    /**
     * getRepeatSegment returns a string array containing a sequence of similar
     * segments for a particular occurence number
     * @param pSegmentID Segment identifier
     * @param pOccurenceNum Occurence number
     * @return String array of segments
     */
    public String[] getRepeatSegment(String pSegmentID, int pOccurenceNum) {
        String aTemp = k.NULL;
        String aArray[] = mData.split(k.CARRIAGE_RETURN);
        int aSegmentCount = 0;
        int aRepeatSegCount = 0;
        boolean aFound = false;
        for (int i = 0; i < aArray.length; i++) {
            if (aArray[i].startsWith(pSegmentID)) {
                if (aFound == false) {
                    aRepeatSegCount++;
                }
                aFound = true;
                if (pOccurenceNum == aRepeatSegCount) {
                    if (aTemp.equalsIgnoreCase(k.NULL)) {
                        aTemp = aArray[i];
                    } else {
                        aTemp = aTemp.concat(k.CARRIAGE_RETURN).concat(aArray[i]);
                    }
                }
            } else {
                aFound = false;
            }
        }
        String aResult[] = aTemp.split(k.CARRIAGE_RETURN);
        return aResult;
    }
}