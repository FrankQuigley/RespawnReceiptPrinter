package com.respawn;

import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

public class Main {
    
    private static HashSet<String> prevIDs = new HashSet<>();

    //private static boolean errorWindowOpened = false;

    /* Uses scheduler to run every 60 seconds 
     * Creates a set of the 20 most recent transaction IDs and compares with previous
     * The new IDs are then checked for printable status 
     * The returned offers are then printed 
     */
    public static void main(String[] args){
        HttpClient client = HttpClient.newHttpClient();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        //try {ReceiptHandler.printReceipt(ReceiptHandler.makeTest());}catch(Exception e){}
        scheduler.scheduleAtFixedRate(() -> {
            try {            
                String transactions = TransactionPoller.pollApi(client);
                HashSet<String> ids = TransactionInterpreter.getIDs(transactions);
                                
                if(!prevIDs.isEmpty()){
                    HashSet<String> newIds = new HashSet<> (ids) ;
                    newIds.removeAll(prevIDs);
                    TransactionInterpreter.removeOffers(transactions, newIds).stream().forEach(o->{
                        try {
                            System.out.println(o);
                            ReceiptHandler.printReceipt(o);
                        } catch (Exception e){
                            handleError("Printing Receipt Failed", e);
                        }
                    });
                }
                prevIDs = ids;
            } catch (Throwable e) {
                handleError("Main scheduler loop failed", e);
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private static synchronized void handleError(String context, Throwable t) {
        try {
            JOptionPane.showMessageDialog(
                    null,
                    context + "\n\n" + t,
                    "Respawn Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

