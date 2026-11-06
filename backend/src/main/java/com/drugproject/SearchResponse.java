package com.drugproject;

import java.util.List;

/**
 * What the search link sends back.
 * status = FOUND      -> "result" has the full answer
 * status = MULTIPLE   -> "matches" has a list, the user picks one
 * status = NOT_FOUND  -> nothing matched
 */
public record SearchResponse(String status, MedicineResult result, List<MatchInfo> matches) {

    /** One medicine in the "pick one" list */
    public record MatchInfo(int id, String name, String ingredients) {}
}
