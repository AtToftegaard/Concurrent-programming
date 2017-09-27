package cp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class WordFinder
{
        private static final ExecutorService executor =                                     // I make use of an executorservice in distributing tasks.
		Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                 
	/**
	 * Finds all the (case-sensitive) occurrences of a word in a directory.
	 * Only text files should be considered (files ending with the .txt suffix).
	 * 
	 * The word must be an exact match: it is case-sensitive and may contain punctuation.
	 * See https://github.com/fmontesi/cp2016/tree/master/exam for more details.
	 * 
	 * The search is recursive: if the directory contains subdirectories,
	 * these are also searched and so on so forth (until there are no more
	 * subdirectories).
	 * 
	 * @param word the word to find (does not contain whitespaces or punctuation)
	 * @param dir the directory to search
	 * @return a list of results ({@link Result}), which tell where the word was found
	 */
	public static List< Result > findAll( String word, Path dir ) throws IOException, InterruptedException
	{
		List<Result> List = new ArrayList<>();                                      // The final list to return.
                BlockingQueue<File> Queue = new LinkedBlockingDeque<>();                    // The Queue the executors will take files to read from. 
                
                TxtToSearch(dir,Queue);                                                     // Method to search directories and add all .txt files to Queue.
                Queue.stream().forEach((file)-> { 
                    try {
                        SearchForWordInFile(file, word, Queue, List);                       // Method to search individual file for a given word.
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.DAYS);
                
                return List;
	}
        
        public static List< Result > SearchForWordInFile(File file, String word, BlockingQueue queue, List list) throws InterruptedException{
             try {
                BufferedReader Reader = Files.newBufferedReader(file.toPath());
                String line;
                int linenumber = 1;
                
                while((line = Reader.readLine())!= null){
                    final int Linenumberr = linenumber++;
                    String currentline = line;
                    executor.submit( 
                            () -> FindWordInLine(currentline,word,file,Linenumberr,list));      // Go through file and pass each line to a thread for reading. 
                }
            } catch (IOException ex) {
                Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            return list;
        }
        public static List< Result > FindWordInLine(String currentline, String word, File file,int linenumber, List list) {
            if (currentline.contains(" "+word+" ")){
                ResultObject resultobject = new ResultObject();
                resultobject.line=linenumber;
                resultobject.path=file.toPath();
                list.add(resultobject);                                                         // For a found word, change the attributes of the Result-object, and add it to the list. 
            }
        return list;
        }
        
        public static void TxtToSearch (Path dir, BlockingQueue<File> Queue) throws IOException{    // Adds all .txt files to the Queue. Called earlier. 
            List<Path> dirlist = new ArrayList<>(); 
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
                for (Iterator<Path> it = stream.iterator(); it.hasNext();) {
                    dirlist.add(dir.resolve(it.next()));
                }
            }
            try {
                for (Path path : dirlist){
                    if (path.toFile().isDirectory()){
                        TxtToSearch(path,Queue);
                    }
                    else if (path.toString().endsWith(".txt")){
                        Queue.add(path.toFile());
                    }
                } 
            } catch (IOException ex) {
                Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
     
        
	/**
	 * Finds an occurrence of a word in a directory and returns.
	 * 
	 * This method searches only for one (any) occurrence of the word in the
	 * directory. As soon as one such occurrence is found, the search can be
	 * stopped and the method can return immediately.
	 * 
	 * As for method {@code findAll}, the search is recursive.
	 * 
	 * @param word
	 * @param dir
	 * @return 
	 */
	public static Result findAny( String word, Path dir )
	{       
                ResultObject rs = new ResultObject();
                BlockingQueue<File> Queue = new LinkedBlockingDeque<>();
                
            try {
                TxtToSearch(dir,Queue);                                                             // Adds txt-files to Queue, same as findAll.
            } catch (IOException ex) {
                Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
                Queue.stream().forEach((file)-> { 
                    FindAnySearchForWordInFile(file, word, rs);                                     // Executor does the same as FindAll, but checks whether the service has already been shut down. 
                });                                                                                 // due to a word already being found. 
                return rs;
	}
        
        public static Result FindAnySearchForWordInFile(File file, String word, ResultObject rs){
           try {
                BufferedReader Reader = Files.newBufferedReader(file.toPath());
                String line;
                int linenumber = 1;
                
                while((line = Reader.readLine())!= null){                                           // Again, same as findAll, but discontinues if a word has already been found. 
                    final int Linenumberr = linenumber++;
                    String currentline = line;
                    if (executor.isShutdown()){
                        break;
                    }
                    executor.submit( 
                            () -> FindAnyFindWordInLine(currentline,word,file,Linenumberr,rs));
                }
            } catch (IOException ex) {
                Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
           return rs;
        }
        
        public static Result FindAnyFindWordInLine(String currentline, String word, File file, int Linenumberr, ResultObject rs ) {
            if ( currentline.contains(" "+word+" ")){
                rs.line=Linenumberr;                                                                // Does not add the result-object to a final list, but just changes attributes of 
                rs.path=file.toPath();                                                              // the object already created and shuts down the executor. 
                executor.shutdown();
            }
            return rs; 
        }
	
	/**
	 * Computes overall statistics about the occurrences of words in a directory.
	 * 
	 * This method recursively searches the directory for all words and returns
	 * a {@link Stats} object containing the statistics of interest. See the
	 * documentation of {@link Stats}.
	 * 
	 * @param dir the directory to search
	 * @return the statistics of occurring words in the directory
	 */
        public static final Map< String, Integer > occurrences = new ConcurrentHashMap<>();
	public static Stats stats( Path dir )
	{
                BlockingQueue<File> Queue = new LinkedBlockingDeque<>();                        // Same as earlier, add txt-files to the Queue.
                try {
                    TxtToSearch(dir,Queue);
                } catch (IOException ex) {
                    Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
                }
                Queue.stream().forEach((file)-> {
                              ScanAndAdd(file,occurrences);                                     // Method to scan all words into the ConcurrentHashmap
                });
                Stats stats = new StatsObject();                                                // From here, I have everything needed to create the final Stats-object.
                executor.shutdown();
                return stats; 
	}
        
        public static void ScanAndAdd(File file, Map<String, Integer> occurrences){
            try {
                BufferedReader Reader = Files.newBufferedReader(file.toPath());
                String line; 
                
                while ((line = Reader.readLine())!= null){                    
                    String currentline = line; 
                    executor.submit( 
                            () -> wordCount ( currentline, occurrences ));
                }
            } catch (IOException ex){
                Logger.getLogger(WordFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
       	public static void wordCount( String line, Map<String, Integer> occurrences )           // Computes all words in a line into the Hashmap.
	{
		String[] words = line.split( "\\s+" );
		for( String word : words ) {
                            occurrences.compute(word, (k,v) -> {
                                if ( v == null ) {
                                    return 1; 
                                } else {
                                    return v+1;
                                }
			} );
		}
	}
}
           
            
          

