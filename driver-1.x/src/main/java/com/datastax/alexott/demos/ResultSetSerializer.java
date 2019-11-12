package com.datastax.alexott.demos;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@SuppressWarnings("serial")
public class ResultSetSerializer extends StdSerializer<ResultSet> {
	private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();
	public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

	public ResultSetSerializer() {
		this(null);
	}

	public ResultSetSerializer(Class<ResultSet> t) {
		super(t);
	}

	void handleCollection(Row row, int i, String name, DataType dt, JsonGenerator jgen) throws IOException {
		jgen.writeStringField(name, row.getObject(i).toString());
	}

	// TODO: cache UDT definitions...

	void writeItem(Row row, int i, String name, DataType dt, JsonGenerator jgen) throws IOException {
		// TODO: use map lookup instead?
		// TODO: how to handle UDTs correctly?
		if (DataType.cboolean().equals(dt)) {
			jgen.writeBooleanField(name, row.getBool(i));
		} else if (DataType.cint().equals(dt)) {
			jgen.writeNumberField(name, row.getInt(i));
		} else if (DataType.cdouble().equals(dt)) {
			jgen.writeNumberField(name, row.getDouble(i));
		} else if (DataType.cfloat().equals(dt)) {
			jgen.writeNumberField(name, row.getFloat(i));
		} else if (DataType.counter().equals(dt) || DataType.bigint().equals(dt)) {
			jgen.writeNumberField(name, row.getLong(i));
		} else if (DataType.smallint().equals(dt)) {
			jgen.writeNumberField(name, row.getShort(i));
		} else if (DataType.tinyint().equals(dt)) {
			jgen.writeNumberField(name, row.getByte(i));
		} else if (DataType.timestamp().equals(dt)) {
			String ts = TIMESTAMP_FORMATTER.format(row.getTimestamp(i).toInstant());
			jgen.writeStringField(name, ts);
		} else if (DataType.date().equals(dt)) {
			jgen.writeStringField(name, row.getDate(i).toString());
		} else if (DataType.time().equals(dt)) {
			LocalTime tm = LocalTime.ofNanoOfDay(row.getTime(i));
			jgen.writeStringField(name, TIME_FORMATTER.format(tm));
		} else if (DataType.blob().equals(dt)) {
			jgen.writeStringField(name, B64_ENCODER.encodeToString(row.getBytes(i).array()));
		} else if (dt.isCollection()) {
			handleCollection(row, i, name, dt, jgen);
		} else {
			jgen.writeStringField(name, row.getObject(i).toString());
		}
	}

	@Override
	public void serialize(ResultSet rs, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		ColumnDefinitions cd = rs.getColumnDefinitions();
		List<ColumnDefinitions.Definition> lcd = cd.asList();
		int lsize = lcd.size();
		String[] names = new String[lsize];
		DataType[] types = new DataType[lsize];
		for (int i = 0; i < lsize; i++) {
			ColumnDefinitions.Definition cdef = lcd.get(i);
			names[i] = cdef.getName();
			types[i] = cdef.getType();
		}
		jgen.writeStartArray();
		for (Row row : rs) {
			jgen.writeStartObject();
			for (int i = 0; i < lsize; i++) {
				String name = names[i];
				if (row.isNull(i)) {
					jgen.writeNullField(name);
				} else {
					writeItem(row, i, name, types[i], jgen);
				}
			}
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

}
