package org.catalyst.biascorrect.algos;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import opennlp.tools.tokenize.DetokenizationDictionary;
import opennlp.tools.tokenize.Detokenizer;
import opennlp.tools.tokenize.DictionaryDetokenizer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.catalyst.biascorrect.PlatformConstants.ENV_RUN_MODE;
import static org.catalyst.biascorrect.PlatformConstants.ENV_VALUE_PROD;

public class TriggerWordBiasDetector {

    private SimpleTokenizer TOKENIZER = SimpleTokenizer.INSTANCE;

    private Detokenizer _detokenizer = null;

    private Set<String> _femalePronouns = new HashSet<String>();

    private Map<String, String> _triggerWordMap = new HashMap<String, String>();

    private static TriggerWordBiasDetector INSTANCE = null;

    public static TriggerWordBiasDetector getInstance() {
        return INSTANCE;
    }

    private boolean hasFemalePronoun(String[] messageTokens) {
        boolean result = false;
        for (String token : messageTokens) {
            if (_femalePronouns.contains(token.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Pair<Boolean, String> getCorrectedMessage(String originalMessage) {
        String[] messageTokens = TOKENIZER.tokenize(originalMessage);
        if (hasFemalePronoun(messageTokens)) {
            boolean corrected = false;
            String correctedMessage = correctMessage(messageTokens);
            if (!correctedMessage.equalsIgnoreCase(originalMessage)) {
                corrected = true;
            }
            return Pair.of(corrected, correctedMessage);
        }
        return null;
    }

    public String correctMessage(String originalMessage) {
        String[] messageTokens = TOKENIZER.tokenize(originalMessage);
        return correctMessage(messageTokens);
    }

    private String correctMessage(String[] messageTokens) {
        String[] correctedMessageTokens = new String[messageTokens.length];
        for (int i = 0; i < messageTokens.length; ++i) {
            String replacement = _triggerWordMap.get(messageTokens[i].toLowerCase());
            if (replacement != null) {
                if (messageTokens[i].charAt(0) == Character.toUpperCase(messageTokens[i].charAt(0))) {
                    char[] replacementChars = replacement.toCharArray();
                    replacementChars[0] = Character.toUpperCase(replacementChars[0]);
                    correctedMessageTokens[i] = String.format("*%s*", new String(replacementChars));
                } else {
                    correctedMessageTokens[i] = String.format("*%s*", replacement);
                }
            } else {
                correctedMessageTokens[i] = messageTokens[i];
            }
        }
        return _detokenizer.detokenize(correctedMessageTokens, null);
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new TriggerWordBiasDetector();
            INSTANCE.initTriggerWords();
        }
    }

    private void initTriggerWords() {

        _femalePronouns.add("she");
        _femalePronouns.add("her");
        _femalePronouns.add("she's");
        _femalePronouns.add("she'll");
        _femalePronouns.add("her's");
        _femalePronouns.add("hers");

        String[] tokens = new String[]{".", "!", "(", ")", "\"", "-", "?", "'"};

        DetokenizationDictionary.Operation[] operations = new DetokenizationDictionary.Operation[]{
                DetokenizationDictionary.Operation.MOVE_LEFT,
                DetokenizationDictionary.Operation.MOVE_LEFT,
                DetokenizationDictionary.Operation.MOVE_RIGHT,
                DetokenizationDictionary.Operation.MOVE_LEFT,
                DetokenizationDictionary.Operation.RIGHT_LEFT_MATCHING,
                DetokenizationDictionary.Operation.MOVE_BOTH,
                DetokenizationDictionary.Operation.MOVE_LEFT,
                DetokenizationDictionary.Operation.MOVE_BOTH
        };

        DetokenizationDictionary dict = new DetokenizationDictionary(tokens, operations);

        _detokenizer = new DictionaryDetokenizer(dict);

        String confDirLocation = null;
        String runMode = System.getenv(ENV_RUN_MODE);
        if (runMode == null || runMode.equals(ENV_VALUE_PROD)) {
            confDirLocation = "/usr/local/catalyst/catalyst-bias-correct-latest/conf";
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
