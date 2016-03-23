package com.realdolmen.timeregistration.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.realdolmen.timeregistration.util.DateUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

/**
 * Deserializes dates in a specific manner.
 */
public class DateDeserializer implements JsonDeserializer<DateTime> {

	@Override
	public DateTime deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		long date = element.getAsLong();
		return new DateTime(date, DateTimeZone.UTC);
	}
}
