package com.fincatto.documentofiscal.transformers;

import org.simpleframework.xml.transform.Transform;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DFDateTimeTransformer implements Transform<ZonedDateTime> {

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public ZonedDateTime read(final String data) {
    	if (data.contains("/")){
    		LocalDateTime ldt = LocalDateTime.from(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ssXXX").parse(data));
			ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
    		return zdt;
    	}else
    		return ZonedDateTime.parse(data, format);
    }

    @Override
    public String write(final ZonedDateTime data) {
        return format.format(data);
    }
}