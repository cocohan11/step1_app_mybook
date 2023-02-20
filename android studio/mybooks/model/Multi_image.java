package com.example.mybooks.model;

import java.io.File;

public class Multi_image {

    public File file;

    public Multi_image(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "multi_image{" +
                "file=" + file +
                '}';
    }
}
