$(function() {
	var login_url = "sign.shtml";
	var return_url = "../index/index.shtml"
	var loginvalid = false;
//	document.write("<script src='../js/security.js'></script>");

	function getPicCode() {
		$("#code-img").attr("src", "code.shtml?rnd=" + Math.random());
	}
	$('#code-link').on("click", function() {
		getPicCode();
	})
	$('#login-btn').on('click', function() {
		login();
	});

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
})
