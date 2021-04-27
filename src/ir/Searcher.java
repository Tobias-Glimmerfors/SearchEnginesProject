/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {
    /** Constructor */
    public Searcher() {
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query ) {
        System.out.println("The search function still needs to be implemented");
        PostingsList retvalue = new PostingsList();
        for(int i = 0; i < 15; i++) {
            retvalue.add(new PostingsEntry());
        }
        return retvalue;
    }
}
