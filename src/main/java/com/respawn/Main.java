package com.respawn;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Main {
    private static int apiFails = 0;
    private static HashSet<String> prevIDs = new HashSet<>();

    //private static boolean errorWindowOpened = false;

    /* Uses scheduler to run every 60 seconds 
     * Creates a set of the 20 most recent transaction IDs and compares with previous
     * The new IDs are then checked for printable status 
     * The returned offers are then printed 
     */
    public static void main(String[] args){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();

                Image image = ImageIO.read(Main.class.getResource("/logo.png"));

                TrayIcon trayIcon = new TrayIcon(image, "Receipt Printer");
                trayIcon.setImageAutoSize(true);

                tray.add(trayIcon);
            }

        } catch (Throwable e) {
            handleError("Failed to generate tray image", e);
        }
        
        //try {ReceiptHandler.printReceipt(ReceiptHandler.makeTest());}catch(Exception e){}
        scheduler.scheduleAtFixedRate(() -> {
            HttpClient client = HttpClient.newHttpClient();
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
            } catch (Exception e) {
                if(e.getMessage().startsWith("Transactions GET fail")){
                    System.out.println("API GET fail :(");
                    apiFails++;
                    if(apiFails>5){ handleError("Main scheduler loop failed", e);}
                } else {
                    handleError("Main scheduler loop failed", e);
                }
            } catch (Throwable t){
                handleError("Main scheduler loop failed", t);
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private static synchronized void handleError(String context, Throwable t) {
        try {
            JOptionPane.showMessageDialog(
                    null,
                    context + "\n\n" + t,
                    "Respawn Error ( SCREENSHOT PLEASE, DON'T PANIC. IT WILL ALL BE OKAY :) )",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

