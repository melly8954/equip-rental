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
    const $memberList = $("#member-list");

    // 상태 변경
    $memberList.on("change", ".member-status", function() {
        const memberId = $(this).data("id");
        const newStatus = $(this).val();
        updateMember(memberId, "status", newStatus, getCurrentFilters());
    });

    // 역할 변경
    $memberList.on("change", ".member-role", function() {
        const memberId = $(this).data("id");
        const newRole = $(this).val();
        updateMember(memberId, "role", newRole, getCurrentFilters());
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
        let statusSelect, roleSelect;

        if (member.role === 'ADMIN') {
            // admin 계정은 수정 불가 (조회만 가능)
            statusSelect = `
                <select class="form-select" disabled>
                    <option value="ACTIVE" selected>ACTIVE</option>
                </select>
            `;
            roleSelect = `
                <select class="form-select" disabled>
                    <option value="ADMIN" selected>ADMIN</option>
                </select>
            `;
        } else {
            // 일반 사용자/매니저 계정
            statusSelect = `
                <select class="form-select member-status" data-id="${member.memberId}">
                    <option value="PENDING" ${member.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="ACTIVE" ${member.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="DELETED" ${member.status === 'DELETED' ? 'selected' : ''}>DELETED</option>
                </select>
            `;
            roleSelect = `
                <select class="form-select member-role" data-id="${member.memberId}">
                    <option value="USER" ${member.role === 'USER' ? 'selected' : ''}>USER</option>
                    <option value="MANAGER" ${member.role === 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                </select>
            `;
        }

        const row = $(`
            <div class="d-flex border-bottom py-2 text-center">
                <div class="col-1">${member.memberId}</div>
                <div class="col-2">${member.name}</div>
                <div class="col-2">${member.department}</div>
                <div class="col-3">${member.email}</div>
                <div class="col-2">${statusSelect}</div>
                <div class="col-2">${roleSelect}</div>
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

function updateMember(memberId, type, value, currentFilters = {}) {
    let url = `/api/v1/members/${memberId}/${type}`;
    let body = {};      // 빈 객체 생성

    if (type === "status") {
        body.updateStatus = value; // 서버 필드명에 맞춤
    } else if (type === "role") {
        body.updateRole = value;   // 서버 필드명에 맞춤
    }

    $.ajax({
        url: url,
        type: "PATCH",
        contentType: "application/json",
        data: JSON.stringify(body),
    }).done(function(response) {
        console.log(response);
        // 변경 후 목록 갱신
        loadMembers(currentFilters);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })

}

function getCurrentFilters() {
    return {
        status: $('input[name="status"]:checked').val(),
        role: $('input[name="role"]:checked').val()
    };
}