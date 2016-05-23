package com.learn.site.common.utils.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.learn.site.common.po.PublicKeyPo;

/**
 * RSA算法加密/解密工具类。
 *
 * @author 刘伟平<liuweiping@ wcp.sina.com> 2015年10月26日 上午10:52:48
 *
 */

public abstract class RSAUtils {
	/** 算法名称 */
	private static final String ALGORITHOM = "RSA";
	private static char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
			'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z' };

	/** 默认的安全服务提供者 */
	private static final Provider DEFAULT_PROVIDER = new BouncyCastleProvider();
	/** 密钥大小 */
	private static final int KEY_SIZE = 1024;
	private static KeyFactory keyFactory = null;
	private static KeyPairGenerator keyPairGen = null;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RSAUtils.class);
	/** 缓存的密钥对。 */
	private static KeyPair oneKeyPair = null;
	/** 保存生成的密钥对的文件名称。 */
	private static final String RSA_PAIR_FILENAME = "/__RSA_LEAEN_SITEKEY.key";

	private static File rsaPairFile = null;

	static {
		try {
			keyPairGen = KeyPairGenerator.getInstance(ALGORITHOM,
					DEFAULT_PROVIDER);
			keyFactory = KeyFactory.getInstance(ALGORITHOM, DEFAULT_PROVIDER);
		} catch (final NoSuchAlgorithmException ex) {
			LOGGER.error(ex.getMessage());
		}
		rsaPairFile = new File(getRSAPairFilePath());
	}

	/**
	 * 使用指定的私钥解密数据。
	 *
	 * @param privateKey
	 *            给定的私钥。
	 * @param data
	 *            要解密的数据。
	 * @return 原数据。
	 */
	public static byte[] decrypt(final PrivateKey privateKey, final byte[] data)
			throws Exception {
		final Cipher ci = Cipher.getInstance(ALGORITHOM, DEFAULT_PROVIDER);
		ci.init(Cipher.DECRYPT_MODE, privateKey);
		return ci.doFinal(data);
	}

	/**
	 * 使用给定的私钥解密给定的字符串。
	 * <p />
	 * 若私钥为 {@code null}，或者 {@code encrypttext} 为 {@code null}或空字符串则返回
	 * {@code null}。 私钥不匹配时，返回 {@code null}。
	 *
	 * @param privateKey
	 *            给定的私钥。
	 * @param encrypttext
	 *            密文。
	 * @return 原文字符串。
	 */
	public static String decryptString(final PrivateKey privateKey,
			final String encrypttext) {
		if (privateKey == null || StringUtils.isBlank(encrypttext)) {
			return null;
		}
		try {
			final byte[] en_data = Hex.decodeHex(encrypttext.toCharArray());
			final byte[] data = decrypt(privateKey, en_data);
			return new String(data);
		} catch (final Exception ex) {
			LOGGER.error(String.format("\"%s\" Decryption failed. Cause: %s",
					encrypttext, ex.getCause().getMessage()));
		}
		return null;
	}

	/**
	 * 使用默认的私钥解密给定的字符串。
	 * <p />
	 * 若{@code encrypttext} 为 {@code null}或空字符串则返回 {@code null}。 私钥不匹配时，返回
	 * {@code null}。
	 *
	 * @param encrypttext
	 *            密文。
	 * @return 原文字符串。
	 */
	public static String decryptString(final String encrypttext) {
		if (StringUtils.isBlank(encrypttext)) {
			return null;
		}
		final KeyPair keyPair = getKeyPair(false);
		try {
			final byte[] en_data = Hex.decodeHex(encrypttext.toCharArray());
			final byte[] data = decrypt(keyPair.getPrivate(), en_data);
			return new String(data);
		} catch (final NullPointerException ex) {
			LOGGER.error("keyPair cannot be null.");
		} catch (final Exception ex) {
			LOGGER.error(String.format("\"%s\" Decryption failed. Cause: %s",
					encrypttext, ex.getMessage()));
		}
		return null;
	}

	/**
	 * 使用默认的私钥解密由JS加密（使用此类提供的公钥加密）的字符串。
	 *
	 * @param encrypttext
	 *            密文。
	 * @return {@code encrypttext} 的原文字符串。
	 */
	public static String decryptStringByJs(final String encrypttext) {
		final String text = decryptString(encrypttext);
		if (text == null) {
			return null;
		}
		return StringUtils.reverse(text);
	}

	/**
	 * 使用指定的公钥加密数据。
	 *
	 * @param publicKey
	 *            给定的公钥。
	 * @param data
	 *            要加密的数据。
	 * @return 加密后的数据。
	 */
	public static byte[] encrypt(final PublicKey publicKey, final byte[] data)
			throws Exception {
		final Cipher ci = Cipher.getInstance(ALGORITHOM, DEFAULT_PROVIDER);
		ci.init(Cipher.ENCRYPT_MODE, publicKey);
		return ci.doFinal(data);
	}

	/**
	 * 使用给定的公钥加密给定的字符串。
	 * <p />
	 * 若 {@code publicKey} 为 {@code null}，或者 {@code plaintext} 为 {@code null}
	 * 则返回 {@code null}。
	 *
	 * @param publicKey
	 *            给定的公钥。
	 * @param plaintext
	 *            字符串。
	 * @return 给定字符串的密文。
	 */
	public static String encryptString(final PublicKey publicKey,
			final String plaintext) {
		if (publicKey == null || plaintext == null) {
			return null;
		}
		final byte[] data = plaintext.getBytes();
		try {
			final byte[] en_data = encrypt(publicKey, data);
			return new String(Hex.encodeHex(en_data));
		} catch (final Exception ex) {
			LOGGER.error(ex.getCause().getMessage());
		}
		return null;
	}

	/**
	 * 使用默认的公钥加密给定的字符串。
	 * <p />
	 * 若{@code plaintext} 为 {@code null} 则返回 {@code null}。
	 *
	 * @param plaintext
	 *            字符串。
	 * @return 给定字符串的密文。
	 */
	public static String encryptString(final String plaintext) {
		if (plaintext == null) {
			return null;
		}
		final byte[] data = plaintext.getBytes();
		final KeyPair keyPair = getKeyPair(false);
		try {
			final byte[] en_data = encrypt(keyPair.getPublic(), data);
			return new String(Hex.encodeHex(en_data));
		} catch (final NullPointerException ex) {
			LOGGER.error("keyPair cannot be null.");
		} catch (final Exception ex) {
			LOGGER.error(ex.getCause().getMessage());
		}
		return null;
	}

	/**
	 * 根据给定的系数和专用指数构造一个RSA专用的私钥对象。
	 *
	 * @param modulus
	 *            系数。
	 * @param privateExponent
	 *            专用指数。
	 * @return RSA专用私钥对象。
	 */
	public static RSAPrivateKey generateRSAPrivateKey(final byte[] modulus,
			final byte[] privateExponent) {
		final RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(
				new BigInteger(modulus), new BigInteger(privateExponent));
		try {
			return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
		} catch (final InvalidKeySpecException ex) {
			LOGGER.error("RSAPrivateKeySpec is unavailable.", ex);
		} catch (final NullPointerException ex) {
			LOGGER.error(
					"RSAUtils#KEY_FACTORY is null, can not generate KeyFactory instance.",
					ex);
		}
		return null;
	}

	/**
	 * 根据给定的系数和专用指数构造一个RSA专用的公钥对象。
	 *
	 * @param modulus
	 *            系数。
	 * @param publicExponent
	 *            专用指数。
	 * @return RSA专用公钥对象。
	 */
	public static RSAPublicKey generateRSAPublicKey(final byte[] modulus,
			final byte[] publicExponent) {
		final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
				new BigInteger(modulus), new BigInteger(publicExponent));
		try {
			return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
		} catch (final InvalidKeySpecException ex) {
			LOGGER.error("RSAPublicKeySpec is unavailable.", ex);
		} catch (final NullPointerException ex) {
			LOGGER.error(
					"RSAUtils#KEY_FACTORY is null, can not generate KeyFactory instance.",
					ex);
		}
		return null;
	}

	/** 返回已初始化的默认的私钥。 */
	public static RSAPrivateKey getDefaultPrivateKey() {
		final KeyPair keyPair = getKeyPair(false);
		if (keyPair != null) {
			return (RSAPrivateKey) keyPair.getPrivate();
		}
		return null;
	}

	/** 返回已初始化的默认的公钥。 */
	public static RSAPublicKey getDefaultPublicKey(final boolean falg) {
		final KeyPair keyPair = getKeyPair(falg);
		if (keyPair != null) {
			return (RSAPublicKey) keyPair.getPublic();
		}
		return null;
	}

	/**
	 * 返回RSA密钥对。
	 */
	public static KeyPair getKeyPair(final boolean falg) {
		// 首先判断是否需要重新生成新的密钥对文件
		if (isCreateKeyPairFile() || falg) {
			// 直接强制生成密钥对文件，并存入缓存。
			return generateKeyPair();
		}
		if (oneKeyPair != null) {
			return oneKeyPair;
		}
		return readKeyPair();
	}

	public static PublicKeyPo getPublicKeyMap(final boolean falg) {
		final PublicKeyPo publicKeyMap = new PublicKeyPo();
		final RSAPublicKey rsaPublicKey = getDefaultPublicKey(falg);
		publicKeyMap.setModulus(new String(Hex.encodeHex(rsaPublicKey
				.getModulus().toByteArray())));
		publicKeyMap.setExponent(new String(Hex.encodeHex(rsaPublicKey
				.getPublicExponent().toByteArray())));
		return publicKeyMap;
	}

	/**
	 * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的私钥对象。
	 *
	 * @param modulus
	 *            系数。
	 * @param privateExponent
	 *            专用指数。
	 * @return RSA专用私钥对象。
	 */
	public static RSAPrivateKey getRSAPrivateKey(final String hexModulus,
			final String hexPrivateExponent) {
		if (StringUtils.isBlank(hexModulus)
				|| StringUtils.isBlank(hexPrivateExponent)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("hexModulus and hexPrivateExponent cannot be empty. RSAPrivateKey value is null to return.");
			}
			return null;
		}
		byte[] modulus = null;
		byte[] privateExponent = null;
		try {
			modulus = Hex.decodeHex(hexModulus.toCharArray());
			privateExponent = Hex.decodeHex(hexPrivateExponent.toCharArray());
		} catch (final DecoderException ex) {
			LOGGER.error("hexModulus or hexPrivateExponent value is invalid. return null(RSAPrivateKey).");
		}
		if (modulus != null && privateExponent != null) {
			return generateRSAPrivateKey(modulus, privateExponent);
		}
		return null;
	}

	/**
	 * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的公钥对象。
	 *
	 * @param modulus
	 *            系数。
	 * @param publicExponent
	 *            专用指数。
	 * @return RSA专用公钥对象。
	 */
	public static RSAPublicKey getRSAPublidKey(final String hexModulus,
			final String hexPublicExponent) {
		if (StringUtils.isBlank(hexModulus)
				|| StringUtils.isBlank(hexPublicExponent)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("hexModulus and hexPublicExponent cannot be empty. return null(RSAPublicKey).");
			}
			return null;
		}
		byte[] modulus = null;
		byte[] publicExponent = null;
		try {
			modulus = Hex.decodeHex(hexModulus.toCharArray());
			publicExponent = Hex.decodeHex(hexPublicExponent.toCharArray());
		} catch (final DecoderException ex) {
			LOGGER.error("hexModulus or hexPublicExponent value is invalid. return null(RSAPublicKey).");
		}
		if (modulus != null && publicExponent != null) {
			return generateRSAPublicKey(modulus, publicExponent);
		}
		return null;
	}

	public static void main(final String[] args) {
		final RSAPrivateKey privatekey = RSAUtils.getDefaultPrivateKey();
		final PublicKeyPo publicKeyMap = RSAUtils.getPublicKeyMap(true);
		final RSAPublicKey publickey = RSAUtils.getDefaultPublicKey(true);
		System.out.println(privatekey.toString());
		System.out.println(publickey.toString());
		System.out.println("----------------------------");
		System.out.println(publicKeyMap);
		System.out.println(getRSAPairFilePath());
	}

	/**
	 * 生成并返回RSA密钥对。
	 */
	private static synchronized KeyPair generateKeyPair() {
		try {
			keyPairGen.initialize(KEY_SIZE, new SecureRandom(DateFormatUtils
					.format(new Date(), "yyyyMMddhhmmss").getBytes()));
			oneKeyPair = keyPairGen.generateKeyPair();
			saveKeyPair(oneKeyPair);
			return oneKeyPair;
		} catch (final InvalidParameterException ex) {
			LOGGER.error("KeyPairGenerator does not support a key length of "
					+ KEY_SIZE + ".", ex);
		} catch (final NullPointerException ex) {
			LOGGER.error(
					"RSAUtils#KEY_PAIR_GEN is null, can not generate KeyPairGenerator instance.",
					ex);
		}
		return null;
	}

	/**
	 * 返回生成/读取的密钥对文件的路径。
	 */
	private static String getRSAPairFilePath() {
		final String urlPath = RSAUtils.class.getResource("/").getPath();
		System.out.printf("{---------" + urlPath + "-----------}");
		return (new File(urlPath).getParent() + RSA_PAIR_FILENAME);
	}

	/**
	 * 若需要创建新的密钥对文件则返回 {@code true}，否则 {@code false}。
	 */
	private static boolean isCreateKeyPairFile() {
		// 是否创建新的密钥对文件
		boolean createNewKeyPair = false;
		if (!rsaPairFile.exists() || rsaPairFile.isDirectory()) {
			createNewKeyPair = true;
		}
		return createNewKeyPair;
	}

	// 同步读出保存的密钥对
	private static KeyPair readKeyPair() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = FileUtils.openInputStream(rsaPairFile);
			ois = new ObjectInputStream(fis);
			oneKeyPair = (KeyPair) ois.readObject();
			return oneKeyPair;
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(ois);
			IOUtils.closeQuietly(fis);
		}
		return null;
	}

	/**
	 * 将指定的RSA密钥对以文件形式保存。
	 *
	 * @param keyPair
	 *            要保存的密钥对。
	 */
	private static void saveKeyPair(final KeyPair keyPair) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = FileUtils.openOutputStream(rsaPairFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(keyPair);
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(oos);
			IOUtils.closeQuietly(fos);
		}
	}

	private RSAUtils() {
	}
}