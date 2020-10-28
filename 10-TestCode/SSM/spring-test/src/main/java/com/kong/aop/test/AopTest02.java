package com.kong.aop.test;

public class AopTest02 {
    public static void main(String[] args) {
        /*String str1 = "abc";
        String str2 = new String("abc");
        System.out.println(str1 == str2);//false
        String str3 = new String("abc");
        System.out.println(str3 == str2);//false
        String str4 = "a" + "b";
        System.out.println(str4 == "ab");//true
        String s = "a";
        String str5 = s + "b";
        System.out.println(str5 == "ab"); //true
        String s1 = "a";
        String s2 = "b";
        String str6 = s1 + s2;
        System.out.println(str6 == "ab"); //false
        String str7 = "abc".substring(0,2);
        System.out.println(str7 == "ab");//false
        String str8 = "abc".toUpperCase();
        System.out.println(str8 == "ABC");//false
        String s3 = "ab";
        String s4 = "ab" + getString();
        System.out.println(s3 == s4);//false
        String s5 = "a";
        String s6 = "abc";
        String s7 = s5 + "bc";
        System.out.println(s6 == s7.intern()); //true*/

        System.out.println("+++++++++++++++++++++++++++++++");
        // 此时堆上有三块空间：new String("a") new String("b") new String("ab")
        // StringTable中有：["a"，“b”]
        String ss = new String("a") + new String("b");
        // 此时StringTable中还没有"ab"，所以会在常量池中创建一个"ab"
        // s变为指向常量池中的那个"ab"
        String ss2 = ss.intern();

        System.out.println(ss2 == "ab"); // true
        System.out.println(ss == "ab");  // true
    }
    private static String getString(){ return "c"; }
}
