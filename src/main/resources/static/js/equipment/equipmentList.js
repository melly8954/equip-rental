// 필터 설정 예시
const filterConfig = {
    category: {
        label: "카테고리",
        type: "radio",
        options: [
            "전체",
            "OFFICE_SUPPLIES",
            "ELECTRONICS",
            "FURNITURE",
            "TOOLS",
            "SAFETY_EQUIPMENT"
        ]
    },

    subCategory: {
        label: "서브카테고리",
        type: "radio",
        options: ["전체"] // 초기값은 전체, 카테고리 선택 시 동적으로 바뀜
    }
};

const subCategoryMap = {
    OFFICE_SUPPLIES: ["문서용품", "필기구", "사무기기"],
    ELECTRONICS: ["컴퓨터", "마우스", "모바일/태블릿"],
    FURNITURE: ["책상/의자", "수납", "회의용 가구"],
    TOOLS: ["전동공구", "수공구", "측정기기"],
    SAFETY_EQUIPMENT: ["보호구", "안전장치", "응급용품"]
};

$(document).ready(function() {
    // 필터 렌더링
    renderFilter("equipment-filters", filterConfig, onFilterChange);

    // 검색 이벤트
    $("#equipment-search").on("input", function() {
        fetchEquipment(1);
    });

    // 초기 장비 리스트 조회
    fetchEquipment(1);
});

// 필터 변경 시 동작
function onFilterChange(values) {
    const selectedCategory = values.category;

    // 서브카테고리 갱신
    updateSubCategoryOptions(selectedCategory);

    // 장비 리스트 새로 조회
    fetchEquipment(1);
}

// 서브카테고리 업데이트
function updateSubCategoryOptions(parentCategory) {
    const options = subCategoryMap[parentCategory] || [];

    // filterConfig도 동기화
    filterConfig.subCategory.options = options;

    // DOM 초기화 후 렌더링
    const container = $("#sub-category-filters");
    container.empty();
    renderFilter("sub-category-filters", {
        subCategory: {
            type: "radio",
            options: options
        }
    }, () => fetchEquipment(1));
}

// 장비 리스트 조회 함수
function fetchEquipment(page = 1) {
    const filters = getFilterValues(filterConfig);
    const modelSearch = $("#equipment-search").val();

    const params = {
        page: page,
        size: 12,
        category: filters.category === "전체" ? null : filters.category,
        subCategory: filters.subCategory,
        model: modelSearch
    };

    $.ajax({
        url: "/api/v1/equipments",
        method: "GET",
        data: params
    }).done(function(response) {
        renderEquipmentList(response.data.content);
        renderPagination("pagination",{
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, fetchEquipment);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR)
    })
}

// 장비 리스트 렌더링
function renderEquipmentList(list) {
    const container = $("#equipment-list");
    container.empty();

    if (!list || list.length === 0) {
        container.append(`<div class="text-center py-3">장비가 없습니다.</div>`);
        return;
    }

    list.forEach(equip => {
        const card = $(`
            <div class="card mb-3">
                <div class="row g-0">
                    <div class="col-md-2">
                        <img src="${equip.imageUrl}" class="img-fluid rounded-start" alt="대표 이미지">
                    </div>
                    <div class="col-md-10">
                        <div class="card-body">
                            <h5 class="card-title">${equip.model}</h5>
                            <p class="card-text">카테고리: ${equip.category}</p>
                            <p class="card-text">서브카테고리: ${equip.subCategory || '-'}</p>
                            <p class="card-text">재고: ${equip.stock}</p>
                        </div>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    });
}