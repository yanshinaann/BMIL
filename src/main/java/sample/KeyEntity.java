package sample;

public class KeyEntity {
    private String key;
    private long upTime;
    private long downTime;

    public KeyEntity(String key, long downTime) {
        this.key = key;
        this.downTime = downTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    @Override
    public String toString() {
        return "KeyEntity{" +
                "key='" + key + '\'' +
                ", upTime=" + upTime +
                ", downTime=" + downTime +
                '}';
    }
}
