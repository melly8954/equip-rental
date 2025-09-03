function signUp(){
    // 입력값 가져오기
    const username = $("#username").val().trim();
    const password = $("#password").val().trim();
    const confirmPassword = $("#confirmPassword").val().trim();
    const name = $("#name").val().trim();
    const department = $("#department").val().trim();
    const email = $("#email").val().trim();

    // 간단한 검증
    if (!username || !password || !confirmPassword || !name || !department || !email) {
        alert("모든 항목을 입력해주세요.");
        return;
    }

    $.ajax({
        url: "/api/v1/members",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            username: username,
            password: password,
            confirmPassword: confirmPassword,
            name: name,
            department: department,
            email: email
        })
    })
        .done(function(response) {
            console.log(response);
            alert(response.message);
            window.location.href = "/";
        })
        .fail(function(jqXHR) {
            handleServerError(jqXHR);
        });
}