let currentEquipmentId = null;

// 검색 이벤트 등록
$(document).ready(function() {
    $("#equipment-search").on("input", function() {
        const currentValues = getFilterValues(filterConfig);
        fetchEquipment(currentValues);
    });
});

// pageshow 이벤트 활용: 뒤로가기/앞으로가기 시에도 실행
window.addEventListener("pageshow", async function (event) {
    // 필터 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='subCategory']").prop("checked", false);
    $("#equipment-search").val("");

    // 필터 구성 가져오기
    window.filterConfig = await fetchFilterConfig();
    if (filterConfig) {
        // 카테고리만 먼저 렌더링
        renderFilter("category-filters", {category: filterConfig.category}, onFilterChange);
        // 서브카테고리는 초기엔 옵션이 없으면 숨김
        $("#sub-category-filters").hide();

        fetchEquipment();
    }
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
                    { id: null, label: "전체" }, // 전체 추가
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
    // 현재 선택된 카테고리 값
    const categoryId = getFilterValues(filterConfig).category;

    // 서브카테고리 갱신
    await updateSubCategoryOptions(categoryId);

    // 최신 filterConfig 값으로 fetch
    const filters = getFilterValues(filterConfig);
    fetchEquipment(filters);
}


// 서브카테고리 업데이트
async function updateSubCategoryOptions(parentCategoryId) {
    let options = [];
    if (parentCategoryId) {
        try {
            const response = await $.getJSON(`/api/v1/categories/${parentCategoryId}/sub-categories`);
            options = response.data.map(sc => ({ id: sc.subCategoryId, label: sc.label }));
            options.unshift({ id: null, label: "전체" });
        } catch (e) {
            console.error("서브카테고리 불러오기 실패", e);
        }
    }

    filterConfig.subCategory.options = options;

    if (options.length > 1) {
        $("#sub-category-filters").addClass("active"); // 옵션이 있으면 표시
    } else {
        $("#sub-category-filters").removeClass("active"); // 옵션 없으면 숨김
    }

    renderFilter("sub-category-filters", { subCategory: { type: "radio", options } }, (values) => {
        const combinedFilters = {
            category: getFilterValues(filterConfig).category,
            subCategory: values.subCategory
        };
        fetchEquipment(combinedFilters);
    });
}

// 장비 리스트 조회 함수
function fetchEquipment(filters={}) {
    // 필터 통합
    const filterValues = filters || getFilterValues(filterConfig);
    const modelSearch = $("#equipment-search").val();
    const params = {
        categoryId: filterValues.category || null,
        subCategoryId: filterValues.subCategory || null,
        model: modelSearch || null
    };

    // page가 있으면 추가
    if (filterValues.page) {
        params.page = filterValues.page;
    }

    $.ajax({
        url: "/api/v1/equipments",
        method: "GET",
        data: params
    }).done(function(response) {
        renderEquipmentList(response.data.content);
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchEquipment({...filterValues, page: newPage});
        });
    }).fail(handleServerError);
}

// 장비 리스트 렌더링
function renderEquipmentList(list) {
    const container = $("#equipment-list");
    container.empty();

    if (!list || list.length === 0) {
        container.append(`<div class="text-center py-3">등록된 장비가 존재하지 않습니다.</div>`);
        return;
    }

    // 행(row) 컨테이너 (5열 카드 구조)
    let row = $('<div class="row row-cols-5 g-3 mb-3"></div>');

    list.forEach((equip, index) => {
        const card = $(`
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <div class="card-body p-2">
                        <div class="mb-2 text-center">
                            <img src="${equip.imageUrl}" class="img-fluid rounded" alt="대표 이미지" style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <h6 class="card-title mb-1 text-center fw-bold">${equip.model}</h6>
                        <p class="card-text small text-muted text-center mb-2">
                            ${equip.category} / ${equip.subCategory}
                        </p>
                        <p class="card-text mb-1 text-center">
                            재고 현황: <span class="fw-bold">${equip.availableStock}</span> / ${equip.totalStock}
                        </p>
                    </div>

                    <!-- 버튼 -->
                    <div class="card-footer bg-white border-0 text-center pb-3">
                        <button class="btn btn-outline-success btn-sm stock-increase-btn" 
                                data-id="${equip.equipmentId}">
                            ➕ 재고 추가
                        </button>
                    </div>
                    <div class="card-footer p-0 border-0">
                        <div class="item-list-btn w-100 text-center py-2 bg-light" 
                             data-id="${equip.equipmentId}">
                            재고 목록
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 10개(=2행×5열)마다 끊어서 container에 붙임
        if ((index + 1) % 10 === 0 || index === list.length - 1) {
            container.append(row);
            row = $('<div class="row row-cols-5 g-3 mb-3"></div>');
        }
    });
}

// 부모 컨테이너에 이벤트 위임
$(document).on("click", ".item-list-btn", function () {
    const equipmentId = $(this).data("id");

    $.ajax({
        url: `/api/v1/manager-scopes/${equipmentId}`,
        method: "GET"
    }).done(function(response) {
        if (response.data) {
            window.location.href = `/admin/equipment/${equipmentId}/item`;
        } else {
            showSnackbar("접근 권한이 없습니다.");
        }
    }).fail(function(xhr) {
        if (xhr.status === 403) {
            showSnackbar("접근 권한이 없습니다.");
        } else {
            showSnackbar("서버 오류가 발생했습니다.");
        }
    });
});

// 버튼 클릭 시 모달 열기
$(document).on("click", ".stock-increase-btn", function() {
    currentEquipmentId = $(this).data("id");
    // 권한 체크
    $.ajax({
        url: `/api/v1/manager-scopes/${currentEquipmentId}`,
        method: "GET"
    }).done(function(response) {
        if (response.data) {
            $("#stockAmount").val(1);
            $("#stockIncreaseModal").modal("show");
        } else {
            showSnackbar("접근 권한이 없습니다.");
        }
    }).fail(function(xhr) {
        if (xhr.status === 403) {
            showSnackbar("접근 권한이 없습니다.");
        } else {
            showSnackbar("서버 오류가 발생했습니다.");
        }
    });
});

// 모달 확인 시 API 호출
$("#confirmStockIncrease").on("click", function() {
    const amount = parseInt($("#stockAmount").val(), 10);

    $.ajax({
        url: `/api/v1/equipments/${currentEquipmentId}/stock`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ amount })
    }).done(function(response) {
        showSnackbar(response.message)
        $("#stockIncreaseModal").modal("hide");

        // 현재 필터와 검색어 상태 가져오기
        const currentFilters = getFilterValues(filterConfig);
        fetchEquipment(currentFilters);
    }).fail(function(xhr) {
        handleServerError(xhr);
    })
});