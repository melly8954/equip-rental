$(document).ready(function() {
    fetchKpiData();
    fetchZeroStock();
    fetchCategoryInventory();

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

// 긴급 관리 현황 페이징
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

// 카테고리 별 장비 보유 현황 호출
function fetchCategoryInventory() {
    $.ajax({
        url: '/api/v1/dashboards/equipments/category',
        method: 'GET',
    }).done(function(response) {
        const categories = response.data;
        renderCategoryChart(categories);

        // 디폴트: 첫 번째 카테고리 선택 후 서브 카테고리 차트 렌더링
        if (categories.length > 0) {
            fetchSubCategoryInventory(categories[0].categoryId, categories[0].categoryLabel);
        }
    }).fail(handleServerError);
}

// 서브 카테고리 별 장비 보유 현황 호출
function fetchSubCategoryInventory(categoryId, categoryLabel) {
    $.ajax({
        url: `/api/v1/dashboards/equipments/categories/${categoryId}`,
        method: 'GET',
    }).done(function(response) {
        renderSubCategoryChart(response.data, categoryLabel);
    }).fail(handleServerError);
}

// 카테고리 차트 렌더링
function renderCategoryChart(data) {
    const ctx = document.getElementById('category-chart').getContext('2d');
    if (window.categoryChart) window.categoryChart.destroy();

    window.categoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.categoryLabel),
            datasets: [{
                data: data.map(d => d.stock),
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                layout: {
                    padding: {
                        top: 20, // 차트와 범례 사이 간격
                        bottom: 10
                    }
                },
                legend: {
                    position: 'bottom',
                    labels: {
                        font: { size: 12 },
                        color: '#000',
                        boxWidth: 15,
                        boxHeight: 15,
                        padding: 15
                    }
                },
                datalabels: {
                    color: '#fff',
                    font: { weight: 'bold', size: 14 },
                    formatter: (value, context) => {
                        const label = context.chart.data.labels[context.dataIndex];
                        return `${label} : ${value}`;
                    }
                }
            },
            onClick: (evt, elements) => {
                if (elements.length > 0) {
                    const index = elements[0].index;
                    const categoryId = data[index].categoryId;
                    const label = data[index].categoryLabel;
                    fetchSubCategoryInventory(categoryId, label);
                }
            }
        },
        plugins: [ChartDataLabels]
    });
    // 테이블 업데이트
    renderCategoryTable(data);
}

// 서브 카테고리 차트 렌더링
function renderSubCategoryChart(data, categoryLabel) {
    const ctx = document.getElementById('sub-category-chart').getContext('2d');
    if (window.subCategoryChart) window.subCategoryChart.destroy();

    window.subCategoryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.subCategoryLabel),
            datasets: [{
                data: data.map(d => d.stock),
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                layout: {
                    padding: {
                        top: 20, // 차트와 범례 사이 간격
                        bottom: 10
                    }
                },
                legend: {
                    position: 'bottom',
                    labels: {
                        font: { size: 12 },
                        color: '#000',
                        boxWidth: 15,
                        boxHeight: 15,
                        padding: 15
                    }
                },
                datalabels: {
                    color: '#fff',
                    font: { weight: 'bold', size: 14 },
                    formatter: (value, context) => {
                        const label = context.chart.data.labels[context.dataIndex];
                        return `${label} : ${value}`;
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });
    // 테이블 업데이트
    renderSubCategoryTable(data, categoryLabel);
}

function renderCategoryTable(data) {
    const table = $('#category-data-table');
    table.empty();

    table.append('<thead><tr><th>카테고리</th><th>재고</th></tr></thead>');
    const tbody = $('<tbody></tbody>');

    data.forEach(d => {
        tbody.append(`<tr><td>${d.categoryLabel}</td><td>${d.stock}</td></tr>`);
    });

    table.append(tbody);
}

function renderSubCategoryTable(data, categoryLabel) {
    const table = $('#sub-category-data-table');
    table.empty();

    table.append(`<thead><tr><th>서브카테고리 [${categoryLabel}]</th><th>재고</th></tr></thead>`);
    const tbody = $('<tbody></tbody>');

    data.forEach(d => {
        tbody.append(`<tr><td>${d.subCategoryLabel}</td><td>${d.stock}</td></tr>`);
    });

    table.append(tbody);
}