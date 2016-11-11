package company;

import mpi.MPI;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Created by lukas on 18.11.15.
 */
public class MPJFormatter extends Formatter {
    int rank;

    public MPJFormatter(int _rank) {
        rank = _rank;
    }

    @Override
    public String format(LogRecord record) {
        //System.out.println(record.getMessage());
        return String.format("<P%d> %s\n", rank, record.getMessage());
    }
}
