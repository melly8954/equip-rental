function login(){
    const username = $('#username').val().trim();
    const password = $('#password').val().trim();

    if (!username || !password) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
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
    })
        .done(function(response) {
            console.log(response);
            alert(response.message);
            window.location.href = "/equipment";
        })
        .fail(function(jqXHR) {
            handleServerError(jqXHR);
        });
}