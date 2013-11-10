package org.nanomvc.utils;

import java.io.Serializable;

public class Resource
        implements Serializable {

    String link;
    String hash;
    String mime;
    byte[] data;

    public Resource() {
    }

    public Resource(String link, String hash, String mime, byte[] data) {
        this.link = link;
        this.hash = hash;
        this.mime = mime;
        this.data = data;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMime() {
        return this.mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}