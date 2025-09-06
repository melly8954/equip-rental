const filterConfig = {
    status: {
        label: "상태",
        type: "radio",
        options: ["전체", "PENDING", "ACTIVE", "DELETED"]
    },
    role: {
        label: "역할",
        type: "radio",
        options: ["전체", "USER", "MANAGER"]
    }
};

$(document).ready(function () {
    renderFilter("filter-container", filterConfig, (filters) => {
        loadMembers(filters);
    });

    loadMembers(); // 초기 로드

    // 상태 변경
    $(".member-status").on("change", function() {
        const memberId = $(this).data("id");
        const newStatus = $(this).val();
        updateMember(memberId, "status", newStatus);
    });

// 역할 변경
    $(".member-role").on("change", function() {
        const memberId = $(this).data("id");
        const newRole = $(this).val();
        updateMember(memberId, "role", newRole);
    });
});

function loadMembers(filters = {}) {
    $.ajax({
        url: "/api/v1/members",
        type: "GET",
        data: filters,
    }).done(function(response) {
        console.log(response);
        renderMemberList(response, filters)
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })
}

function renderMemberList(response, filters = {}) {
    const memberList = response.data.content;
    const $container = $("#member-list");
    $container.empty(); // 기존 내용 초기화

    if (!memberList || memberList.length === 0) {
        $container.append("<div>사용자가 없습니다.</div>");
        return;
    }

    memberList.forEach(member => {
        const row = $(`
                <div class="d-flex border-bottom py-2 text-center">
                    <div class="col-1">${member.memberId}</div>
                    <div class="col-2">${member.name}</div>
                    <div class="col-2">${member.department}</div>
                    <div class="col-3">${member.email}</div>
                    <div class="col-2">
                        <select class="form-select member-status" data-id="${member.memberId}">
                            <option value="PENDING" ${member.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="ACTIVE" ${member.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                            <option value="DELETED" ${member.status === 'DELETED' ? 'selected' : ''}>DELETED</option>
                        </select>
                    </div>
                    <div class="col-2">
                        <select class="form-select member-role" data-id="${member.memberId}">
                            <option value="USER" ${member.role === 'USER' ? 'selected' : ''}>USER</option>
                            <option value="MANAGER" ${member.role === 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                        </select>
                    </div>
                </div>
            `);
        $container.append(row);
    });

    // 페이징 렌더링
    renderPagination("member-pagination", {
        page: response.data.page,
        totalPages: response.data.totalPages,
        first: response.data.first,
        last: response.data.last
    }, (newPage) => {
        loadMembers({ ...filters, page: newPage });
    });
}

// 상태, 역할 변경
function updateMember(memberId, type, value) {
    let url = `/api/members/${memberId}/${type}`;
    let body = {};      // 빈 객체 생성
    body[type] = value;     // type 을 문자열 key 로 사용해서 value 선언

    $.ajax({
        url: url,
        type: "PATCH",
        contentType: "application/json",
        data: JSON.stringify(body),
        success: function(response) {
            alert(type.toUpperCase() + "가 변경되었습니다.");
        },
        error: function(xhr) {
            alert(type.toUpperCase() + " 변경 실패: " + xhr.responseText);
        }
    });
}
