package model;

import java.io.File;
import java.io.IOException;

public class RawPagesMonitor {

    private static final int FIRST_PAGE = 0;

    private final File file;
    private int pageNumber;

    public RawPagesMonitor(File f) throws IOException {
        this.file = new File(f.getAbsolutePath());
        this.pageNumber = FIRST_PAGE;
    }

    public synchronized Page getPageNumber() {
        this.pageNumber++;
        return new Page(this.file, this.pageNumber);
    }

}
