package com.respawn;


import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;


public class ReceiptHandler {
    private static String buildReceipt(Order o){
        StringBuilder receipt = new StringBuilder();
        receipt.append("\nTime : "+ o.time() +"\n");
        receipt.append("\nUser : "+ o.user() +"\n");

        o.items().stream().forEach(i->
            receipt.append(i+"\n")
        );

        receipt.append("\nEmployee : "+ o.employee() +"\n");

        return receipt.toString();
    }


    public static void printReceipt(Order o) throws Exception {
        String receiptText = buildReceipt(o);

        byte[] bytes = receiptText.getBytes("UTF-8");
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(bytes, flavor, null);


        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) {
            throw new RuntimeException("No default printer found");
        }

        DocPrintJob job = service.createPrintJob();
        job.print(doc, null);
    
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
        tester.setTime("2025-09-04T05:08:39.401Z");
        tester.setUser("Quigley");
        tester.setEmployee("FrankQ");
        return tester;
    }
    

}
