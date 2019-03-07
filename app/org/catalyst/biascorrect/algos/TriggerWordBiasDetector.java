package org.catalyst.biascorrect.algos;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.catalyst.biascorrect.PlatformConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TriggerWordBiasDetector {

    private String[] FEMALE_PRONOUNS = {
            "she",
            "her",
            "she's",
            "she'd",
            "she'll",
            "her's",
            "hers",
    };

    private Map<String, String> _triggerWordMap = new HashMap<String, String>();

    private static TriggerWordBiasDetector INSTANCE = null;

    public static TriggerWordBiasDetector getInstance() {
        return INSTANCE;
    }

    private boolean hasFemalePronoun(String originalMessage) {
        boolean result = false;
        for (String word : FEMALE_PRONOUNS) {
            if (originalMessage.contains(word)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Pair<Boolean, String> getCorrectedMessage(String originalMessage) {
        String lowerOriginalMessage = originalMessage.toLowerCase();
        if (hasFemalePronoun(lowerOriginalMessage)) {
            boolean corrected = false;
            String correctedMessage = correctMessage(lowerOriginalMessage);
            if (!correctedMessage.equals(lowerOriginalMessage)) {
                corrected = true;
            }
            return Pair.of(corrected, correctedMessage);
        }
        return null;
    }

    public String correctMessage(String originalMessage) {
        String correctedMessage = originalMessage.toLowerCase();
        for (Map.Entry<String, String> entry : _triggerWordMap.entrySet()) {
            String word = entry.getKey();
            String correctedWord = entry.getValue();
            if (correctedMessage.contains(word)) {
                correctedMessage = correctedMessage.replace(word, String.format("*%s*", correctedWord));
            }
        }
        return correctedMessage;
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new TriggerWordBiasDetector();
            INSTANCE.initTriggerWords();
        }
    }

    private void initTriggerWords() {
        String confDirLocation = null;
        String runMode = System.getenv(PlatformConstants.ENV_RUN_MODE);
        if (runMode == null || runMode.equals(PlatformConstants.ENV_VALUE_PROD)) {
            confDirLocation = "/usr/local/eskalera/eskalera-bias-correction-java-latest/conf";
        } else {
            confDirLocation = "./conf";
        }
        try {
            CSVParser csvParser = new CSVParserBuilder().withQuoteChar('"').withSeparator(',').build();
            CSVReader reader = new CSVReaderBuilder(
                    new InputStreamReader(
                            new FileInputStream(String.format("%s/data/bias_correction_trigger_words.csv", confDirLocation))
                    )).withCSVParser(csvParser).build();
            String[] line = reader.readNext();
            while (line != null) {
                if (line.length == 2) {
                    _triggerWordMap.put(line[0], line[1]);
                }
                line = reader.readNext();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
