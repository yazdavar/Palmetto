package org.aksw.palmetto.weight;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.palmetto.subsets.AnyAny;
import org.aksw.palmetto.subsets.OneAll;
import org.aksw.palmetto.subsets.OneAny;
import org.aksw.palmetto.subsets.SubsetCreator;
import org.aksw.palmetto.subsets.SubsetDefinition;
import org.aksw.palmetto.subsets.SubsetProbabilities;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class WordSetSizeBasedWeighterTest {

    private static final double DOUBLE_PRECISION_DELTA = 0.00000001;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                /*
                 * word1 1 1 1
                 * 
                 * word2 0 1 1
                 * 
                 * word3 0 1 1
                 */
                { new OneAll(), 3,
                        new double[] { 0, 1.0, 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0 },
                        new double[] { 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0 } },

                /*
                 * word1 0 1 1
                 * 
                 * word2 1 0 1
                 * 
                 * word3 1 1 0
                 */{ new OneAll(), 3,
                        new double[] { 0, 2.0 / 3.0, 2.0 / 3.0, 1.0 / 3.0, 2.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 0 },
                        new double[] { 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0 } },
                /*
                 * word1 0 0 0 1
                 * 
                 * word2 0 1 0 1
                 * 
                 * word3 0 0 1 1
                 */
                { new OneAll(), 3, new double[] { 0, 0.25, 0.5, 0.25, 0.5, 0.25, 0.25, 0.25 },
                        new double[] { 2.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0 } },
                /*
                 * word1 0 0 0 1
                 * 
                 * word2 0 1 0 1
                 * 
                 * word3 0 0 1 1
                 * 
                 * oneany=((P(w_1|w_2)-P(w_1)) + (P(w_1|w_3)-P(w_1)) + (P(w_1|w_2,w_3)-P(w_1)) + 
                 * (P(w_2|w_1)-P(w_2)) + (P(w_2|w_3)-P(w_2)) + (P(w_2|w_1,w_3)-P(w_2)) +
                 * (P(w_3|w_1)-P(w_3)) + (P(w_3|w_2)-P(w_3))) + (P(w_3|w_1,w_2)-P(w_3))
                 */
                { new OneAny(), 3, new double[] { 0, 0.25, 0.5, 0.25, 0.5, 0.25, 0.25, 0.25 },
                        new double[] { 1.0, 1.0, 2.0 / 3.0, 1.0, 1.0, 2.0 / 3.0, 1.0, 1.0, 2.0 / 3.0 } },
                /*
                 * word1 0 0 0 1
                 * 
                 * word2 0 1 0 1
                 * 
                 * word3 0 0 1 1
                 * 
                 * anyany= ((P(w_1|w_2)-P(w_1)) + (P(w_1|w_3)-P(w_1)) + (P(w_1|w_2,w_3)-P(w_1)) + 
                 * (P(w_2|w_1)-P(w_2)) + (P(w_2|w_3)-P(w_2)) + (P(w_2|w_1,w_3)-P(w_2)) +
                 * (P(w_1,w_2|w_3)-P(w_1,w_2)) +
                 * (P(w_3|w_1)-P(w_3)) + (P(w_3|w_2)-P(w_3)) + (P(w_3|w_1,w_2)-P(w_3)) + 
                 * (P(w_1,w_3|w_2)-P(w_1,w_3)) + (P(w_2,w_3|w_1)-P(w_2,w_3)))
                 */
                {
                        new AnyAny(),
                        3,
                        new double[] { 0, 0.25, 0.5, 0.25, 0.5, 0.25, 0.25, 0.25 },
                        new double[] { 1.0, 1.0, 2.0 / 3.0, 1.0, 1.0, 2.0 / 3.0, 2.0 / 3.0, 1.0, 1.0, 2.0 / 3.0,
                                2.0 / 3.0, 2.0 / 3.0 } } });
    }

    private SubsetCreator subsetCreator;
    private int wordsetSize;
    private double probabilities[];
    private double expectedWeightings[];

    public WordSetSizeBasedWeighterTest(SubsetCreator subsetCreator, int wordsetSize, double[] probabilities,
            double[] expectedWeightings) {
        this.subsetCreator = subsetCreator;
        this.wordsetSize = wordsetSize;
        this.probabilities = probabilities;
        this.expectedWeightings = expectedWeightings;
    }

    @Test
    public void test() {
        SubsetDefinition subsets = subsetCreator.getSubsetDefinition(wordsetSize);
        SubsetProbabilities subProbs = new SubsetProbabilities(subsets.segments, subsets.conditions, probabilities);
        Weighter weighter = new WordSetSizeBasedWeighter();
        Assert.assertArrayEquals(expectedWeightings, weighter.createWeights(subProbs), DOUBLE_PRECISION_DELTA);
    }
}