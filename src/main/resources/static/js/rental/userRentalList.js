let filterConfig;

// 상태 코드 → 한글 + 색상 클래스
const rentalStatusMap = {
    PENDING: { label: "대기 중", class: "text-warning" },    // 주황
    APPROVED: { label: "대여 승인", class: "text-success" },  // 초록
    REJECTED: { label: "대여 거절", class: "text-danger" },   // 빨강
    COMPLETED: { label: "반납 완료", class: "text-success" },
};

$(document).ready(function() {
    // 이름 검색(input)
    $("#member-search").on("input", function() {
        fetchRentalList(getFilterValues(filterConfig));
    });
    // 필터 초기화
    $('#reset-filters').on('click', function() {
        $('#category-filters input[type=radio][value=""]').prop('checked', true);
        $('#sub-category-filters input[type=radio][value=""]').prop('checked', true);
        filterConfig.subCategory.options = [];
        $("#sub-category-filters").hide();

        fetchRentalList(getFilterValues(filterConfig));
    });
});


// 페이지 로드 또는 뒤로/앞으로가기 시
window.addEventListener("pageshow", async function (event) {
    $("input[name='category']").prop("checked", false);
    $("input[name='subCategory']").prop("checked", false);
    $("input[name='status']").prop("checked", false);

    // 필터 구성 가져오기
    const serverFilter = await fetchFilterConfig();
    filterConfig = {
        ...serverFilter,
        status: {
            label: "",
            type: "radio",
            options: [
                { id: "PENDING", label: "대기 중", default: true },
                { id: "APPROVED", label: "대여 승인" },
                { id: "REJECTED", label: "대여 거절" },
                { id: "COMPLETED", label: "반납 완료" },
            ]
        }
    };

    renderFilter("category-filters", { category: filterConfig.category }, onFilterChange);
    $("#sub-category-filters").hide();

    renderFilter("status-filters", { status: filterConfig.status }, () => {
        fetchRentalList(getFilterValues(filterConfig));
    });

    fetchRentalList(getFilterValues(filterConfig));
});

// 필터 구성 가져오기
async function fetchFilterConfig() {
    try {
        const categoryResp = await $.getJSON('/api/v1/categories');
        const categories = categoryResp.data;

        return {
            category: {
                label: "카테고리",
                type: "radio",
                options: [
                    { id: null, label: "전체", default: true }, // 전체 추가
                    ...categories.map(c => ({ id: c.categoryId, label: c.label }))
                ]
            },
            subCategory: {
                label: "서브카테고리",
                type: "radio",
                options: []
            }
        };
    } catch (e) {
        console.error("필터 구성 불러오기 실패", e);
        return null;
    }
}

// 필터 렌더링
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
                .prop("checked", opt.default === true)

            const button = $("<label>")
                .addClass("filter-pill-btn")
                .attr("for", inputId)
                .text(opt.label);

            input.on("change", () => onChange(getFilterValues(filterConfig)));

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

// 필터 변경 시 동작
async function onFilterChange() {
    const categoryId = getFilterValues(filterConfig).category;
    await updateSubCategoryOptions(categoryId); // 서버 fetch 후 렌더링
    fetchRentalList(getFilterValues(filterConfig));
}

// 서브카테고리 업데이트
async function updateSubCategoryOptions(categoryId) {
    let options = [];
    if (categoryId) {
        try {
            const resp = await $.getJSON(`/api/v1/categories/${categoryId}/sub-categories`);
            options = options.concat(resp.data.map(sc => ({id: sc.subCategoryId, label: sc.label})));
            options.unshift({ id: null, label: "전체" });
        } catch (e) {
            console.error("서브카테고리 불러오기 실패", e);
        }
    }

    filterConfig.subCategory.options = options;

    const container = $("#sub-category-filters");
    container.empty();

    if (options.length > 1) container.show();
    else container.hide();

    renderFilter("sub-category-filters", {subCategory: {label: "서브카테고리", type: "radio", options}}, (values) => {
        // 카테고리 값 포함해서 fetch
        const combinedFilters = {
            category: getFilterValues(filterConfig).category,
            subCategory: values.subCategory,
            status: getFilterValues(filterConfig).status
        };
        fetchRentalList(combinedFilters);
    });
}


// 대여 신청내역 조회 AJAX
function fetchRentalList(filters={}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const params = {
        categoryId: filterValues.category,
        subCategoryId: filterValues.subCategory,
        rentalStatus: filterValues.status,
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/rentals/me",
        method: "GET",
        data: params,
    }).done(function (response){
        renderRentalList(response.data.content);
        renderPagination("pagination-container", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchRentalList({...filterValues, page: newPage});
        });
    }).fail(function (xhr) {
        handleServerError(xhr)
    })
}

// div 리스트 렌더링
function renderRentalList(data) {
    const container = $("#rental-list");
    container.empty();

    if (!data || data.length === 0) {
        container.append(`<div class="text-center py-3">대여 장비 신청내역이 없습니다.</div>`);
        return;
    }

    let row = $('<div class="row"></div>');

    data.forEach((r, index) => {
        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto">
                            <img src="${r.thumbnailUrl}" class="img-fluid rounded" alt="대표 이미지" style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <div class="col">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1">
                                    <p class="mb-0 fw-bold">${r.model}</p>
                                    <p class="mb-0 text-muted">
                                        [${r.category} / ${r.subCategory}]
                                    </p>
                                </h6>
                                <p class="card-text mb-1">
                                    신청 ID: ${r.rentalId} 
                                        <span class="${rentalStatusMap[r.status]?.class || ""} ms-2">
                                            [${rentalStatusMap[r.status]?.label || r.status}]
                                        </span> <br>
                                    신청 수량: ${r.quantity} <br>
                                    ${r.status === "REJECTED"
                                        ? `<span>대여 기간: ${r.requestStartDate || ""} ~ ${r.requestEndDate || ""}<br>거절 사유: ${r.rejectReason || "-"}</span>`
                                        : ""
                                    }
                                </p>
                            </div>
                        </div>
                        <div class="col-auto pe-2 ps-2 text-center">
                            ${r.status === "PENDING"?
                                `<button class="btn btn-sm btn-danger ms-2 cancel-rental-btn"
                                    data-id="${r.rentalId}" data-equipment-id="${r.equipmentId}">취소</button>`
                                : ""
                            }
                            ${r.status === "APPROVED"
                                ? `<button class="btn btn-sm btn-primary mb-1 view-items-btn" data-id="${r.rentalId}">대여 현황</button>`
                                : ""
                            }
                    
                            ${r.status === "COMPLETED"
                                ? `<button class="btn btn-sm btn-success mb-1 view-returned-items-btn" data-id="${r.rentalId}">반납 현황</button>`
                                : ""
                            }
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 2개마다 row append 후 새 row 시작
        if ((index + 1) % 2 === 0 || index === data.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });
}

$(document).on("click", ".view-items-btn", function() {
    const rentalId = $(this).data("id");
    window.location.href = `/rental/${rentalId}/item`;
});

$(document).on("click", ".cancel-rental-btn", function() {
    const rentalId = $(this).data("id");
    const equipmentId = $(this).data("equipment-id");

    if (!confirm("정말 이 대여 신청을 취소하시겠습니까?")) return;

    $.ajax({
        url: `/api/v1/rentals/${rentalId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({
            equipmentId: equipmentId,
            newStatus: "CANCELLED"
        })
    }).done(function(response) {
        showSnackbar("대여 신청이 취소되었습니다.");
        fetchRentalList(getFilterValues(filterConfig)); // 목록 갱신
    }).fail(handleServerError);
});

$(document).on("click", ".view-returned-items-btn", function() {
    const rentalId = $(this).data("id");
    // 반납 완료 조회 페이지로 이동
    window.location.href = `/rental/${rentalId}/return-item`;
});