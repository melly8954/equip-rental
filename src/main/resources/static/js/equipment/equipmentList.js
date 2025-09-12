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

// pageshow 이벤트 활용: 뒤로가기/앞으로가기 시에도 실행
window.addEventListener("pageshow", function(event) {
    // 필터 강제 초기화
    $("input[name='category']").prop("checked", false);
    $("input[name='category'][value='전체']").prop("checked", true);

    $("input[name='subCategory']").prop("checked", false);
    $("input[name='subCategory'][value='전체']").prop("checked", true);

    // 검색어 초기화
    $("#equipment-search").val("");

    // 필터 렌더링
    renderFilter("equipment-filters", filterConfig, onFilterChange);

    // 초기 장비 리스트 조회
    fetchEquipment();
});

$(document).ready(function() {
    // 검색 이벤트
    $("#equipment-search").on("input", function() {
        fetchEquipment();
    });
});

// 필터 변경 시 동작
function onFilterChange(values) {
    // 현재 선택된 카테고리 값
    const category = getFilterValues(filterConfig).category;

    // 서브카테고리 갱신
    updateSubCategoryOptions(category);

    // 최신 filterConfig 값으로 fetch
    const filters = getFilterValues(filterConfig);
    fetchEquipment(filters);
}


// 서브카테고리 업데이트
function updateSubCategoryOptions(parentCategory) {
    const options = subCategoryMap[parentCategory] || [];

    filterConfig.subCategory.options = options;

    const container = $("#sub-category-filters");
    container.empty();
    renderFilter("sub-category-filters", {
        subCategory: {
            type: "radio",
            options: options
        }
    }, (values) => {
        // category 필터도 함께 포함
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
        category: filterValues.category || null,
        subCategory: filterValues.subCategory || null,
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
        container.append(`<div class="text-center py-3">장비가 없습니다.</div>`);
        return;
    }

    list.forEach(equip => {
        const card = $(`
            <div class="card mb-3">
                <div class="row g-0 h-100">
                    <!-- 이미지 영역 고정 -->
                    <div class="col-md-2 d-flex align-items-center justify-content-center bg-light" style="height: 168px;">
                        <img src="${equip.imageUrl}"  
                             style="max-width: 100%; max-height: 100%; object-fit: contain;" 
                             alt="대표 이미지">
                    </div>
            
                    <!-- 텍스트 영역 -->
                    <div class="col-md-10 d-flex align-items-center">
                        <div class="card-body">
                            <h5 class="card-title">${equip.model}</h5>
                            <p class="card-text">카테고리: ${equip.category}</p>
                            <p class="card-text">서브카테고리: ${equip.subCategory || '-'}</p>
                            <p class="card-text">재고: ${equip.availableStock}</p>
                        </div>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    });
}