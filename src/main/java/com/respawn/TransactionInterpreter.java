package com.respawn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


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
     * prizes: wfood, drinks, snacks
     */
    private static final Set<String> includedCategories = 
    Set.of("7671f6e0-6401-4a51-87b1-232c284d5b7b","2746588c-4faf-46b8-ae03-22e057522f65",
           "413a61df-9d12-4bac-81c1-eccfe2244584", "eb552470-da82-4eb5-8383-9fa17fee33ff",
           "870e3beb-4ea2-40ed-96d2-9fa7d1973d46", "86fbb02d-7262-46b4-ba9e-04e55086ebd9",
           "ee3cb341-6fa1-480c-a5c0-1844ecef6dde", "c839660f-909e-4e61-92e9-683561a996e0",
           "5c763fc4-1136-4e03-92f7-b8b3737c7014", "e74ba05b-8e97-42a8-8664-00b71ed455b7",
           "0b1af0e2-cda6-402f-94b2-5488af1e7008", "388a5ccd-2068-4506-98e5-d9865a4f480b",
           "aac2f2e0-cab7-473a-87d0-8d3eb6d08351","2281506d-aadc-4e29-916a-2e438a5e5f28"
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

    /*
     * Takes each order and loops through every item, adding it to the order
     * If ANY item meets the requirements of printing the whole order is added to
     * the print array and returned. 
     */
     public static List<Order> removeOffers(String transactions, HashSet<String> targetIDs) throws Exception {
        List<Order> printableOrders = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(transactions);
        Iterator<String> fields = rootNode.fieldNames();

        String transactionsKey = fields.next();
        JsonNode transactionsNode = rootNode.path(transactionsKey);

        for (JsonNode transaction : transactionsNode) {
            JsonNode itemsNode = transaction.path("Items");
            Iterator<String> itemIds = itemsNode.fieldNames();
            while (itemIds.hasNext()) {
                String itemId = itemIds.next();
                JsonNode item = itemsNode.get(itemId);
                if(includedCategories.contains(item.get("CategoryUuid").asText())
                    || (transaction.get("Source").isNull() 
                    && userCategories.contains(item.get("CategoryUuid").asText()))){
                    System.out.println(item.get("Name").asText() + " ordered. Printing Receipt. ");
                        
                    Order o = new Order();
                    try{
                        o.setEmployee(transaction.get("Source").asText());
                        JsonNode userNode = transaction.path("User");

                        o.setUser(userNode.get("Username").asText());
                    
                    } catch(java.lang.NullPointerException npe){

                    }


                    JsonNode detailsNode = transaction.path("Details");

                    o.setTime(
                        detailsNode.get("DateTime").asText().replace('T','\n').replace('Z', ' ')
                    );

                    printableOrders.add(o); 
                }
            }
        }
        
    return printableOrders;
    }

    
}
