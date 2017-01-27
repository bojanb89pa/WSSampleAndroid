package rs.bojanb89.wssample.ws;

import java.io.Serializable;

/**
 * Created by bojanb on 1/11/17.
 */
public class Channel implements Serializable {

    public enum Status{
        INIT, OPENING, OPEN, CLOSING, CLOSED, CANCELED
    }

    public Channel(String channelId, String channelUrl, Status status) {
        this.channelId = channelId;
        this.channelUrl = channelUrl;
        this.status = status;
    }

    private String channelId;

    private String channelUrl;

    private Status status;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
