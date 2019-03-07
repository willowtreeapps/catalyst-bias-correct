package org.catalyst.biascorrect;

import org.catalyst.biascorrect.algos.TriggerWordBiasDetector;
import org.catalyst.biascorrect.db.DbManager;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StartupBootstrap {

    @Inject
    public void onStartup() {
        SlackSecrets.init();
        DbManager.init();
        DbManager dbManager = DbManager.getInstance();
        Logger.of("play").info("DB Manager Instance Type: " + dbManager.getType());
        TriggerWordBiasDetector.init();
        TriggerWordBiasDetector biasDetector = TriggerWordBiasDetector.getInstance();
        Logger.of("play").info("Initialzed Bias Detector: " + biasDetector.toString());
    }
}