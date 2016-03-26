package com.apptech.android.bushero;

public class TextHelper {
    public static String getDestination(String dest) {
        int start = dest.lastIndexOf("(");
        int end = dest.lastIndexOf(")");

        if (start > -1 && end > -1) {
            return dest.substring(start + 1, end);
        }

        return dest;
    }

    public static String capitalise(String direction) {
        char[] chars = direction.toCharArray();
        if (chars.length > 0) {
            chars[0] = Character.toUpperCase(chars[0]);
        }
        return new String(chars);
    }

    public static String getOperator(String operator) {
        switch (operator) {
            case "FGL":
                return "First Glasgow";
            default:
                return operator;
        }
    }

    public static String getBearing(String bearing) {
        switch (bearing) {
            case "N":
                return "North";
            case "S":
                return "South";
            case "E":
                return "East";
            case "W":
                return "West";
            case "NE":
                return "North East";
            case "SE":
                return "South East";
            case "NW":
                return "North West";
            case "SW":
                return "South West";
            default:
                return bearing;
        }
    }
}
