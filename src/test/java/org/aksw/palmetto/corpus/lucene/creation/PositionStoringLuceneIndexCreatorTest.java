/**
 * This file is part of Palmetto.
 *
 * Palmetto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Palmetto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Palmetto.  If not, see <http://www.gnu.org/licenses/>.
 */
							(roeder@informatik.uni-leipzig.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aksw.palmetto.corpus.lucene.creation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.aksw.palmetto.Palmetto;
import org.aksw.palmetto.corpus.lucene.WindowSupportingLuceneCorpusAdapter;
import org.apache.lucene.index.CorruptIndexException;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;

public class PositionStoringLuceneIndexCreatorTest {

    public static IndexableDocument DOCUMENTS[] = { new IndexableDocument("This is a test document.", 5),
            new IndexableDocument("This is another test document.", 5),
            new IndexableDocument("This is the third.", 4) };
    public static int WINDOW_SIZE = 3;
    public static String TEST_WORDS[] = { "is", "document", "dog" };
    // expected positions of the words inside the documents (negativ number means that the word is not inside the
    // document
    public static int EXPECTED_WORD_POSITIONS[][] = new int[][] { { 1, 4, -1 }, { 1, 4, -1 }, { 1, -1, -1 } };

    @Test
    public void test() throws CorruptIndexException, IOException {
        File indexDir = createTempDirectory();
        Iterator<IndexableDocument> docIterator = Arrays.asList(DOCUMENTS).iterator();
        // create the index
        PositionStoringLuceneIndexCreator creator = new PositionStoringLuceneIndexCreator(
                Palmetto.DEFAULT_TEXT_INDEX_FIELD_NAME, Palmetto.DEFAULT_DOCUMENT_LENGTH_INDEX_FIELD_NAME);
        Assert.assertTrue(creator.createIndex(indexDir, docIterator));
        LuceneIndexHistogramCreator hCreator = new LuceneIndexHistogramCreator(
                Palmetto.DEFAULT_DOCUMENT_LENGTH_INDEX_FIELD_NAME);
        hCreator.createLuceneIndexHistogram(indexDir.getAbsolutePath());

        // test the created index
        // create an adapter
        WindowSupportingLuceneCorpusAdapter adapter = WindowSupportingLuceneCorpusAdapter.create(
                indexDir.getAbsolutePath(), Palmetto.DEFAULT_TEXT_INDEX_FIELD_NAME,
                Palmetto.DEFAULT_DOCUMENT_LENGTH_INDEX_FIELD_NAME);
        // query the test words
        IntIntOpenHashMap docLengths = new IntIntOpenHashMap();
        IntObjectOpenHashMap<IntArrayList[]> wordPositions = adapter.requestWordPositionsInDocuments(TEST_WORDS,
                docLengths);
        // compare the result with the expected counts
        int positionInDoc;
        IntArrayList[] positionsInDocs;
        for (int i = 0; i < EXPECTED_WORD_POSITIONS.length; ++i) {
            positionsInDocs = wordPositions.get(i);
            for (int j = 0; j < positionsInDocs.length; ++j) {
                if (EXPECTED_WORD_POSITIONS[i][j] < 0) {
                    Assert.assertNull("Expected null because the word \""
                            + TEST_WORDS[j] + "\" shouldn't be found inside document " + i
                            + ". But got a position list instead.",
                            positionsInDocs[j]);
                } else {
                    Assert.assertEquals(1, positionsInDocs[j].elementsCount);
                    positionInDoc = positionsInDocs[j].buffer[0];
                    Assert.assertEquals("Expected the word \""
                            + TEST_WORDS[j] + "\" in document " + i + " at position " + EXPECTED_WORD_POSITIONS[i][j]
                            + " but got position " + positionInDoc + " form the index.",
                            EXPECTED_WORD_POSITIONS[i][j], positionInDoc);
                }
            }
        }
    }

    public static File createTempDirectory()
            throws IOException {
        File temp = File.createTempFile("temp_index", Long.toString(System.nanoTime()));
        if (!(temp.delete())) {
            return null;
        }
        if (!(temp.mkdir())) {
            return null;
        }
        return temp;
    }

}
