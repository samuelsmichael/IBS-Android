package com.inductivebiblestudyapp.util;

import java.util.Locale;
import java.util.TreeMap;

/**
 * http://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java
 */
public class RomanNumberal {

    private static final TreeMap<Integer, String> map = new TreeMap<Integer, String>();

     static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

     /** @return the number as an uppercase roman numberal. */
    public final static String toRoman(int number) {
        int floor =  map.floorKey(number);
        if ( number == floor ) {
            return map.get(number);
        }
        return map.get(floor) + toRoman(number-floor);
    }
    
    /** Same as calling {@link #toRoman(int)}.{@link String#toLowerCase() toLowerCase(Locale.US)} */
    public final static String toRomanLower(int number) {
        return toRoman(number).toLowerCase(Locale.US);
    }

}
