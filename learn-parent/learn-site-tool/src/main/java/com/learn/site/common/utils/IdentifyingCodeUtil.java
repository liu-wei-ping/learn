package com.learn.site.common.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class IdentifyingCodeUtil {
	private static int codeCount = 4;// 定义图片上显示验证码的个数
	private static char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
			'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
			'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z' };
	private static int codeY = 36;
	private static int fontHeight = 35;
	private static int height = 47;// 定义图片的height
	private static int lins = 10;// 干扰线
	private static int width = 90;// 定义图片的width
	private static int xx = 15;

	public static void getCode(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException {

		// 定义图像buffer
		final BufferedImage buffImg = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// Graphics2D gd = buffImg.createGraphics();
		final Graphics2D gd = (Graphics2D) buffImg.getGraphics();
		// Graphics gd = buffImg.getGraphics();
		// 创建一个随机数生成器类
		final Random random = new Random();
		// 将图像填充为白色
		gd.setColor(Color.white);
		gd.fillRect(0, 0, width, height);

		// 创建字体，字体的大小应该根据图片的高度来定。
		final Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
		// final Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
		// 设置字体。
		gd.setFont(font);

		// 画边框。
		gd.setColor(Color.GRAY);
		gd.draw3DRect(0, 0, width - 1, height - 1, true);
		gd.setColor(Color.GRAY);
		for (int i = 0; i < lins; i++) {
			final int x = random.nextInt(width);
			final int y = random.nextInt(height);
			final int xl = random.nextInt(20);
			final int yl = random.nextInt(20);
			gd.drawLine(x, y, x + xl, y + yl);
		}

		// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
		final StringBuffer randomCode = new StringBuffer();
		int red = 0, green = 0, blue = 0, x = 5;

		// 随机产生codeCount数字的验证码。
		for (int i = 0; i < codeCount; i++) {
			// 得到随机产生的验证码数字。
			final String code = String
					.valueOf(codeSequence[random.nextInt(62)]);

			// 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);

			// 用随机产生的颜色将验证码绘制到图像中。
			final int degree = new Random().nextInt() % 30;
			gd.rotate(degree * Math.PI / 180, x, 20);
			gd.setColor(new Color(red, green, blue));
			gd.drawString(code, (i + 1) * xx, codeY);
			gd.rotate(-degree * Math.PI / 180, x, 20);
			x += 30;
			// 将产生的四个随机数组合在一起。
			randomCode.append(code);
		}
		// 将四位数字的验证码保存到Session中。
		final HttpSession session = req.getSession();
		System.out.print(randomCode + "=====================");
		session.setAttribute(Constants.LOGIN_CODE, randomCode.toString());

		// 禁止图像缓存。
		resp.setHeader("Pragma", "no-cache");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setDateHeader("Expires", 0);
		resp.setContentType("image/png");
		// resp.setContentType("image/jpeg");
		gd.dispose();
		// 将图像输出到Servlet输出流中。
		final ServletOutputStream sos = resp.getOutputStream();
		ImageIO.write(buffImg, "png", sos);
		sos.close();
	}

}