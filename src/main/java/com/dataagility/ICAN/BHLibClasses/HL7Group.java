package com.dataagility.ICAN.BHLibClasses;

/**
 * HL7 Group provides methods to manipulate a group of segments
 */
public class HL7Group extends HL7Data {
    //This class contains field specific methods for HL7 field
    //  string manipulation

    //Authors: Ray Fillingham and Norman Soh
    //Organisation: The Alfred
    //Year: 2005

    /**
     * This constructor creates a group with the specified segments passed through
     * as a parameter
     * @param pMessage The message text string containing a group of segments
     * @param pTrimFlag The trim flag indicator
     */
    public HL7Group(String pMessage, int pTrimFlag) {
        //Initialises HL7Group with segment separator pre-defined

        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = pTrimFlag;
        mItemNum = 1;
        mData = trimMessage(pMessage);
    }

    /**
     * Initialises a HL7Group with a message text string.  No trimming is default
     * @param pMessage Message text string
     */
    public HL7Group(String pMessage) {
        //Initialises HL7Group with segment separator pre-defined

        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = k.NO_TRIM;
        mItemNum = 1;
        mData = trimMessage(pMessage);
    }

    /** This constructor creates a blank group used to assemble a number of segments together.
     */

    public HL7Group() {
        mSeparatorSet = k.CARRIAGE_RETURN;
        mSeparatorGet = k.CARRIAGE_RETURN;
        mTrimFlag = k.NO_TRIM;
        mItemNum = 1;
        mData = k.NULL;
    }

    /**
     * Gets the group of segment
     * @return Returns the group of segment
     */
    public String getGroup() {

        if (mTrimFlag == k.TRIM_ALL) {
            return trimMessage(getData());
        } else {
            return getData();
        }
    }

    /**
     * Sets the group of segment
     * @param pGroup A group text string of segments
     */
    public void setGroup(String pGroup) {

        setData(pGroup);
    }

    /**
     * Gets a segment from the group
     * @return A segment text string
     */
    public String getSegment() {

        return getItem();
    }

    /**
     * Gets a segment from specified location
     * @return A segment text string
     * @param pSegmentNum Position or location number
     */
    public String getSegment(int pSegmentNum) {

        return getItem(pSegmentNum);
    }

    /**
     * Gets the first occurrence of a segment with a specific segment ID
     * @return A segment text string
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
     * Gets the nth occurrence of a segment with a specific segment ID
     * @return A segment text string
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
     * Gets the next segment
     * @return A segment text string
     */
    public String getNextSegment() {

        return getNextItem();
    }

    /**
     * Sets a segment to a specified value at the current location
     * @param pSegment Segment text string
     */
    public void setSegment(String pSegment) {

        setItem(pSegment);
    }

    /**
     * Sets a segment to a specified value at a specified location
     * @param pSegment Segment text string
     * @param pSegmentNum Segment position number
     */
    public void setSegment(String pSegment, int pSegmentNum) {

        setItem(pSegment, pSegmentNum);
    }

    /**
     * Count the total number of segments
     * @return Returns the number of segments
     */
    public int countSegments() {

        return countItems();
    }

    /**
     * Count the total number of segments with a specific segment ID
     * @return Returns the number of segments
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

    /**
     * Copies all segments from a linked segment
     */
    public void copySegments() {

        copyItems();
    }

    /**
     * Copies a specific number of segments contained in an array of segment positions
     * from a linked segment
     * @param pSegmentNum Segment position number array
     */
    public void copySegments(int pSegmentNum[]) {

        copyItems(pSegmentNum);
    }

    /**
     * Copies a specific number of segments contained in an array of segment IDs
     * from a linked segment
     * @param pSegmentID Segment HL7 identifier string array
     */
    public void copySegments(String pSegmentID[]) {

        copyItems(pSegmentID);
    }

    /**
     * Clears a segment at the current location
     */
    public void clearSegments() {

        clearItems();
    }

    /**
     * Clears a specific number of segments contained in an array of segment positions
     * @param pSegmentNum Segment position number array
     */
    public void clearSegments(int pSegmentNum[]) {

        clearItems(pSegmentNum);
    }

    /**
     * Copies a specific number of segments contained in an array of segment IDs
     * @param pSegmentID Segment HL7 identifier string array
     */
    public void clearSegments(String pSegmentID[]) {

        clearItems(pSegmentID);
    }

    /**
     * Trims trailing "|" characters from all segments
     * @return A message text string
     * @param pMessage Message text string
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
     * Appends pItem to the end of the item message separated by a
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
     * Appends a HL7Segment to the end of the group
     * @param aSegment HL7Segment class
     */
    public void append(HL7Segment aSegment) {
        append(aSegment.getSegment());
    }

    /**
     * Appends a HL7Group to the end of the group
     * @param aGroup HL7Group class
     */
    public void append(HL7Group aGroup) {
        append(aGroup.getGroup());
    }
}
