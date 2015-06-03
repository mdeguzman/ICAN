/*
 * ProcessErrorMessage.java
 *
 * Created on 31 January 2006, 11:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package BHLibClasses;

import java.util.*;
import java.text.*;

/**
 *
 * @author sohn
 */
public class ProcessErrorMessage {
    public String mCollborationName = "";
    public String mStatusCode = "";
    public String mErrorText = "";
    public String mUFDControlID = "";

    /** Creates a new instance of ProcessErrorMessage */
    public ProcessErrorMessage(String pHL7Message, String pCollaborationName, String pStatusCode, String pErrorText) {
        mCollborationName = pCollaborationName;
        mStatusCode = pStatusCode;
        mErrorText = pErrorText;
        HL7Message aInMessage = new HL7Message(pHL7Message);
        HL7Segment aInMSHSegment = new HL7Segment( aInMessage.getSegment( "MSH" ) );
        mUFDControlID = aInMSHSegment.get( HL7_23.MSH_10_message_control_ID );
    }

    public ProcessErrorMessage(String pCollaborationName, String pStatusCode, String pErrorText) {
        mCollborationName = pCollaborationName;
        mStatusCode = pStatusCode;
        mErrorText = pErrorText;
        mUFDControlID = "";
    }

    public HL7Message getErrorMessage() {
        HL7Message aOutErrorMessage = new HL7Message( "" );
        HL7Segment aOutErrorMSHSegment = new HL7Segment( "MSH" );
        aOutErrorMSHSegment.set( HL7_23.MSH_2_encoding_characters, "^~\\&" );
        aOutErrorMSHSegment.set( HL7_23.MSH_3_sending_application, mCollborationName );
        aOutErrorMSHSegment.set( HL7_23.MSH_4_sending_facility, "STATUS" );
        Format aDateTimeFormat = new SimpleDateFormat( "yyyyMMddHHmm" );
        Date aDate = new Date();
        String aDateResult = aDateTimeFormat.format( aDate );
        aOutErrorMSHSegment.set( HL7_23.MSH_7_message_date_time, aDateResult );
        aOutErrorMSHSegment.set( HL7_23.MSH_9_message_type, mStatusCode );
        aOutErrorMSHSegment.set( HL7_23.MSH_10_message_control_ID,  mUFDControlID);
        aOutErrorMSHSegment.set( HL7_23.MSH_11_processing_ID, "P" );
        aOutErrorMSHSegment.set( HL7_23.MSH_12_version_ID, "2.3" );
        HL7Segment aOutErrorZERSegment = new HL7Segment( "ZER" );
        aOutErrorZERSegment.set( "ZER_1", mErrorText );
        aOutErrorMessage.append( aOutErrorMSHSegment );
        aOutErrorMessage.append( aOutErrorZERSegment );

        return aOutErrorMessage;
    }
}
