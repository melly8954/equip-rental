// 페이지 조회
let selectedRentalId = null;
let selectedEquipmentId = null;
let filterConfig = {}; // 전역 필터 객체

$(document).ready(function() {
    // 검색 이벤트
    $("#member-search").on("input", function() {
        fetchRentalList(getFilterValues());
    });
});

// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", async function () {
    // 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='subCategory']").prop("checked", false);
    $("#member-search").val("");
    $("#department").val("");

    // 필터 구성 가져오기
    filterConfig = await fetchFilterConfig();
    if (filterConfig) {
        renderFilter("category-filters", { category: filterConfig.category }, onFilterChange);
        $("#sub-category-filters").hide();
    }

    // 부서 필터 가져오기
    await fetchDepartments();

    fetchRentalList();
});

// 필터 구성 가져오기 (카테고리 + 서브카테고리)
async function fetchFilterConfig() {
    try {
        const resp = await $.getJSON('/api/v1/categories');
        const categories = resp.data;

        return {
            category: {
                label: "카테고리",
                type: "radio",
                options: [{ id: null, label: "전체" }, ...categories.map(c => ({ id: c.categoryId, label: c.label }))]
            },
            subCategory: {
                label: "서브카테고리",
                type: "radio",
                options: []
            }
        };
    } catch (e) {
        console.error("필터 구성 불러오기 실패", e);
        return { category: { options: [] }, subCategory: { options: [] } };
    }
}

// 부서 필터 렌더링
async function fetchDepartments() {
    try {
        const response = await $.ajax({
            url: "/api/v1/departments",
            method: "GET",
            contentType: "application/json"
        });

        const select = $("#department");
        select.empty();
        select.append(`<option value="">전체</option>`); // 전체 옵션
        response.data.forEach(d => select.append(`<option value="${d.departmentId}">${d.departmentName}</option>`));

        // 변경 시 필터 적용
        select.off("change").on("change", () => fetchRentalList(getFilterValues()));
    } catch (e) {
        console.error("부서 데이터 불러오기 실패", e);
    }
}

// 필터 렌더링 (카테고리, 서브카테고리)
function renderFilter(containerId, config, onChange) {
    const container = $("#" + containerId);
    container.empty();

    Object.entries(config).forEach(([key, value]) => {
        if (!value.options || value.options.length === 0) {
            container.hide();
            return;
        } else {
            container.show();
        }

        const group = $("<div>").addClass("mb-3");

        const groupLabel = $(`<div class="mb-2 fw-semibold">${value.label}</div>`);
        group.append(groupLabel);

        const btnGroup = $("<div>").addClass("btn-group w-100").attr("role", "group");

        value.options.forEach(opt => {
            const inputId = key + "-" + (opt.id ?? opt.label);
            const input = $("<input>")
                .attr("type", value.type)
                .addClass("btn-check")
                .attr("name", key)
                .attr("id", inputId)
                .val(opt.id ?? "")
                .prop("checked", opt.id === null); // 전체 체크

            const button = $("<label>")
                .addClass("filter-pill-btn")
                .attr("for", inputId)
                .text(opt.label);

            input.off("change").on("change", () => onChange(getFilterValues()));

            btnGroup.append(input, button);
        });

        group.append(btnGroup);
        container.append(group);
    });
}

// 필터 값 가져오기
function getFilterValues() {
    const values = {};

    // 카테고리 / 서브카테고리 (radio 버튼)
    Object.keys(filterConfig).forEach(key => {
        const selected = $(`input[name="${key}"]:checked`);
        let val = "";
        if (selected.length) {
            val = selected.val();
            // null 체크 후 숫자로 변환
            val = val !== "" ? Number(val) : null;
        }
        values[key] = val;
    });

    // 부서 select
    const deptVal = $("#department").val();
    values.department = deptVal ? Number(deptVal) : null;

    // 검색어
    values.memberName = $("#member-search").val() || "";

    return values;
}

// 필터 변경 시
async function onFilterChange() {
    const categoryId = getFilterValues().category;
    await updateSubCategoryOptions(categoryId);
    fetchRentalList(getFilterValues());
}

// 서브카테고리 업데이트
async function updateSubCategoryOptions(parentCategoryId) {
    let options = [];
    if (parentCategoryId) {
        try {
            const resp = await $.getJSON(`/api/v1/categories/${parentCategoryId}/sub-categories`);
            options = resp.data.map(sc => ({ id: sc.subCategoryId, label: sc.label }));
            options.unshift({ id: null, label: "전체" });
        } catch (e) {
            console.error("서브카테고리 불러오기 실패", e);
        }
    }

    filterConfig.subCategory.options = options;

    if (options.length > 1) {
        $("#sub-category-filters").show();
    } else {
        $("#sub-category-filters").hide();
    }

    renderFilter("sub-category-filters", { subCategory: { label: "서브카테고리", type: "radio", options } }, () => {
        fetchRentalList(getFilterValues());
    });

    // 기본 체크 처리
    $(`#sub-category-filters input[value=""]`).prop("checked", true);
}

// 대여 신청내역 조회
function fetchRentalList(filters = {}) {
    const filterValues = filters || getFilterValues();
    const params = {
        departmentId: filterValues.department || null,
        categoryId: filterValues.category || null,
        subCategoryId: filterValues.subCategory || null,
        memberName: filterValues.memberName
    };
    if (filterValues.page) params.page = filterValues.page;

    $.ajax({
        url: "/api/v1/rentals",
        method: "GET",
        data: params
    }).done(resp => {
        renderRentalList(resp.data.content);
        renderPagination("pagination", {
            page: resp.data.page,
            totalPages: resp.data.totalPages,
            first: resp.data.first,
            last: resp.data.last
        }, newPage => fetchRentalList({ ...filterValues, page: newPage }));
    }).fail(xhr => handleServerError(xhr));
}

// 리스트 렌더링
function renderRentalList(data) {
    const container = $("#rental-list");
    container.empty();

    if (!data || data.length === 0) {
        container.append(`<div class="text-center py-3">사용자들의 장비 대여 신청 내역이 존재하지 않습니다.</div>`);
        return;
    }

    // 행(row) 컨테이너 추가
    const row = $('<div class="row row-cols-5 g-3"></div>');

    data.forEach(r => {
        const card = $(`
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <div class="card-body d-flex flex-column">
                        <p class="d-flex align-items-center mb-1 fw-bold">대여 ID ${r.rentalId}</p>
                        <div class="mb-2 text-center">
                            <img src="${r.thumbnailUrl}" class="img-fluid rounded" alt="대표 이미지" style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <h6 class="card-title fw-bold">${r.model}</h6>
                        <p class="card-subtitle text-muted small mb-2">
                            ${r.category} / ${r.subCategory}
                        </p>
                        <p class="card-text small flex-grow-1">
                            수량: ${r.quantity} <br>
                            기간: ${r.requestStartDate || ""} ~ ${r.requestEndDate || ""} <br>
                            신청자: ${r.name} (${r.department}) <br>
                            사유: ${(r.rentalReason && r.rentalReason.length > 20)
                                ? r.rentalReason.substring(0, 20) + "..."
                                : (r.rentalReason || "-")}
                        </p>
                        <div class="d-flex gap-2 mt-auto">
                            <button class="btn btn-success btn-sm btn-approve" 
                                data-rental-id="${r.rentalId}" 
                                data-equipment-id="${r.equipmentId}">
                                승인
                            </button>
                            <button class="btn btn-danger btn-sm btn-reject" 
                                data-rental-id="${r.rentalId}" 
                                data-equipment-id="${r.equipmentId}">
                                거절
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `);
        row.append(card);
    });

    container.append(row);
}


// 승인 버튼
$(document).on("click", ".btn-approve", function () {
    const rentalId = $(this).data("rental-id");
    const equipmentId = $(this).data("equipment-id");

    $.ajax({
        url: `/api/v1/rentals/${rentalId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({ equipmentId, newStatus: "APPROVED" })
    }).done(resp => {
        showSnackbar(resp.message);
        fetchRentalList(getFilterValues());
    }).fail(xhr => handleServerError(xhr));
});

// 거절 버튼
$(document).on("click", ".btn-reject", function () {
    selectedRentalId = $(this).data("rental-id");
    selectedEquipmentId = $(this).data("equipment-id");

    const modal = new bootstrap.Modal(document.getElementById("rejectReasonModal"));
    modal.show();
});

// 거절 확정
$("#confirm-reject").on("click", function () {
    const reasonSelect = $("#reject-reason").val();
    const reasonText = $("#reject-reason-text").val();
    const rejectReason = reasonSelect === "기타" ? reasonText : reasonSelect;

    if (!rejectReason) return showSnackbar("거절 사유를 입력해주세요.");

    $.ajax({
        url: `/api/v1/rentals/${selectedRentalId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({ equipmentId: selectedEquipmentId, newStatus: "REJECTED", rejectReason })
    }).done(resp => {
        showSnackbar(resp.message);
        fetchRentalList(getFilterValues());

        const modal = bootstrap.Modal.getInstance(document.getElementById("rejectReasonModal"));
        modal.hide();

        $("#reject-reason").val("");
        $("#reject-reason-text").val("");
    }).fail(xhr => handleServerError(xhr));
});
