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

const statusLabelMap = {
    PENDING: "대기 중",
    ACTIVE: "활성화",
    DELETED: "탈퇴"
};

const roleLabelMap = {
    USER: "사용자",
    MANAGER: "매니저",
    ADMIN: "관리자"
};

let categoryList = []; // 전역으로 선언
let memberData = [];

$(document).ready(function () {
    // 카테고리 가져오기
    $.getJSON("/api/v1/categories")
        .done(function(response) {
            categoryList = response.data;
        })
        .fail(function(jqXHR) {
            handleServerError(jqXHR);
        });

    renderFilter("filter-container", filterConfig, (filters) => {
        loadMembers(filters);
    });

    loadMembers(); // 초기 로드
    const $memberList = $("#member-list");

    // 상태 변경
    $memberList.on("change", ".member-status", function() {
        const memberId = $(this).data("id");
        const newStatus = $(this).val();
        updateMember(memberId, "status", newStatus, getFilterValues(filterConfig));
    });

    // 역할 변경
    $memberList.on("change", ".member-role", function() {
        const memberId = $(this).data("id");
        const newRole = $(this).val();
        updateMember(memberId, "role", newRole, getFilterValues(filterConfig));
    });
    // 필터 초기화
    $('#reset-filters').on('click', function() {
        // 각 필터 그룹에서 "전체" 값 선택
        Object.keys(filterConfig).forEach(key => {
            $(`input[name="${key}"][value="전체"]`).prop('checked', true);
        });

        // select 필터가 있다면 초기화
        $('.member-status, .member-role').each(function() {
            $(this).val($(this).find('option:first').val());
        });

        loadMembers();
    });
});

// 필터 UI 렌더링
function renderFilter(containerId, filterConfig, onChangeCallback) {
    const $container = $("#" + containerId);
    $container.empty();

    for (const key in filterConfig) {
        const config = filterConfig[key];

        // 그룹 div
        const $filterGroup = $("<div>").addClass("mb-3");

        // 상단 라벨
        const $groupLabel = $(`<div class="mb-2 fw-semibold">${config.label}</div>`);
        $filterGroup.append($groupLabel);

        // 버튼 그룹
        const $btnGroup = $("<div>").addClass("btn-group").attr("role", "group");

        config.options.forEach(option => {
            const inputId = `filter-${key}-${option}`;

            const $input = $("<input>")
                .attr("type", "radio")
                .addClass("btn-check")
                .attr("name", key)
                .attr("id", inputId)
                .val(option)
                .prop("checked", option === "전체");

            // 한글 라벨 적용
            let labelText = option; // 기본값
            if (key === 'status') {
                labelText = statusLabelMap[option] || option;
            } else if (key === 'role') {
                labelText = roleLabelMap[option] || option;
            }

            const $label = $("<label>")
                .addClass("filter-pill-btn") // 버튼 스타일
                .attr("for", inputId)
                .text(labelText);

            $btnGroup.append($input, $label);
        });

        // 변경 이벤트
        $btnGroup.on("change", "input", function() {
            const values = getFilterValues(filterConfig);
            onChangeCallback(values);
        });

        $filterGroup.append($btnGroup);
        $container.append($filterGroup);
    }
}

// 현재 필터 UI에서 선택된 값 가져오기
function getFilterValues(filterConfig) {
    const values = {};
    for (const key in filterConfig) {
        const selected = $(`input[name="${key}"]:checked`).val();
        if (selected && selected !== "전체") {
            values[key] = selected;
        }
    }
    return values;
}

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
    memberData = response.data.content;
    const memberList = response.data.content;
    const $container = $("#member-list");
    $container.empty(); // 기존 내용 초기화

    if (!memberList || memberList.length === 0) {
        $container.append("<div>사용자가 없습니다.</div>");
        return;
    }

    memberList.forEach(member => {
        let statusSelect, roleSelect, scopeSelect;

        if (member.role === 'ADMIN') {
            // admin 계정은 수정 불가 (조회만 가능)
            statusSelect = `
                <select class="form-select" disabled>
                    <option value="ACTIVE" selected>활성화</option>
                </select>
            `;
            roleSelect = `
                <select class="form-select" disabled>
                    <option value="ADMIN" selected>관리자</option>
                </select>
            `;
        } else {
            // 일반 사용자/매니저 계정
            statusSelect = `
                <select class="form-select member-status" data-id="${member.memberId}">
                    <option value="PENDING" ${member.status === 'PENDING' ? 'selected' : ''}>${statusLabelMap['PENDING']}</option>
                    <option value="ACTIVE" ${member.status === 'ACTIVE' ? 'selected' : ''}>${statusLabelMap['ACTIVE']}</option>
                    <option value="DELETED" ${member.status === 'DELETED' ? 'selected' : ''}>${statusLabelMap['DELETED']}</option>
                </select>
            `;
            roleSelect = `
                <select class="form-select member-role" data-id="${member.memberId}">
                    <option value="USER" ${member.role === 'USER' ? 'selected' : ''}>${roleLabelMap['USER']}</option>
                    <option value="MANAGER" ${member.role === 'MANAGER' ? 'selected' : ''}>${roleLabelMap['MANAGER']}</option>
                </select>
            `;
        }

        if (member.role === 'MANAGER') {
            scopeSelect = `<button class="btn btn-sm btn-primary edit-scope" data-id="${member.memberId}">접근 범위 관리</button>`;
        } else {
            scopeSelect = `<span>-</span>`;
        }

        const row = $(`
            <div class="d-flex border-bottom py-2 text-center">
                <div class="col-1">${member.memberId}</div>
                <div class="col-2">${member.name}</div>
                <div class="col-2">${member.department}</div>
                <div class="col-3">${member.email}</div>
                <div class="col-1">${statusSelect}</div>
                <div class="col-1">${roleSelect}</div>
                <div class="col-2">${scopeSelect}</div> 
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

$(document).on("click", ".edit-scope", function() {
    const memberId = $(this).data("id");
    const member = memberData.find(m => m.memberId === memberId); // 현재 멤버 정보
    const modalBody = $("#scopeModalBody");
    modalBody.empty();

    categoryList.forEach(cat => {
        const checked = member.categories?.map(String).includes(String(cat.categoryId)) ? 'checked' : '';
        modalBody.append(`
            <div class="form-check">
                <input class="form-check-input" type="checkbox" value="${cat.categoryId}" ${checked}>
                <label class="form-check-label">${cat.label}</label>
            </div>
        `);
    });

    $("#saveScopeBtn").data("id", memberId);
    const modal = new bootstrap.Modal(document.getElementById('scopeModal'));
    modal.show();
});

$("#saveScopeBtn").on("click", function() {
    const memberId = $(this).data("id");
    const categoryIds = $("#scopeModalBody input:checked").map((i, el) => $(el).val()).get();

    updateManagerScope(memberId, categoryIds);
    bootstrap.Modal.getInstance(document.getElementById('scopeModal')).hide();
});

function updateManagerScope(memberId, categoryIds) {
    $.ajax({
        url: `/api/v1/manager-scopes`,
        type: "POST", // 처음 등록 시에는 POST, 기존에 있으면 서버에서 처리 가능
        contentType: "application/json",
        data: JSON.stringify({
            managerId: memberId,
            categoryIds : categoryIds
        }),
    }).done(function(response) {
        showSnackbar("스코프가 변경되었습니다.");
        loadMembers(getFilterValues(filterConfig)); // 갱신
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}