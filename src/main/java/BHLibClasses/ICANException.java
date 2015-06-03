/*
 * ICANException.java
 *
 * Created on 23 October 2005, 11:56
 *
 */

package BHLibClasses;

/**
 * ICANException provides basic error handling methods.
 * @author Norman Soh
 */
public class ICANException extends Exception {

    public String mEnvironment = "";
    /**
     * Error text description
     */
    public String mErrorText = null;
    /**
     * Error code defined as:
     * [Error Type][3 digit error number]
     * Examples: E001, F239, W777
     */
    public String mErrorCode = null;
    /**
     * Creates a new instance of ICANException
     */
    public ICANException(String pEnvironment) {
        mEnvironment = pEnvironment;
    }

    /**
     * Creates an instance of ICANException with an error code set
     * @param pErrorCode Error code
     */
    public ICANException(String pErrorCode, String pEnvironment) {
        mEnvironment = pEnvironment;
        mErrorCode = pErrorCode;
        setErrorText();
    }

    /**
     * Sets the error code - overides the error code that was previously set
     * @param pErrorCode Error code
     */
    public void setErrorCode(String pErrorCode) {
        mErrorCode = pErrorCode;
        setErrorText();
    }

    /**
     * Retrieves the error text description for the error code using the CodeLookUp class
     */
    public void setErrorText() {
        CodeLookUp aCode = new CodeLookUp("ICANError.table", mEnvironment);
        mErrorText = aCode.getValue(mErrorCode);
    }

    /**
     * Returns the error text
     * @return Error text description
     */
    public String getErrorText() {
        return mErrorText;
    }

    /**
     * Returns the error code
     * @return Error code value
     */
    public String getErrorCode() {
        return mErrorCode;
    }

    /**
     * Returns the error type
     * @return Error type
     */
    public String getErrorType() {
        return mErrorCode.substring(0,1);
    }
}
