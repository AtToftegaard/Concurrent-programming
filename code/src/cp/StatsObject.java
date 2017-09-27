/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Andreas
 */
public class StatsObject implements Stats {
    
    public Path path = Paths.get("C:\\Users\\Andreas\\Desktop\\Testerfolder\\Testerfolder");    // The foundIn method takes the path, but I was unable to deliver it from the other classes.
    public List<String> occurrencelist = OccurrencesToClass(WordFinder.occurrences);            // I instantiated the object method-variables into the methods, so that the object would be ready
    public List<List> listFoundin= FoundInToClass(WordFinder.occurrences);                      // after creation. 
    public String MostFrequent = mostFrequent(WordFinder.occurrences);
    public String LeastFrequent = leastFrequent(WordFinder.occurrences);
    public List<String> WordsList = words(WordFinder.occurrences);
    public List<String> WordsByOccurrence = wordsByOccurrences(WordFinder.occurrences);
    
    public List<String> OccurrencesToClass( Map occurrences ){                                  // To call the occurrences method on all words in the hashmap, I had to create this method. 
       List<String> list = new ArrayList();
       Set<Entry> entries = occurrences.entrySet();
       for (Entry entry : entries){
           String key = (String) entry.getKey();
           list.add(key+" -> "+occurrences(key,occurrences));
       }
       return list;
    };
    public int occurrences(String word, Map occurrences) {                                      // Returns the number of occurrences linked to a given word in the hashmap.
        int occurs = 0; 
        if ( occurrences.containsKey(word)){
            occurs =  (int) occurrences.get(word);
        }
        return occurs;
    }
    public List<List> FoundInToClass(Map occurrences){                                          // Same as above, I needed this method to use findAll on all words. 
        List<List> list = new ArrayList();
        Set<Entry> entries = occurrences.entrySet();
            for (Entry entry : entries){
                 String key = (String) entry.getKey();
                 list.add(foundIn(key,path));
            }
        return list;
    };
    public List<Result> foundIn(String word, Path path) {                                       // Calls findAll with a given word. 
        List<Result> rs = new LinkedList();
        try {
           rs = WordFinder.findAll(word, path);
        } catch (IOException ex) {
            Logger.getLogger(ResultObject.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ResultObject.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public String mostFrequent( Map occurrences ) {                                             // Finds the largest occurrence value, and searches the hashmap for the corresponding key. 
        int maxValue = (int) Collections.max(occurrences.values());
        Set<Entry> entries = occurrences.entrySet();
        String maxKey = null; 
            for(Entry entry : entries) {
                if ((int) entry.getValue()==maxValue){
                    maxKey = (String) entry.getKey();
                    this.MostFrequent=maxKey;
                    break;
                }
            }
        return maxKey;
    }
    
    public String leastFrequent( Map occurrences ) {                                             // Finds the smallest occurrence value, and searches the hashmap for the corresponding key. 
        int minValue = (int) Collections.min(occurrences.values());
        Set<Entry> entries = occurrences.entrySet();
        String minKey = null; 
            for(Entry entry : entries) {
                if ((int) entry.getValue()==minValue){
                minKey = (String) entry.getKey();
                this.LeastFrequent=minKey;
                break;
                }
            }
        return minKey;
    }

    public List<String> words( Map occurrences ) {                                              // Lists the keys, for all entries in the hashmap, corresponding to all words found. 
        List<String> list = new ArrayList();
        Set<Entry> entries = occurrences.entrySet();
        for(Entry entry : entries) {
           list.add((String) entry.getKey());
    }
        return list;
    }
    
    public List<String> wordsByOccurrences( Map occurrences ) {                                 // Sorts the entries, adds them to new list, and returns it. 
        List<String> list = new LinkedList();
        Set<Entry> entries = sortByValue(occurrences).entrySet();
        for ( Entry entry : entries ){
            list.add(entry.getKey().toString());
        }
        return list;
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )   // The actual sorting-method. Opens an entryset-stream, compares by value and adds to the linkedhashmap.
    {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();

        st.sorted( Map.Entry.comparingByValue() ).forEachOrdered( e -> 
                                                                result.put(e.getKey(), e.getValue()) );
    return result;
    }
}
