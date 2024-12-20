package com.fincatto.documentofiscal.transformers;

import org.simpleframework.xml.transform.Transform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DFLocalDateTimeTransformer implements Transform<LocalDateTime> {
    
    private static final DateTimeFormatter SIMPLE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter DATETIME_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    
    @Override
    public LocalDateTime read(final String data) {
        try {
            return LocalDateTime.parse(data, DFLocalDateTimeTransformer.DATETIME_FORMATTER);
        } catch (final Exception e) {
        	try {
            	return LocalDateTime.parse(data, DFLocalDateTimeTransformer.DATETIME_FORMATTER_2);
			} catch (Exception e2) {
				try{
					return LocalDateTime.from(DFLocalDateTimeTransformer.SIMPLE_DATETIME_FORMATTER.parse(data));
				}catch(Exception e4){
					return LocalDateTime.from(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").parse(data));//DJB-07/06/2022
				}
			}
        }
    }
    
    @Override
    public String write(final LocalDateTime data) {
        return DFLocalDateTimeTransformer.SIMPLE_DATETIME_FORMATTER.format(data);
    }
}