package demo.payroll.files;

import org.springframework.stereotype.Component;

@Component
public class ImportStats {

    private long linesProcessed;
    private int importsRun;

    public void recordLines(long lines) {
        linesProcessed += lines;
        importsRun++;
    }

    public long linesProcessed() {
        return linesProcessed;
    }

    public long importsRun() {
        return importsRun;
    }
}
