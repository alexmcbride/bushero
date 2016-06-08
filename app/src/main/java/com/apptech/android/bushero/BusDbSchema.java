package com.apptech.android.bushero;

/**
 * Class to define our DB schema.
 */
class BusDbSchema {
    public class NearestBusStopsTable {
        public static final String NAME = "NearestBusStops";

        public class Columns {
            public static final String ID = "id";
            public static final String MIN_LONGITUDE = "minLongitude";
            public static final String MIN_LATITUDE = "minLatitude";
            public static final String MAX_LONGITUDE = "maxLongitude";
            public static final String MAX_LATITUDE = "maxLatitude";
            public static final String SEARCH_LONGITUDE = "searchLongitude";
            public static final String SEARCH_LATITUDE = "searchLatitude";
            public static final String PAGE = "page";
            public static final String RETURNED_PER_PAGE = "returnedPerPage";
            public static final String TOTAL = "total";
            public static final String REQUEST_TIME = "requestTime";
        }
    }

    public class BusRouteTable {
        public static final String NAME = "BusRoute";

        public class Columns {
            public static final String ID = "id";
            public static final String BUS_ID = "busId";
            public static final String REQUEST_TIME = "requestTime";
            public static final String OPERATOR = "operator";
            public static final String LINE = "line";
            public static final String ORIGIN_ATCOCODE = "originAtcoCode";
        }
    }

    public class BusStopTable {
        public static final String NAME = "BusStop";

        public class Columns {
            public static final String ID = "id";
            public static final String NEAREST_BUS_STOPS_ID = "nearestBusStopsId";
            public static final String BUS_ROUTE_ID = "busRouteId"; // only set if part of bus route
            public static final String ATCOCODE = "atcoCode";
            public static final String SMSCODE = "smsCode";
            public static final String NAME = "name";
            public static final String MODE = "mode";
            public static final String BEARING = "bearing";
            public static final String LOCALITY = "locality";
            public static final String INDICATOR = "indicator";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
            public static final String DISTANCE = "distance";
            public static final String TIME = "time"; // only set if part of bus route
        }
    }

    public class BusTable {
        public static final String NAME = "Bus";

        public class Columns {
            public static final String ID = "id";
            public static final String BUS_STOP_ID = "busStopId";
            public static final String FAVOURITE_STOP_ID = "favouriteStopId";
            public static final String MODE = "mode";
            public static final String LINE = "line";
            public static final String DIRECTION = "direction";
            public static final String DESTINATION = "destination";
            public static final String OPERATOR = "operator";
            public static final String AIMED_DEPARTURE_TIME = "aimedDepartureTime";
            public static final String EXPECTED_DEPARTURE_TIME = "expectedDepartureTime";
            public static final String BEST_DEPARTURE_ESTIMATE = "bestDepartureEstimate";
            public static final String SOURCE = "source";
            public static final String DATE = "date";
            public static final String DEPARTURE_TIME = "departureTime";
            public static final String IS_OVERDUE = "isOverdue";
        }
    }

    public class FavouriteStopTable {
        public static final String NAME = "FavouriteStop";

        public class Columns {
            public static final String ID = "id";
            public static final String ATCOCODE = "atcoCode";
            public static final String NAME = "name";
            public static final String MODE = "mode";
            public static final String BEARING = "bearing";
            public static final String LOCALITY = "locality";
            public static final String INDICATOR = "indicator";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
        }
    }

    public class OperatorColorTable {
        public static final String NAME = "OperatorTable";

        public class Columns {
            public static final String NAME = "name";
            public static final String COLOR = "color";
        }
    }
}
