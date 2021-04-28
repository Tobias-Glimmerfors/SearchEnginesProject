/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 *   Modifications: Johan Boye, 2016
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.border.*;
import java.util.*;


/**
 *   A graphical interface to the information retrieval system.
 */
public class SearchGUI extends JFrame {

    /**  The search engine. */
    Engine engine;

    /**  The query posed by the user. */
    private Query query;

    /**  The results of a search query. */
    private PostingsList results;

    /**  Max number of results to display. */
    static final int MAX_RESULTS = 10;

    /** Demarkator between file name and file contents in the file contents text area*/
    private static final String MARKER = "----------------------------------------------------";


    /*
     *   Common GUI resources
     */
    public JCheckBox[] box = null;
    public JPanel resultWindow = new JPanel();
    private JScrollPane resultPane = new JScrollPane( resultWindow );
    public JTextField queryWindow = new JTextField( "", 28 );
    // public JTextArea docTextView = new JTextArea( "", 15, 28 );
    public JTextPane docTextView = new JTextPane();
    private JScrollPane docViewPane = new JScrollPane( docTextView );
    
    private Font queryFont = new Font( "Arial", Font.BOLD, 24 );
    private Font resultFont = new Font( "Arial", Font.BOLD, 16 );
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu( "File" );
    JMenu optionsMenu = new JMenu( "Options" );
    JMenu structureMenu = new JMenu( "Text structure" );
    JMenuItem saveItem = new JMenuItem( "Save index and exit" );
    JMenuItem quitItem = new JMenuItem( "Quit" );
    JRadioButtonMenuItem searchItem = new JRadioButtonMenuItem( "Search" );
    JRadioButtonMenuItem enterLikeItem = new JRadioButtonMenuItem( "Enter things you like" );
    JRadioButtonMenuItem enterDislikeItem = new JRadioButtonMenuItem( "Enter things you dislike" );
    ButtonGroup queries = new ButtonGroup();

    private HashMap<Integer, PostingsEntry> resultLookup; // used for displaying text after clicking a result
    private Option currOption = Option.SEARCH;
    /**
     *  Constructor
     */
    public SearchGUI( Engine e ) {
        engine = e;
        resultLookup = new HashMap<>();
    }

    ArrayList<String> thingsToRemove(Iterator<String> it, JCheckBox[] box) {
        ArrayList<String> retvalue = new ArrayList<>();
        int i = 0;
        while(it.hasNext()) {
            String curr = it.next();
            if (box != null && i < box.length && box[i].isSelected()) {
                retvalue.add(curr);
            }
            i++;
        }
        return retvalue;
    }

    /**
     *  Sets up the GUI and initializes
     */
    void init() {
        // Create the GUI
        setSize( 600, 650 );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        resultWindow.setLayout(new BoxLayout(resultWindow, BoxLayout.Y_AXIS));
        resultPane.setLayout(new ScrollPaneLayout());
        resultPane.setBorder( new EmptyBorder(10,10,10,0) );
        resultPane.setPreferredSize( new Dimension(400, 450 ));
        getContentPane().add(p, BorderLayout.CENTER);
        // Top menus
        menuBar.add( fileMenu );
        menuBar.add( optionsMenu );
        fileMenu.add( quitItem );
        optionsMenu.add( searchItem );
        optionsMenu.add( enterLikeItem );
        optionsMenu.add( enterDislikeItem );
        searchItem.setSelected( true );
        p.add( menuBar );
        // Logo
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add( new JLabel( new ImageIcon( engine.pic_file )));
        p.add( p1 );
        // Search box
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add( queryWindow );
        queryWindow.setFont( queryFont );
        p.add( p3 );
        p.add( resultPane );

        docViewPane.setPreferredSize(new Dimension(400, 200));
        docTextView.setContentType("text/html");
        docTextView.setFont(resultFont);
        docTextView.setText("\n  The contents of the document will appear here.");
        // docTextView.setLineWrap(true);
        // docTextView.setWrapStyleWord(true);
        p.add(docViewPane);
        setVisible( true );

        /*
         *  Searches for documents matching the string in the search box, and displays
         *  the first few results.
         */
        Action search = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                String queryString = queryWindow.getText().toLowerCase().trim();

                if (currOption == Option.SEARCH) {
                    // Empty the results window
                    displayInfoText( " " );
                    // Turn the search string into a Query
                    query = new Query( queryString );
                    // Take relevance feedback from the user into account (assignment 3)
                    // Check which documents the user has marked as relevant.
                    PostingsList relevantPostings = new PostingsList();
                    if ( box != null ) {
                        for ( int i=0; i<box.length; i++ ) {
                            if ( box[i] != null && box[i].isSelected() ) {
                                relevantPostings.add(resultLookup.get(i));
                            }
                        }
                    }
                    // Search and print results. Access to the index is synchronized since
                    // we don't want to search at the same time we're indexing new files
                    // (this might corrupt the index).
                    long startTime = System.currentTimeMillis();
                    synchronized ( engine.indexLock ) {
                        if (relevantPostings.size() == 0) {
                            results = engine.searcher.search( queryString );
                        }
                        else {
                            System.out.println("doing a search with relevance feedback");
                            results = engine.searcher.relevanceSearch( queryString, relevantPostings );
                        }
                    }
                    System.out.println("size of results = " + results.size());
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    // Display the first few results + a button to see all results.
                    //
                    // We don't want to show all results directly since the displaying itself
                    // might take a long time, if there are many results.
                    if ( results != null ) {
                        displayResults( MAX_RESULTS, elapsedTime/1000.0 );
                    } else {
                        displayInfoText( "Found 0 matching document(s)" );
                    }
                }
                else if (currOption == Option.LIKE) {
                    for(String s : thingsToRemove(engine.profile.favorsIterator(), box)) {
                        engine.profile.removeFavor(s);
                    }
                    if (queryString.length() != 0) {
                        engine.profile.addFavor(queryString);
                    }
                    displayListOfWords(MAX_RESULTS, engine.profile.favorsIterator(), engine.profile.numberOfFavors());
                }
                else if (currOption == Option.DISLIKE) {
                    for(String s : thingsToRemove(engine.profile.disfavorsIterator(), box)) {
                        engine.profile.removeDisfavor(s);
                    }
                    if (queryString.length() != 0) {
                        engine.profile.addDisfavor(queryString);
                    }
                    displayListOfWords(MAX_RESULTS, engine.profile.disfavorsIterator(), engine.profile.numberOfDisfavors());
                }
            }
            };

        // A search is carried out when the user presses "return" in the search box.
        queryWindow.registerKeyboardAction( search,
                            "",
                            KeyStroke.getKeyStroke( "ENTER" ),
                            JComponent.WHEN_FOCUSED );

        Action quit = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                System.exit( 0 );
            }
            };
        quitItem.addActionListener( quit );

        Action setSearch = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
              System.out.println("Now in search mode");
                currOption = Option.SEARCH;
                searchItem.setSelected( true );
                enterLikeItem.setSelected( false );
                enterDislikeItem.setSelected( false );
                displayInfoText("Waiting for a search");
            }
            };
        searchItem.addActionListener( setSearch );

        Action setLike = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                System.out.println("Now in LIKE mode");
                currOption = Option.LIKE;
                searchItem.setSelected( false );
                enterLikeItem.setSelected( true );
                enterDislikeItem.setSelected( false );
                displayListOfWords(MAX_RESULTS, engine.profile.favorsIterator(), engine.profile.numberOfFavors());
            }
            };
        enterLikeItem.addActionListener( setLike );

        Action setDislike = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                System.out.println("Now in DISLIKE mode");
                currOption = Option.DISLIKE;
                searchItem.setSelected( false );
                enterLikeItem.setSelected( false );
                enterDislikeItem.setSelected( true );
                displayListOfWords(MAX_RESULTS, engine.profile.disfavorsIterator(), engine.profile.numberOfDisfavors());
            }
            };
        enterDislikeItem.addActionListener( setDislike );
    }


    /* ----------------------------------------------- */

    /**
     *  Clears the results window and writes an info text in it.
     */
    void displayInfoText( String info ) {
        resultWindow.removeAll();
        JLabel label = new JLabel( info );
        label.setFont( resultFont );
        resultWindow.add( label );
        revalidate();
        repaint();
    }

    /**
     *  Displays the results in the results window.
     *  @param maxResultsToDisplay The results list is cut off after this many results
     *      have been displayed.
     *  @param elapsedTime Shows how long time it took to compute the results.
     */
    void displayResults( int maxResultsToDisplay, double elapsedTime ) {
        displayInfoText( String.format( "Found %d matching document(s) in %.3f seconds", results.numResults(), elapsedTime ));
        box = new JCheckBox[maxResultsToDisplay];
        int i;
        for ( i=0; i<results.numResults() && i<maxResultsToDisplay; i++ ) {
            if (i == results.size()) {
                // trying to display more entries than have been fetched
                engine.searcher.getMoreResults(results);
            }

            resultLookup.put(i, results.get(i));
            String description = i + ". " + results.get(i).getDescription();
            description += "   " + String.format( "%.5f", results.get(i).getScore() );
            box[i] = new JCheckBox();
            box[i].setSelected( false );

            JPanel result = new JPanel();
            result.setAlignmentX(Component.LEFT_ALIGNMENT);
            result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));

            JLabel label = new JLabel(description);
            label.setFont( resultFont );

            MouseAdapter showDocument = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int orderIndex = Integer.parseInt(((JLabel)e.getSource()).getText().split("\\.")[0]);
                    PostingsEntry resultToShow = resultLookup.get(orderIndex);
                    String content = "Displaying contents of " + resultToShow.getDescription() + "\n" + MARKER + "\n";
                    content += resultToShow.getContent();

                    docTextView.setText(content);
                    docTextView.setCaretPosition(0);
                }
            };
            label.addMouseListener(showDocument);
            result.add(box[i]);
            result.add(label);

            resultWindow.add( result );
        }
        // If there were many results, give the user an option to see all of them.
        if ( i<results.numResults() ) {
            JPanel actionButtons = new JPanel();
            actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.X_AXIS));
            actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton display10MoreBut = new JButton( "Display 10 more results" );
            display10MoreBut.setFont( resultFont );
            actionButtons.add( display10MoreBut );
            Action display10More = new AbstractAction() {
                public void actionPerformed( ActionEvent e ) {
                    displayResults( (int)this.getValue("resCurSize") + 10, elapsedTime );
                }
            };
            display10More.putValue("resCurSize", i);
            display10MoreBut.addActionListener( display10More );

            actionButtons.add(Box.createRigidArea(new Dimension(5,0)));

            JButton displayAllBut = new JButton( "Display " + results.size() + " results" );
            displayAllBut.setFont( resultFont );
            actionButtons.add( displayAllBut );
            Action displayAll = new AbstractAction() {
                public void actionPerformed( ActionEvent e ) {
                    displayResults( results.size(), elapsedTime );
                }
            };
            displayAllBut.addActionListener( displayAll );

            resultWindow.add(actionButtons);
        }
        revalidate();
        repaint();
    };


    void displayListOfWords( int maxResultsToDisplay, Iterator<String> words, int numberOfWords) {
        displayInfoText("Here is a list of your preferences");
        box = new JCheckBox[maxResultsToDisplay];
        int i;
        for ( i=0; i < numberOfWords && i<maxResultsToDisplay; i++ ) {
            box[i] = new JCheckBox();
            box[i].setSelected( false );

            JPanel result = new JPanel();
            result.setAlignmentX(Component.LEFT_ALIGNMENT);
            result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));

            JLabel label = new JLabel(words.next());
            label.setFont( resultFont );

            result.add(box[i]);
            result.add(label);

            resultWindow.add( result );
        }
        if ( i < numberOfWords ) {
            JPanel actionButtons = new JPanel();
            actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.X_AXIS));
            actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton display10MoreBut = new JButton( "Display 10 more results" );
            display10MoreBut.setFont( resultFont );
            actionButtons.add( display10MoreBut );
            Action display10More = new AbstractAction() {
                public void actionPerformed( ActionEvent e ) {
                    displayListOfWords( (int)this.getValue("resCurSize") + 10, words, numberOfWords );
                }
            };
            display10More.putValue("resCurSize", i);
            display10MoreBut.addActionListener( display10More );

            actionButtons.add(Box.createRigidArea(new Dimension(5,0)));

            JButton displayAllBut = new JButton( "Display all " + numberOfWords + " results" );
            displayAllBut.setFont( resultFont );
            actionButtons.add( displayAllBut );
            Action displayAll = new AbstractAction() {
                public void actionPerformed( ActionEvent e ) {
                    displayListOfWords( numberOfWords, words, numberOfWords );
                }
            };
            displayAllBut.addActionListener( displayAll );

            resultWindow.add(actionButtons);
        }
        revalidate();
        repaint();
    };
}
