package model;

import java.io.File;

public class Page {

    private final File file;
    private final int pageNumber;

    public Page(File f, int p) {
        this.file = new File(f.getAbsolutePath());
        this.pageNumber = p;
    }

    public File getFile() {
        return this.file;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }
}
