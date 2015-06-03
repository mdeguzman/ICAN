/*
 * PARISProcessFromUFD.java
 *
 * Created on 11 October 2005, 15:25
 *
 */

package BHLibPARIS;

import BHLibClasses.*;

import java.util.*;

/**
 * Constructor
 * @author fillinghamr and sohn
 */
public class PARISProcessFromUFD extends ProcessSegmentsFromUFD {

    /**
     * Class wide Patient class variable
     */
    String cPatientClass = "";
    /**
     * Class wide Home phone variable
     */
    String cHomePhone = "";
    /**
     * Class wide Pension number variable
     */
    String cPensionNumber = "";
    /**
     * Class wide Medicare number variable
     */
    String cMedicareNumber = "";
    /**
     * Class wide Attending doctor variable
     */
    String cAttendingDr = "";
    /**
     * Class wide DVA number variable
     */
    String cDVANumber = "";
    /**
     * Class wide HL7 message variable
     */
    public HL7Message mInHL7Message;
    public String mEnvironment = "";
    /**
     * Class wide temp variable
     */
    public String mSReg1 = "";
    String cSourceSystem = "";

    /**
     * Creates a new instance of PARISProcessFromUFD
     * @param pHL7Message
     * @throws BHLibClasses.ICANException
     */
    public PARISProcessFromUFD(String pHL7Message, String pEnvironment)  throws ICANException {
        super(pHL7Message);
        mVersion = "c";
        mHL7Message = pHL7Message;
        mEnvironment = pEnvironment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @throws BHLibClasses.ICANException
     * @return
     */
    public String[] processMessage() throws ICANException {

        String aPARISMessageArray[] = {k.NULL, k.NULL, k.NULL};

        String aSegment;
        HL7Group aGroup;
        mInHL7Message = new HL7Message(mHL7Message);
        HL7Message aOutMess = new HL7Message(k.NULL);
        HL7Segment aMSHSegment = new HL7Segment(mInHL7Message.getSegment(HL7_23.MSH));
        HL7Segment aPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aORCSegment = new HL7Segment(mInHL7Message.getSegment("ORC"));
        HL7Segment aOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR"));
        HL7Segment aZCDSegment = new HL7Segment(mInHL7Message.getSegment("ZCD"));

        cSourceSystem = aMSHSegment.get(HL7_23.MSH_3_sending_application);

        if(mInHL7Message.isEvent("A01, A02, A03, A04, A08, A11, A12, A13, A21, A22")) {
            // If discharge date is empty or message is an A03 or message contains a Claim Number  ...
            // ... then send to PARIS.
            if (aPV1Segment.isEmpty(HL7_23.PV1_45_discharge_date_time) ||
                    mInHL7Message.isEvent("A03") ||
                    getFromZBX("FINANCE", "CLAIM_NUMBER").length() > 1)  {

                if (aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"I") ||
                        aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"E") ||
                        aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"O") ||
                        (aPV1Segment.hasValue(HL7_23.PV1_2_patient_class,"PO") && (mInHL7Message.isEvent("A03") || mInHL7Message.isEvent("A04") || mInHL7Message.isEvent("A08") || mInHL7Message.isEvent("A11")))) {
                    aOutMess = new HL7Message(processMSHFromUFD("PARIS").getSegment());
                    aOutMess.append(processEVNFromUFD());
                    aOutMess.append(processPIDFromUFD());
                    aOutMess.append(processNK1s_FromUFD());
                    aOutMess.append(processPV1FromUFD());
                    aOutMess.append(processPV2FromUFD());
                    aOutMess.append(processGT1s_FromUFD());
                    aOutMess.append(processZB2FromUFD());

                    //Norman Soh: Added filter to not pass message if claim num. and disch date exists
                    //and message event date is greater than 7 days of disch date
                    if (getFromZBX("FINANCE", "CLAIM_NUMBER").length() > 1 &&
                            !aPV1Segment.isEmpty(HL7_23.PV1_45_discharge_date_time)) {
                        //The message is most likely to be an update message for a patient that has been discharged
                        //Get EVN message date
                        HL7Segment aEVNSegment = new HL7Segment(aOutMess.getSegment("EVN"));
                        String aEVNMessageDate = aEVNSegment.get(HL7_23.EVN_2_date_time_of_event);
                        //Get PV1 discharge date
                        String aPV1DischargeDate = aPV1Segment.get(HL7_23.PV1_45_discharge_date_time);
                        Date aDateEVN = new Date(Integer.parseInt(aEVNMessageDate.substring(0, 4)) - 1900, Integer.parseInt(aEVNMessageDate.substring(4, 6)), Integer.parseInt(aEVNMessageDate.substring(6, 8)));
                        Date aDateDISCHARGE = new Date(Integer.parseInt(aPV1DischargeDate.substring(0, 4)) - 1900, Integer.parseInt(aPV1DischargeDate.substring(4, 6)), Integer.parseInt(aPV1DischargeDate.substring(6, 8)));
                        long aDayDiff = (aDateEVN.getTime() - aDateDISCHARGE.getTime()) / (60*60*24*1000);
                        //System.out.println("DIFF = " + aDayDiff);
                        if (aDayDiff > 7) {
                            //ignore message
                            return aPARISMessageArray;
                        } else {
                            //ok pass message on
                        }
                    }

                    aPARISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
                    aPARISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
                    aPARISMessageArray[2] = aOutMess.getMessage();
                }
            }
        // Commented out for PathNet release
        }
           // else if(mInHL7Message.isEvent("O01")) {  /// Make certain this is a valid department order for PARIS ....

//            if (! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "DC") &&
//                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "NM") &&
//                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "VL") &&
//                    ! aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RT") &&
//                    ! aORCSegment.hasValue(HL7_23.ORC_1_order_control, "NA")) {

            //Only send pathology orders through to PARIS
//            if (( aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "LA") ||
//                    aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "MA") ||
//                   aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "BA") ||
//                    aOBRSegment.hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "AP")) &&
//                    ! aORCSegment.hasValue(HL7_23.ORC_1_order_control, "NA")) {

//                aOutMess = new HL7Message(processMSHFromUFD("PARIS").getSegment());
 //               aOutMess.append(processPIDFromUFD());
//                aOutMess.append(processPV1FromUFD());
 //               aOutMess.append(processORCs_FromUFD());
 //               aOutMess.append(processOBRs_FromUFD());
 //               aOutMess.append(processZB2FromUFD());

   //             aPARISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
              //  aPARISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            //    aPARISMessageArray[2] = aOutMess.getMessage();
          //  }
        //}

        else if (mInHL7Message.isEvent("R01")) {
            aMSHSegment = processR01MSHFromUFD();
            HL7Segment aPIDSegment = processR01PIDFromUFD();
            HL7Group aPD1NTEGroup = processR01PD1NTEs_FromUFD();
            aPV1Segment = processR01PV1FromUFD();
            HL7Group aReqDetsGroup = processR01ReqDets_FromUFD();
            aOutMess.append(aMSHSegment);
            aOutMess.append(aPIDSegment);
            aOutMess.append(aPD1NTEGroup);
            aOutMess.append(aPV1Segment);
            aOutMess.append(aReqDetsGroup);

            aPARISMessageArray[0] = aMSHSegment.getField(HL7_23.MSH_3_sending_application);
            aPARISMessageArray[1] = aMSHSegment.getField(HL7_23.MSH_4_sending_facility);
            aPARISMessageArray[2] = aOutMess.getMessage();
        }
        return aPARISMessageArray;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing Group in ana A17 message.<p>
     * @return Returns the processed HL7 PID group as a HL7Group.
     * @param pGroupNumber
     * @throws BHLibClasses.ICANException
     */
    public HL7Group processA17GroupFromUFD( int pGroupNumber) throws ICANException {

        HL7Message aInMessage = new HL7Message(mHL7Message) ;
        String aGroup = aInMessage.getGroup(HL7_23.Group_A17_Patient, pGroupNumber);
        HL7Group aOutGroup = new HL7Group();

        HL7Segment aOutPID = this.processPIDFromUFD(aGroup);
        aOutGroup.append(aOutPID.getSegment());

        HL7Segment aOutPV1 = this.processPV1FromUFD(aGroup);
        aOutGroup.append(aOutPV1.getSegment());

        return aOutGroup;

    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To PARIS" EVN segment.
     * @return Returns the processed HL7 EVN segment as a String.
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processEVNFromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aEVNSegmentIN = new HL7Segment(NULL);
        HL7Segment aEVNSegmentOUT = new HL7Segment("EVN");

        aEVNSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.EVN));

        // Initialze aEVNSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.EVN_1_event_type_code,
                    HL7_23.EVN_2_date_time_of_event
        };
        aEVNSegmentOUT.linkTo(aEVNSegmentIN);
        aEVNSegmentOUT.copyFields(aCopyFields);
        return aEVNSegmentOUT;
    }
//------------------------------------------------------------------------------
    /**
     * Generic processing for an Outgoing i.e "To Vendor" GT1 segment.
     * @return Returns the processed HL7 GT1 segment as a String.
     * @throws BHLibClasses.ICANException
     */
    public HL7Group processGT1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aGT1SegIN = new HL7Segment(NULL);
        HL7Segment aGT1SegOUT = new HL7Segment("GT1");
        HL7Group aGT1GroupOUT = new HL7Group();
        HL7Segment aPIDSegIN = new HL7Segment(aHL7Message.getSegment("PID"));
        HL7Segment aPV1SegIN = new HL7Segment(aHL7Message.getSegment("PV1"));
        HL7Segment aMSHSegIN = new HL7Segment(aHL7Message.getSegment("MSH"));
        String aLastName = k.NULL;
        String aGivenName = k.NULL;
        String aMiddleName = k.NULL;
        String aStreet1 = k.NULL;
        String aStreet2 = k.NULL;
        String aStateProvince = k.NULL;
        String aCity = k.NULL;
        String aZIP = k.NULL;
        String aPhone = k.NULL;
        String aWorkPhone = k.NULL;
        String aCountry = k.NULL;
        String aGuarantorType = k.NULL;

        String aGT1 = aHL7Message.getSegment(HL7_24.GT1);

        if (aGT1.length() < 4 && aHL7Message.isEvent("A01, A02, A08, A12, A13") && hasValue(HL7_23.MSH_3_sending_application, "CSC-ALF|CSC-CGMC|CSC-SDMH") ||
                aGT1.length() < 4 && (hasValue(HL7_23.MSH_3_sending_application, "CERNERPM"))) {        // If no GT1 then default Guarantor to be the patient.

            String aPV1FinClass = aPV1SegIN.get(HL7_23.PV1_20_financial_class, HL7_23.FC_finacial_class);
            CodeLookUp aLookup = new CodeLookUp("DefaultGuarantors.table", mEnvironment);
            if (aPV1FinClass.equalsIgnoreCase("DVA")) {
                aLastName = aLookup.getValue("DVA1");
                aStreet1 = aLookup.getValue("DVA2");
                aStreet2 = aLookup.getValue("DVA3");
                aStateProvince = aLookup.getValue("DVA4");
                aCity = aLookup.getValue("DVA5");
                aZIP = aLookup.getValue("DVA6");
                aPhone = aLookup.getValue("DVA7");
                aGuarantorType = aLookup.getValue("DVA8");
            } else if (aPV1FinClass.equalsIgnoreCase("TAC")) {
                aLastName = aLookup.getValue("TAC1");
                aStreet1 = aLookup.getValue("TAC2");
                aStreet2 = aLookup.getValue("TAC3");
                aStateProvince = aLookup.getValue("TAC4");
                aCity = aLookup.getValue("TAC5");
                aZIP = aLookup.getValue("TAC6");
                aPhone = aLookup.getValue("TAC7");
                aGuarantorType = aLookup.getValue("TAC8");
            } else if (aPV1FinClass.equalsIgnoreCase("W/C") && hasValue(HL7_23.MSH_3_sending_application, "CERNERPM")) {
                aLastName = getFromZBX("FINANCE", "EMPLOYER_NAME");
                aStreet1 = getFromZBX("FINANCE", "EMPLOYER_ADDRESS");
//                aStreet2 = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_street_2);
//                aCity = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_city);
//                aStateProvince = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_county_parish);
//                aZIP = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_zip);
//                aCountry = aZEISegment.get(PARIS_23.ZEI_4_Employer_Address, HL7_23.XAD_country);
//                aPhone = aZEISegment.get(PARIS_23.ZEI_5_Employer_Phone, HL7_23.XTN_telephone_number);
//                aGuarantorType = "E";
            } else {
                aLastName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
                aGivenName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
                aMiddleName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
                aStreet1 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
                aStreet2 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
                aStateProvince = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
                aCity = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
                aZIP = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
                aPhone = aPIDSegIN.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
                aWorkPhone = aPIDSegIN.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);

                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP, 1);
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone, 1);
                aGT1SegOUT.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, aWorkPhone, 1);
            }
            if (aPV1FinClass.equalsIgnoreCase("DVA") ||
                    aPV1FinClass.equalsIgnoreCase("TAC") ||
                    aPV1FinClass.equalsIgnoreCase("W/C")) {
                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1);
                if (aPV1FinClass.equalsIgnoreCase("DVA") ||
                        aPV1FinClass.equalsIgnoreCase("TAC")) {
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP);
                    aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_country, aCountry);
                }
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone);
                aGT1SegOUT.set(HL7_23.GT1_10_guarantor_type, aGuarantorType);
            }
            aGT1GroupOUT.append(aGT1SegOUT.getSegment());

        } else if (aGT1.length() > 4) {

            aGT1SegIN.setSegment(aGT1);

            if (aGT1SegIN.get(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name).length() > 0) {

// Initialze aGT1SegOUT with those fields that are straight copies
                String aCopyFields[] =  {
                    HL7_23.GT1_1_set_ID,
                            HL7_23.GT1_2_guarantor_number,
                            HL7_23.GT1_3_guarantor_name,
                            HL7_23.GT1_5_guarantor_address,
                            HL7_23.GT1_6_guarantor_phone_home,
                            HL7_23.GT1_7_guarantor_phone_business,
                            HL7_23.GT1_10_guarantor_type,
                            HL7_23.GT1_11_guarantor_relationship,
                            HL7_23.GT1_12_guarantor_SSN
                };
                aGT1SegOUT.linkTo(aGT1SegIN);
                int aGT1GroupCount = aHL7Message.countSegments(HL7_24.GT1);
                for (int i = 1; i <= aGT1GroupCount; i++) {
                    aGT1SegIN.setSegment(aHL7Message.getSegment("GT1", i));
                    aGT1SegOUT.linkTo(aGT1SegIN);
                    aGT1SegOUT.copyFields(aCopyFields);
                    aGT1GroupOUT.append(aGT1SegOUT.getSegment());
                }
            } else {
                aLastName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
                aGivenName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
                aMiddleName = aPIDSegIN.get(HL7_23.PID_5_patient_name, HL7_23.XPN_middle_name, 1);
                aStreet1 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
                aStreet2 = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
                aStateProvince = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
                aCity = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
                aZIP = aPIDSegIN.get(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
                aPhone = aPIDSegIN.get(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
                aWorkPhone = aPIDSegIN.get(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);

                aGT1SegOUT.set(HL7_23.GT1_1_set_ID, "1");
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_family_name, aLastName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_given_name,  aGivenName, 1);
                aGT1SegOUT.set(HL7_23.GT1_3_guarantor_name, HL7_23.XPN_middle_name, aMiddleName, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_1, aStreet1, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_street_2, aStreet2, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_city, aCity, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_state_or_province, aStateProvince, 1);
                aGT1SegOUT.set(HL7_23.GT1_5_guarantor_address, HL7_23.XAD_zip, aZIP, 1);
                aGT1SegOUT.set(HL7_23.GT1_6_guarantor_phone_home, HL7_23.XTN_telephone_number, aPhone, 1);
                aGT1SegOUT.set(HL7_23.GT1_7_guarantor_phone_business, HL7_23.XTN_telephone_number, aWorkPhone, 1);
                aGT1GroupOUT.append(aGT1SegOUT.getSegment());
            }
        }
        return aGT1GroupOUT;

    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To" MSH segment.
     * @return Returns the processed HL7 MSH segment as a String.
     * @param pReceivingApplication
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processMSHFromUFD(String pReceivingApplication) throws ICANException {
// Non copy fields are
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);

        HL7Segment aMSHSegmentIN = new HL7Segment(NULL);
        HL7Segment aMSHSegmentOUT = new HL7Segment("MSH");

        aMSHSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.MSH));
        mHL7Segment = aMSHSegmentIN;                    // In case any of the "do" functions need to see the segment
        mFacility = aMSHSegmentIN.get(HL7_24.MSH_4_sending_facility);
        mHospitalID = mFacility.substring(mFacility.length()-1, mFacility.length());
        mHL7MessageEvent = aMSHSegmentIN.get(HL7_23.MSH_9_message_type, "CM_2");

// Initialze aMSHSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.MSH_2_encoding_characters,
                    HL7_23.MSH_3_sending_application,
                    HL7_23.MSH_4_sending_facility,
                    HL7_23.MSH_9_message_type,
                    HL7_23.MSH_10_message_control_ID,
                    HL7_23.MSH_11_processing_ID,
                    HL7_23.MSH_12_version_ID
        };

// Initialze OUT with those fields that are straight copies
        aMSHSegmentOUT.linkTo(aMSHSegmentIN);
        aMSHSegmentOUT.copyFields(aCopyFields);

// ... now sort out the non standard ones ...

        if (mFacility.equalsIgnoreCase("ALF")) {
            aMSHSegmentOUT.set(HL7_23.MSH_6_receiving_facility, "01");
        } else if (mFacility.equalsIgnoreCase("CGMC")) {
            aMSHSegmentOUT.set(HL7_23.MSH_6_receiving_facility, "02");
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            aMSHSegmentOUT.set(HL7_23.MSH_6_receiving_facility, "04");
        }

        aMSHSegmentOUT.set(HL7_23.MSH_5_receiving_application, pReceivingApplication);
        aMSHSegmentOUT.set(HL7_23.MSH_7_message_date_time, aMSHSegmentIN.getDateTime());
        aMSHSegmentOUT.copy(HL7_23.MSH_9_2_trigger_event);
        return (aMSHSegmentOUT);
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To Vendor" NK1 segment.
     * @return Returns the processed HL7 NK1 segment as a Group.
     * @throws BHLibClasses.ICANException
     */
    public HL7Group processNK1s_FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aNK1SegIN = new HL7Segment(k.NULL);
        HL7Segment aNK1SegOUT = new HL7Segment("NK1");
        HL7Group aNK1GroupOUT = new HL7Group();
        // Initialze aNK1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.NK1_1_set_ID,
                    HL7_23.NK1_3_next_of_kin_relationship,
                    HL7_23.NK1_7_contact_role
        };
        int aNK1GroupCount = aHL7Message.countSegments("NK1");
        for (int i = 1; i <= aNK1GroupCount && i <= 2; i++) {
            aNK1SegIN.setSegment(aHL7Message.getSegment("NK1", i));
            aNK1SegOUT.linkTo(aNK1SegIN);
            aNK1SegOUT.copyFields(aCopyFields);

            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_family_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_given_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_middle_name);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_suffix);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_prefix);
            aNK1SegOUT.copy(HL7_23.NK1_2_next_of_kin_name, HL7_23.XPN_degree);
            aNK1SegOUT.set(HL7_23.NK1_2_next_of_kin_name, aNK1SegOUT.get(HL7_23.NK1_2_next_of_kin_name).toUpperCase());

            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_1);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_street_2);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_city);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_state_or_province);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_zip);
            aNK1SegOUT.copy(HL7_23.NK1_4_next_of_kin__address, HL7_23.XAD_country);
            aNK1SegOUT.copy(HL7_23.NK1_5_next_of_kin__phone, HL7_23.XTN_telephone_number);
            aNK1SegOUT.copy(HL7_23.NK1_6_business_phone_num, HL7_23.XTN_telephone_number);


            aNK1GroupOUT.append(aNK1SegOUT.getSegment());
        }
        return aNK1GroupOUT;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To Vendor" OBR segment.
     * @return Returns the processed HL7 OBR segment as a Group.
     * @throws BHLibClasses.ICANException
     */
    public HL7Group processOBRs_FromUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aOBRSegIN = new HL7Segment(k.NULL);
        HL7Segment aOBXSegIN = new HL7Segment(k.NULL);
        HL7Segment aNTESegIN = new HL7Segment(k.NULL);
        HL7Segment aPV1SegIN = new HL7Segment(aHL7Message.getSegment("PV1"));
        HL7Segment aOBRSegOUT = new HL7Segment("OBR");
        HL7Segment aOBXSegOUT = new HL7Segment(k.NULL);
        HL7Segment aNTESegOUT = new HL7Segment(k.NULL);
        HL7Group aOBRGroupOUT = new HL7Group();
        int aNTEcount = 0;
        int aNTEid = 1;
        int aOBXcount = 0;
        int aOBXid = 1;
        String aClinicalNotesDesc = "";

        // Initialze aOBRSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.OBR_1_Set_ID,
                    HL7_23.OBR_4_Universal_Service_ID,
                    HL7_23.OBR_10_collector_ID,
                    HL7_23.OBR_11_Specimen_Action_Code,
                    HL7_23.OBR_13_Relevant_Clinical_Information,
                    HL7_23.OBR_14_Specimen_Received_Date_Time,
                    HL7_23.OBR_15_Specimen_Source,
                    HL7_23.OBR_18_Placers_Field_1,
                    HL7_23.OBR_19_Placers_Field_2,
                    HL7_23.OBR_20_Fillers_Field_1,
                    HL7_23.OBR_21_Fillers_Field_2,
                    HL7_23.OBR_24_Diagnostic_Service_Section_ID,
                    HL7_23.OBR_30_Transportation_Mode
        };
        int aOBRGroupCount = aHL7Message.countSegments("OBR");
        for (int i = 1; i <= aOBRGroupCount; i++) {
            aOBRSegIN.setSegment(aHL7Message.getSegment("OBR", i));
            aOBRSegOUT.linkTo(aOBRSegIN);
            aOBRSegOUT.copyFields(aCopyFields);

            aOBRSegOUT.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.EI_1_entity_ID);
            aOBRSegOUT.copy(HL7_23.OBR_4_Universal_Service_ID, HL7_23.EI_2_namespace_ID);
            aOBRSegOUT.copy(HL7_23.OBR_5_Priority);
            aOBRSegOUT.copy(HL7_23.OBR_9_Collection_Volume);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source,  HL7_23.Source_CE_1_1_Specimen_code);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_CE_1_2_Specimen_text);
            aOBRSegOUT.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);

//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider,  HL7_23.CE_ID_code);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.CE_text);

            aOBRSegOUT.set(HL7_23.OBR_10_collector_ID, "HIS");

            // For PARIS the Ordering Provider code is always "WARD" .. or "WARD.TEST" if in Dev environment
            CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
            String aORUWard = aLU.getValue("PARISWard");

            if (aHL7Message.isEvent("O01")) {
                aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num);
            } else {
                aOBRSegOUT.set(HL7_23.OBR_16_Ordering_Provider, aORUWard);
            }
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name);
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name);
            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table);
//            aOBRSegOUT.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_assigning_authority);

// "OBR:27 Priorities" "PARIS has special priority codes defined that will print a label.
// These are based on user POWERCHART selection for 'Ward Collect' and a hidden 'Collected' value (defaulted to 'N').
// Combinations of OBR:11 (values 'P', 'L' or 'O') and Priority are mapped to select the desired PARIS priority that will/not print a label.
// Reporting Priority has to default to Requesting Priority if not provided.
// In format field is "requestPriority~reportPriority" and the rules are ...
//     1.  Copy requestPriorityIN to requestPriorityOUT mapping value on way through
//     2.  Does reportPriority exist? ...
//           if YES ... copy reportPriorityIN to reportPriorityOUT mapping value on way through
//           if NO  ... copy requestPriorityIN to reportPriorityOUT mapping value on way through

            if (aOBRSegIN.countRepeatFields(HL7_23.OBR_27_Quantity_Timing) > 0) {
                aLU = new CodeLookUp("CERNER_Priorities_PARIS.table", mEnvironment);
                String aDecodePriority = aOBRSegIN.get(HL7_23.OBR_11_Specimen_Action_Code);
                aDecodePriority = aDecodePriority + aOBRSegIN.get(HL7_23.OBR_27_Quantity_Timing,HL7_23.TQ_6_priority,1);
                String aPriority = aLU.getValue(aDecodePriority);
//
// ... Request Priority ...
                aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aLU.getValue(aPriority), 1);
                aOBRSegOUT.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1);
//
// ... Reporting Priority ...
                if (aOBRSegIN.countRepeatFields(HL7_23.OBR_27_Quantity_Timing) > 1) {
                    aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aPriority, 2);
                    aOBRSegOUT.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 2);
                } else {    // ... default Reporting to Requesting priority ...
                    aOBRSegOUT.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, aPriority, 2);
                    aOBRSegOUT.move(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 2,
                            HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1);
                }
            }
//
// Fudge for "COPY TO DRS" ... PARIS requires an extra Copy To Dr if any exist
//  (ie 0 Drs = blank field, 1 Dr = 2 Drs, 2 Drs = 3 ... etc).
// So we force an extra Dr 'ZZ999' if any Copy To Dr's exist.")
            int aRep = 1;
            int aCnt = aOBRSegIN.countRepeatFields(HL7_23.OBR_28_Results_Copies_To);
            if (aCnt > 0) {

                for (i = 1; i <= aCnt; i++) {
//                    if (! aOBRSegIN.isEmpty(HL7_23.OBR_28_Results_Copies_To)) {
                    aOBRSegOUT.copy(HL7_23.OBR_28_Results_Copies_To, aRep++);
//                    }
                }
            }
            aOBRSegOUT.set(HL7_23.OBR_28_Results_Copies_To, "ZZ999", aRep++);

// Reason for study ...
            aCnt = aOBRSegIN.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
            if (aCnt > 0) {
                for (i = 1; i < aCnt; i++) {
//                   if (! aOBRSegIN.isEmpty(HL7_23.OBR_31_Reason_For_Study,HL7_23.CE_text)) {
                    aOBRSegOUT.copy(HL7_23.OBR_31_Reason_For_Study,HL7_23.CE_text, i);
//                   }
                }
            }

            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
                aOBRSegOUT.set(HL7_23.OBR_7_Observation_Date_Time,
                        aOBRSegIN.get(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, 1));
            }

            aOBRSegOUT.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text);

            aOBRGroupOUT.append(aOBRSegOUT.getSegment());
//
// Process any actual and forced NTE segments ...
            String aSREG1 = "";
            String aSREG2 = "";
            String aSREG3 = "";
            String aSREG4 = "";
//            String aSREG4 = "";
            int aObsCnt = 0;


            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
                if (! aOBRSegIN.isEmpty(HL7_23.OBR_13_Relevant_Clinical_Information)) {
                    aNTESegOUT = new HL7Segment("NTE");
                    aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
                    aNTESegOUT.set(HL7_23.NTE_3_comment, aOBRSegIN.get(HL7_23.OBR_13_Relevant_Clinical_Information));
                    aNTEcount++;

                    aOBRGroupOUT.append(aNTESegOUT.getSegment());
                }
            }

            int aOBXGroupCount = aHL7Message.countSegments("OBX");

// Scan all OBX and get NumCVC  into SREG2 and get Diabetic or Multi-Slice into SREG4
            for (i = 1; i <= aOBXGroupCount; i++) {
                aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));
                if (aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, "NumCVC")) {
                    if (aOBXSegIN.hasValue(HL7_23.OBX_5_observation_value, "0")) {
                        aSREG2 = "CVC(x" + aOBXSegIN.get(HL7_23.OBX_5_observation_value) + ")";
                    }
                } else if (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Diabetic Patient") &&
                        !aOBXSegIN.isEmpty(HL7_23.OBX_5_observation_value)) {
                    aSREG4 = "Diabetic Patient = " + aOBXSegIN.get(HL7_23.OBX_5_observation_value);
                } else if (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Reason for Multi Slice") &&
                        !aOBXSegIN.isEmpty(HL7_23.OBX_5_observation_value)) {
                    aObsCnt = aOBXSegIN.countRepeatFields(HL7_23.OBX_5_observation_value);
                    int j;
                    for ( j=1 ; j <= aObsCnt ; j++) {
                        if (aSREG4.length() > 0) {
                            aSREG4 = aSREG4 + ", " + aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
                        } else {
                            aSREG4 = aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
                        }
                    }
//                    aSREG4 = "Diabetic Patient = " + aOBXSegIN.get(HL7_23.OBX_5_observation_value);
                } else if (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Clinical Notes (Generic)")) {
                    aClinicalNotesDesc = aOBXSegIN.get(HL7_23.OBX_5_observation_value);
                }
            }

// Having picked out the CVC, Diabetic and Multislice, ensure they are sent to PARIS in
// the required priority order.
            for (i = 1; i <= aOBXGroupCount; i++) {
                aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));

                if (aOBXSegIN.isEmpty(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code)
                || aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"NumCVC")) {
                    if (aSREG4.length() > 0)  {
                        aNTESegOUT = new HL7Segment("NTE");
                        aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
                        aNTESegOUT.set(HL7_23.NTE_3_comment, aSREG4);
                        aOBRGroupOUT.append(aNTESegOUT.getSegment());
                        aNTEcount++;
                        aSREG4 = "";
                    }
                } else {
                    if (aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"Invasive Devices")) {
                        aNTESegOUT = new HL7Segment("NTE");
                        aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
                        aSREG1 ="";
                        aObsCnt = aOBXSegIN.countRepeatFields(HL7_23.OBX_5_observation_value);
                        int j;
                        for ( j=1 ; j <= aObsCnt ; j++) {
                            aSREG3 = aOBXSegIN.get(HL7_23.OBX_5_observation_value, j);
                            if (aSREG3.matches("CVC")) {
                                if (aSREG2.length() > 0) {
                                    aSREG3 = aSREG2;
                                    aSREG2="";
                                }
                            }
                            if (aSREG1.length() > 0) {
                                aSREG1 = aSREG1 + ", " + aSREG3;
                            } else {
                                aSREG1 = aSREG3;
                            }
                        }

                        if (aSREG2.length() > 0) {
                            if (aSREG1.length() > 0) {
                                aSREG1 = aSREG2 + ", " + aSREG1;
                            } else {
                                aSREG1 = aSREG2;
                            }
                        }
                        aNTESegOUT.set(HL7_23.NTE_3_comment, aSREG1);
                        aOBRGroupOUT.append(aNTESegOUT.getSegment());
                        aNTEcount++;
                    }
                }
            }
        }

//        int aNTEGroupCount = aHL7Message.countSegments("NTE");
//// Now process any real NTE segments rather than OBX segments that have to be sent as NTE segments.
//        for (int i = 1; i <= aNTEGroupCount; i++) {
//            aNTESegIN = new HL7Segment(aHL7Message.getSegment("NTE", i ));
//            aNTESegOUT = new HL7Segment("NTE");
//            aNTESegOUT.linkTo(aNTESegIN);
//            aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
//            aNTESegOUT.copy(HL7_23.NTE_3_comment);
//            aOBRGroupOUT.append(aNTESegOUT.getSegment());
//        }

// Process clinical notes as a NTE segment
        if (aClinicalNotesDesc.length() > 0) {
            aNTESegOUT = new HL7Segment("NTE");
            aNTESegOUT.set(HL7_23.NTE_1_setID, Integer.toString(aNTEid++));
            aNTESegOUT.set(HL7_23.NTE_3_comment, aClinicalNotesDesc);
            aOBRGroupOUT.append(aNTESegOUT.getSegment());
        }

// Now process any real OBX segments
        int aOBXGroupCount = aHL7Message.countSegments("OBX");
        for (int i = 1; i <= aOBXGroupCount; i++) {
            aOBXSegIN = new HL7Segment(aHL7Message.getSegment("OBX", i ));
            if (! aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"") &&
                    ! aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"NumCVC") &&
                    ! aOBXSegIN.hasValue(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code,"Invasive Devices") &&
                    ! (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Diabetic Patient")) &&
                    ! (aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Reason for Multi Slice")) &&
                    ! aOBXSegIN.get(HL7_23.OBX_3_observation_identifier, HL7_23.CE_text).startsWith("Clinical Notes (Generic)")) {
                aOBXSegOUT = new HL7Segment("OBX");
                aOBXSegOUT.linkTo(aOBXSegIN);
                aOBXSegOUT.set(HL7_23.OBX_1_set_ID,Integer.toString(aOBXid++));
                aOBXSegOUT.copy(HL7_23.OBX_2_value_type);
                aOBXSegOUT.copy(HL7_23.OBX_3_observation_identifier);
                aOBXSegOUT.copy(HL7_23.OBX_5_observation_value);
                aOBXSegOUT.copy(HL7_23.OBX_6_units);
                aOBXSegOUT.copy(HL7_23.OBX_7_references_range);
                aOBXSegOUT.copy(HL7_23.OBX_8_abnormal_flags);
                aOBXSegOUT.copy(HL7_23.OBX_11_observ_results_status);
                aOBRGroupOUT.append(aOBXSegOUT.getSegment());
            }
        }
        return aOBRGroupOUT;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To Vendor" ORC segment.
     * @return Returns the processed HL7 ORC segment as a Group.
     * @throws BHLibClasses.ICANException
     */
    public HL7Group processORCs_FromUFD() throws ICANException {

        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aORCSegIN = new HL7Segment(k.NULL);
        HL7Segment aOBRSegIN = new HL7Segment(k.NULL);
        HL7Segment aORCSegOUT = new HL7Segment("ORC");
        HL7Group aORCGroupOUT = new HL7Group();

        // Initialze aORCSegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.ORC_1_order_control,
        };
        int aORCGroupCount = aHL7Message.countSegments("ORC");
        for (int i = 1; i <= aORCGroupCount; i++) {
            aORCSegIN.setSegment(aHL7Message.getSegment("ORC", i));
            aOBRSegIN.setSegment(aHL7Message.getSegment("OBR", i));

            aORCSegOUT.linkTo(aORCSegIN);
            aORCSegOUT.copyFields(aCopyFields);

            aORCSegOUT.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_1_entity_ID);
            aORCSegOUT.copy(HL7_23.ORC_4_placer_group_num, HL7_23.EI_2_namespace_ID);
            aORCSegOUT.copy(HL7_23.ORC_5_order_status);
            aORCSegOUT.copy(HL7_23.ORC_9_date_time_of_trans);
            aORCSegOUT.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_ID_code);
            aORCSegOUT.copy(HL7_23.ORC_16_order_control_code_reason, HL7_23.CE_text);

            // For PARIS the Entered By Provider code is always "WARD" .. or "WARD.TEST" if in Dev environment
            CodeLookUp aLU = new CodeLookUp("DefaultValues.table", mEnvironment);
            String aORUWard = aLU.getValue("PARISWard");

            aORCSegOUT.set(HL7_23.ORC_10_entered_by,HL7_23.XCN_ID_num, aORUWard);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_last_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_first_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_middle_initial_or_name);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_suffix);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_prefix);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_degree);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_code_source_table);
            aORCSegOUT.copy(HL7_23.ORC_10_entered_by,HL7_23.XCN_assigning_authority);

//
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_ID_num);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_last_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_first_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_middle_initial_or_name);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_suffix);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_prefix);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_degree);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_code_source_table);
            aORCSegOUT.copy(HL7_23.ORC_12_ordering_provider,HL7_23.XCN_assigning_authority);
//
//"ORC-13 Enterers Location" "If not provided from CERNER default to the patients location.
// "Facility Logic" "The following code is required to force the Ordering Location to either 'TAC', 'CAS' or 'RAD'
//   and the Observation Date/Time to the Transaction date/time ..... but only for Radiology Orders.
//   Note:- "RADIOLOGY_PRINTER" is ONLY populated for Radiology so we use this as a flagging mechanism for Radiology.")

            if (getFromZBX("ORDER", "RADIOLOGY_PRINTER").length() != 0) {
                if (mPV1NursingStation.matches("ED") ) {
                    if (hasValue(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "7")) {  /// Radiology?
                        aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "TAC") ;
                    }  else {
                        aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "CAS") ;
                    }
                } else {
                    aORCSegOUT.set(HL7_23.ORC_13_enterers_location, "RAD") ;
                }
            } else if (aORCSegIN.isEmpty(HL7_23.ORC_13_enterers_location)) {
                aORCSegOUT.set(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu, mPV1NursingStation) ;
            } else {
                aORCSegOUT.copy(HL7_23.ORC_13_enterers_location, HL7_23.PL_point_of_care_nu) ;
            }
            aORCGroupOUT.append(aORCSegOUT.getSegment());
        }
        return aORCGroupOUT;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To" PID segment where the PID is that contained in the HL7 message itself.
     * @return Returns the processed HL7 PID segment as a String.
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processPIDFromUFD() throws ICANException {
        return (this.processPIDFromUFD(mHL7Message));
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To" PID segment where the PID is in the [pHL7MessageBlock] .
     * @return Returns the processed HL7 PID segment as a String.
     * @param pHL7MessageBlock Either a full HL7 Message or may just be an A17 Patient Group.
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processPIDFromUFD(String pHL7MessageBlock) throws ICANException {

        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);

        HL7Segment aPIDSegmentIN = new HL7Segment(NULL);
        HL7Segment aPIDSegmentOUT = new HL7Segment("PID");
        HL7Segment aMSHSegmentIN = new HL7Segment(aHL7Message.getSegment("MSH"));

        aPIDSegmentIN.setSegment(aHL7Message.getSegment(HL7_24.PID));
        // mHL7Segment = aPIDSegmentIN;

        // Initialze aPIDSegmentOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PID_1_set_ID,
                    HL7_23.PID_5_patient_name,
                    HL7_23.PID_7_date_of_birth,
                    HL7_23.PID_8_sex,
                    HL7_23.PID_10_race,
                    HL7_23.PID_15_language,
                    HL7_23.PID_29_patient_death_date_time
        };

        aPIDSegmentOUT.linkTo(aPIDSegmentIN);
        aPIDSegmentOUT.copyFields(aCopyFields);

        aPIDSegmentOUT.set(HL7_23.PID_5_patient_name, HL7_23.XPN_prefix, "");   // Clear the Title and force upper case names
        String aName = aPIDSegmentOUT.get(HL7_23.PID_5_patient_name);
        aPIDSegmentOUT.set(HL7_23.PID_5_patient_name, aName.toUpperCase());

        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip);
        aPIDSegmentOUT.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country);
        if (aMSHSegmentIN.get(HL7_23.MSH_3_sending_application).indexOf("CSC") >= 0) {
            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.CE_ID_code, HL7_23.PID_11_patient_address, HL7_23.XAD_country);
        } else if (aMSHSegmentIN.get(HL7_23.MSH_3_sending_application).indexOf("CERNER") >= 0) {
            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.CE_ID_code, HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province);
        } else {
            aPIDSegmentOUT.copy(HL7_23.PID_12_county_code);
        }
        // Process Unique identifiers(UR, Medicare, Pension and DVA) held in PID-3 ....
        // NOTE :- PBS Number is also held in PID-3 (type "PB") but when required is normally passed in a Z segment.
        String aTmpField[] = aPIDSegmentIN.getRepeatFields(HL7_23.PID_3_patient_ID_internal);
        int i;
        HL7Field aInField;
        HL7Field aOutField;
        String aStr;

        for (i=0 ; i < aTmpField.length ; i++) {
            aInField = new HL7Field(aTmpField[i]);
            aOutField = new HL7Field();
            aStr = aInField.getSubField(HL7_23.CX_ID_type_code);
            if (aStr.equalsIgnoreCase("PI")) {
                aOutField.setSubField(aPIDSegmentIN.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number), HL7_23.CX_ID_number);
                if (mFacility.equalsIgnoreCase("ALF")) {
                    aOutField.setSubField("00001",HL7_23.CX_assigning_authority);
                } else if (mFacility.equalsIgnoreCase("CGMC")) {
                    aOutField.setSubField("00009",HL7_23.CX_assigning_authority);
                } else if (mFacility.equalsIgnoreCase("SDMH")) {
                    aOutField.setSubField("00012",HL7_23.CX_assigning_authority);
                }
                aPIDSegmentOUT.set(HL7_23.PID_3_patient_ID_internal,aOutField.getField());
            }
            if (aStr.equalsIgnoreCase("PEN")) {
                cPensionNumber =  aInField.getSubField(HL7_23.CX_ID_number);
            }
            if (aStr.equalsIgnoreCase("VA")) {
                cDVANumber = aInField.getSubField(HL7_23.CX_ID_number);
            }
            if (aStr.equalsIgnoreCase("MC")) {
                cMedicareNumber =  aInField.getSubField(HL7_23.CX_ID_number);
                if (cMedicareNumber.length() > 10) {
                    aPIDSegmentOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number).substring(0, 10));
                } else {
                    aPIDSegmentOUT.set(HL7_23.PID_19_SSN_number, aInField.getSubField(HL7_23.CX_ID_number));
                }
            }
        }

        aPIDSegmentOUT.linkTo(aPIDSegmentIN);

// The contents of PID_11_4_state_or_province has to be moved to PARIS in PID_12_country_code
//        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_11_patient_address,HL7_23.XAD_state_or_province)) {
//            aPIDSegmentOUT.move(HL7_23.PID_12_county_code, HL7_23.XAD_street_1, HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province);
//        }

// Home phone number is passed in ZB2 segment since PARIS has Home Phone hard coded to U.S. phone format.
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number)) {
            cHomePhone = aPIDSegmentIN.get(HL7_23.PID_13_home_phone,HL7_23.XTN_telephone_number);
        }
        if ( !aPIDSegmentIN.isEmpty(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number)) {
            aPIDSegmentOUT.copy(HL7_23.PID_14_business_phone,HL7_23.XTN_telephone_number);
        }
        if (mFacility.equalsIgnoreCase("ALF")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "01");
        } else if (mFacility.equalsIgnoreCase("CGMC")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "02");
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            aPIDSegmentOUT.set(HL7_23.PID_18_account_number,HL7_23.CX_assigning_authority, "04");
        }

        // PARIS takes the PV1 Visit Number as the PID_28 Account Number.
        HL7Segment aPV1SegmentIN = new HL7Segment(aHL7Message.getSegment(HL7_24.PV1));
        String aPV1_19_VisitNumber = aPV1SegmentIN.get(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);

        aPIDSegmentOUT.set(HL7_23.PID_18_account_number, HL7_23.CX_ID_number, aPV1_19_VisitNumber);

        return aPIDSegmentOUT;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To PARIS" PV1 segment.
     * @return Returns the processed HL7 PV1 segment as a String.
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processPV1FromUFD() throws ICANException {
        return (this.processPV1FromUFD(mHL7Message));
    }
//------------------------------------------------------------------------------
    /**
     *
     * @param pHL7MessageBlock
     * @throws BHLibClasses.ICANException
     * @return
     */
    public HL7Segment processPV1FromUFD(String pHL7MessageBlock) throws ICANException {
        HL7Message aHL7Message = new HL7Message(pHL7MessageBlock);
        HL7Segment aPV1SegIN = new HL7Segment(NULL);
        aPV1SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV1));

        HL7Segment aPV2SegIN = new HL7Segment(NULL);
        aPV2SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));

        HL7Segment aPV1SegOUT = new HL7Segment("PV1");
        cPatientClass = aPV1SegIN.get(HL7_23.PV1_2_patient_class);
        CodeLookUp aUnitLookUp = new CodeLookUp("Unit_PARIS.table", mFacility, mEnvironment);
        CodeLookUp aFinClassLookUp = new CodeLookUp("FinClass_PARIS.table", mFacility, mEnvironment);

// Initialze aPV1SegOUT with those fields that are straight copies
        String aCopyFields[] =  {
            HL7_23.PV1_1_set_ID,
                    HL7_23.PV1_2_patient_class,
                    HL7_23.PV1_15_ambulatory_status,
                    HL7_23.PV1_16_VIP_indicator,
                    HL7_23.PV1_18_patient_type,
                    HL7_23.PV1_36_discharge_disposition,
                    HL7_23.PV1_37_discharged_to_location,
                    HL7_23.PV1_41_account_status,
                    HL7_23.PV1_45_discharge_date_time
        };

        aPV1SegOUT.linkTo(aPV1SegIN);
        aPV1SegOUT.copyFields(aCopyFields);

// Swap Pre-op "PO" patient class to "W"
        if (cPatientClass.matches("PO")) {
            aPV1SegOUT.set(HL7_23.PV1_2_patient_class, "W");
        }

//
// Patient Current Location .............
        aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);

// No Bed for ED or for OP and for IP Room is same as Point of care.
        if (cPatientClass.equalsIgnoreCase("E") || cPatientClass.equalsIgnoreCase("O")) {
//                aPV1SegOUT.move(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
//                aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
        } else {
// "ICU Special" "ICU patients are admitted to Surgical or General Beds identified by S1-S18 and G19-G36 (or similar).
//  Paris cannot handle the G or S prefix so we have to translate to room ICUG or ICUS for Paris.")

            if ((aPV1SegIN.hasValue(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "ICU")) &&
                    ! aPV1SegIN.isEmpty(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed )) {
                String aBed =  aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                String aNu = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location,  HL7_23.PL_point_of_care_nu) + aBed.substring(0,1);
                aBed = aBed.substring(1);
                aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, aNu);
                aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed, aBed);
            } else {
                aPV1SegOUT.move(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                aPV1SegOUT.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
            }
        }

        if (mFacility.equalsIgnoreCase("ALF")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "A ^");
        } else if (mFacility.equalsIgnoreCase("CGMC")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "C ^");
        } else if (mFacility.equalsIgnoreCase("SDMH")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "S ^");
        }

        if (aPV1SegIN.isEmpty(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_last_name) ||
                aPV1SegIN.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num).equalsIgnoreCase("XX999")) {
            if (cPatientClass.equalsIgnoreCase("E")) {
                if(mFacility.equalsIgnoreCase("SDMH")) {
                    aPV1SegOUT.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "TJ294");
                } else {
                    aPV1SegOUT.set(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num, "SD465");
                }
            }
        } else  {
            aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_7_attending_doctor);
        }

// Note Attending Dr for inserion in ZB2 segment
        cAttendingDr = aPV1SegOUT.get(HL7_23.PV1_7_attending_doctor, HL7_23.XCN_ID_num);

        //aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_9_consulting_doctor);
        int aConsultingDoctorCount = aPV1SegIN.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
        for (int i = 1; i <= aConsultingDoctorCount && i <= 5; i++) {
            String aConsultingDoctorIDNum = aPV1SegIN.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
            if (aConsultingDoctorIDNum.length() > 0  && !aConsultingDoctorIDNum.equalsIgnoreCase(cAttendingDr)) {
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                aPV1SegOUT.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_code_source_table, i);
            }
        }

        if (aPV1SegIN.isEmpty(HL7_23.PV1_10_hospital_service) && (cPatientClass.equalsIgnoreCase("E"))) {
            if (mFacility.equalsIgnoreCase("ALF")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"AEM");
            } else
                if (mFacility.equalsIgnoreCase("CGMC")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"CEM");
                } else
                    if (mFacility.equalsIgnoreCase("SDMH")) {
                aPV1SegOUT.set(HL7_23.PV1_10_hospital_service,"SED");
                    }
        } else {
            aPV1SegOUT.set(HL7_23.PV1_10_hospital_service, aUnitLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_10_hospital_service)));
        }

        aPV1SegOUT = doDoctor(aPV1SegOUT, HL7_23.PV1_17_admitting_doctor);

        if (aPV1SegOUT.isEmpty(HL7_23.PV1_18_patient_type)) {
            aPV1SegOUT.move(HL7_23.PV1_18_patient_type,HL7_23.PV1_2_patient_class);
        }

        aPV1SegOUT.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);

        HL7Segment aMSHSegment = new HL7Segment(aHL7Message.getSegment("MSH"));
        if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CERNER") >= 0) {
            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aFinClassLookUp.getValue(aPV1SegIN.get(HL7_23.PV1_20_financial_class)));
        } else if (aMSHSegment.get(HL7_23.MSH_3_sending_application).indexOf("CSC") >= 0) {
            String aChargePriceInd = aPV1SegIN.get(HL7_23.PV1_21_charge_price_indicator);
            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, aChargePriceInd);
        }

        if (! aHL7Message.isEvent("O01") &&
                (cPatientClass.equalsIgnoreCase("O") ||
                ( cPatientClass.equalsIgnoreCase("E") && ! aPV2SegIN.isEmpty(HL7_23.PV2_8_expected_admit_date)))) {
            aPV1SegOUT.set(HL7_23.PV1_44_admit_date_time, aPV2SegIN.get(HL7_23.PV2_8_expected_admit_date));
        } else {
            aPV1SegOUT.copy(HL7_23.PV1_44_admit_date_time);
        }

        mPV1NursingStation = aPV1SegIN.get(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);

        //Norman Soh: PO code change 21Nov2007
        if (cPatientClass.matches("PO")) {
            aPV1SegOUT.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu, "PRE");
            aPV1SegOUT.set(HL7_23.PV1_20_financial_class, "H");
        }

        return aPV1SegOUT;
    }
//------------------------------------------------------------------------------
// Do the processing required for a Doctor entry
    private HL7Segment doDoctor(HL7Segment pSegment, String pFieldID) throws ICANException {
        pSegment.copy(pFieldID, HL7_23.XCN_ID_num);
        if (! pSegment.isEmpty(pFieldID, HL7_23.XCN_ID_num)) {
            pSegment.copy(pFieldID, HL7_23.XCN_first_name);
            pSegment.copy(pFieldID, HL7_23.XCN_last_name);
            pSegment.copy(pFieldID, HL7_23.XCN_middle_initial_or_name);
            pSegment.copy(pFieldID, HL7_23.XCN_prefix);
        }
        return pSegment;
    }
//------------------------------------------------------------------------------
    /**
     * PARIS specific processing for an Outgoing i.e "To Vendor" PV2 segment.
     * @return Returns the processed HL7 PV2 segment as a String.
     * @throws BHLibClasses.ICANException
     */
    public HL7Segment processPV2FromUFD() throws ICANException {
        HL7Message aHL7Message = new HL7Message(mHL7Message, 0);
        HL7Segment aPV2SegIN = new HL7Segment(NULL);
        HL7Segment aPV2SegOUT = new HL7Segment("");

        aPV2SegIN.setSegment(aHL7Message.getSegment(HL7_24.PV2));
        if (aPV2SegIN.getSegment().length() > 0) {
            aPV2SegOUT = new HL7Segment("PV2");
// Initialze aPV2SegOUT with those fields that are straight copies
            String aCopyFields[] =  {
                HL7_23.PV2_2_accommodation_code,
                        HL7_23.PV2_7_visit_user_code,
                        HL7_23.PV2_8_expected_admit_date,
                        HL7_23.PV2_3_admit_reason,
                        HL7_23.PV2_21_visit_publicity_code,
                        HL7_23.PV2_22_visit_protection_indic
            };
            aPV2SegOUT.linkTo(aPV2SegIN);
            aPV2SegOUT.copyFields(aCopyFields);
//        aPV2SegOUT.move(HL7_23.PV2_3_admit_reason, HL7_23.CE_text, HL7_23.PV2_3_admit_reason, HL7_23.CE_ID_code);
//
        }
        return aPV2SegOUT;
    }
//------------------------------------------------------------------------------
    /* Extract the Unified ZBX segment data */
    /**
     *
     * @throws BHLibClasses.ICANException
     * @return
     */
    public HL7Group processZBXSegmentsFromUFD() throws ICANException {
        HL7Group aOBXOutGroup = new HL7Group();
        HL7Segment aOBXOut = new HL7Segment("OBX");
        String aOBXValue;
        String aMedRecDateTime;

        aOBXValue = getFromZBX("PMI", "MEDICARE_EXPIRY");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("MEDEXPDT", "ST", aOBXValue).getSegment());
        }
        aOBXValue = getFromZBX("PMI", "DVA_CARD_TYPE");
        if ( ! aOBXValue.equals(k.NULL)) {
            aOBXOutGroup.append( processOBXFromZBX("DVACARDTYP", "CD", aOBXValue).getSegment());
        }

        return aOBXOutGroup;
    }
//------------------------------------------------------------------------------
    /**
     * Move contents of ZBX stored data identified by pGroupID+ pItemID into an OBX segment with Segment_ID pOBXNum
     * @param pOBXidentifier
     * @param pOBXtype
     * @param pOBXvalue
     * @throws BHLibClasses.ICANException
     * @return
     */
    public HL7Segment processOBXFromZBX(String pOBXidentifier, String pOBXtype, String pOBXvalue) throws ICANException {
        HL7Segment aOBXSegment = new HL7Segment("OBX");
        aOBXSegment.set(HL7_23.OBX_1_set_ID, Integer.toString(++mOBXSegmentCount));
        aOBXSegment.set(HL7_23.OBX_2_value_type, pOBXtype );
        aOBXSegment.set(HL7_23.OBX_3_observation_identifier, pOBXidentifier );
        aOBXSegment.set(HL7_23.OBX_5_observation_value, pOBXvalue );

        return aOBXSegment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @throws BHLibClasses.ICANException
     * @return
     */
    public HL7Segment processZB2FromUFD() throws ICANException {
        HL7Message aHL7Mess = new HL7Message(mHL7Message, 0);
        HL7Segment aZB2Out = new HL7Segment("ZB2");

        if (! aHL7Mess.isEvent("O01")) {   // for ADT only ...
            if (cMedicareNumber.length() == 3) {
                String aCardNum = "            " + cMedicareNumber;
                aZB2Out.set(PARIS_23.ZB2_1_medicare_number, aCardNum);
            } else {
                aZB2Out.set(PARIS_23.ZB2_1_medicare_number, cMedicareNumber);
            }
            aZB2Out.set(PARIS_23.ZB2_2_DVA_number, cDVANumber);
            aZB2Out.set(PARIS_23.ZB2_3_HIC_provider_number, cAttendingDr);
            aZB2Out.set(PARIS_23.ZB2_4_TAC_claim_number, getFromZBX("FINANCE", "CLAIM_NUMBER"));
            aZB2Out.set(PARIS_23.ZB2_6_pension_number, cPensionNumber);
            aZB2Out.set(PARIS_23.ZB2_7_home_phone_number, cHomePhone);
        } else {        // ... For Orders only ...
            aZB2Out.set(PARIS_23.ZB2_9_packet_print_location, getFromZBX("ORDER", "RADIOLOGY_PRINTER"));
            aZB2Out.set(PARIS_23.ZB2_10_spare, "XX");
        }
        return aZB2Out;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processR01MSHFromUFD() {
        HL7Segment aOutMSHSegment = new HL7Segment("MSH");
        HL7Segment aInMSHSegment = new HL7Segment(mInHL7Message.getSegment("MSH"));
        HL7Segment aInOBRSegment = new HL7Segment(mInHL7Message.getSegment("OBR", 1));
        aOutMSHSegment.linkTo(aInMSHSegment);
        aOutMSHSegment.copy(HL7_23.MSH_2_encoding_characters);
        aOutMSHSegment.copy(HL7_23.MSH_3_sending_application);
        aOutMSHSegment.set(HL7_23.MSH_4_sending_facility, "ALF");
        aOutMSHSegment.set(HL7_23.MSH_5_receiving_application, "PARIS");
        aOutMSHSegment.set(HL7_23.MSH_6_receiving_facility, "01");
        aOutMSHSegment.set(HL7_23.MSH_7_message_date_time, aOutMSHSegment.getDateTime());
        aOutMSHSegment.copy(HL7_23.MSH_9_message_type);
        String aMSH7MsgDateTime = aOutMSHSegment.get(HL7_23.MSH_7_message_date_time);
        String aOBR3FillerOrderNum = aInOBRSegment.get(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID);
        String aMsgCtrlID = aOBR3FillerOrderNum.concat(aMSH7MsgDateTime);
        aOutMSHSegment.set(HL7_23.MSH_10_message_control_ID, aMsgCtrlID);
        aOutMSHSegment.set(HL7_23.MSH_11_processing_ID, "P");
        aOutMSHSegment.set(HL7_23.MSH_12_version_ID, "2.3");

        return aOutMSHSegment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processR01PIDFromUFD() {
        HL7Segment aInPIDSegment = new HL7Segment(mInHL7Message.getSegment("PID"));
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        HL7Segment aOutPIDSegment = new HL7Segment("PID");
        aOutPIDSegment.linkTo(aInPIDSegment);
        aOutPIDSegment.set(HL7_23.PID_1_set_ID, "1");

        String aPID3PatientID = aInPIDSegment.get(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
        if (aPID3PatientID.startsWith("Z")) {
            aPID3PatientID = aPID3PatientID.substring(1);
            aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, aPID3PatientID, 1);
        } else {
            aOutPIDSegment.copy(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_ID_number, 1);
        }
        aOutPIDSegment.set(HL7_23.PID_3_patient_ID_internal, HL7_23.CX_assigning_authority, "00001");

        String aPID5PatientLastName = aInPIDSegment.get(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        if (aPID5PatientLastName.startsWith("ZZZTEST-")) {
            aPID5PatientLastName = aPID5PatientLastName.substring(8);
            aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, aPID5PatientLastName, 1);
        } else {
            aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_family_name, 1);
        }

        aOutPIDSegment.copy(HL7_23.PID_5_patient_name, HL7_23.XPN_given_name, 1);
        aOutPIDSegment.set(HL7_23.PID_5_patient_name, HL7_23.XPN_name_type, "L");
        aOutPIDSegment.copy(HL7_23.PID_7_date_of_birth);
        aOutPIDSegment.copy(HL7_23.PID_8_sex);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_1, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_street_2, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_city, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_state_or_province, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_zip, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_country, 1);
        aOutPIDSegment.copy(HL7_23.PID_11_patient_address, HL7_23.XAD_geographic_designation, 1);
        aOutPIDSegment.copy(HL7_23.PID_13_home_phone, HL7_23.XTN_telephone_number, 1);
        aOutPIDSegment.copy(HL7_23.PID_14_business_phone, HL7_23.XTN_telephone_number, 1);
        String aPV119VisitNum = aInPV1Segment.get(HL7_23.PV1_19_visit_number);
        aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_ID_number, aPV119VisitNum);
        aOutPIDSegment.set(HL7_23.PID_18_account_number, HL7_23.CX_assigning_authority, "01");

        return aOutPIDSegment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Group processR01PD1NTEs_FromUFD() {
        HL7Group aOutPD1NTEGroup = new HL7Group("");
        int aGroupCount;
        int aCount = 1;
        String aGroupID[] = HL7_23.Group_PD1Notes;
        aGroupCount = mInHL7Message.countGroups(aGroupID);

        for (aCount = 1; aCount <= aGroupCount; aCount++) {
            aOutPD1NTEGroup.append(mInHL7Message.getGroup(aGroupID, aCount));
        }

        return aOutPD1NTEGroup;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Segment processR01PV1FromUFD() {
        HL7Segment aOutPV1Segment = new HL7Segment("");
        HL7Segment aInPV1Segment = new HL7Segment(mInHL7Message.getSegment("PV1"));
        if (!cSourceSystem.equalsIgnoreCase("POWERSCRIBE")) {
            if (aInPV1Segment.getSegment().length() > 0) {
                aOutPV1Segment = new HL7Segment("PV1");
                aOutPV1Segment.linkTo(aInPV1Segment);
                aOutPV1Segment.copy(HL7_23.PV1_1_set_ID);
                aOutPV1Segment.copy(HL7_23.PV1_2_patient_class);
                aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_point_of_care_nu);
                aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room, "01");
                String aPatientClass = aInPV1Segment.get(HL7_23.PV1_2_patient_class);
                if (aPatientClass.equalsIgnoreCase("E") || aPatientClass.equalsIgnoreCase("O")) {
                    //Do nothing
                } else {
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_room);
                    aOutPV1Segment.copy(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_bed);
                }
            }
            aOutPV1Segment.set(HL7_23.PV1_3_assigned_patient_location, HL7_23.PL_facility_ID, "A/.SPC./");
            String aTempStr = aOutPV1Segment.getSegment();
            aTempStr = aTempStr.replaceAll("/.SPC./", " ");
            aOutPV1Segment.setSegment(aTempStr);
            aOutPV1Segment.copy(HL7_23.PV1_4_admission_type);
            int aConsultingDoctorCount = aInPV1Segment.countRepeatFields(HL7_23.PV1_9_consulting_doctor);
            for (int i = 1; i <= aConsultingDoctorCount; i++) {
                String aConsultingDoctorIDNum = aInPV1Segment.get(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
                if (aConsultingDoctorIDNum.length() > 0) {
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_last_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_first_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_middle_initial_or_name, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_suffix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_prefix, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_degree, i);
                    aOutPV1Segment.copy(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_code_source_table, i);
                }
            }

            if (!aInPV1Segment.isEmpty(HL7_23.PV1_9_consulting_doctor, HL7_23.XCN_ID_num)) {
                aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            }
            aOutPV1Segment.copy(HL7_23.PV1_10_hospital_service);
            aOutPV1Segment.copy(HL7_23.PV1_14_admit_source);
            aOutPV1Segment.copy(HL7_23.PV1_18_patient_type);
            aOutPV1Segment.copy(HL7_23.PV1_19_visit_number, HL7_23.CX_ID_number);
            aOutPV1Segment.copy(HL7_23.PV1_44_admit_date_time);
        } else {
            aOutPV1Segment = aInPV1Segment;
        }
        return aOutPV1Segment;
    }
//------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    public HL7Group processR01ReqDets_FromUFD() {
        HL7Group aOutReqDetsGroup = new HL7Group("");
        HL7Group aInReqDetsGroup = new HL7Group("");
        HL7Segment aOutORCSegment = new HL7Segment("");
        HL7Segment aOutOBRSegment = new HL7Segment("");
        HL7Segment aOutNTESegment = new HL7Segment("");
        HL7Segment aOutOBXSegment = new HL7Segment("");

        int aRequestDetsCount = mInHL7Message.countGroups(HL7_23.Group_Orders);
        for (int i = 1; i <= aRequestDetsCount; i++) {
            aInReqDetsGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Orders, i));

            if (!cSourceSystem.equalsIgnoreCase("POWERSCRIBE")) {
                //Process DDS message
                //process ORC
                HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
                HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                String aPlacerOrderNumEntID = aInOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                String aFillerOrderNumEntID = aInORCSegment.get(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                if (aPlacerOrderNumEntID.equalsIgnoreCase(aFillerOrderNumEntID)) {
                    //do nothing
                } else {
                    aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID, aPlacerOrderNumEntID);
                    aOutORCSegment.set(HL7_23.ORC_2_placer_order_num, HL7_23.EI_2_namespace_ID, "RA");
                }
                aOutORCSegment.copy(HL7_23.ORC_3_filler_order_num, HL7_23.EI_1_entity_ID);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, HL7_23.EI_2_namespace_ID, "RA");
                aOutORCSegment.copy(HL7_23.ORC_5_order_status);
                aOutORCSegment.copy(HL7_23.ORC_9_date_time_of_trans);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_10_entered_by, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_ID_num);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_last_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_first_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_middle_initial_or_name);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_suffix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_prefix);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_degree);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_code_source_table);
                aOutORCSegment.copy(HL7_23.ORC_12_ordering_provider, HL7_23.XCN_assigning_authority);
                aOutORCSegment.copy(HL7_23.ORC_13_enterers_location);
                aOutReqDetsGroup.append(aOutORCSegment);

                //process OBR
                //HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copy(HL7_23.OBR_1_Set_ID);
                if (aPlacerOrderNumEntID.equalsIgnoreCase(aFillerOrderNumEntID)) {
                    //do nothing
                } else {
                    aOutOBRSegment.copy(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_1_entity_ID);
                    aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
                }
                aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_1_entity_ID, aFillerOrderNumEntID);
                aOutOBRSegment.set(HL7_23.OBR_3_Filler_Order_Number, HL7_23.EI_2_namespace_ID, "RA");
                String aRelClinicalInfo = aInOBRSegment.get(HL7_23.OBR_13_Relevant_Clinical_Information);
                if (aRelClinicalInfo.length() == 0) {
                    String aUniveralServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                    CodeLookUp aLU = new CodeLookUp("PARIS_Exam_Procedure.table", mEnvironment);
                    mSReg1 = aLU.getValue(aUniveralServiceID);
                    aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, mSReg1);
                } else {
                    aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, aRelClinicalInfo);
                }
                String aUniversalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aUniversalServiceID);
                String aFillerField1 = aInOBRSegment.get(HL7_23.OBR_20_Fillers_Field_1);
                if (aFillerField1.length() >= 14) {
                    aFillerField1 = aFillerField1.substring(0, 14);
                }
                aOutOBRSegment.set(HL7_23.OBR_7_Observation_Date_Time, aFillerField1);
                int aCollectorIDCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_10_collector_ID);
                for (int j = 1; j <= aCollectorIDCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_middle_initial_or_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_suffix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_prefix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_degree, j);
                    aOutOBRSegment.copy(HL7_23.OBR_10_collector_ID, HL7_23.XCN_code_source_table, j);
                }
                aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_ID_code);
                aOutOBRSegment.copy(HL7_23.OBR_12_Danger_Code, HL7_23.CE_text);
                aOutOBRSegment.copy(HL7_23.OBR_14_Specimen_Received_Date_Time);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.CE_ID_code);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.CE_text);
                aOutOBRSegment.copy(HL7_23.OBR_15_Specimen_Source, HL7_23.Source_3_Collection_Method);

                int aOrderingProviderCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_16_Ordering_Provider);
                for (int j = 1; j <= aOrderingProviderCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_ID_num, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_last_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_first_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_middle_initial_or_name, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_suffix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_prefix, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_degree, j);
                    aOutOBRSegment.copy(HL7_23.OBR_16_Ordering_Provider, HL7_23.XCN_code_source_table, j);
                }

                aOutOBRSegment.copy(HL7_23.OBR_18_Placers_Field_1);
                aOutOBRSegment.copy(HL7_23.OBR_19_Placers_Field_2);
                if (mEnvironment.indexOf("TEST") >= 0 ) {
                    aOutOBRSegment.copy(HL7_23.OBR_21_Fillers_Field_2);
                } else {
                    String aORCPlacerOrderNumEntID = aInORCSegment.get(HL7_23.ORC_2_placer_order_num, HL7_23.EI_1_entity_ID);
                    if (aORCPlacerOrderNumEntID.length() >= 10) {
                        String aFillersField2 = "00".concat(aORCPlacerOrderNumEntID.substring(0, 10));
                        aOutOBRSegment.set(HL7_23.OBR_21_Fillers_Field_2, aFillersField2);
                    }
                }

                if (aFillerField1.length() > 14) {
                    aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, aFillerField1.substring(0, 14));
                } else {
                    aOutOBRSegment.set(HL7_23.OBR_22_Results_RPT_Status_Change, aFillerField1);
                }

                aOutOBRSegment.set(HL7_23.OBR_24_Diagnostic_Service_Section_ID, "RAD");
                aOutOBRSegment.copy(HL7_23.OBR_25_Results_Status);
                aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.CE_ID_code);
                aOutOBRSegment.copy(HL7_23.OBR_26_parent_result, HL7_23.Observation_sub_ID);

                int aQuantityTimingCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_27_Quantity_Timing);
                for (int j = 1; j <= aQuantityTimingCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_4_start_date_time, j);
                    aOutOBRSegment.set(HL7_23.OBR_27_Quantity_Timing, HL7_23.TQ_6_priority, "RT", j);
                }

                aOutOBRSegment.copy(HL7_23.OBR_29_Parent);
                aOutOBRSegment.copy(HL7_23.OBR_30_Transportation_Mode);

                int aReasonForStudyCount = aInOBRSegment.countRepeatFields(HL7_23.OBR_31_Reason_For_Study);
                for (int j = 1; j <= aReasonForStudyCount; j++) {
                    aOutOBRSegment.copy(HL7_23.OBR_31_Reason_For_Study, HL7_23.CE_text, j);
                }
                CodeLookUp aLU = new CodeLookUp("DDS_PARIS_ID.table", mEnvironment);
                if (mEnvironment.indexOf("TEST") == -1) {
                    String aInterpNameField = aInOBRSegment.get(HL7_23.OBR_32_Principal_Result_Interpreter, "CN_1", 1);
                    HL7Field aInterpNameSubfield = new HL7Field(aInterpNameField, k.AMPERSAND_GET, k.AMPERSAND_SET);
                    String aInterpNameIDNum = aInterpNameSubfield.getSubField(1);
                    aInterpNameIDNum = aLU.getValue(aInterpNameIDNum);
                    aOutOBRSegment.set(HL7_23.OBR_32_Principal_Result_Interpreter, aInterpNameIDNum);
                } else {
                    aOutOBRSegment.copy(HL7_23.OBR_32_Principal_Result_Interpreter, 1);
                }

                //aInterpNameField = aInOBRSegment.get(HL7_23.OBR_35_transcriptionist, "CN_1", 1);
                //aInterpNameSubfield = new HL7Field(aInterpNameField, k.AMPERSAND_GET, k.AMPERSAND_SET);
                //aInterpNameIDNum = aInterpNameSubfield.getSubField(1);
                //aOutOBRSegment.set(HL7_23.OBR_35_transcriptionist, aInterpNameIDNum, 1);
                aOutOBRSegment.copy(HL7_23.OBR_35_transcriptionist, 1);

                aOutReqDetsGroup.append(aOutOBRSegment);

                //process NTE
                HL7Group aOutNTEGroup = new HL7Group("");
                int aNTESegmentCount = aInReqDetsGroup.countSegments(HL7_23.NTE);
                for (int j = 1; j <= aNTESegmentCount; j++) {
                    HL7Segment aInNTESegment = new HL7Segment(aInReqDetsGroup.getSegment("NTE", j));
                    aOutNTESegment = new HL7Segment("NTE");
                    aOutNTESegment.linkTo(aInNTESegment);
                    aOutNTESegment.copy(HL7_23.NTE_1_setID);
                    aOutNTESegment.copy(HL7_23.NTE_2_source_of_comment);
                    aOutNTESegment.copy(HL7_23.NTE_3_comment, 1);
                    aOutNTEGroup.append(aOutNTESegment);
                }
                aOutReqDetsGroup.append(aOutNTEGroup);

                //process OBX
                HL7Group aOutOBXGroup = new HL7Group("");
                int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
                for (int k = 1; k <= aOBXSegmentCount; k++) {
                    HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                    aOutOBXSegment = new HL7Segment("OBX");
                    aOutOBXSegment.linkTo(aInOBXSegment);
                    aOutOBXSegment.copy(HL7_23.OBX_1_set_ID);
                    aOutOBXSegment.copy(HL7_23.OBX_2_value_type);

                    if (aRelClinicalInfo.length() == 0) {
                        aLU = new CodeLookUp("PARIS_EXAM_PROCEDURE.table", mEnvironment);
                        mSReg1 = aLU.getValue(aUniversalServiceID);
                        aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, mSReg1.concat("&READ"));
                    } else {
                        aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, HL7_23.CE_ID_code, aRelClinicalInfo.concat("&READ"));
                    }

                    int aOBX5FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_5_observation_value);
                    for (int m = 1; m <= aOBX5FieldCount; m++) {
                        String aObsValue = aInOBXSegment.get(HL7_23.OBX_5_observation_value);

                        if (aObsValue.equalsIgnoreCase("")) {
                            aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "\\.br\\\\.br\\", m);
                            if (k > 1) {
                                //set the OBX values for previous group
                            }
                        } else {
                            aOutOBXSegment.copy(HL7_23.OBX_5_observation_value, m);
                        }
                    }
                    if (aOBX5FieldCount == 0) {
                        aOutOBXSegment.set(HL7_23.OBX_5_observation_value, "\\.br\\\\.br\\");
                    }
                    String aOBX6Unit = aInOBXSegment.get(HL7_23.OBX_6_units);
                    aOBX6Unit = aOBX6Unit.replaceAll("\\^", "/S/");
                    aOutOBXSegment.set(HL7_23.OBX_6_units, aOBX6Unit);
                    aOutOBXSegment.copy(HL7_23.OBX_7_references_range);

                    int aOBX8FieldCount = aInOBXSegment.countRepeatFields(HL7_23.OBX_8_abnormal_flags);
                    for (int n = 1; n <= aOBX8FieldCount; n++) {
                        String aAbnormalFlag = aInOBXSegment.get(HL7_23.OBX_8_abnormal_flags);
                        if (aAbnormalFlag.length() > 0) {
                            aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                        } else {
                            String aObservResultsStatus = aInOBXSegment.get(HL7_23.OBX_11_observ_results_status);
                            if (aObservResultsStatus.equalsIgnoreCase("C")) {
                                aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, "N", n);
                            } else {
                                aOutOBXSegment.set(HL7_23.OBX_8_abnormal_flags, aAbnormalFlag, n);
                            }
                        }
                    }

                    aOutOBXSegment.copy(HL7_23.OBX_11_observ_results_status);
                    aOutOBXSegment.copy(HL7_23.OBX_12_date_last_obs_normal_values);
                    aOutOBXSegment.copy(HL7_23.OBX_14_date_time_of_the_observation);
                    aOutOBXSegment.copy(HL7_23.OBX_15_producers_ID, HL7_23.CE_ID_code);

                    aOutOBXGroup.append(aOutOBXSegment);

                    //process OBX NTEs
                    HL7Group aOutOBXNTEGroup = new HL7Group("");
                    HL7Group aInOBXNTEGroup = new HL7Group(mInHL7Message.getGroup(HL7_23.Group_Observation_Details, k));
                    int aOBXNTESegmentCount = aInOBXNTEGroup.countSegments(HL7_23.NTE);
                    for (int j = 1; j <= aOBXNTESegmentCount; j++) {
                        HL7Segment aInOBXNTESegment = new HL7Segment(aInOBXNTEGroup.getSegment("NTE", j));
                        HL7Segment aOutOBXNTESegment = new HL7Segment("NTE");
                        aOutOBXNTESegment.linkTo(aInOBXNTESegment);
                        aOutOBXNTESegment.copy(HL7_23.NTE_1_setID);
                        aOutOBXNTESegment.copy(HL7_23.NTE_3_comment);
                        aOutOBXNTEGroup.append(aOutOBXNTESegment);
                    }
                    aOutOBXGroup.append(aOutOBXNTEGroup);
                }
                aOutReqDetsGroup.append(aOutOBXGroup);
            } else {
                //Powerscribe processing

                //ORC processing
                HL7Segment aInORCSegment = new HL7Segment(aInReqDetsGroup.getSegment("ORC"));
                HL7Segment aInOBRSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBR"));
                aOutORCSegment = new HL7Segment("ORC");
                aOutORCSegment.linkTo(aInORCSegment);
                aOutORCSegment.copy(HL7_23.ORC_1_order_control);
                String aOBR_2_value = aInOBRSegment.get(HL7_23.OBR_2_Placer_Order_Number);
                aOutORCSegment.set(HL7_23.ORC_3_filler_order_num, aOBR_2_value);
                aOutReqDetsGroup.append(aOutORCSegment);

                //OBR processing
                aOutOBRSegment = new HL7Segment("OBR");
                aOutOBRSegment.linkTo(aInOBRSegment);
                aOutOBRSegment.copyFields();
                String aORC_2_placerOrderNum = aInORCSegment.get(HL7_23.ORC_2_placer_order_num);
                if (mEnvironment.indexOf("TEST") >= 0) {
                    aORC_2_placerOrderNum = aORC_2_placerOrderNum.substring(0, 2) + "0" + aORC_2_placerOrderNum.substring(3, 10);
                }
                aOutOBRSegment.set(HL7_23.OBR_21_Fillers_Field_2, "00".concat(aORC_2_placerOrderNum));
                aOutOBRSegment.move(HL7_23.OBR_3_Filler_Order_Number, HL7_23.OBR_2_Placer_Order_Number);
                aOutOBRSegment.set(HL7_23.OBR_2_Placer_Order_Number, "");
                CodeLookUp aPowerscribeLU = new CodeLookUp("POWERSCRIBE_PARIS_ID.table", mEnvironment);
                String aOBR_4_universalServiceID = aInOBRSegment.get(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code);
                String aOBR_4_LU = aPowerscribeLU.getValue(aOBR_4_universalServiceID);
                if (aOBR_4_LU.length() > 0) {
                    aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_ID_code, aOBR_4_LU);
                    aOutOBRSegment.set(HL7_23.OBR_4_Universal_Service_ID, HL7_23.CE_text, aOBR_4_universalServiceID);
                }
                aOutReqDetsGroup.append(aOutOBRSegment);

                //OBX processing
                HL7Group aOutOBXGroup = new HL7Group("");
                int aOBXSegmentCount = aInReqDetsGroup.countSegments(HL7_23.OBX);
                for (int k = 1; k <= aOBXSegmentCount; k++) {
                    HL7Segment aInOBXSegment = new HL7Segment(aInReqDetsGroup.getSegment("OBX", k));
                    aOutOBXSegment = new HL7Segment("OBX");
                    aOutOBXSegment.linkTo(aInOBXSegment);
                    aOutOBXSegment.copyFields();
                    String aOBX_3_observationIdentifier = aInOBXSegment.get(HL7_23.OBX_3_observation_identifier);
                    String aOBX_3_LU = aPowerscribeLU.getValue(aOBR_4_universalServiceID.concat(" ").concat(aOBX_3_observationIdentifier));
                    if (aOBX_3_LU.length() > 0) {
                        aOutOBXSegment.set(HL7_23.OBX_3_observation_identifier, aOBX_3_LU);
                    }
                    aOutOBXGroup.append(aOutOBXSegment);
                }
                aOutReqDetsGroup.append(aOutOBXGroup);
            }
        }
        return aOutReqDetsGroup;
    }
}
