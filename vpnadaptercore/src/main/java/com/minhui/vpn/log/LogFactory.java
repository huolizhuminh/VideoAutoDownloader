package com.minhui.vpn.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a suitable log class by config.
 *
 * @author minhui
 * @since 17/9/22 21:28
 */

/* package */ final class LogFactory {

    /* package */ static ILog create(VLog.VLogConfig config) {
        if (!config.enable) {
            return new EmptyLog();
        }
        List<ILog> logs = new ArrayList<>();
        if (config.consoleConfig != null && config.consoleConfig.output) {
            logs.add(new AndroidLog());
        }
        if (config.fileConfig != null && config.fileConfig.logFile != null) {
            logs.add(new FileLog(config.fileConfig));
        }
        return logs.isEmpty() ? new EmptyLog() : new CombinationLog(logs);
    }

}
