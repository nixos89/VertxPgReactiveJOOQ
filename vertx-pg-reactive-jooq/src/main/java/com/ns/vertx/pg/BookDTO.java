package com.ns.vertx.pg;

import java.io.OutputStream;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.Configuration;
import org.jooq.Converter;
import org.jooq.Field;
import org.jooq.JSONFormat;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Record11;
import org.jooq.Record12;
import org.jooq.Record13;
import org.jooq.Record14;
import org.jooq.Record15;
import org.jooq.Record16;
import org.jooq.Record17;
import org.jooq.Record18;
import org.jooq.Record19;
import org.jooq.Record2;
import org.jooq.Record20;
import org.jooq.Record21;
import org.jooq.Record22;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Record5;
import org.jooq.Record6;
import org.jooq.Record7;
import org.jooq.Record8;
import org.jooq.Record9;
import org.jooq.RecordMapper;
import org.jooq.Row7;
import org.jooq.TXTFormat;
import org.jooq.Table;
import org.jooq.XMLFormat;
import org.jooq.exception.DataTypeException;
import org.jooq.exception.IOException;
import org.jooq.exception.MappingException;


// TODO: check 'Record7' interface implementation !!!
public class BookDTO implements Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1942406929503935826L;

	@Override
	public <T> Field<T> field(Field<T> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> field(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> field(Name name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?> field(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?>[] fields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?>[] fields(Field<?>... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?>[] fields(String... fieldNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?>[] fields(Name... fieldNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<?>[] fields(int... fieldIndexes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Field<T> field) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Field<?> field, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, U> U get(Field<T> field, Converter<? super T, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String fieldName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(String fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U get(String fieldName, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(Name fieldName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Name fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U get(Name fieldName, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(int index) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(int index, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U get(int index, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void set(Field<T> field, T value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T, U> void set(Field<T> field, U value, Converter<? extends T, ? super U> converter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> Record with(Field<T> field, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, U> Record with(Field<T> field, U value, Converter<? extends T, ? super U> converter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Record original() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T original(Field<T> field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object original(int fieldIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object original(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object original(Name fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean changed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changed(Field<?> field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changed(int fieldIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changed(String fieldName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changed(Name fieldName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changed(boolean changed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changed(Field<?> field, boolean changed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changed(int fieldIndex, boolean changed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changed(String fieldName, boolean changed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changed(Name fieldName, boolean changed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(Field<?> field) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(int fieldIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(String fieldName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(Name fieldName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] intoArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> intoList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Object> intoStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> intoMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record into(Field<?>... fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1> Record1<T1> into(Field<T1> field1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2> Record2<T1, T2> into(Field<T1> field1, Field<T2> field2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3> Record3<T1, T2, T3> into(Field<T1> field1, Field<T2> field2, Field<T3> field3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4> Record4<T1, T2, T3, T4> into(Field<T1> field1, Field<T2> field2, Field<T3> field3,
			Field<T4> field4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5> Record5<T1, T2, T3, T4, T5> into(Field<T1> field1, Field<T2> field2, Field<T3> field3,
			Field<T4> field4, Field<T5> field5) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6> Record6<T1, T2, T3, T4, T5, T6> into(Field<T1> field1, Field<T2> field2,
			Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7> Record7<T1, T2, T3, T4, T5, T6, T7> into(Field<T1> field1, Field<T2> field2,
			Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8> Record8<T1, T2, T3, T4, T5, T6, T7, T8> into(Field<T1> field1,
			Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7,
			Field<T8> field8) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9> Record9<T1, T2, T3, T4, T5, T6, T7, T8, T9> into(Field<T1> field1,
			Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6, Field<T7> field7,
			Field<T8> field8, Field<T9> field9) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Record10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Record11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Record12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Record13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Record14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Record15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Record16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> Record17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> Record18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17, Field<T18> field18) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> Record19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17, Field<T18> field18, Field<T19> field19) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> Record20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> Record21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20, Field<T21> field21) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> Record22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> into(
			Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5, Field<T6> field6,
			Field<T7> field7, Field<T8> field8, Field<T9> field9, Field<T10> field10, Field<T11> field11,
			Field<T12> field12, Field<T13> field13, Field<T14> field14, Field<T15> field15, Field<T16> field16,
			Field<T17> field17, Field<T18> field18, Field<T19> field19, Field<T20> field20, Field<T21> field21,
			Field<T22> field22) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E into(Class<? extends E> type) throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E into(E object) throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R extends Record> R into(Table<R> table) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet intoResultSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> E map(RecordMapper<Record, E> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void from(Object source) throws MappingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void from(Object source, Field<?>... fields) throws MappingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void from(Object source, String... fieldNames) throws MappingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void from(Object source, Name... fieldNames) throws MappingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void from(Object source, int... fieldIndexes) throws MappingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromMap(Map<String, ?> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromMap(Map<String, ?> map, Field<?>... fields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromMap(Map<String, ?> map, String... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromMap(Map<String, ?> map, Name... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromMap(Map<String, ?> map, int... fieldIndexes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromArray(Object... array) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromArray(Object[] array, Field<?>... fields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromArray(Object[] array, String... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromArray(Object[] array, Name... fieldNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromArray(Object[] array, int... fieldIndexes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String format() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String format(TXTFormat format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void format(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void format(OutputStream stream, TXTFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void format(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void format(Writer writer, TXTFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String formatJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String formatJSON(JSONFormat format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void formatJSON(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatJSON(OutputStream stream, JSONFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatJSON(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatJSON(Writer writer, JSONFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String formatXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String formatXML(XMLFormat format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void formatXML(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatXML(OutputStream stream, XMLFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatXML(Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatXML(Writer writer, XMLFormat format) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Record record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T getValue(Field<T> field) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(Field<T> field, T defaultValue) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(Field<?> field, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(Field<?> field, Class<? extends T> type, T defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, U> U getValue(Field<T> field, Converter<? super T, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, U> U getValue(Field<T> field, Converter<? super T, ? extends U> converter, U defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String fieldName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String fieldName, Object defaultValue) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(String fieldName, Class<? extends T> type)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(String fieldName, Class<? extends T> type, T defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U getValue(String fieldName, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U getValue(String fieldName, Converter<?, ? extends U> converter, U defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(Name fieldName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(Name fieldName, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U getValue(Name fieldName, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(int index) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(int index, Object defaultValue) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(int index, Class<? extends T> type) throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getValue(int index, Class<? extends T> type, T defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U getValue(int index, Converter<?, ? extends U> converter)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U getValue(int index, Converter<?, ? extends U> converter, U defaultValue)
			throws IllegalArgumentException, DataTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void setValue(Field<T> field, T value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T, U> void setValue(Field<T> field, U value, Converter<? extends T, ? super U> converter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attach(Configuration configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Configuration configuration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Row7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> fieldsRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Row7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> valuesRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Long> field1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<String> field2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Double> field3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Integer> field4() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Boolean> field5() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Set<Long>> field6() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field<Set<Long>> field7() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long value1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String value2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double value3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer value4() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean value5() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Long> value6() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Long> value7() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value1(Long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value2(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value3(Double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value4(Integer value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value5(Boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value6(Set<Long> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> value7(Set<Long> value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record7<Long, String, Double, Integer, Boolean, Set<Long>, Set<Long>> values(Long t1, String t2, Double t3,
			Integer t4, Boolean t5, Set<Long> t6, Set<Long> t7) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long component1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String component2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double component3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer component4() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean component5() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Long> component6() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Long> component7() {
		// TODO Auto-generated method stub
		return null;
	}

}
