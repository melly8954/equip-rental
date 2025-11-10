function logout() {
    $.ajax({
        url: "/api/v1/auth/logout",
        method: "POST"
    }).done(function(response){
        showSnackbar(response.message);
        window.location.href = "/"; // 로그아웃 후 홈으로 이동
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}