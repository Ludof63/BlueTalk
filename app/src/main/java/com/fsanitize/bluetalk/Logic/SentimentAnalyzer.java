package com.fsanitize.bluetalk.Logic;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.fsanitize.bluetalk.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class SentimentAnalyzer {
    private static  Context context;
    private final static String LEXICON_FILE = "vader_lexicon.txt";
    private final static String NEGATIVE_FILE = "negate_words.txt";
    private final static String BOOSTER_FILE = "booster_dict.txt";

    private static final double B_INCR = 0.293;
    private static final double B_DECR = 0.293;
    private static final double NEG_SCALAR = -0.74;

    private static HashSet<String> NEGATIVE_WORDS = null;
    private static HashMap<String, Double> BOOSTER = null;
    private static HashMap<String, Double> LEXICON = null;

    private String[] messageWords;
    private List<Double> wordsValence;

    // SETUP ------------------------------------------------
    private static void initializeNegativeWords() {
        if (NEGATIVE_WORDS != null)
            return;

        NEGATIVE_WORDS = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(NEGATIVE_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                NEGATIVE_WORDS.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Lexicon db not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not close lexicon db file", e);
        }
    }

    private static void initializeBooster() {
        if (BOOSTER != null)
            return;

        BOOSTER = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(BOOSTER_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                String word = parts[0];
                if (parts[1] == "B_INCR")
                    BOOSTER.put(word, B_INCR);
                else
                    BOOSTER.put(word, B_DECR);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Affini db not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not close affini db file", e);
        }
    }

    private static void initializeLexicon() {
        if (LEXICON != null)
            return;

        LEXICON = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(LEXICON_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s(?![a-z])");
                String word = parts[0];
                Double value = Double.parseDouble(parts[1]);
                LEXICON.put(word, value);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Affini db not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not close affini db file", e);
        }
    }


    public SentimentAnalyzer(Context context) {
        this.context = context;
        initializeLexicon();
        initializeBooster();
        initializeNegativeWords();
    }

    // ------------------------------------------------
    // UTILS ------------------------------------------------

    private boolean isNegated(String word) {
        if (NEGATIVE_WORDS.contains(word))
            return true;
        if (word.contains("n't"))
            return true;
        return false;
    }

    private double normalize(double score) {
        double alpha = 15;
        double normalizedScore = score / Math.sqrt(Math.pow(score, 2) + alpha);
        if (normalizedScore < -1)
            return -1;
        else
            return normalizedScore > 1 ? 1 : normalizedScore;
    }

    private double lookPreviousWords(int wordPosition, int nStep, double currentValence) {
        for (int i = 1; i <= nStep; i++) {
            if (wordPosition - i < 0)
                return currentValence;

            if (!LEXICON.containsKey(messageWords[wordPosition - i])) {

                if (BOOSTER.containsKey(messageWords[wordPosition - i])) {
                    double booster = BOOSTER.get(messageWords[wordPosition - i]);
                    if (currentValence < 0) {
                        booster *= -1;
                    }
                    booster = booster * (1 - (0.1 * i));
                    currentValence += booster;

                } else if (isNegated(messageWords[wordPosition - i])) {
                    currentValence *= NEG_SCALAR;
                }

            }
        }
        return currentValence;

    }

    private String[] getMessageWords(String message) {
        message = message.toLowerCase();
        return message.split("'?[^\\p{L}']+'?");
    }

    private void getMessageSentiment(String message) {
        messageWords = getMessageWords(message);
        wordsValence = new LinkedList<>();

        for (int pos = 0; pos < messageWords.length; pos++) {

            if (BOOSTER.containsKey(messageWords[pos]))
                continue;

            if (LEXICON.containsKey(messageWords[pos])) {
                Double valence = LEXICON.get(messageWords[pos]);
                valence = lookPreviousWords(pos, 3, valence);
                wordsValence.add(valence);
            }

        }
    }

    public interface SENTIMENT_STATUS {
        public static final int EXTRA_NEGATIVE = -2;
        public static final int NEGATIVE = -1;
        public static final int NEUTRAL = 0;
        public static final int POSITIVE = 1;
        public static final int EXTRA_POSITIVE = 2;
    }

    public int getMessageSentimentStatus(String message) {
        getMessageSentiment(message);
        // for (String s : messageWords)
        // System.out.println(s);
        // System.out.println("");
        // for (Double r : wordsValence)
        // System.out.println(r);
        // System.out.println("");

        Double res = 0.0;
        for (double valence : wordsValence)
            res += valence;

        res = normalize(res);
        Double module = Math.abs(res);
        if(module < 0.25)
            return SENTIMENT_STATUS.NEUTRAL;
        else if (module <0.65) {
            return res > 0 ? SENTIMENT_STATUS.POSITIVE : SENTIMENT_STATUS.NEGATIVE;
        }
        else
            return res > 0 ? SENTIMENT_STATUS.EXTRA_POSITIVE: SENTIMENT_STATUS.EXTRA_NEGATIVE;
    }
}
