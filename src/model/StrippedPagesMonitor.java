package model;

import java.util.ArrayList;
import java.util.List;

public class StrippedPagesMonitor {

    private List<String> strippedPages = new ArrayList<>();

    public synchronized void producePage(String page) {
        strippedPages.add(page);
        notifyAll();
    }

    public synchronized List<String> consumePages() throws InterruptedException {
        while(this.strippedPages.isEmpty()) {
            wait();
        }
        final List<String> tmp = new ArrayList<>(this.strippedPages);
        this.strippedPages.clear();
        return tmp;
    }
}
