$(document).ready(function() {
    loadDepartments();
});

function loadDepartments() {
    $.ajax({
        url: "/api/v1/departments", // 백엔드에서 부서 리스트 API
        method: "GET",
        contentType: "application/json"
    }).done(function(response) {
        const select = $("#department");
        response.data.forEach(dept => {
            // value에 department_id, 화면에 department_name
            select.append(`<option value="${dept.departmentId}">${dept.departmentName}</option>`);
        });
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
}

function signUp(){
    // 입력값 가져오기
    const username = $("#username").val().trim();
    const password = $("#password").val().trim();
    const confirmPassword = $("#confirmPassword").val().trim();
    const name = $("#name").val().trim();
    const departmentId = $("#department").val();
    const email = $("#email").val().trim();

    // 간단한 검증
    if (!username || !password || !confirmPassword || !name || !departmentId || !email) {
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
            departmentId: departmentId,
            email: email
        })
    }).done(function(response) {
        console.log(response);
        alert(response.message);
        window.location.href = "/";
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}