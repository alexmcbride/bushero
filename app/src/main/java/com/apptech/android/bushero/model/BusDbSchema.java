package com.apptech.android.bushero.model;

public class BusDbSchema {
    public static final String DB_FILE = "busHero.db";
    public static final int DB_VERSION = 1;

    public class NearestBusStopsTable {
        public static final String NAME = "NearestBusStops";

        public class Columns {
            public static final String ID = "id";
            public static final String MIN_LONGITUDE = "minLongitude";
            public static final String MIN_LATITUDE = "MinLatitude";
            public static final String MAX_LONGITUDE = "MaxLongitude";
            public static final String MAX_LATITUDE = "MaxLatitude";
            public static final String SEARCH_LONGITUDE = "SearchLongitude";
            public static final String SEARCH_LATITUDE = "SearchLatitude";
            public static final String PAGE = "Page";
            public static final String RETURNED_PER_PAGE = "ReturnedPerPage";
            public static final String TOTAL = "Total";
            public static final String REQUEST_TIME = "RequestTime";
        }
    }

    public class BusRouteTable {
        public static final String NAME = "BusRoute";

        public class Columns {
            public static final String ID = "id";
            public static final String BUS_ID = "busId";
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
            public static final String MODE = "mode";
            public static final String LINE = "line";
            public static final String DIRECTION = "direction";
            public static final String OPERATOR = "operator";
            public static final String TIME = "time";
            public static final String SOURCE = "source";
        }
    }
}
