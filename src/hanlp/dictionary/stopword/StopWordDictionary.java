
package hanlp.dictionary.stopword;

import hanlp.collection.MDAG.MDAGSet;
import hanlp.dictionary.common.CommonDictionary;
import hanlp.seg.common.Term;

import java.io.*;
import java.util.Collection;

import static hanlp.utility.Predefine.logger;

/**
 * @author hankcs
 */
public class StopWordDictionary extends MDAGSet implements Filter
{
    public StopWordDictionary(File file) throws IOException
    {
        super(file);
    }

    public StopWordDictionary(Collection<String> strCollection)
    {
        super(strCollection);
    }

    public StopWordDictionary()
    {
    }

    @Override
    public boolean shouldInclude(Term term)
    {
        return contains(term.word);
    }
}
