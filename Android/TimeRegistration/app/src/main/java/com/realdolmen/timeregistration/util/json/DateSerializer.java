package com.realdolmen.timeregistration.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.realdolmen.timeregistration.util.DateUtil;
import com.realdolmen.timeregistration.util.UTC;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public class DateSerializer implements JsonSerializer<DateTime> {
	@Override
	public JsonElement serialize(@UTC DateTime src, Type typeOfSrc, JsonSerializationContext context) {
		if (src == null)
			return null;
		DateUtil.enforceUTC(src, "Date to serialize must be in UTC format!");
		return new JsonPrimitive(ISODateTimeFormat.dateTime().print(src));
	}
}
