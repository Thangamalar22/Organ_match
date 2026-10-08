package com.organmatch.util;

import com.organmatch.model.City;
import org.springframework.stereotype.Service;

/**
 * Utility service that provides travel / transport time between cities.
 * Uses a symmetric matrix based on realistic air transport and intra-city travel times.
 */
@Service
public class TravelTimeService {

    // Symmetric matrix representing transit time in hours between the 10 cities.
    // Order matches City enum ordinal:
    // 0: MUMBAI, 1: DELHI, 2: CHENNAI, 3: BENGALURU, 4: KOLKATA,
    // 5: HYDERABAD, 6: AHMEDABAD, 7: PUNE, 8: KOCHI, 9: JAIPUR
    private static final double[][] TRAVEL_TIME_MATRIX = {
        /* MUMBAI    */ {0.5, 3.5, 3.5, 3.0, 4.0, 3.0, 2.5, 2.5, 3.5, 3.0},
        /* DELHI     */ {3.5, 0.5, 4.5, 4.5, 3.5, 4.0, 3.0, 3.5, 5.0, 2.0},
        /* CHENNAI   */ {3.5, 4.5, 0.5, 2.0, 4.0, 3.0, 4.5, 3.5, 2.5, 4.5},
        /* BENGALURU */ {3.0, 4.5, 2.0, 0.5, 4.5, 2.5, 4.0, 3.0, 2.0, 4.5},
        /* KOLKATA   */ {4.0, 3.5, 4.0, 4.5, 0.5, 4.0, 4.5, 4.5, 5.0, 4.0},
        /* HYDERABAD */ {3.0, 4.0, 3.0, 2.5, 4.0, 0.5, 3.5, 3.0, 3.0, 3.5},
        /* AHMEDABAD */ {2.5, 3.0, 4.5, 4.0, 4.5, 3.5, 0.5, 3.0, 4.0, 2.5},
        /* PUNE      */ {2.5, 3.5, 3.5, 3.0, 4.5, 3.0, 3.0, 0.5, 3.5, 3.5},
        /* KOCHI     */ {3.5, 5.0, 2.5, 2.0, 5.0, 3.0, 4.0, 3.5, 0.5, 4.5},
        /* JAIPUR    */ {3.0, 2.0, 4.5, 4.5, 4.0, 3.5, 2.5, 3.5, 4.5, 0.5}
    };

    /**
     * Returns transport time in hours between city 'a' and city 'b'.
     * 
     * @param a Source city
     * @param b Destination city
     * @return Travel time in hours
     */
    public double hours(City a, City b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Cities must not be null");
        }
        return TRAVEL_TIME_MATRIX[a.ordinal()][b.ordinal()];
    }
}
