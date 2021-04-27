/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.*;
import java.io.*;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }


    public String getDescription() {
        return "This is a test description";
    }

    public String getContent() {
        return "This is test content";
    }
}
