let filterConfig = {}; // 전역 필터 객체

$(document).ready(function() {
    // 이름 검색(input)
    $("#member-search").on("input", function() {
        fetchRentalItemList(getFilterValues(filterConfig));
    });
});

// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", async function (event) {
    // 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='subCategory']").prop("checked", false);
    $("#member-search").val("");
    $("#department").val("");

    // 필터 구성 가져오기
    await fetchDepartments();

    filterConfig = await fetchFilterConfig();
    if (filterConfig) {
        renderFilter("category-filters", {category: filterConfig.category}, onFilterChange);
        $("#sub-category-filters").hide();
    }

    // 초기 데이터 로딩
    fetchRentalItemList();
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
        select.off("change").on("change", () => fetchRentalItemList(getFilterValues()));
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
                .addClass("btn btn-outline-primary")
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
    fetchRentalItemList(getFilterValues());
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

    renderFilter("sub-category-filters", { subCategory: { type: "radio", options } }, () => {
        fetchRentalItemList(getFilterValues());
    });

    // 기본 체크 처리
    $(`#sub-category-filters input[value=""]`).prop("checked", true);
}

// 대여 신청내역 조회 AJAX
function fetchRentalItemList(filters = {}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const memberSearch = $("#member-search").val();
    const params = {
        departmentId: filters.department,
        categoryId: filters.category,
        subCategoryId: filters.subCategory,
        memberName: memberSearch || filters.memberName || "",
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/rental-items",
        method: "GET",
        data: params,
    }).done(function (response) {
        renderRentalItemList(response.data.content);
        renderPagination("pagination-container", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchRentalItemList({...filterValues, page: newPage});
        });
    }).fail(function (xhr) {
        handleServerError(xhr)
    })
}

// div 리스트 렌더링
function renderRentalItemList(data) {
    const container = $("#rental-item-list");
    container.empty();

    if (!data || data.length === 0) {
        container.append(`<div class="text-center py-3">사용자들의 장비 대여 물품내역이 없습니다.</div>`);
        return;
    }

    let row = $('<div class="row"></div>');

    data.forEach((r, index) => {
        const overdueBadge = r.overdue
            ? `<span class="badge bg-danger ms-2">연체</span>`
            : '';

        const extendedBadge = r.isExtended
            ? `<span class="badge bg-warning text-dark ms-1">연장</span>`
            : '';

        const thumbnail = r.thumbnailUrl
            ? `<img src="${r.thumbnailUrl}" class="img-fluid rounded-start" alt="${r.model}" style="width:100px; height:100px; object-fit:cover;">`
            : `<div class="placeholder-thumbnail d-flex align-items-center justify-content-center bg-light rounded-start" 
                   style="width:100px; height:100px;">No Image</div>`;

        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto">
                            ${thumbnail}
                        </div>
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="d-flex align-items-center mb-1 fw-bold">Rental ${r.rentalId}</p>
                                    <p class="d-flex align-items-center mb-1 fw-bold">
                                      <span>${r.model}</span>
                                      <span class="text-muted ms-2">[${r.category} - ${r.subCategory}]</span>
                                    </p> 
                                    <i class="mb-0 text-muted">${r.serialName}</i>
                                </h6>
                                <p class="card-text mb-1">
                                    대여자: ${r.memberName} (${r.department})
                                </p>
                                <p class="card-text mb-0 text-muted">
                                    대여기간: ${r.startDate} ~ ${r.endDate} 
                                    ${r.actualReturnDate ? `(반납: ${r.actualReturnDate})` : ''}
                                    ${overdueBadge} ${extendedBadge}
                                </p>
                            </div>
                        </div>
                        <div class="col-auto pe-2">
                            ${!r.actualReturnDate
                            ? `<button class="btn btn-sm btn-success return-btn" data-id="${r.rentalItemId}">반납처리</button>`
                            : `<span class="text-success small">반납완료</span>`}
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 2개마다 container에 추가 후 새로운 row 시작
        if ((index + 1) % 2 === 0 || index === data.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });

    // 반납 처리 버튼 이벤트 바인딩
    $(".return-btn").off("click").on("click", function() {
        const rentalItemId = $(this).data("id");
        handleReturn(rentalItemId, $(this));
    });
}

// 반납 처리 예시 함수
function handleReturn(rentalItemId, button) {
    $.ajax({
        url: `/api/v1/rental-items/${rentalItemId}`,
        method: "PATCH",
    }).done(() => {
        button.replaceWith(`<span class="text-success small">반납완료</span>`);
    }).fail(xhr => {
        alert("반납 처리 실패: " + xhr.responseText);
    });
}