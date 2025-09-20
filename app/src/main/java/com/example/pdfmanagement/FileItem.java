package com.example.pdfmanagement;

public class FileItem {
    private String name;
    private String uri;       // content:// URI string
    private String meta;      // formatted "date • size"
    private String type;      // "pdf","doc","xls","ppt", etc.
    private String mimeType;

    public FileItem(String name, String uri, String meta, String type, String mimeType) {
        this.name = name;
        this.uri = uri;
        this.meta = meta;
        this.type = type;
        this.mimeType = mimeType;
    }

    public String getName() { return name; }
    public String getUri() { return uri; }
    public String getMeta() { return meta; }
    public String getType() { return type; }
    public String getMimeType() { return mimeType; }
}
