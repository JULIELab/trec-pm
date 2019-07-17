package de.julielab.ir.sheets;

import java.io.IOException;
import java.util.List;

public interface Sheet {

    List<List<Object>> read(String spreadsheetId, String range) throws IOException;

    List<List<Object>> read(String spreadsheetId, String... ranges) throws IOException;

    int write(String spreadsheetId, String range, List<List<Object>> values) throws IOException;
}
