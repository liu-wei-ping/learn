/****/
package com.learn.site.common.po;

/**
 * @author liuwp
 * @date 2016年5月11日
 */
public class PublicKeyPo {
    private String exponent;
	private String modulus;
	public String getExponent() {
		return exponent;
	}
	public String getModulus() {
		return modulus;
	}
	public void setExponent(final String exponent) {
		this.exponent = exponent;
	}
    public void setModulus(final String modulus) {
		this.modulus = modulus;
	}
}
