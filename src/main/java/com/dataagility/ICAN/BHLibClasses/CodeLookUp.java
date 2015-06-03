package com.dataagility.ICAN.BHLibClasses;
import java.io.*;

//Authors: Ray Fillingham and Norman Soh
//Organisation: The Alfred
//Year: 2005

/**
 * CodeLookUp compares a [code] against a two column [table] and, if a match occurs with
 * an entry in the first column it returns the values from the 2nd column.
 *
 * If the last entry in the table file is ....<p>
 * %default%,a_value<p>
 *
 * ... then a_value will be returned if no other match was found.<p>
 * If there is no %default% defined then the original [code] will be returned as the value.
 *
 *NOTE: Any spaces contained within the table columns will be considered as part
 * of the [code} or value (i.e. no space trimming occurs).
 *
 *The table file has the format:-<p>
 * index_code_1,value_code_1<p>
 * index_code_2,value_code_2<p>
 * index_code_3,value_code_3<p>
 * index_code_4,value_code_4<p>
 * .....<p>
 * index_code_n,value_code_n<p>
 * %default%,a_value<p>
 */

public class CodeLookUp {

    private String mFileName;       // Name of the table file
    private String mTablePath;      // Path where the table file is located
    private String mCode;           // Code to be looked up
    private String mValue;          // Value to be returned
    private String mFacility;       // Facility i.e. ALF, CGMC, SDMH ... used when Facility specific translation required
    BHConstants k = new BHConstants();

    /**
     * Creates a new instance of CodeLookUp
     * @param pTableName Name of Table to be used for translations.
     * NOTE:- Assumes the path for pTableName is defined in "C:\ICANConfig\ICAN.ini" file
     */
    public CodeLookUp(String pTableName, String pEnvironment) {
        mTablePath = ReadDefaultPath("CodeTableLookupPath").concat(pEnvironment).concat("\\");
        mFileName = pTableName;
        mFacility = k.NULL;
    }

    /**
     * Creates a new instance of CodeLookUp
     * @param pTableName the name of the Table to be used for translations.
     * @param pFacility is used when a Facility specific table is required (e.g. "ALF-PARIS-UNIT.table").
     */
    public CodeLookUp(String pTableName, String pFacility, String pEnvironment) {
        mTablePath = ReadDefaultPath("CodeTableLookupPath").concat(pEnvironment).concat("\\");
        mFacility = pFacility  + k.HYPHEN;
        mFileName = pTableName;
    }

    /**
     * Look up [pCode} in selected Table and return the translated value
     * @return Returns value from lookup table.
     * @param pCode Code to be looked up in table.
     */
    public String getValue(String pCode) {

        mCode = pCode;
        String aValue = ReadTableValue();
        return(aValue);
    }

//-------------------------------------------------------------------------------
    /**
     * Look up [pCode} in selected Table and return the translated value
     * @return Returns value from lookup table.
     * @param pCode Code to be looked up in table.
     */

    private  String ReadTableValue(){
        try {
            FileReader aFile = new FileReader(mTablePath.concat(mFacility.concat(mFileName)));
            BufferedReader aBuff = new BufferedReader(aFile);

            boolean eof = false;
            mValue = mCode;     // Default if we dont find a match.
            while (!eof){
                String aLine = aBuff.readLine();
                if (aLine == null) {
                    eof = true;
                } else {
                    String[] aTableRow = aLine.split("\\|");
                    if ((aTableRow[0].equalsIgnoreCase(mCode)) || (aTableRow[0].equalsIgnoreCase(k.DEFAULT_VALUE))) {
                        if (aTableRow.length > 1) {
                            mValue = aTableRow[1];
                        } else {
                            mValue = k.NULL;
                        }
                        break;
                    }
                }
            }
            aBuff.close();
        } catch (IOException e) {
            System.out.println("Error ..." + e.toString() + " " + mTablePath.concat(mFileName));
            mValue = null;
        }
        return (mValue);
    }
    /** Locate the drive and path for [pPathRequired] in ...<p>
     *  ... The configuration parameters held in "ICAN.ini" in "C:\\ICANConfig" */

    private  String ReadDefaultPath(String pPathRequired){
        String aPath = k.NULL;

        try {
            FileReader aFile = new FileReader(k.CODE_LOOKUP_PATH);
            BufferedReader aBuff = new BufferedReader(aFile);

            boolean eof = false;
            while (!eof){
                String aLine = aBuff.readLine();
                if (aLine == null) {
                    eof = true;
                } else {
                    String[] aConfigItem = aLine.split(k.EQUAL_SIGN);
                    if ((aConfigItem[0].equalsIgnoreCase(pPathRequired))) {
                        if (aConfigItem.length > 1) {
                            aPath = aConfigItem[1];
                        } else {
                            aPath = k.NULL;
                        }
                        break;
                    }
                }
            }
            aBuff.close();
        } catch (IOException e) {
            System.out.println("Error ..." + e.toString() + " " + mTablePath.concat(mFileName));
            aPath = null;
        }
        return (aPath);
    }

}
