package com.example.android.contactslist.language;

import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.StringTokenizer;

/**
 * Created by Tyson Macdonald on 9/17/2014.
 *
 * This is the bottleneck of the SMS database processing
 */
public class LanguageAnalysis {

    private String mInputString;
    private Boolean mPreserveString;

    public final Boolean PRESERVE_STRING = true;
    public final Boolean CONSUME_STRING = false;

    private StringTokenizer stringTokenizer;

    int count = 0;

    String[] smileys = {":)",":D",":-)",":-D",";)",";-)"
            ,"(:","(-:","^_^","(^_-)","(-_^)", ":-P", ":P", ":-p", ":p"};

    String[] hearts = {"<3","<kiss>","<muah>","love you","hugs","Hugs",":-*",":*","Kiss", "kiss", "XOXO", "xoxo"};

    String[] first_person = {"I ","i "," me ","We "," we ", "I'll", "I've", "we'll", "we've", "We'll", "We've", "I'm", "mine", "Mine"};

    String[] second_person = {"You","you"};

    String[] question_marks = {"?"};



    public LanguageAnalysis(){
    }

    public void setString(String str, Boolean preserveString){
        this.mInputString = str;
        this.mPreserveString = preserveString;

        stringTokenizer = new StringTokenizer(mInputString);
    }


    // the tokenizing is actually done long before any of the destructive method calls
    public int countWordsInString(){
        return stringTokenizer.countTokens();
    }


    /*
    Method ot count the number of smileys in a string
    */
    public int countSmileysInString(){


        count = 0;

        for(String sub: smileys){
            count += countSubstring(sub, mInputString);
        }

        return count;
    }

    /*
Method ot count the number of Kisses, hugs, and hearts in a string
 */
    public int countHeartsInString(){

        count = 0;

        for(String sub: hearts){
            count += countSubstring(sub, mInputString);
        }

        return count;
    }

    /*
Method ot count the number of questionmarks in a string
*/
    public int countQuestionsInString(){

        count = 0;

        for(String sub: question_marks){
            count += countSubstring(sub, mInputString);
        }

        return count;
    }

    /*
Method ot count the number of First Person Pronouns in a string
*/
    public int countFirstPersonPronounsInString(){

        count = 0;

        for(String sub: first_person){
            count += countSubstring(sub, mInputString);
        }

        return count;
    }

    /*
Method ot count the number of Second Person Pronouns in a string
*/
    public int countSecondPersonPronounsInString(){

        int count = 0;

        for(String sub: second_person){
            count += countSubstring(sub, mInputString);
        }

        return count;
    }

    /*
    Method to count the number of unique substrings in a string
    http://rosettacode.org/wiki/Count_occurrences_of_a_substring#Java
     */
    private int countSubstring(String subStr, String str){
        if(mPreserveString){
            return (int)((float)(str.length() - str.replace(subStr, "").length()) / (float)subStr.length());
        }else {
            return (int)((float)(mInputString.length() - mInputString.replace(subStr, "").length()) / (float)subStr.length());
        }
    }
}
