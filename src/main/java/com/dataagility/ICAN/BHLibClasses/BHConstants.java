/*
 * Constants.java
 *
 * Created on 4 October 2005, 12:26
 *
 */

package com.dataagility.ICAN.BHLibClasses;

/**
 * This class contains constants to be referenced from other classes
 * @author sohn
 */
public class BHConstants {

    /**
     * Defines the version number
     */
    public String VERSION_NUMBER = "C";

    /** Creates a new instance of Constants */
    public BHConstants() {
    }
    /**
     * Defined as ""
     */
    public final String NULL = "";
    /**
     * Defined as "\r"
     */
    public final String CARRIAGE_RETURN = "\r";
    /**
     * Defined as "\n"
     */
    public final String NEW_LINE = "\n";
    /**
     * Defined as "\\|"
     */
    public final String PIPE_GET = "\\|";
    /**
     * Defined as "|"
     */
    public final String PIPE_SET = "|";
    /**
     * Defined as "\\^"
     */
    public final String CARROT_GET = "\\^";
    /**
     * Defined as "^"
     */
    public final String CARROT_SET = "^";
    /**
     * Defined as "\\&"
     */
    public final String AMPERSAND_GET = "\\&";
    /**
     * Defined as "&"
     */
    public final String AMPERSAND_SET = "&";
    /**
     * Defined "\\~"
     */
    public final String REPEAT_GET = "\\~";
    /**
     * Defined "~"
     */
    public final String REPEAT_SET = "~";


    /**
     * Defined as 0
     */
    public final int NO_TRIM = 0;
    /**
     * Defined as 1
     */
    public final int TRIM_LAST = 1;
    /**
     * Defined as 2
     */
    public final int TRIM_ALL = 2;

    /**
     * Defined as "--->"
     */
    public final String ARROW_RIGHT = "--->";
    /**
     * Defined as "-"
     */
    public final String HYPHEN = "-";
    /**
     * Defined as "_"
     */
    public final String UNDERSCORE = "_";
    /**
     * The directory where ICAN log files are created
     */
    public final String LOG_FILE_PATH = "c:\\ICANLog\\";

    /**
     * The directory with code tables are stored
     */
    public final String CODE_LOOKUP_PATH = "C:\\ICANConfig\\ICAN.ini";
    /**
     * Defined as ","
     */
    public final String COMMA = ",";
    /**
     * Defined as "%default%"
     */
    public final String DEFAULT_VALUE = "%default%";
    /**
     * Defined as "="
     */
    public final String EQUAL_SIGN = "=";

}
