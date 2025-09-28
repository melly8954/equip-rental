$(document).ready(function() {
    fetchKpiData();
    fetchZeroStock();

    // 이벤트 위임: 카드 내 상세보기 버튼 클릭
    $('#kpi-cards').on('click', '.kpi-detail-label', function() {
        const kpiName = $(this).closest('.card-body').find('.card-title').text();

        let params = new URLSearchParams();
        let targetUrl = '';

        if (kpiName.includes('연체')) {
            params.set("rentalItemStatus", "OVERDUE");
            targetUrl = '/admin/rental/item/list';
        } else if (kpiName.includes('대기')) {
            // 승인 대기 건수는 다른 페이지로 이동
            targetUrl = '/admin/rental/list';
        }

        if (targetUrl) {
            window.location.href = targetUrl + (params.toString() ? '?' + params.toString() : '');
        }
    });

    // 클릭 이벤트 위임
    $('#zero-stock-container').on('click', '.equipment-search-label', function() {
        const model = $(this).data('model');
        const searchUrl = `/admin/equipment/list?model=${encodeURIComponent(model)}`;
        window.location.href = searchUrl;
    });
});

// KPI 데이터 가져오기
function fetchKpiData() {
    $.ajax({
        url: '/api/v1/dashboards/kpi',
        method: 'GET',
    }).done(function(response) {
        renderKpiCards(response.data.kpis);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })
}

// KPI 카드 렌더링
function renderKpiCards(kpis) {
    const container = $('#kpi-cards');
    container.empty();

    kpis.forEach(kpi => {
        let changeHtml = '';
        if (kpi.changeRate) {
            if (kpi.changeRate === '신규 발생') {
                changeHtml = `<small class="text-primary fw-bold">(${kpi.changeRate})</small>`;
            } else {
                const rate = parseFloat(kpi.changeRate.replace('%', ''));
                const color = rate > 0 ? 'text-success' : 'text-danger';
                const arrow = rate > 0 ? '↑' : '↓';
                changeHtml = `<small class="${color} fw-bold">(${arrow} ${kpi.changeRate})</small>`;
            }
        }

        const cardHtml = `
            <div style="flex: 1 1 18%; min-width: 150px;">
                <div class="card text-center shadow-sm h-100">
                    <div class="card-body d-flex flex-column justify-content-center">
                        <h5 class="card-title">${kpi.name}</h5>
                        <p class="card-text fs-3 fw-bold mb-1">${kpi.value}</p>
                        ${changeHtml}
                        ${kpi.name.includes('연체') || kpi.name.includes('대기')
                            ? `<span class="kpi-detail-label">(목록 이동)</span>`
                            : ''}
                    </div>
                </div>
            </div>
        `;

        container.append(cardHtml);
    });
}

// 재고 0 데이터 가져오기
function fetchZeroStock(filters={}) {
    const params = {
        size: 5
    };

    params.page = filters.page;

    $.ajax({
        url: '/api/v1/dashboards/zero-stock',
        method: 'GET',
        data: params
    }).done(function(response) {
        renderZeroStock(response.data.content);
        renderPaginationInDashBoard("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchZeroStock({...filters, page: newPage});
        });
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    });
}

// 재고 0 카드 렌더링
function renderZeroStock(items) {
    const container = $('#zero-stock-container');
    container.empty();

    if (!items || items.length === 0) {
        container.append(`
            <div class="col-12">
                <div class="alert alert-success text-center mb-0">
                    모든 장비 재고가 정상입니다 🎉
                </div>
            </div>
        `);
        return;
    }

    items.forEach(item => {
        const cardHtml = `
            <div class="col-12 mb-2">
                <div class="card border-danger shadow-sm h-100">
                    <div class="card-body">
                        <h6 class="card-title text-danger mb-1">
                            ${item.category} > ${item.subCategory}
                        </h6>
                        <p class="fw-bold mb-1">${item.model}
                            <span class="equipment-search-label text-primary" 
                                  style="cursor:pointer" 
                                  data-model="${item.model}">
                                (🔍)
                            </span>
                        </p>
                        <small class="text-muted">재고: 0</small>
                    </div>
                </div>
            </div>
        `;
        container.append(cardHtml);
    });
}

function renderPaginationInDashBoard(containerId, pageInfo, onPageChange) {
    const container = $("#" + containerId);
    container.empty();

    const pagination = $('<ul class="pagination justify-content-center mb-0"></ul>');

    // 이전 버튼
    const prevLi = $('<li class="page-item"></li>');
    const prevLink = $('<a class="page-link" href="#">&lt;</a>');
    if (pageInfo.page <= 1) prevLi.addClass("disabled");
    prevLink.on("click", (e) => {
        e.preventDefault();
        if (pageInfo.page > 0) onPageChange(pageInfo.page - 1);
    });
    prevLi.append(prevLink);
    pagination.append(prevLi);

    // 다음 버튼
    const nextLi = $('<li class="page-item"></li>');
    const nextLink = $('<a class="page-link" href="#">&gt;</a>');
    if (pageInfo.page >= pageInfo.totalPages - 1) nextLi.addClass("disabled");
    nextLink.on("click", (e) => {
        e.preventDefault();
        if (pageInfo.page < pageInfo.totalPages - 1) onPageChange(pageInfo.page + 1);
    });
    nextLi.append(nextLink);
    pagination.append(nextLi);

    container.append(pagination);
}