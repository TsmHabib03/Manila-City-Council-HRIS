package com.ian.web.systemsettings.common;

import org.springframework.dao.DataIntegrityViolationException;

import com.ian.web.common.model.UXMessage;

public final class SettingsDeleteUtil {

    private SettingsDeleteUtil() {
    }

    public static UXMessage tryDelete(Runnable deleteAction, String entityLabel) {
        try {
            deleteAction.run();
            return new UXMessage("SUCCESS", entityLabel + " deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            return new UXMessage("ERROR", "Cannot delete this " + entityLabel
                    + ". Ensure it is not being used by any employee or record first, then try again.");
        }
    }
}
