package KXK190012;

import java.util.*;

/**
 * MDS class stores the Item information mapping and description mapping
 */
public class MDS {
    HashMap<Long, Entry> itemMap;                      //stores the Item ID - key , Item info - value
    HashMap<Long, TreeMap<Long,Money>> descriptionMap; //stores the Description - key , Map(Item ID-key , Item Price-value)

    /**
     * MDSEntry class stores the Item information like Item ID, Description, Price
     */
    private class Entry {
        long id;
        ArrayList<Long> description;
        Money price;

        public Entry(long id, ArrayList<Long> description, Money price){
            this.id = id;
            this.description = description;
            this.price = price;

        }
    }

    /**
     * Orders the Items in the TreeMap using prices of the Items stored in the key
     */
    class TreeMapPriceComparator implements Comparator<Long> {
        @Override
        public int compare(Long id1, Long id2) {
            if(itemMap.get(id2)==null)
                return 0;
            if (itemMap.get(id1).price.compareTo(itemMap.get(id2).price) > 0){
                return 1;
            } else if (itemMap.get(id1).price.compareTo(itemMap.get(id2).price) < 0) {
                return -1;
            } else {
                return id1.compareTo(id2);
            }
        }
    }

    /**
     * IDComparator compares the IDs of 2 items
     */
    class IDComparator implements Comparator<Entry>{

        @Override
        public int compare(Entry entry1, Entry entry2) {
            return Long.compare(entry1.id,entry2.id);
        }

    }


    /**
     * PriceComparator class compares the prices of 2 Items and returns 1 if price of Item o1>o2, -1 if price of Item o1<o2, 0 if both have equal prices
     */
    class PriceComparator implements Comparator<Entry> {

        @Override
        public int compare(Entry entry1, Entry entry2) {
            if(entry1.price.compareTo(entry2.price) == 0)
                return -1;
            return entry1.price.compareTo(entry2.price);
        }

    }


    // Constructors
    public MDS() {
        this.itemMap = new HashMap<>();
        this.descriptionMap = new HashMap<>();
    }

    /* Public methods of MDS. Do not change their signatures.
       __________________________________________________________________
       a. Insert(id,price,list): insert a new item whose description is given
       in the list.  If an entry with the same id already exists, then its
       description and price are replaced by the new values, unless list
       is null or empty, in which case, just the price is updated.
       Returns 1 if the item is new, and 0 otherwise.
    */

    public int insert(long id, Money price, List<Long> list) {

        ArrayList<Long> arrayList = new ArrayList<>(list);
        Entry newEntry = new Entry(id,arrayList,price);

        if(itemMap.containsKey(id)){
            if(list.size() == 0 || list == null){
                arrayList = itemMap.get(id).description;
            }
            delete(id);
            insert(id,price,arrayList);
            return 0;
        }else{
            itemMap.put(id,newEntry);
            for(Long desc : arrayList){
                TreeMap<Long,Money> tmap = descriptionMap.get(desc);
                if(tmap == null){
                    TreeMapPriceComparator comparator = new TreeMapPriceComparator();
                    TreeMap<Long,Money> newtmap = new TreeMap<Long,Money>(comparator);
                    newtmap.put(id,price);
                    descriptionMap.put(desc, newtmap);
                }else{
                    tmap.put(id,price);
                }
            }
            return 1;
        }
    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public Money find(long id) {
        Entry entry = itemMap.get(id);
        if(entry != null){
            return entry.price;
        }else{
            return new Money(0,0);
        }
    }

    /*
       c. Delete(id): delete item from storage.  Returns the sum of the
       long ints that are in the description of the item deleted,
       or 0, if such an id did not exist.
    */
    public long delete(long id) {
        Entry entry = itemMap.getOrDefault(id,null);
        if(entry ==  null){
            return 0;
        }else{
            long sum = 0;
            for(Long desc : entry.description){
                TreeMap<Long,Money> tmap = descriptionMap.get(desc);
                if(tmap!=null){
                    sum += desc;
                    if (tmap.size() > 1) {
                        tmap.remove(entry.id);
                    } else {
                        descriptionMap.remove(desc);
                    }
                }
            }
            itemMap.remove(id);
            return sum;
        }
    }


    /*
       d. FindMinPrice(n): given a long int, find items whose description
       contains that number (exact match with one of the long ints in the
       item's description), and return lowest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMinPrice(long n) {
        if(descriptionMap.containsKey(n)){
            return descriptionMap.get(n).firstEntry().getValue();
        }else
            return new Money("0.0");
    }

    /*
       e. FindMaxPrice(n): given a long int, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMaxPrice(long n) {
        if(descriptionMap.containsKey(n)){
            return itemMap.get(descriptionMap.get(n).lastKey()).price;
        }else
            return new Money("0.0");
    }

    /*
       f. FindPriceRange(n,low,high): given a long int n, find the number
       of items whose description contains n, and in addition,
       their prices fall within the given range, [low, high].
    */
    public int findPriceRange(long n, Money low, Money high) {
        if(low.compareTo(high) >0 )
            return 0;
        TreeMap<Long,Money> tmap = descriptionMap.get(n);
        int count = 0;

        for(Map.Entry<Long,Money> entry : tmap.entrySet()){

            if(entry.getValue().compareTo(low) >= 0 && entry.getValue().compareTo(high) <= 0)
                count++;
        }

        return count;
    }

    /*
       g. PriceHike(l,h,r): increase the price of every product, whose id is
       in the range [l,h] by r%.  Discard any fractional pennies in the new
       prices of items.  Returns the sum of the net increases of the prices.
    */
    public Money priceHike(long l, long h, double rate) {
        long netIncrease = 0;
        for (Map.Entry<Long, Entry> item : itemMap.entrySet()) {
            Long id = item.getKey();
            Entry entry = item.getValue();
            if(id >= l && id <= h){
                long price = entry.price.d * 100 + entry.price.c;
                long increase = (long) (price * rate / 100);
                price += increase;
                int cents = (int) price % 100;
                long dollar = price / 100;

                netIncrease+= increase;
            //update the price in all description values in this for loop
                for(Long desc:entry.description){
                    if(descriptionMap.containsKey(desc)){
                        if(descriptionMap.get(desc).containsKey(id)){
                            descriptionMap.get(desc).remove(id);
                            descriptionMap.get(desc).put(id,new Money(dollar,cents));
                        }
                    }
                }
                entry.price = new Money(dollar,cents);
                itemMap.put(id,entry);
            }
        }
        int cents = (int) netIncrease % 100;
        long dollar = netIncrease / 100;
        return new Money(dollar,cents);
    }

    /*
      h. RemoveNames(id, list): Remove elements of list from the description of id.
      It is possible that some of the items in the list are not in the
      id's description.  Return the sum of the numbers that are actually
      deleted from the description of id.  Return 0 if there is no such id.
    */
    public long removeNames(long id, java.util.List<Long> list) throws IllegalAccessException {
        Entry entry = itemMap.get(id);
        long sum = 0;
        for(Long desc : list){
            if(descriptionMap.containsKey(desc)){
                if(descriptionMap.get(desc).containsKey(id)){
                    descriptionMap.get(desc).remove(id);
                    sum += desc;
                }
            }
        }
        entry.description.removeAll(list);
        return sum;
    }

    // Do not modify the Money class in a way that breaks LP3Driver.java
    public static class Money implements Comparable<Money> {
        long d;  int c;
        public Money() { d = 0; c = 0; }
        public Money(long d, int c) { this.d = d; this.c = c; }
        public Money(String s) {
            String[] part = s.split("\\.");
            int len = part.length;
            if(len < 1) { d = 0; c = 0; }
            else if(part.length == 1) { d = Long.parseLong(s);  c = 0; }
            else { d = Long.parseLong(part[0]);  c = Integer.parseInt(part[1]); }
        }
        public long dollars() { return d; }
        public int cents() { return c; }

        public int compareTo(Money other) { // Complete this, if needed
            int compare = Long.compare(this.d,other.d);
            if( compare != 0){
                return compare;
            } else{
                return Integer.compare(this.c,other.c);
            }
        }
        public String toString() { return d + "." + c; }
    }

}