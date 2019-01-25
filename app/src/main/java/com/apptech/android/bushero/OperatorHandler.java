package com.apptech.android.bushero;

import java.util.List;

public class OperatorHandler {
    private static final String[] OPERATOR_COLORS = {"ic_bus_purple", "ic_bus_red", "ic_bus_green", "ic_bus_blue", "ic_bus_yellow"};
    private static final String DEFAULT_OPERATOR_COLOR = "ic_bus_black";
    private final BusDatabase mBusDatabase;
    private List<OperatorColor> mOperatorColours;

    public OperatorHandler(BusDatabase busDatabase) {
        mBusDatabase = busDatabase;
        mOperatorColours = mBusDatabase.getOperatorColors();
    }

    public OperatorColor getColor(String operator) {
        // find existing color for operator
        for (OperatorColor color : mOperatorColours) {
            if (color.getName().equals(operator)) {
                return color;
            }
        }

        // create new color and add to DB
        String unusedColor = getUnusedColor();
        OperatorColor color = new OperatorColor();
        color.setName(operator);
        color.setColor(unusedColor);
        mBusDatabase.addOperatorColor(color);
        mOperatorColours.add(color);

        return color;
    }

    private String getUnusedColor() {
        // loop through operator colours looking for one that's unused.
        for (String color : OPERATOR_COLORS) {
            boolean found = false;

            for (OperatorColor operator : mOperatorColours) {
                if (operator.getColor().equals(color)) {
                    found = true;
                    break;
                }
            }

            // if not found then return that colour
            if (!found) {
                return color;
            }
        }

        // all colours used so return default.
        return DEFAULT_OPERATOR_COLOR;
    }
}
