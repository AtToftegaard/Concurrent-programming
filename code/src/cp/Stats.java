package cp;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public interface Stats                                                          // I chose to implement the interfaces provided, and create objects based on that. 
{
	/**
	 * Returns the number of times a word was found.
	 * @param word the word
	 * @return the number of times the word was found
	 */
	public int occurrences( String word, Map occurrences );
	
	/**
	 * Returns the list of results in which a word was found.
	 * @param word the word
	 * @return the list of results in which the word was found
	 */
	public List< Result > foundIn( String word, Path path );
	
	/**
	 * Returns the word that was found the most times.
	 * @return the word that was found the most times
	 */
	public String mostFrequent(Map <String,Integer> occurrences);
	
	/**
	 * Returns the word that was found the least times.
	 * @return the word that was found the least times
	 */
	public String leastFrequent(Map <String,Integer> occurrences);
	
	/**
	 * Returns a list of all the words found.
	 * @return a list of all the words found
	 */
	public List< String > words( Map <String,Integer> occurrences );
	
	/**
	 * Returns a list of all the words found, ordered from the least frequently occurring (first of the list)
	 * to the most frequently occurring (last of the list).
	 * @return a list of all the words found, ordered from the least to the most frequently occurring
	 */
	public List< String > wordsByOccurrences( Map <String,Integer> occurrences);
       

}
