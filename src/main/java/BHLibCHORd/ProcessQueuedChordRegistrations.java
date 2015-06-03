/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BHLibCHORd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author da-admin
 */
public class ProcessQueuedChordRegistrations {

    private static final String ADTTemplate = "MSH|^~\\&|DGATE|{@HospCode}|OPAS|{@HospCode}|{@now}||ADT^{@rowType}|{@now}|P|2.3\r" +
            "EVN||{@now}\r" +
            "PID|||{@PatientId}^^^{@HospCode}^PI^{@HospCode}|{@PensionId}|{@PatientFamilyName}^{@PatientGivenName}^{@PatientMiddleName}^^{@PatientTitle}^^L||{@BirthDate}|{@SexCode}||{@EthnicOriginCode}|{@PatientAddress1}^{@PatientAddress2}^{@PatientSuburb}^{@PatientState}^{@PatientPostcode}||{@PatientPhoneNo}|{@PatientBusPhoneNo}|{@LanguageCode}|{@MaritalStatusCode}|{@ReligionCode}||{@MedicareNo}{@MedicareLineNo} {@MedicareSuffix}||||{@PatientCountryCode}||||{@DVAId}||{@DeathDate}\r" +
            "PV1|1|{@AdmTypeCode}||{@AdmTypeDesc}||||||{@UnitDesc}|||||||||{@VisitNumber}|||||||||||||||||{@DispositionCode}^{@DispositionDesc}|{@DestinationCode}^{@DestinationDesc}|||||||{@AdmDate}|{@DischargeDate}\r" +
            "PV2|||^{@AdmReason}\r" +
            "ZUI|{@UID}\r";
    private static final String PMITemplate = "MSH|^~\\&|DGATE|{@HospCode}|OPAS|{@HospCode}|{@now}||ADT^A31|{@now}|P|2.3\r" +
            "EVN||{@now}\r" +
            "PID|||{@PatientId}^^^MRN^PI^{@HospCode}|{@PensionId}|{@PatientFamilyName}^{@PatientGivenName}^{@PatientMiddleName}^^{@PatientTitle}^^L||{@BirthDate}|{@SexCode}||{@EthnicOriginCode}|{@PatientAddress1}^{@PatientAddress2}^{@PatientSuburb}^{@PatientState}^{@PatientPostcode}||{@PatientPhoneNo}|{@PatientBusPhoneNo}|{@LanguageCode}|{@MaritalStatusCode}|{@ReligionCode}||{@MedicareNo}{@MedicareLineNo} {@MedicareSuffix}||||{@PatientCountryCode}||||{@DVAId}||{@DeathDate}\r" +
            "ZUI|{@UID}\r";
    private static final SimpleDateFormat hl7Date = new SimpleDateFormat("yyyyMMddhhmmss");
    Connection conn;
    //PreparedStatement vwCHORdQueuedPMI;
    PreparedStatement vwCHORdQueuedADT;  //not use
    PreparedStatement selectTblCHORdQueue;
    PreparedStatement selectVwCHORdQueuedPMI;
    PreparedStatement selectVwCHORdQueuedADT;
    PreparedStatement updateTblCHORdQueue;
    private static final String selectTblCHORdQueueString = "SELECT id, rowType FROM tblCHORdQueue where id in (1396595,1396595)";  //"SELECT id FROM tblCHORdQueue where sent = 0";
    private static final String selectVwCHORdQueuedPMIString = "SELECT * FROM vwCHORdQueuedPMI where id = ?";
    private static final String selectVwCHORdQueuedADTString = "SELECT * FROM vwCHORdQueuedADM where id = ?";
    private static final String updateTblCHORdQueueString = "Update tblCHORdQueue set sent = 1, HL7Text = ? where id = ?";

    public ProcessQueuedChordRegistrations() throws Exception {
        CHORdWrapper chordWrapper = new CHORdWrapper();
        conn = chordWrapper.getConnection();

        selectTblCHORdQueue = conn.prepareStatement(selectTblCHORdQueueString);
        selectVwCHORdQueuedPMI = conn.prepareStatement(selectVwCHORdQueuedPMIString);
        selectVwCHORdQueuedADT = conn.prepareStatement(selectVwCHORdQueuedADTString);
        updateTblCHORdQueue = conn.prepareStatement(updateTblCHORdQueueString);
    //vwCHORdQueuedPMI = conn.prepareStatement("select TOP 1 * from vwCHORdQueuedPMI");
    //vwCHORdQueuedADT = conn.prepareStatement("SELECT * FROM vwCHORdQueuedADM where PatientId like '6106791'");

    }

    private ArrayList<HashMap> SelectQueuedRows() throws Exception {
        ResultSet rsTblCHORdQueueSelect = selectTblCHORdQueue.executeQuery();
        ArrayList<HashMap> ids = new ArrayList<HashMap>();
        while (rsTblCHORdQueueSelect.next()) {
            HashMap<String, String> idRowTypeMap = new HashMap<String, String>();
            int id = rsTblCHORdQueueSelect.getInt("id");
            String rowType = rsTblCHORdQueueSelect.getString("rowType");
            idRowTypeMap.put("id", String.valueOf(id));
            idRowTypeMap.put("rowType", rowType);
            ids.add(idRowTypeMap);
        }
        return ids;
    }

    public Object[] ProcessQueueMessages() throws Exception {
        //ResultSet rsCHORdQueuedPMI = vwCHORdQueuedPMI.executeQuery();
        ArrayList messages = new ArrayList();
        //messages.addAll(createMessages(rsCHORdQueuedPMI, PMITemplate));
        ArrayList<HashMap> ids = SelectQueuedRows();
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            HashMap<String, String> id = (HashMap) it.next();
            int rowId = Integer.valueOf(id.get("id"));
            String rowType = id.get("rowType");
            String hl7Msg = "";
            if (rowType.equals("PMI")) {
                selectVwCHORdQueuedPMI.setInt(1, rowId);
                ResultSet rsSelectRow = selectVwCHORdQueuedPMI.executeQuery();
                hl7Msg = createMessage(rsSelectRow, PMITemplate);
                messages.add(hl7Msg);
            } else {
                selectVwCHORdQueuedADT.setInt(1, rowId);
                ResultSet rsSelectRow = selectVwCHORdQueuedADT.executeQuery();
                hl7Msg = createMessage(rsSelectRow, ADTTemplate);
                messages.add(hl7Msg);
            }
            //update queue table comment out for testing
//            updateTblCHORdQueue.setString(1, hl7Msg);
//            updateTblCHORdQueue.setInt(2, rowId);
//            updateTblCHORdQueue.executeUpdate();
            //conn.commit();
        }
        return messages.toArray();
    }

    public Object[] ProcessQueue() throws Exception {
        //ResultSet rsCHORdQueuedPMI = vwCHORdQueuedPMI.executeQuery();
        ArrayList messages = new ArrayList();
        //messages.addAll(createMessages(rsCHORdQueuedPMI, PMITemplate));
        ResultSet rsCHORdQueuedADT = vwCHORdQueuedADT.executeQuery();
        messages.addAll(createMessages(rsCHORdQueuedADT, ADTTemplate));

        return messages.toArray();
    }

    private String createMessage(ResultSet rsCHORdQueuedPMI, String template) throws SQLException {
        //ArrayList messages = new ArrayList();
        Date datenow = new Date();
        String now = hl7Date.format(datenow);
        ResultSetMetaData rsmdCHORdQueuedPMI = rsCHORdQueuedPMI.getMetaData();
        //int fetchSize = rsCHORdQueuedPMI.getFetchSize();
        int colCount = rsmdCHORdQueuedPMI.getColumnCount();
        String outHL7 = template;
        while (rsCHORdQueuedPMI.next()) {
            for (int i = 1; i < colCount; i++) {
                String label = rsmdCHORdQueuedPMI.getColumnLabel(i);
                String value = rsCHORdQueuedPMI.getString(i);
                String type = rsmdCHORdQueuedPMI.getColumnTypeName(i);
                if (type.equalsIgnoreCase("datetime") && value != null) {
                    value = hl7Date.format(rsCHORdQueuedPMI.getDate(i));
                }

                if (value == null) {
                    value = "";
                }

                outHL7 = outHL7.replaceAll("\\{@" + label + "\\}", value.trim());
            }
            outHL7 = outHL7.replaceAll("\\{@now\\}", now);
        //messages.add(outHL7);
        }
        return outHL7;
    }

    private ArrayList createMessages(ResultSet rsCHORdQueuedPMI, String template) throws SQLException {
        ArrayList messages = new ArrayList();
        Date datenow = new Date();
        String now = hl7Date.format(datenow);
        ResultSetMetaData rsmdCHORdQueuedPMI = rsCHORdQueuedPMI.getMetaData();
        int colCount = rsmdCHORdQueuedPMI.getColumnCount();
        while (rsCHORdQueuedPMI.next()) {
            String outHL7 = template;
            for (int i = 1; i < colCount; i++) {
                String label = rsmdCHORdQueuedPMI.getColumnLabel(i);
                String value = rsCHORdQueuedPMI.getString(i);
                String type = rsmdCHORdQueuedPMI.getColumnTypeName(i);
                if (type.equalsIgnoreCase("datetime") && value != null) {
                    value = hl7Date.format(rsCHORdQueuedPMI.getDate(i));
                }

                if (value == null) {
                    value = "";
                }

                outHL7 = outHL7.replaceAll("\\{@" + label + "\\}", value.trim());
            }
            outHL7 = outHL7.replaceAll("\\{@now\\}", now);
            messages.add(outHL7);
        }
        return messages;
    }
}
