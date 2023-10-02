package com.fincatto.documentofiscal.transformers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.simpleframework.xml.transform.Transform;

public class DFZonedDateTimeTransformer implements Transform<ZonedDateTime> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX]");
    
    @Override
    public ZonedDateTime read(final String data) {
    	if (data.contains("/")){//DJB-07/06/2022
    		LocalDateTime ldt = LocalDateTime.from(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").parse(data));
    		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
    		return zdt;
    	}else
    		return ZonedDateTime.parse(data, DFZonedDateTimeTransformer.FORMATTER);
    }
    
    @Override
    public String write(final ZonedDateTime data) {
        return DFZonedDateTimeTransformer.FORMATTER.format(data);
    }
}