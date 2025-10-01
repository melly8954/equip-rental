$(document).ready(function() {
    // 검색 이벤트
    $("#equipment-search").on("input", function() {
        const currentValues = getFilterValues(filterConfig);
        fetchEquipment(currentValues);
    });
});

// 페이지가 보여질 때 초기화
window.addEventListener("pageshow", async function () {
    // 필터 구성 가져오기
    window.filterConfig = await fetchFilterConfig();
    if (!filterConfig) return;

    // 카테고리 렌더링
    renderFilter("category-filters", {category: filterConfig.category}, onFilterChange);
    $("#sub-category-filters").hide(); // 초기엔 서브카테고리 숨김

    // URL에서 필터값 복원
    const urlParams = new URLSearchParams(window.location.search);
    const savedFilters = {
        category: urlParams.get("category"),
        subCategory: urlParams.get("subCategory"),
        model: urlParams.get("model"),
        page: urlParams.get("page")
    };

    if (savedFilters.category) $(`input[name="category"][value="${savedFilters.category}"]`).prop("checked", true);
    if (savedFilters.model) $("#equipment-search").val(savedFilters.model);

    // 카테고리에 따라 서브카테고리 fetch
    const categoryId = savedFilters.category || getFilterValues(filterConfig).category;
    await updateSubCategoryOptions(categoryId);

    if (savedFilters.subCategory) $(`input[name="subCategory"][value="${savedFilters.subCategory}"]`).prop("checked", true);

    // 최종 필터값으로 fetch
    fetchEquipment(getFilterValues(filterConfig));
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
    // 페이지 input이나 URL에서 가져오기
    const urlPage = new URLSearchParams(window.location.search).get("page");
    values.page = urlPage || 1;
    return values;
}

// 필터 상태 URL에 반영
function updateUrlWithFilters(filters) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([k,v]) => {
        if (v) params.set(k, v);
    });
    const newUrl = `${window.location.pathname}?${params.toString()}`;
    window.history.replaceState({}, "", newUrl);
}

// 필터 변경 시
async function onFilterChange() {
    const categoryId = getFilterValues(filterConfig).category;
    await updateSubCategoryOptions(categoryId);

    updateUrlWithFilters(getFilterValues(filterConfig));
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

    renderFilter("sub-category-filters", { subCategory: { label: "서브카테고리", type: "radio", options } }, (values) => {
        const combinedFilters = {
            category: getFilterValues(filterConfig).category,
            subCategory: values.subCategory
        };
        updateUrlWithFilters(combinedFilters);
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
        model: modelSearch || null,
        page: filterValues.page || 1
    };

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
            const currentFilters = getFilterValues(filterConfig);
            currentFilters.page = newPage;          // 여기서 page 포함
            updateUrlWithFilters(currentFilters);   // URL 반영
            fetchEquipment(currentFilters);
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

    let row = $('<div class="row row-cols-5 g-3 mb-3"></div>');

    list.forEach((equip, index) => {
        const card = $(`
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <div class="card-body p-2 text-center">
                        <div class="mb-2 text-center">
                            <img src="${equip.imageUrl}" class="img-fluid rounded" alt="대표 이미지" style="width:100px; height:100px; object-fit:cover;">
                        </div>
                        <h6 class="card-title mb-1 fw-bold">${equip.model}</h6>
                        <p class="mb-1 text-muted small">[${equip.category} / ${equip.subCategory || '-'}]</p>
                        <p class="mb-2">대여 가능: <span class="fw-bold">${equip.availableStock}</span></p>
                        
                        <div class="card-footer p-0 border-0">
                            <div class="rental-btn w-100 text-center py-2 bg-light"  data-id="${equip.equipmentId}">
                                <i class="bi-pencil-square"></i> 대여 신청
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `);

        row.append(card);

        // 10개 단위(2행 × 5열)마다 끊기
        if ((index + 1) % 10 === 0 || index === list.length - 1) {
            container.append(row);
            row = $('<div class="row row-cols-5 g-3 mb-3"></div>');
        }
    });
}

// 모달 이벤트
$(document).on("click", ".rental-btn", function() {
    const equipmentId = $(this).data("id");
    $("#modalEquipmentId").val(equipmentId);

    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, "0"); // 0~11
    const dd = String(now.getDate()).padStart(2, "0");

    const today = `${yyyy}-${mm}-${dd}`;
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
        showSnackbar("모든 항목을 올바르게 입력해주세요.");
        return;
    }

    const payload = { equipmentId: parseInt(equipmentId, 10), quantity, startDate, endDate, rentalReason };

    $.ajax({
        url: "/api/v1/rentals",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(payload)
    }).done(function(response) {
        showSnackbar(response.message);
        $("#rentalModal").modal("hide");

        fetchEquipment(getFilterValues(filterConfig));
    }).fail(handleServerError);
});

$("#rentalModal").on("hidden.bs.modal", function () {
    $("#rentalForm")[0].reset();
    $("#modalEquipmentId").val("");
});
