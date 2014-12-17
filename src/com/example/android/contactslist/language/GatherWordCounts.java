package com.example.android.contactslist.language;

import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Comparator;

/**
 * Created by Tyson Macdonald on 10/16/2014.
 * Inspired by http://faculty.washington.edu/stepp/courses/2004autumn/tcss143/lectures/files/2004-11-10/WordCount.java
 *
 * I might just switch to the much more complete library at https://github.com/vcl-xx/cue.language
 */
public class GatherWordCounts {

    HashMap map = new HashMap();  // word --> # of occurrences
    static private final String regular_expression = "\\s+|(^['-]|['-]$|['-]\\W+|[^\\w'-]\\W*)";
    // http://stackoverflow.com/questions/13793192/java-regular-expression-split-keeping-contractions

    //TODO figure out why this class returns with a count of 'd'


    public void clear(){
        map.clear();
    }

    public void addEventList(ArrayList<EventInfo> log){

        if(log != null) {
            // cycle through the list of events and analyze the text body
            for (EventInfo eventInfo : log) {
                addStringToTally(eventInfo.eventNotes);
            }
        }
    }


    //get a list of the keys and values in descending value,
     // while removing any strings that might be contained in words_to_remove
    // Returns an ArrayList of Entries of Strings and Integers
    // reference: http://stackoverflow.com/questions/4258700/collections-sort-with-multiple-fields
    public ArrayList<Entry<String,Integer>> getWordList(String[] words_to_remove){

        //remove the list of words from the map, if not null
        if(words_to_remove !=null){
            for(String word:words_to_remove){
                if(map.containsKey(word)){
                    map.remove(word);
                }
            }
        }


        // place each Entry in the map in an element of an ArrayList
        ArrayList arraylist = new ArrayList(map.entrySet());

        //Sorts the given list according to the number of word occurances
        // in descending order.
        //Collections.sort(arraylist);
        Collections.sort(arraylist, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(final Entry record1, final Entry record2) {
                int c;
                //for descending order
                c = (Integer)record2.getValue() - (Integer)(record1.getValue());
                return c;
            }

        });


       // Collections.reverse(arraylist);  //change to descending order


        return arraylist;
    }

    private void addStringToTally(String inputText) {
        String[] temp = inputText.split(regular_expression);

        // send each token of the string to be tallied up
        for (String token : temp) {
            addTokenToTally(token.toLowerCase());
        }

    }

    private void addTokenToTally(String word) {

        //count each token with a hash map
        if(map.containsKey(word)) {
            // if we have already seen this word before,
            // increment its count by one
            Integer count = (Integer)map.get(word);
            map.put(word, new Integer(count.intValue() + 1));
        } else {
            // we haven't seen this word, so add it with count of 1
            map.put(word, new Integer(1));
        }
    }

}
