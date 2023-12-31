package com.followinsider.secapi.forms.refs.daily;

import com.followinsider.secapi.forms.FormType;
import com.followinsider.secapi.forms.refs.FormRef;
import com.followinsider.common.utils.DateUtils;
import com.followinsider.common.utils.StringUtils;
import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@UtilityClass
public class DailyFeedParser {

    public List<FormRef> parse(String data) throws ParseException {
        String hr = StringUtils.substring(data, "<hr>", "<hr>");
        if (hr == null) throw new ParseException("<hr> field is missing", -1);

        List<FormRef> refs = new ArrayList<>();
        for (String entry : hr.split("\n")) {
            refs.add(parseEntry(entry));
        }
        return refs;
    }

    private FormRef parseEntry(String entry) {
        String[] parts = entry.split("</a>");

        // Parse issuer CIK
        int issuerCikIdx = parts[1].indexOf(">") + 1;
        String issuerCik = parts[1].substring(issuerCikIdx);

        // Parse accession number
        int accNumIdx = parts[0].indexOf(issuerCik) + issuerCik.length() + 1;
        String accNum = parts[0].substring(accNumIdx, parts[0].indexOf("-index"));

        // Parse form type
        int typeIdx = parts[0].indexOf(">") + 1;
        String typeValue = parts[0].substring(typeIdx);
        FormType type = FormType.ofValue(typeValue);

        // Parse filing date
        String dateValue = entry.substring(0, entry.indexOf(" "));
        LocalDate filedAt = DateUtils.parse(dateValue, "MM-dd-yyyy");

        return new FormRef(accNum, issuerCik, type, filedAt);
    }

}
