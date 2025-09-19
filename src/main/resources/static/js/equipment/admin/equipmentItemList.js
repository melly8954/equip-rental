let equipmentId = null;

const statusFilterConfig = {
    status: {
        label: "상태",
        type: "radio",
        options: ["전체", "AVAILABLE", "RENTED", "REPAIRING", "OUT_OF_STOCK", "LOST"]
    }
};

const statusLabelMap = {
    "전체": "전체",
    "AVAILABLE": "사용 가능",
    "RENTED": "대여 중",
    "REPAIRING": "수리 중",
    "OUT_OF_STOCK": "폐기",
    "LOST": "분실",
};


$(document).ready(function () {
    // URL에서 equipmentId 추출
    const pathParts = window.location.pathname.split("/");
    equipmentId = pathParts[pathParts.indexOf("equipment") + 1];
});

// pageshow 이벤트: 뒤로가기/앞으로가기 시 항상 초기화
window.addEventListener("pageshow", function(event) {
    // 상태 필터 전체로 초기화
    $("input[name='status']").prop("checked", false);
    $("input[name='status'][value='전체']").prop("checked", true);

    // 필터 렌더링
    renderFilter("equipment-filter", statusFilterConfig, function(filters) {
        fetchEquipmentItems(equipmentId, filters, 1);
    });

    // 리스트 초기 조회
    fetchEquipmentItems(equipmentId, {}, 1);
});

function renderFilter(containerId, config, onChange) {
    const container = $("#" + containerId);
    container.empty();

    Object.entries(config).forEach(([key, value]) => {
        const group = $("<div>").addClass("mb-3");

        // 라벨
        if (value.label) {
            group.append($("<label>").addClass("form-label fw-bold me-2").text(value.label));
        }

        // 버튼 그룹 또는 라디오
        const btnGroup = $("<div>").addClass("btn-group").attr("role", "group");

        value.options.forEach(opt => {
            const inputId = key + "-" + (opt ?? "");
            const input = $("<input>")
                .attr("type", value.type)
                .addClass("btn-check")
                .attr("name", key)
                .attr("id", inputId)
                .val(opt)
                .prop("checked", opt === "전체"); // 기본 전체 선택

            const button = $("<label>")
                .addClass("btn btn-outline-primary btn-sm")
                .attr("for", inputId)
                .text(opt);

            input.on("change", () => onChange(getFilterValues(config)));

            btnGroup.append(input, button);
        });

        group.append(btnGroup);
        container.append(group);
    });
}

// 현재 필터 값 가져오기
function getFilterValues(config) {
    const values = {};
    Object.keys(config).forEach(key => {
        const selected = $(`input[name="${key}"]:checked`);
        values[key] = selected.length ? selected.val() : "";
    });
    return values;
}

function fetchEquipmentItems(equipmentId, filters = {}, page = 1) {
    $.ajax({
        url: `/api/v1/equipments/${equipmentId}/items`,
        method: "GET",
        data: {
            equipmentStatus: (filters.status === "전체" ? "" : filters.status),
            page: page
        }
    }).done(function(response) {
        const items = response.data.equipmentItems.content;
        const pageInfo = {
            page: response.data.equipmentItems.page,       // 1-based
            totalPages: response.data.equipmentItems.totalPages,
            first: response.data.equipmentItems.first,
            last: response.data.equipmentItems.last
        };
        const equipmentSummary = response.data.equipmentSummary;

        $("#equipment-summary").html(`
            <div class="card mb-3" style="max-width: 540px;">
                <div class="row g-0">
                    <div class="col-md-4">
                        <img src="${equipmentSummary.imageUrl}" 
                             class="img-fluid rounded-start" alt="${equipmentSummary.model}" id="equipment-image">
                        <div class="mt-2">
                            <button id="change-image-btn" class="btn btn-sm btn-secondary">이미지 변경</button>
                            <input type="file" id="image-input" accept="image/*" style="display:none">
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card-body">
                            <h5 class="card-title">${equipmentSummary.model}</h5>
                            <p class="card-text mb-1"><strong>카테고리:</strong> ${equipmentSummary.category}</p>
                            ${equipmentSummary.subCategory ? `<p class="card-text mb-1"><strong>서브카테고리:</strong> ${equipmentSummary.subCategory}</p>` : ""}
                            <p class="card-text mb-1"><strong>사용 가능한 재고:</strong> ${equipmentSummary.availableStock}</p>
                            <p class="card-text"><strong>총 재고:</strong> ${equipmentSummary.totalStock}</p>
                        </div>
                    </div>
                </div>
            </div>
        `);

        const container = $("#equipment-items");
        container.empty();

        if (!items.length) {
            container.append(`<div class="text-center py-3">해당 장비모델의 아이템이 존재하지 않습니다.</div>`);
            $("#equipment-pagination").empty();
            return;
        }

        // 열 제목
        container.append(`
            <div class="d-flex fw-bold border-bottom border-dark pb-2 mb-2">
                <div class="col-4">Serial Number</div>
                <div class="col-4">Status</div>
                <div class="col-4">Histroy</div>
            </div>
        `);

        items.forEach(item => {
            const statusOptions = ["AVAILABLE", "RENTED", "REPAIRING", "OUT_OF_STOCK", "LOST"]
                .map(opt => `<option value="${opt}" ${opt === item.status ? "selected" : ""}>${statusLabelMap[opt]}</option>`)
                .join("");

            container.append(`
                <div class="d-flex py-1 border-bottom">
                    <div class="col-4">${item.serialNumber || '-'}</div>
                    <div class="col-4">
                        <select class="form-select form-select-sm item-status w-auto"
                                data-id="${item.equipmentItemId}"
                                data-original-status="${item.status}">">
                            ${statusOptions}
                        </select>
                    </div>
                    <div class="col-4">
                        <button class="btn btn-sm btn-primary view-history" data-item-id="${item.equipmentItemId}">보기</button>
                    </div>
                </div>
            `);
        });

        // 페이징 렌더링
        renderPagination("equipment-pagination", pageInfo, (newPage) => {
            fetchEquipmentItems(equipmentId, filters, newPage);
        });
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
}

$(document).on("change", ".item-status", function() {
    const itemId = Number($(this).data("id"));
    const newStatus = $(this).val();

    // 현재 필터와 페이지를 DOM에서 가져오기
    const filters = getFilterValues(statusFilterConfig);
    const page = getCurrentPage("equipment-pagination");

    updateItemStatus(itemId, newStatus, filters, page);
});

function updateItemStatus(itemId, newStatus, filters, page) {
    $.ajax({
        url: `/api/v1/equipment-items/status`, // API 엔드포인트 예시
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({
            equipmentItemId: itemId,
            newStatus: newStatus
        })
    }).done(function(response) {
        alert(response.message);
        fetchEquipmentItems(equipmentId, filters, page);
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
}

$(document).on("click", ".view-history", function() {
    const equipmentItemId = $(this).data("item-id");
    // 아이템 히스토리 페이지로 이동
    window.location.href = `/admin/equipment/${equipmentId}/item/${equipmentItemId}/history`;
});

$(document).on("click", "#change-image-btn", function() {
    $("#image-input").click(); // 숨겨진 input 트리거
});

$(document).on("change", "#image-input", function() {
    const file = this.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("files", file);

    $.ajax({
        url: `/api/v1/equipments/${equipmentId}/image`,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false
    }).done(function(response) {
        alert(response.message);
        fetchEquipmentItems(equipmentId, {}, 1); // 새로고침 없이 데이터 다시 불러오기
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
});