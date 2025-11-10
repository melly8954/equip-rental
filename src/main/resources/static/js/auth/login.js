function login(){
    const username = $('#username').val().trim();
    const password = $('#password').val().trim();

    if (!username || !password) {
        showSnackbar('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }

    $.ajax({
        url: "/api/v1/auth/login",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            username: username,
            password: password,
        })
    }).done(function(response) {
        showSnackbar(response.message);
        window.location.href = "/home";
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}