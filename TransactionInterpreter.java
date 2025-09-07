package com.respawn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TransactionInterpreter {

     
    /*  
     *
     * Each subcategory has associated ID listed below in order;
     * Burgers, Wings, Sides, Sauces, Desserts, 
     * Burger Removals, Burger Additions, Takeaways,
     * Milkshake, Share Jugs. 
     * 
     * Lollies, Chocolate, Savoury + Biscuits, Cans, Coca-cola Bottles,
     * Redbull + V Energy, Monsters, Water, Tea, Milk, Powerade
     * 
     */
    private static final Set<String> includedCategories = 
    Set.of("7671f6e0-6401-4a51-87b1-232c284d5b7b","2746588c-4faf-46b8-ae03-22e057522f65",
           "413a61df-9d12-4bac-81c1-eccfe2244584", "eb552470-da82-4eb5-8383-9fa17fee33ff",
           "870e3beb-4ea2-40ed-96d2-9fa7d1973d46", "86fbb02d-7262-46b4-ba9e-04e55086ebd9",
           "ee3cb341-6fa1-480c-a5c0-1844ecef6dde", "c839660f-909e-4e61-92e9-683561a996e0",
           "5c763fc4-1136-4e03-92f7-b8b3737c7014", "e74ba05b-8e97-42a8-8664-00b71ed455b7"
    );
     
    private static final Set<String> userCategories = 
    Set.of("b013c94b-0be0-4ff4-89ae-47506aea5334","9882eef7-905c-42a4-87f2-814e3cb1f079",
           "0f4fb4b8-30a3-4632-8d57-fd86554a6eba", "7104e340-dd18-4f91-85d1-3d95a87ae3f2",
           "709f0a21-ec7c-424b-85eb-c8b768dfe1b0", "4b505e21-d333-4d6a-8e6f-dc17317e9ccf",
           "95a051e0-e572-48b9-ba77-1a30b392c345", "08fb3111-e138-4b7e-8c1d-1871ea97380d"
    );

    /*
     * The API defines items through unique IDs like above
     * This makes referenceing the item difficult, thus the raw JSON string
     * is instead searched and parsed. This could be improved with a generic
     * JSON object which stores a list of field data so fields don't need to
     * be directly addressed. 
     */
    public static String getID(String s){
        StringBuilder id = new StringBuilder();
        int index = s.lastIndexOf("TransactionId\":\"") + 16;
        while(s.charAt(index) != '\"'){
            id.append(s.charAt(index++) );
        }
        return id.toString();
    }

    public static String getEmployee(String s){
        if(isUser(s)){ return "";}
        StringBuilder employee = new StringBuilder();
        int index = s.indexOf("\"Name\":\"") + 8;
        while(s.charAt(index) != '\"'){
            employee.append(s.charAt(index++) );
        }
        return employee.toString();
    }    

    public static String getUser(String s){
        if(s.contains("\"User\":null")){ return "Guest"; }
        StringBuilder user = new StringBuilder();
        int index = s.indexOf("\"Username\":\"") + 12;
        while(s.charAt(index) != '\"'){
            user.append(s.charAt(index++) );
        }
        return user.toString();
    }

    public static String getTime(String s){
        StringBuilder time = new StringBuilder();
        int index = s.indexOf("\"DateTime\":\"") + 12;
        while(s.charAt(index) != '\"'){
            time.append(s.charAt(index++) );
        }
        return time.toString();
    }


    /*
     * Name is used to refer to several points of data
     * so some instances need to be skipped
     */
    public static String getItemName(String s, int itemNum){
        StringBuilder name = new StringBuilder();
        for(int i = itemNum*2+1; i>0; i--){
            int skipIndex = s.indexOf("\"Name\":\"") + 8;
            int nullCheck = s.indexOf("\"Employee\":null") + 16;
            if(nullCheck != 15 && nullCheck < skipIndex){
                s = s.substring(nullCheck);
                continue;
            } 
            s = s.substring(skipIndex);
            
        }

        int index = s.indexOf("\"Name\":\"") + 8;
        while(s.charAt(index) != '\"'){
            name.append(s.charAt(index++) );
        }

        return name.toString();
        
    }

    public static String getItemQuantity(String s, int itemNum){
        StringBuilder num = new StringBuilder();
        for(int i = itemNum; i>0; i--){
            int skipIndex = s.indexOf("\"Quantity\":") + 11;
            s = s.substring(skipIndex);      
        }

        
        int index = s.indexOf("\"Quantity\":") + 11;
        while(s.charAt(index) != ','){
            num.append(s.charAt(index++) );
        }
        return num.append("x ").toString();
        
    }

    public static boolean isUser(String s){
        return s.contains("\"Employee\":null");
    }
    /*
     * Takes each order and loops through every item, adding it to the order
     * If ANY item meets the requirements of printing the whole order is added to
     * the print array and returned. 
     */
    public static List<Order> removeOffers(String[] transactions, HashSet<String> targetIDs) throws Exception {
        List<Order> printableOrders = new ArrayList<>();
        for(String s : transactions){
            if(!targetIDs.contains(getID(s))){ continue; }

            String originalTransaction = s;
            int num = 0;
            Order order = new Order();
            Boolean printable = false;
            while(true){

                int index = s.indexOf("CategoryUuid\":\"") + 15;
                if(index==14){
                    if(printable){ 
                        order.setEmployee(getEmployee(originalTransaction));
                        order.setUser(getUser(originalTransaction));
                        order.setTime(getTime(originalTransaction));
                        printableOrders.add(order); 
                    }
                    System.out.println("\nFull transaction : " + originalTransaction);
                    break;
                }

                StringBuilder categoryBuilder = new StringBuilder();
                while(s.charAt(index) != '\"'){
                    categoryBuilder.append(s.charAt(index++));
                }
                
                if(includedCategories.contains(categoryBuilder.toString()) || 
                    (isUser(originalTransaction) && userCategories.contains(categoryBuilder.toString()))){
                    System.out.println("\nFound Printable Category!!! : " + categoryBuilder);
                    printable = true; 
                }
                String item = getItemQuantity(originalTransaction, num);
                item  += getItemName(originalTransaction, num++);
                System.out.println("Item is : " +  item +"\n");
                order.addItem(item);

                s = s.substring(index);
            }
        }    
        return printableOrders;   
    }
}
