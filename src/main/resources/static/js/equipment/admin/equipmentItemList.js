const statusFilterConfig = {
    status: {
        label: "상태",
        type: "radio",
        options: ["전체", "AVAILABLE", "RENTED", "REPAIRING", "OUT_OF_STOCK", "LOST", "DISCARDED"]
    }
};

$(document).ready(function () {
    // URL에서 equipmentId 추출
    const pathParts = window.location.pathname.split("/");
    const equipmentId = pathParts[pathParts.indexOf("equipment") + 1];

    // 상태 필터 렌더링
    renderFilter("equipment-filter", statusFilterConfig, function(filters) {
        fetchEquipmentItems(equipmentId, filters, 1); // 필터 변경 시 1페이지로 호출
    });

    // 초기 조회
    fetchEquipmentItems(equipmentId, {}, 1);
});

function fetchEquipmentItems(equipmentId, filters = {}, page = 1) {
    $.ajax({
        url: `/api/v1/equipments/${equipmentId}/items`,
        method: "GET",
        data: {
            equipmentStatus: filters.status || "",
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
                             class="img-fluid rounded-start" alt="${equipmentSummary.model}">
                    </div>
                    <div class="col-md-8">
                        <div class="card-body">
                            <h5 class="card-title">${equipmentSummary.model}</h5>
                            <p class="card-text mb-1"><strong>카테고리:</strong> ${equipmentSummary.category}</p>
                            ${equipmentSummary.subCategory ? `<p class="card-text mb-1"><strong>서브카테고리:</strong> ${equipmentSummary.subCategory}</p>` : ""}
                            <p class="card-text"><strong>재고:</strong> ${equipmentSummary.stock}</p>
                        </div>
                    </div>
                </div>
            </div>
        `);

        const container = $("#equipment-items");
        container.empty();

        if (!items.length) {
            container.append(`<div class="text-center py-3">아이템이 없습니다.</div>`);
            $("#equipment-pagination").empty();
            return;
        }

        // 열 제목
        container.append(`
            <div class="d-flex fw-bold border-bottom border-dark pb-2 mb-2">
                <div class="col-2">ID</div>
                <div class="col-6">Serial Number</div>
                <div class="col-4">Status</div>
            </div>
        `);

        items.forEach(item => {
            container.append(`
                <div class="d-flex py-1 border-bottom">
                    <div class="col-2">${item.equipmentItemId}</div>
                    <div class="col-6">${item.serialNumber || '-'}</div>
                    <div class="col-4">${item.status}</div>
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