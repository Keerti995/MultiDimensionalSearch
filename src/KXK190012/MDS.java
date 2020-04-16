package KXK190012;

import java.util.*;

/**
 * MDS class stores the Item information and its behavior like price hike, removal and addition of Items etc.
 */
public class MDS {
    HashMap<Long, MDSEntry> itemTable;                      //stores the Item ID - key , Item info - value
    HashMap<Long, TreeMap<Long,Money>> descTable;           //stores the Item ID - key , Map(Item ID-key , Item Price-value)

    /**
     * MDSEntry class stores the Item information like Item ID, Description, Price
     */
    private class MDSEntry{
        long id;
        ArrayList<Long> description;
        Money price;

        public MDSEntry(long id, ArrayList<Long> description,Money price){
            this.id = id;
            this.description = description;
            this.price = price;

        }

    }

    /**
     * PriceComparator class compares the prices of 2 Items and returns 1 if price of Item o1>o2, -1 if price of Item o1<o2, 0 if both have equal prices
     */
    class PriceComparator implements Comparator<MDSEntry> {

        @Override
        public int compare(MDSEntry o1, MDSEntry o2) {
            if(o1.price.compareTo(o2.price) == 0)
                return -1;
            return o1.price.compareTo(o2.price);
        }

    }

    /**
     * IDComparator compares the IDs of 2 items
     */
    class IDComparator implements Comparator<MDSEntry>{

        @Override
        public int compare(MDSEntry o1, MDSEntry o2) {
            return Long.compare(o1.id,o2.id);
        }

    }

    /**
     * Orders the Items in the Map using prices of the Items stored in the key
     */
    class byValue implements Comparator<Long> {
        @Override
        public int compare(Long e1, Long e2) {
            if (itemTable.get(e1).price.compareTo(itemTable.get(e2).price) > 0){
                return 1;
            } else if (itemTable.get(e1).price.compareTo(itemTable.get(e2).price) < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    // Constructors
    public MDS() {
        this.itemTable = new HashMap<>();
        this.descTable = new HashMap<>();
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
        ArrayList<Long> localList = new ArrayList<>(list);
        MDSEntry newEntry = new MDSEntry(id,localList,price);
        if(itemTable.containsKey(id)){
            if(list.size() == 0){
                localList = itemTable.get(id).description;
            }
            delete(id);
            insert(id,price,localList);
            return 0;
        }else{
            itemTable.put(id,newEntry);
            for(Long desc : localList){
                //LinkedList<Long> set = descTable.get(desc);
                TreeMap<Long,Money> tmap = descTable.get(desc);
                if(tmap == null){
                    byValue cmp = new byValue();
                    TreeMap<Long,Money> newtmap = new TreeMap<Long,Money>(cmp);
                    newtmap.put(id,price);
                    descTable.put(desc, newtmap);
                }else{
                    tmap.put(id,price);
                }
            }
            return 1;
        }
    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public Money find(long id) {
        MDSEntry entry = itemTable.get(id);
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
        MDSEntry entry = itemTable.get(id);
        if(entry ==  null){
            return 0;
        }else{
            long sum = 0;
            for(Long desc : entry.description){
                TreeMap<Long,Money> tmap = descTable.get(desc);
                if(tmap!=null){
                    sum += desc;
                    if (tmap.size() > 1) {
                        tmap.remove(entry.id);
                    } else {
                        descTable.remove(desc);
                    }
                }
            }
            itemTable.remove(id);
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
        if(descTable.containsKey(n)){
            return descTable.get(n).firstEntry().getValue();
        }else
            return new Money("0.0");
    }

    /*
       e. FindMaxPrice(n): given a long int, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMaxPrice(long n) {
        if(descTable.containsKey(n)){
            return descTable.get(n).lastEntry().getValue();
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
        TreeMap<Long,Money> tmap = descTable.get(n);
        int count = 0;
        for(Money price : tmap.values()){
            if(price.compareTo(low) >=0 && price.compareTo(high) <=0 ){
                count++;
            }
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
        for (Map.Entry<Long, MDSEntry> item : itemTable.entrySet()) {
            Long id = item.getKey();
            MDSEntry entry = item.getValue();
            if(id >= l && id <= h){
                long price = entry.price.d * 100 + entry.price.c;
                long increase = (long) (price * rate / 100);
                price += increase;
                int cents = (int) price % 100;
                long dollar = price / 100;
                entry.price = new Money(dollar,cents);
                netIncrease+= increase;

                for(Long desc:entry.description){
                    if(descTable.containsKey(desc)){
                        if(descTable.get(desc).containsKey(id)){
                            descTable.get(desc).put(id,entry.price);
                        }
                    }
                } //updated the price in all description values in this for loop
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
        MDSEntry entry = itemTable.get(id);
        long sum = 0;
        for(Long desc : list){
            if(descTable.containsKey(desc)){
                if(descTable.get(desc).containsKey(id)){
                    descTable.get(desc).remove(id);
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

    public static void main(String[] args) throws IllegalAccessException {
        MDS mds = new MDS();
        LinkedList<Long> list = new LinkedList<>();
        list.add(0L);
        list.add(1L);
//        for(int i = 1 ; i < 11; i++){
//            System.out.println(i+" "+mds.insert(i,new Money(i,0),list));
//        }
//        System.out.println("11 "+mds.insert(1,new Money(12,0),list));
////        TreeMap<Long,Money> tmap = mds.descTable.get(0L);
////        System.out.println(tmap.firstEntry().getValue());
////        System.out.println(tmap.lastEntry().getValue());
////        System.out.println(mds.delete(1));
////        System.out.println(mds.find(1));
//        System.out.println(mds.findMinPrice(0));
//        System.out.println(mds.findMaxPrice(1));
//        System.out.println(mds.priceHike(1,2,10));
//        System.out.println(mds.findPriceRange(0,new Money("1.10"),new Money("2.20")));
//        System.out.println(mds.removeNames(1,list));

//        Money m1 = new Money("10000.10");
//        Money m2 = new Money("2.80");
//        System.out.println(m1.compareTo(m2));
    }

}
