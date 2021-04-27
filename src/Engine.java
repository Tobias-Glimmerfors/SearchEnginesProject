/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

import java.util.*;

import java.io.*;

/**
 *  This is the main class for the search engine.
 */
public class Engine {

    /** The searcher used to search the index. */
    Searcher searcher;

    /** The engine GUI. */
    SearchGUI gui;

    /** Lock to prevent simultaneous access to the index. */
    Object indexLock = new Object();

    /** The file containing the logo. */
    String pic_file = "../ir20.png";

    /* ----------------------------------------------- */


    /**
     *   Constructor.
     *   Indexes all chosen directories and files
     */
    public Engine( String[] args ) {
        searcher = new Searcher();
        gui = new SearchGUI( this );
        gui.init();
        gui.displayInfoText("Pls do stuff as you like");
    }

    /* ----------------------------------------------- */


    public static void main( String[] args ) {
        Engine e = new Engine( args );
    }

}
