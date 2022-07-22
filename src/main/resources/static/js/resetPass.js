function forget() {
    var email = document.getElementById("your-email").value;
    console.log(email);
    $.get(
        "/getCode",
        {"email": email},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#codebutton").attr("disabled", "disabled");
                document.getElementById("your-email").value = email;
                alert("发送成功！请前往邮箱查看验证码，有效时间为 5分钟.")
            } else {
                alert("发送验证码失败，请稍后重新发送！");
            }
        }
    );
}