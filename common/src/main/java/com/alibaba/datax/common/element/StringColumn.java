package com.alibaba.datax.common.element;

import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by jingxing on 14-8-24.
 */

public class StringColumn extends Column {

	public StringColumn() {
		this((String) null);
	}

	public StringColumn(final String rawData) {
		super(rawData, Column.Type.STRING, (null == rawData ? 0 : rawData
				.length()));
	}

	@Override
	public String asString() {
		if (null == this.getRawData()) {
			return null;
		}

		return (String) this.getRawData();
	}

	private void validateDoubleSpecific(final String data) {
		if ("NaN".equals(data) || "Infinity".equals(data)
				|| "-Infinity".equals(data)) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT,
					String.format("String[\"%s\"] is a special type of Double and cannot be converted to any other type.", data));
		}

		return;
	}

	@Override
	public BigInteger asBigInteger() {
		if (null == this.getRawData()) {
			return null;
		}

		this.validateDoubleSpecific((String) this.getRawData());

		try {
			return this.asBigDecimal().toBigInteger();
		} catch (Exception e) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT, String.format(
							"String[\"%s\"]can't turnBigInteger .", this.asString()));
		}
	}

	@Override
	public Long asLong() {
		if (null == this.getRawData()) {
			return null;
		}

		this.validateDoubleSpecific((String) this.getRawData());

		try {
			BigInteger integer = this.asBigInteger();
			OverFlowUtil.validateLongNotOverFlow(integer);
			return integer.longValue();
		} catch (Exception e) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT,
					String.format("String[\"%s\"] can't convert Long .", this.asString()));
		}
	}

	@Override
	public BigDecimal asBigDecimal() {
		if (null == this.getRawData()) {
			return null;
		}

		this.validateDoubleSpecific((String) this.getRawData());

		try {
			return new BigDecimal(this.asString());
		} catch (Exception e) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT, String.format(
							"String [\"%s\"] can't turnBigDecimal .", this.asString()));
		}
	}

	@Override
	public Double asDouble() {
		if (null == this.getRawData()) {
			return null;
		}

		String data = (String) this.getRawData();
		if ("NaN".equals(data)) {
			return Double.NaN;
		}

		if ("Infinity".equals(data)) {
			return Double.POSITIVE_INFINITY;
		}

		if ("-Infinity".equals(data)) {
			return Double.NEGATIVE_INFINITY;
		}

		BigDecimal decimal = this.asBigDecimal();
		OverFlowUtil.validateDoubleNotOverFlow(decimal);

		return decimal.doubleValue();
	}

	@Override
	public Boolean asBoolean() {
		if (null == this.getRawData()) {
			return null;
		}

		if ("true".equalsIgnoreCase(this.asString())) {
			return true;
		}

		if ("false".equalsIgnoreCase(this.asString())) {
			return false;
		}

		throw DataXException.asDataXException(
				CommonErrorCode.CONVERT_NOT_SUPPORT,
				String.format("String[\"%s\"] cannot be converted to a Bool.", this.asString()));
	}

	@Override
	public Date asDate() {
		try {
			return ColumnCast.string2Date(this);
		} catch (Exception e) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT,
					String.format("String[\"%s\"]can't turnDate .", this.asString()));
		}
	}

	@Override
	public byte[] asBytes() {
		try {
			return ColumnCast.string2Bytes(this);
		} catch (Exception e) {
			throw DataXException.asDataXException(
					CommonErrorCode.CONVERT_NOT_SUPPORT,
					String.format("String[\"%s\"] Cannot convert to Bytes.", this.asString()));
		}
	}
}
