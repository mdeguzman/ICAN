package com.dataagility.ICAN.BHLibClasses;

import java.text.*;
import java.util.*;

/**
 * This class provides the basic methods to manipulate items in a message
 */
abstract class HL7Data {
    //This abstract class contains the basic elements/functions for HL7 message
    //  string manipulation

    //Authors: Ray Fillingham and Norman Soh
    //Organisation: The Alfred
    //Year: 2005

    /**
     * Constants
     */
    BHConstants k = new BHConstants();

    /**
     * The separator used to split items into an array
     */
    protected String mSeparatorGet;
    /**
     * The separator used to join items
     */
    protected String mSeparatorSet;
    /**
     * The data held for manipulation
     */
    protected String mData;
    /**
     * Holds the type of trimming required
     */
    protected int mTrimFlag;
    /**
     * Counter pointing to the current item in the message for manipulation
     */
    protected int mItemNum;
    /**
     * HL7Data class object
     */
    protected HL7Data mLinkTo;

    /**
     * Constructor
     */
    protected HL7Data() {
        //Empty contructor - Actual initialisation achieved in inherited class

        //Use the following constructor syntax below in sub-classes using this
        //  abstract class:

        //  protected <sub-class name>(String pSeparator, String pData, int pTrimFlag) {
        //      mSeparator = pSeparator;
        //      mData = pData;
        //      mTrimFlag = pTrimFlag;
        //      mItemNum = 1;
        //  }
    }

    /**
     * getItem() returns the Current item in the string defined by the Separator
     * @return Returns the current item
     */
    protected String getItem() {

        String aResult = k.NULL;
        String aArray[] = mData.split(mSeparatorGet);
        int aArrayCount = aArray.length;

        //Check mItemNum coming from getNextItem()
        if (mItemNum > aArrayCount) {
            mItemNum = aArrayCount + 1;
            aResult = k.NULL;
        } else {
            aResult = aArray[mItemNum - 1];
        }
        return aResult;
    }

    /**
     * getItem(int pItemNum) returns the <pItemNum> item in the string defined by the Separator
     * @return Returns the current item
     * @param pItemNum Item Number
     */
    protected String getItem(int pItemNum) {

        mItemNum = pItemNum;
        return getItem();
    }

    /**
     * getNextItem() returns the Next item in the string defined by the Separator
     * @return Returns the next item
     */
    protected String getNextItem() {
        //getNextItem() will return the next item in the list.  The current item
        //is specified by the internal item counter, mItemNum.

        mItemNum++;
        return getItem();
    }

    /**
     * getData() returns the source Data string
     * @return Returns the contents
     */
    protected String getData() {
        //getData() will return the original string

        return mData;
    }

    /**
     * setData() sets the source Data string
     * @param pData Data
     */
    protected void setData(String pData) {
        //setData(string) will set the original string with new content

        mData = pData;
    }

    /**
     * countItems() retuns a count of the number of seperated items contained in the source Data string.
     * NOTE: A non seperator terminated item at the end of the string, still counts as an item.
     * @return Returns the number of items
     */
    protected int countItems() {
        //countItems() returns the number of occurrence of items

        int aItemCount = 0;
        int aPos = mData.indexOf(mSeparatorSet);
        while (aPos >= 0) {
            aItemCount++;
            aPos = mData.indexOf(mSeparatorSet, aPos + 1);
        }

//        int aStringLength = mData.length();
//        int aItemCount = 0;
//        String aCharacter = k.NULL;
//        for (int i = 0; i < aStringLength; i++) {
//            aCharacter = mData.substring(i, i + 1);
//            if (aCharacter.equalsIgnoreCase(mSeparatorSet)) {
//                aItemCount++;
//            }
//        }
        if (!mSeparatorSet.equalsIgnoreCase(k.CARRIAGE_RETURN)) {
            aItemCount++;
        }
        return aItemCount;
    }

    /**
     * setItem(pItem) set/replace the current item in the list with new contents <pItem>.
     * @param pItem Item
     */
    protected void setItem(String pItem) {
        setItem(pItem, mItemNum);
    }

    /**
     * setItem(pItem, pItemNum) set/replace the <pItenNum> item in the list with new contents <pItem>.
     * @param pItem Item
     * @param pItemNum Item Number
     */
    protected void setItem(String pItem, int pItemNum) {
        //setItem(string, int) will replace an item in the list with new contents

        String aResult = k.NULL;
        String aArray[] = mData.split(mSeparatorGet);
        int aArrayCount = aArray.length;
        int aItemCount = countItems();
        int aArraySize = 0;
        int i = 0;

        //Trim trailing spaces from pItem
        boolean aContinue = true;
        while (aContinue) {
            if (pItem.endsWith(" ")) {
                pItem = pItem.substring(0, pItem.length() - 1);
            }  else {
                aContinue = false;
            }
        }

        if (pItemNum > aItemCount) {
            aArraySize = pItemNum;
        } else {
            aArraySize = aItemCount;
        }
        String aArrayTemp[] = new String[aArraySize];

        //Initialise array to null string
        for (i = 0; i < aArrayTemp.length; i++) {
            aArrayTemp[i] = k.NULL;
        }
        //Fill up temp array
        for (i = 0; i < aArray.length; i++) {
            aArrayTemp[i] = aArray[i];
        }
        //Set item in the correct location
        aArrayTemp[pItemNum - 1] = pItem;
        //Create result string
        for (i = 0; i < aArrayTemp.length; i++) {
            if (i == 0) {
                aResult = aResult.concat(aArrayTemp[i]);
            } else {
                aResult = aResult.concat(mSeparatorSet).concat(aArrayTemp[i]);
            }
        }
        if (mSeparatorSet.equalsIgnoreCase(k.CARRIAGE_RETURN)) {
            aResult = aResult.concat(mSeparatorSet);
        }
        mData = aResult;
        mItemNum = pItemNum;
    }

    /** clearItem() clears the Current item in the list of items.  */
    protected void clearItems() {
        //clearItems() will clear the current item in the list

        setItem(k.NULL);
    }

    /**
     * clearItem(pItemNum) clears the <pItemNum> item in the list of items.
     * @param pItemNum Item Number
     */
    protected void clearItems(int pItemNum[]) {

        int aCounter = 0;
        int aItemNum;
        String aDataOrig = mData;

        try {
            for (aCounter = 0;; aCounter++) {
                aItemNum = pItemNum[aCounter];
                if (aItemNum <= countItems()) {
                    setItem(k.NULL, aItemNum);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //Do nothing - found end of array - exit method
        }
    }

    /**
     * clearItem(pItemIDm) clears the item idendified by its name <pItemID>.
     * @param pItemID Item ID
     */
    protected void clearItems(String pItemID[]) {
        //clearItems(string) will clear an item specified by a item name
        //as opposed to a item number

        int aCounter = 0;
        int aItemNum = 0;
        String aItemStr;
        HL7FieldDescriptor aFD;

        try {
            for (aCounter = 0;; aCounter++) {
                aItemStr = pItemID[aCounter];
                aFD = new HL7FieldDescriptor(aItemStr);
                if (aFD.subSubFieldNum > 0) {
                    aItemNum = aFD.subSubFieldNum;
                } else if (aFD.subFieldNum > 0) {
                    aItemNum = aFD.subFieldNum;
                } else if (aFD.fieldNum > 0) {
                    aItemNum = aFD.fieldNum;
                }
                if (aItemNum > 0 && aItemNum <= countItems()) {
                    setItem(k.NULL, aItemNum);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //Do nothing - found end of array - exit method
        }
    }

    /**
     * linkTo( pHL7Data) creates a link to another object instance of this Class for use when copying items between objects.
     * @param pHL7Data HL7Data class object
     */
    public void linkTo(HL7Data pHL7Data) {
        //linkTo() copies HL7Data object to another HL7Data instance

        mLinkTo = pHL7Data;
    }

    /**
     * copyItems() copies all items from the linked(see linkTo) HL7Data Object to
     * this Object.
     */
    protected void copyItems() {
        //copyItems() copies all items from the 2nd HL7Data instance to the 1st

        mData = mLinkTo.mData;
    }

    /**
     * copyItems(pItemNum[]) copies all items idenified by their <pItemNUm> from the linked(see linkTo)
     * HL7Data Object to this Object.
     * @param pItemNum Item number
     */
    protected void copyItems(int pItemNum[]) {
        //copyItems(int) copies only the specified item from the 2nd HL7Data
        //instance to the 1st in the same location

        int aCounter = 0;
        int aItemNum;

        try {
            for (aCounter = 0;; aCounter++) {
                aItemNum = pItemNum[aCounter];
                setItem(mLinkTo.getItem(aItemNum), aItemNum);
            }
        } catch (IndexOutOfBoundsException e) {
            //Do nothing found - found end of array - exit method
        }
    }

    /**
     * copyItems(pItemNum[]) copies all items identified by their <pItemID> from the linked(see linkTo)
     * HL7Data Object to this Object.
     * @param pItemID Item ID
     */
    protected void copyItems(String pItemID[]) {
        //copyItems(int) copies only the specified item name from the 2nd HL7Data
        //instance to the 1st in the same location

        int aCounter = 0;
        String aItemStr;
        HL7FieldDescriptor aFD;
        int aItemNum = 0;

        try {
            for (aCounter = 0;; aCounter++) {
                aItemStr = pItemID[aCounter];
                aFD = new HL7FieldDescriptor(aItemStr);
                if (aFD.subSubFieldNum > 0) {
                    aItemNum = aFD.subSubFieldNum;
                } else if (aFD.subFieldNum > 0) {
                    aItemNum = aFD.subFieldNum;
                } else if (aFD.fieldNum > 0) {
                    aItemNum = aFD.fieldNum;
                }
                if (aItemNum > 0) {
                    setItem(mLinkTo.getItem(aItemNum), aItemNum);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //Do nothing - found end of array - exit method
        }
    }

    /**
     * getDateTime() will return the current time formatted as "yyyyMMddHHmm"
     * @return Returns the current date and time
     */
    public String getDateTime() {
        return getDateTime("yyyyMMddHHmm");
    }

    /**
     * getDateTime(pFormat) will return the current time formatted according to a
     * format string passed as [pFormat].
     *
     * Format string structure as follows;
     *
     * yyyy = Year
     * MM = Month
     * dd = Day
     * HH = Hour
     * mm = Minute
     * ss = Second
     * SSS = Milisecond
     * @param pFormat DateTime format string (e.g. "yyyyMMddHHmmss")
     * @return Returns a formatted datetime string
     */
    public String getDateTime(String pFormat) {
        Format aDateTimeFormat = new SimpleDateFormat(pFormat);
        Date aDate = new Date();
        String aResult = aDateTimeFormat.format(aDate);
        return aResult;
    }

    /**
     * Formats a string to include version number and date time stamp.  Version number
     * contains BHLib package version plus Class version number.
     * @param pVersionNum Version number
     * @return Formated version number plus date time stamp
     */
    public String getVerDateTime(String pVersionNum) {
        return k.VERSION_NUMBER.concat(pVersionNum).concat(getDateTime("MMddHHmmssSSS"));
    }
}