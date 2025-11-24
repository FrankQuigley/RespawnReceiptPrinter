package com.respawn;


import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class ReceiptHandler {
    public static List<String> buildReceipt(Order o){
        String header = "=== Respawn ===\n\n";
        String time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
        String user = "User : "+o.user() +"\n\n";
        String body = "";
        for(String i : o.items()){
            body+=i +"\n\n";
        }
        String employee = "Employee: "+o.employee()+"\n\n";
        return List.of(header,time, user, body,employee);
    }


    public static void printReceipt(Order o) throws Exception {
        // ESC/POS commands
        final byte[] INIT = {0x1B, 0x40};         // Initialize
        final byte[] CENTER = {0x1B, 0x61, 0x01}; // Align center
        final byte[] LEFT = {0x1B, 0x61, 0x00};   // Align left
        final byte[] CUT = {0x1D, 0x56, 0x00};    // Full cut

        final byte[] SIZE = {0x1D, 0x21, 0x10};    // Font size

        String spacer = "\n\n\n\n\n\n\n\n\n\n\n\n";
        List<String> receipt = buildReceipt(o);
        String header = receipt.get(0);
        String time = receipt.get(1);
        String user = receipt.get(2);
        String body = receipt.get(3);
        String employee = receipt.get(4);

        // Build receipt with ESC/POS commands
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(INIT);       // reset printer
        out.write(SIZE);       // Set font size
        out.write(CENTER);     // center align
        out.write(spacer.getBytes("UTF-8"));
        out.write(header.getBytes("UTF-8"));
        out.write(time.getBytes("UTF-8"));
        out.write(user.getBytes("UTF-8"));
        out.write(LEFT);       // back to left align
        out.write(body.getBytes("UTF-8"));
        out.write("----------------------\n".getBytes("UTF-8"));
        out.write(employee.getBytes("UTF-8"));
        out.write("\n\n\n".getBytes("UTF-8")); // some spacing before cut
        out.write(CUT);        // send cut command

        // Send to printer
        byte[] data = out.toByteArray();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(data, flavor, null);

        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) throw new RuntimeException("No default printer found");

        service.createPrintJob().print(doc, null);
    }


    /*
     * Builds a fake order for testing
     */
    public static Order makeTest(){
        Order tester = new Order();
        tester.addItem("Mayo Machine");
        tester.addItem("Add Fries for Burger (Add 1x Sauce)");
        tester.addItem("Aioli");
        tester.addItem("Small New York Cheesecake");
        tester.setTime("2025-09-04\n05:08:39.401\n");
        tester.setUser("TESTER");
        tester.setEmployee("TESTER");
        return tester;
    }
    

}
