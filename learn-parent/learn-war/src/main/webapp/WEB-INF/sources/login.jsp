<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--[if lt IE 7 ]> <html lang="en" class="ie6 ielt8"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="ie7 ielt8"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="ie8"> <![endif]-->
<!--[if (gte IE 9)|!(IE)]><!-->
<html lang="en">
<!--<![endif]-->
<head>
<meta charset="utf-8">
<title>登录页面</title>
<link rel='stylesheet' type='text/css'
	href="${pageContext.request.contextPath}/css/login.css" />
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/jquery.min.js"></script>


</head>
<body>
	<div class="container">
		<section id="content">
<%-- 		<input type="text" id="code"  value="${sessionScope['login_code']}"/> --%>
		<form id="login-form" onSubmit="return false;" method="post">
			<h1>Learn Site</h1>
			<em id="err"></em>
			<div>
				<input type="text" class="login-input" id="username" oninput="errorFun('',false)" name="username" placeholder="用户名"
					required />
			</div>
			<div>
				<input type="password" class="login-input" id="password"
					name="password" oninput="errorFun('',false)" placeholder="密码"  required />
			</div>
			<div>
				<input type="text" class="code-input" id="code" name="code" placeholder="验证码" oninput="errorFun('',false)"
					onBlur="if (this.value == '') {this.placeholder = '验证码';}"
					onFocus="this.placeholder = '';" required /><a href="#"
					id="code-link"><img class="refresh-code" id="code-img" src="code.shtml"
					 alt="点击刷新"></a>
			</div>
			<div>
<!-- 				<input style="display: block;" id="submit" type="submit" /> -->
				 <a href="#" class="login-btn" id="login-btn">登 录</a> 
				 <a href="#"
					class="link-btn">Lost your password?</a><a href="#"
					class="link-btn">Register</a>
			</div>
		</form>
		<!-- form -->
		<div class="button">
			<a href="#">Download source file</a>
		</div>
		<!-- button --> </section>
		<!-- content -->
	</div>
	<!-- container -->
</body>
<script type="text/javascript">
	document.write("<script language=\"javascript\" src=\"../js/security.js\" > <\/script>");
	var login_url = "sign.shtml";
	var return_url = "../index/index.shtml"
	function getPicCode() {
		$("#code-img").attr("src", "code.shtml?rnd=" + Math.random());
	}
	$('#code-link').on("click", function() {
		getPicCode();
	})
	$('#login-btn').on('click', function() {
		login();
	});
<%-- 	var id = '<%=Session["login_code"] %>'; --%>
// 	alert(id);
	// function check(input, errormsg) {
	// if (input.value == "") {
	// loginvalid = false;
	// input.setCustomValidity(errormsg);
	// } else {
	// input.setCustomValidity('');
	// }
	// console.log(loginvalid);
	// }
	// function clearCustomValidity(input) {
	// input.setCustomValidity('');
	// loginvalid = true;
	// }
	function login() {
		if (!checkParams()) {
			return
		}
		;
		var publicKey = RSAUtils.getKeyPair('${key.exponent}', '',
				'${key.modulus}');
		console.log("---1");
		var params = {};
		var fields = $('#login-form').serializeArray();
		$.each(fields, function(i, field) {
			var key = field.name;
			var value = key != "password" ? $.trim(field.value) : RSAUtils
					.encryptedString(publicKey, $.trim(field.value));
			console.log(key + "=" + value);
			params[key] = value;
		});
		console.log("---:" + params);
		$.ajax({
			url : login_url,
			type : 'POST',
			data : JSON.stringify(params),
			contentType : 'application/json;charset=utf-8',
			dataType : 'json',
			success : function(data) {
				if (data.success) {
					setTimeout(function() {
						location.href = return_url;
					}, 1000);
				} else {
					getPicCode();
					$("#password").val('');
					$("#code").val('');
				}
			},
			error : function(data) {
			}
		});
	}

	function checkParams() {
		var f1, f2, f3;
		var username = $("#username").val();
		f1 = errorFun("请填写用户名！", username == "")
		if (!f1) {
			return f1;
		}
		var password = $("#password").val();
		f2 = errorFun("请填写密码！", password == "")
		if (!f2) {
			return f2;
		}
		var code = $("#code").val();
		f3 = errorFun("请填写验证码！", code == "")
		if (!f3) {
			return f3;
		}
		return f1 && f2 && f3;
	}
function errorFun(errorMsg, flag) {
		if (flag) {
			$("#err").html("<font>" + errorMsg + "</font>");
			return false;
		} else {
			$("#err").html("");
			return true;
		}
	}
</script>

</html>