package com.agenthun.blemonitor.bean.base;

/**
 * @project BleMonitor
 * @authors agenthun
 * @date 16/6/3 21:54.
 */
public class HistoryData {
    private String id;
    private Integer actionType;
    private String createDatetime;
    private String content;

    public HistoryData() {
    }

    public HistoryData(Integer actionType, String createDatetime, String content) {
        this.actionType = actionType;
        this.createDatetime = createDatetime;
        this.content = content;
    }

    public HistoryData(String id, Integer actionType, String createDatetime, String content) {
        this.id = id;
        this.actionType = actionType;
        this.createDatetime = createDatetime;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public String getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(String createDatetime) {
        this.createDatetime = createDatetime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                "id=" + id +
                ", actionType=" + actionType +
                ", createDatetime='" + createDatetime + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
