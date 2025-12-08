package top.mrxiaom.sweet.taskplugin.tasks;

public enum EnumTaskType {
    DAILY("daily", "每日"),
    WEEKLY("weekly", "每周"),
    MONTHLY("monthly", "每月"),

    ;
    private final String key;
    private final String displayInLog;
    EnumTaskType(String key, String displayInLog) {
        this.key = key;
        this.displayInLog = displayInLog;
    }
    public String key() {
        return key;
    }

    public String getDisplayInLog() {
        return displayInLog;
    }
}
