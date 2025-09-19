$(document).ready(function() {
    // 검색 이벤트
    $("#equipment-search").on("input", function() {
        const currentValues = getFilterValues(filterConfig);
        fetchEquipment(currentValues);
    });
});

// 페이지가 보여질 때 초기화
window.addEventListener("pageshow", async function () {
    // 필터 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='subCategory']").prop("checked", false);
    $("#equipment-search").val("");

    // 필터 구성 가져오기
    window.filterConfig = await fetchFilterConfig();
    if (filterConfig) {
        // 카테고리만 먼저 렌더링
        renderFilter("category-filters", { category: filterConfig.category }, onFilterChange);
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

// 필터 변경 시
async function onFilterChange() {
    const categoryId = getFilterValues(filterConfig).category;
    await updateSubCategoryOptions(categoryId);

    fetchEquipment(getFilterValues(filterConfig));
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

// 장비 리스트 조회
function fetchEquipment(filters={}) {
    const filterValues = filters || getFilterValues(filterConfig);
    const modelSearch = $("#equipment-search").val();

    const params = {
        categoryId: filterValues.category || null,
        subCategoryId: filterValues.subCategory || null,
        model: modelSearch || null
    };

    if (filterValues.page) params.page = filterValues.page;

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

    let row = $('<div class="row"></div>');

    list.forEach((equip, index) => {
        const card = $(`
            <div class="col-md-6 mb-3">
                <div class="card shadow-sm h-100">
                    <div class="row g-0 align-items-center">
                        <div class="col-auto bg-light d-flex align-items-center justify-content-center" style="width:120px; height:120px;">
                            <img src="${equip.imageUrl}"  
                                 style="max-width:100%; max-height:100%; object-fit:contain;" 
                                 alt="대표 이미지"
                                 class="p-1 rounded-start">
                        </div>
                        <div class="col d-flex align-items-center justify-content-between">
                            <div class="card-body p-2">
                                <h6 class="card-title mb-1 fw-bold">${equip.model}</h6>
                                <p class="mb-0 text-muted">${equip.category} / ${equip.subCategory || '-'}</p>
                                <p class="card-text mb-1">재고: ${equip.availableStock}</p>
                            </div>
                            <div class="pe-2">
                                <button class="btn btn-outline-primary btn-sm rental-btn" data-id="${equip.equipmentId}">
                                    <i class="bi bi-box-seam"></i> 대여 신청
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        if ((index + 1) % 2 === 0 || index === list.length - 1) {
            container.append(row);
            row = $('<div class="row"></div>');
        }
    });
}

// 모달 이벤트
$(document).on("click", ".rental-btn", function() {
    const equipmentId = $(this).data("id");
    $("#modalEquipmentId").val(equipmentId);

    const today = new Date().toISOString().split("T")[0];
    $("#rentalStartDate").val(today);
    $("#rentalEndDate").val(today);

    new bootstrap.Modal(document.getElementById('rentalModal')).show();
});

$("#submitRental").on("click", function() {
    const equipmentId = $("#modalEquipmentId").val();
    const quantity = parseInt($("#rentalQuantity").val(), 10);
    const startDate = $("#rentalStartDate").val();
    const endDate = $("#rentalEndDate").val();
    const rentalReason = $("#rentalReason").val();

    if (!startDate || !endDate || !rentalReason || quantity <= 0) {
        alert("모든 항목을 올바르게 입력해주세요.");
        return;
    }

    const payload = { equipmentId: parseInt(equipmentId, 10), quantity, startDate, endDate, rentalReason };

    $.ajax({
        url: "/api/v1/rentals",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    }).done(function(response) {
        alert(response.message);
        $("#rentalModal").modal("hide");

        fetchEquipment(getFilterValues(filterConfig));
    }).fail(handleServerError);
});

$("#rentalModal").on("hidden.bs.modal", function () {
    $("#rentalForm")[0].reset();
    $("#modalEquipmentId").val("");
});
