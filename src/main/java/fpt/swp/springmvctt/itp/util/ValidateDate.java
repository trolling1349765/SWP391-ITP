package fpt.swp.springmvctt.itp.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidateDate {
    public boolean isDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            if (dateStr.equals(formatter.format(LocalDate.parse(dateStr, formatter)))) return true;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Sai định dạng ngày. Định dạng đúng là dd/MM/yyyy");
        }
        return false;
    }
}


