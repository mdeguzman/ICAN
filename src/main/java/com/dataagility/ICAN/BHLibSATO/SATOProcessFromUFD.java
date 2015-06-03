/*
 * SQLProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.dataagility.ICAN.BHLibSATO;

import com.dataagility.ICAN.BHLibClasses.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

//import com.dataagility.ICAN.BHLibSQL.*;



/**
 *
 * @author fillinghamr
 */
public class SATOProcessFromUFD extends ProcessSegmentsFromUFD {

    public String mEnvironment = "";
    public String aPatientLocation = "";

    /**
     * Creates a new instance of SQLProcessFromUFD
     */
    public SATOProcessFromUFD(String pHL7Message, String pEnvironment) throws ICANException {
        super(pHL7Message);
        mVersion = "A";
        mEnvironment = pEnvironment;
    }

    public String[] processMessage() throws ICANException {
        String aSQLMessageArray[] = {k.NULL, k.NULL, k.NULL};
        String aSegment;
        HL7Group aGroup;
        HL7Message aInMess = new HL7Message(mHL7Message);
        String aOutMess = new String();
        HL7Segment aMSHSegment = new HL7Segment(aInMess.getSegment(HL7_23.MSH));
        HL7Segment aPV1SegmentTemp = new HL7Segment(aInMess.getSegment("PV1"));
        mFacility = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
        aPatientLocation = aPV1SegmentTemp.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
        CodeLookUp locations = new CodeLookUp("SATO_Locations.table", mEnvironment);
        String lookup = locations.getValue(aPatientLocation);
        //changed---------------------------------------------------------------
        //checking admission date against current date
        // get admission date
       // HL7Segment dateSeg = new HL7Segment( aInMess.getSegment( "PV1" ) );
      //  String admissionDate = dateSeg.get( HL7_23.PV1_44_admit_date_time );
        //------------------------------------------------------------------------------------
        //get message type
      //  HL7Segment typeSeg = new HL7Segment(aInMess.getSegment("MSH"));
      //  String messType = typeSeg.get(HL7_23.MSH_9_message_type);
       // String a01 = "ADT^A01";
        //------------------------------------------------------------------------------------
        //ckecking if admission date is empty and if message is an admit message
      //  if(admissionDate != null && messType.equals(a01)){
        // get current date
      //  Calendar cal = Calendar.getInstance();
       // SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );
       // String currentDate = dateFormat.format( cal.getTime() );
      //  Date admission = null;
      //  Date current = null;
      //  try {
       //     admission = dateFormat.parse(admissionDate);
       //     current = dateFormat.parse( currentDate );
      //  } catch (ParseException ex) {
      //      Logger.getLogger(SATOProcessFromUFD.class.getName()).log(Level.SEVERE, null, ex);
      //  }
         // compare if date of admission is in the past
        //if (current.equals( admission ) || current.after( admission )) {
      //  if(!(admission.before(current))){
        //----------------------------------------------------------------------
        if (aInMess.isEvent("A01")&&
                !aPatientLocation.equals(lookup)) {
            aOutMess = getCSV(mHL7Message);
            aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aSQLMessageArray[1] = mFacility;
            aSQLMessageArray[2] = aOutMess;
   //     }
    //    }
        //changed---------------------------------------------------------------
       // }else{
       //     if (aInMess.isEvent("A01")&&
       //         !aPatientLocation.equals(lookup)) {
        //    aOutMess = getCSV(mHL7Message);
       //     aSQLMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
        //    aSQLMessageArray[1] = mFacility;
       //     aSQLMessageArray[2] = aOutMess;
     //   }
        }
        //----------------------------------------------------------------------
        return aSQLMessageArray;
    }
    /**
     * SQL specific processing for an Outgoing i.e "To" PID segment where the PID is in the [pHL7MessageBlock] .
     * @param pHL7MessageBlock Either a full HL7 Message or may just be an A17 Patient Group.
     * @return Returns the processed HL7 PID segment as a String.
     */
    public String getCSV(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegmentIN = new HL7Segment(NULL);
        HL7Segment aPIDSegmentOUT = new HL7Segment("PID");

        aPIDSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
//        mHL7Segment = aPIDSegmentIN;

        /* Get UR*/
        String aTmpField[] = aPIDSegmentIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        int i;
        HL7Field aInField;
        String aStr;
        CodeLookUp aLookUp = new CodeLookUp("SDMH_6To7Digit_UR.table", mEnvironment);
        String UR = "";
        for (i = 0; i < aTmpField.length; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                UR = aInField.getSubField(HL7_23.CX_ID_number);
                if (mFacility.equalsIgnoreCase("SDMH") &&
                        Integer.parseInt(UR) < 899546) {
                    UR = aLookUp.getValue(UR);
                //aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aSDMHIdentifier);
                }
            }
        }
        /* Finish GET UR */
        String familyName = aPIDSegmentIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name);
        String givenName = aPIDSegmentIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name);
        String dob = aPIDSegmentIN.get(HL7_23.PID_7_date_of_birth);
        String ph = new String();
        if (!aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number)) {
            ph = aPIDSegmentIN.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number);
        } else {
            ph = "-";
        }
        String gender = aPIDSegmentIN.get(HL7_23.PID_8_sex);

        String age = calculateAge(dob);
        /* Get address */
        String aTmpAddField[] = aPIDSegmentIN.getRepeatFields(HL7_23.PID_11_patient_address);
        //String address = aTmpAddField[0];
        HL7Field addressField = new HL7Field(aTmpAddField[0]);
        StringBuffer address = new StringBuffer();
        address.append(addressField.getSubField(HL7_23.XAD_street_1) + ",");
        address.append(addressField.getSubField(HL7_23.XAD_street_2) + ",");
        address.append(addressField.getSubField(HL7_23.XAD_city) + ",");
        address.append(addressField.getSubField(HL7_23.XAD_state_or_province) + ",");
        address.append(addressField.getSubField(HL7_23.XAD_zip));


        HL7Segment aPV1SegmentIN = new HL7Segment(NULL);
        aPV1SegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));

        String finClass = aPV1SegmentIN.get(HL7_23.PV1_20_financial_class);

        /* finish get address */
        StringBuffer csvLine = new StringBuffer();
        //csvLine.append("\"");
        csvLine.append(UR + "\t");
        csvLine.append(mFacility + "\t");
        csvLine.append(familyName + "\t");
        csvLine.append(givenName + "\t");
        csvLine.append(dob + "\t");
        csvLine.append(age + "\t");
        csvLine.append(ph + "\t");
        csvLine.append(gender + "\t");
        csvLine.append(address.toString().replace("\"", "").replace("\t\t", "-") + "\t");
        csvLine.append(finClass+ "\t");
        csvLine.append(aPatientLocation);
        return csvLine.toString();
    }

    private String calculateAge(String dob){
      Calendar cal1 = new GregorianCalendar();
      Calendar cal2 = new GregorianCalendar();
      int age = 0;
      int factor = 0;
      Date date1 = new Date();
      try{
        date1 = new SimpleDateFormat("yyyyMMdd").parse(dob);
      } catch (Exception e) {
          return "";
      }
      Date date2 = new Date();
      cal1.setTime(date1);
      cal2.setTime(date2);
      if(cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
            factor = -1;
      }
      age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
      return "" + age;
    }

}
